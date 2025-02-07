/* ----------------------------------------------------------------------------
 * Copyright (C) 2021      European Space Agency
 *                         European Space Operations Centre
 *                         Darmstadt
 *                         Germany
 * ----------------------------------------------------------------------------
 * System                : ESA NanoSat MO Framework
 * ----------------------------------------------------------------------------
 * Licensed under European Space Agency Public License (ESA-PL) Weak Copyleft – v2.4
 * You may not use this file except in compliance with the License.
 *
 * Except as expressly set forth in this License, the Software is provided to
 * You on an "as is" basis and without warranties of any kind, including without
 * limitation merchantability, fitness for a particular purpose, absence of
 * defects or errors, accuracy or non-infringement of intellectual property rights.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 * ----------------------------------------------------------------------------
 */
package esa.mo.com.impl.archive.db;

import esa.mo.com.impl.archive.entities.COMObjectEntity;
import esa.mo.com.impl.provider.ArchiveManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.ccsds.moims.mo.com.archive.structures.ArchiveQuery;
import org.ccsds.moims.mo.com.archive.structures.PaginationFilter;
import org.ccsds.moims.mo.com.archive.structures.QueryFilter;
import org.ccsds.moims.mo.mal.structures.IntegerList;
import org.ccsds.moims.mo.mal.structures.LongList;

/**
 * The Transactions Processor is responsible for executing the transactions with the database. It
 * has one general executor with 2 threads for flushing the generated COM Events and a single thread
 * executor for orderly executing the interactions with the database. It includes an optimization
 * for batch stores, basically, if many stores are received sequentially, they will be consolidated
 * and executed in one single transaction.
 */
public class TransactionsProcessor {

  private static final String QUERY_SELECT_ALL
      = "SELECT PU.objId FROM COMObjectEntity PU WHERE PU.objectTypeId=:objectTypeId AND PU.domainId=:domainId";

  private static final Boolean SAFE_MODE = false;
  private static final Class<COMObjectEntity> CLASS_ENTITY = COMObjectEntity.class;
  private final DatabaseBackend dbBackend;

  // This executor is responsible for the interactions with the db
  // Guarantees sequential order
  private final ExecutorService dbTransactionsExecutor = Executors.newSingleThreadExecutor(
      new DBThreadFactory("Archive_DBTransactionsProcessor"));

  // This executor is expecting "short-lived" runnables that generate Events.
  // 2 Threads minimum because we need to acquire the lock from 2 different tasks during startup
  private final ExecutorService generalExecutor = Executors.newFixedThreadPool(2,
      new DBThreadFactory("Archive_GeneralProcessor"));
  private final AtomicBoolean sequencialStoring;

  private final LinkedBlockingQueue<StoreCOMObjectsContainer> storeQueue;

  public TransactionsProcessor(DatabaseBackend dbBackend) {
    this.dbBackend = dbBackend;
    this.storeQueue = new LinkedBlockingQueue<>();
    this.sequencialStoring = new AtomicBoolean(false);
  }

  public void submitExternalTask(final Runnable task) {
    this.sequencialStoring.set(false); // Sequential stores can no longer happen otherwise we break order

    generalExecutor.execute(task);
  }

  public void submitExternalTask2(final Runnable task) {
    this.sequencialStoring.set(false); // Sequential stores can no longer happen otherwise we break order

    dbTransactionsExecutor.execute(task);
  }

  /**
   * Resizes the database file to match its contents.
   */
  public void vacuum() {
    try {
      Connection c = dbBackend.getConnection();
      Statement stmt;
      Logger.getLogger(TransactionsProcessor.class.getName()).log(Level.FINE, "Vacuuming database");
      stmt = c.createStatement();
      stmt.executeUpdate("VACUUM");
    } catch (SQLException ex) {
      Logger.getLogger(TransactionsProcessor.class.getName()).log(Level.SEVERE,
          "Failed to vacuum database", ex);
    }
  }

  public COMObjectEntity getCOMObject(final Integer objTypeId, final Integer domain,
      final Long objId) {
    this.sequencialStoring.set(false); // Sequential stores can no longer happen otherwise we break order

    Future<COMObjectEntity> future = dbTransactionsExecutor.submit(() -> {
      try {
        dbBackend.createEntityManager();
        final COMObjectEntity perObj = dbBackend.getEM().find(CLASS_ENTITY,
            COMObjectEntity.generatePK(objTypeId, domain, objId));
        return perObj;
      } finally {
        dbBackend.closeEntityManager();
      }
    });

    try {
      return future.get();
    } catch (InterruptedException | ExecutionException ex) {
      Logger.getLogger(TransactionsProcessor.class.getName()).log(Level.SEVERE, null, ex);
    }

    return null;
  }

  public List<COMObjectEntity> getCOMObjects(final Integer objTypeId, final Integer domain, final LongList ids) {
    this.sequencialStoring.set(false); // Sequential stores can no longer happen otherwise we break order

    Future<List<COMObjectEntity>> future = dbTransactionsExecutor.submit(() -> {
      try {
        dbBackend.createEntityManager();
        EntityManager manager = dbBackend.getEM();
        List<COMObjectEntity> perObjs = new ArrayList<>();
        COMObjectEntityPK key = COMObjectEntity.generatePK(objTypeId, domain, 0L);
        for(Long objId : ids) {
          key.setObjId(objId);
          final COMObjectEntity perObj = manager.find(CLASS_ENTITY, key);
          perObjs.add(perObj);
        }
        return perObjs;
      } finally {
        dbBackend.closeEntityManager();
      }
    });

    try {
      return future.get();
    } catch (InterruptedException | ExecutionException ex) {
      Logger.getLogger(TransactionsProcessor.class.getName()).log(Level.SEVERE, null, ex);
    }

    return null;
  }

  public List<COMObjectEntity> getAllCOMObjects(final Integer objTypeId, final Integer domainId) {
    this.sequencialStoring.set(false); // Sequential stores can no longer happen otherwise we break order

    Future<List<COMObjectEntity>> future = dbTransactionsExecutor.submit(() -> {
      try {
        dbBackend.createEntityManager();
        String fieldsList = "objectTypeId, objId, domainId, network, OBJ, providerURI, "
                      + "relatedLink, sourceLinkDomainId, sourceLinkObjId, sourceLinkObjectTypeId, timestampArchiveDetails";
        String queryString = "SELECT " + fieldsList + " FROM COMObjectEntity WHERE objectTypeId=" + objTypeId + " AND domainId=" + domainId;

        final Query query = dbBackend.getEM().createNativeQuery(queryString);
        final List<?> resultList = query.getResultList();

        final ArrayList<COMObjectEntity> perObjs = new ArrayList<>(resultList.size());

        // Conversion from the raw SQL response into a COMObjectEntity
        for (Object obj : resultList) {
          // (final Integer objectTypeId, final Integer domainId, final Long objId)
          final SourceLinkContainer source = new SourceLinkContainer(
                  (Integer) ((Object[]) obj)[9],
                  (Integer) ((Object[]) obj)[7],
                  convert2Long(((Object[]) obj)[8])
          );

          perObjs.add(new COMObjectEntity(
                  (Integer) ((Object[]) obj)[0],
                  (Integer) ((Object[]) obj)[2],
                  convert2Long(((Object[]) obj)[1]),
                  convert2Long(((Object[]) obj)[10]),
                  (Integer) ((Object[]) obj)[5],
                  (Integer) ((Object[]) obj)[3],
                  source,
                  convert2Long(((Object[]) obj)[6]),
                  (byte[]) ((Object[]) obj)[4]
          ));
        }

        return perObjs;
      } finally {
        dbBackend.closeEntityManager();
      }
    });

    try {
      return future.get();
    } catch (InterruptedException | ExecutionException ex) {
      Logger.getLogger(TransactionsProcessor.class.getName()).log(Level.SEVERE, null, ex);
    }

    return null;
  }

  public boolean existsCOMObject(final Integer objTypeId, final Integer domain, final Long objId) {
    Future<Boolean> future = dbTransactionsExecutor.submit(() -> {
      dbBackend.createEntityManager();
      final COMObjectEntity perObj = dbBackend.getEM().find(CLASS_ENTITY,
          COMObjectEntity.generatePK(objTypeId, domain, objId));
      dbBackend.closeEntityManager();
      return (perObj != null);
    });

    try {
      return future.get();
    } catch (InterruptedException | ExecutionException ex) {
      Logger.getLogger(TransactionsProcessor.class.getName()).log(Level.SEVERE, null, ex);
    }

    return false;
  }

  public LongList getAllCOMObjectsIds(final Integer objTypeId, final Integer domainId) {
    this.sequencialStoring.set(false); // Sequential stores can no longer happen otherwise we break order

    Future<LongList> future = dbTransactionsExecutor.submit(() -> {
      dbBackend.createEntityManager();
      try {
        Query query = dbBackend.getEM().createQuery(QUERY_SELECT_ALL);
        query.setParameter("objectTypeId", objTypeId);
        query.setParameter("domainId", domainId);
        LongList objIds = new LongList();
        objIds.addAll(query.getResultList());
        return objIds;
      } finally {
        dbBackend.closeEntityManager();
      }

    });

    try {
      return future.get();
    } catch (InterruptedException | ExecutionException ex) {
      Logger.getLogger(TransactionsProcessor.class.getName()).log(Level.SEVERE, null, ex);
    }

    return null;
  }

  private void persistObjects(final ArrayList<COMObjectEntity> perObjs) {
    for (int i = 0; i < perObjs.size(); i++) { // 6.510 ms per cycle
      if (SAFE_MODE) {
        final COMObjectEntity objCOM = dbBackend.getEM().find(CLASS_ENTITY,
            perObjs.get(i).getPrimaryKey()); // 0.830 ms

        if (objCOM == null) { // Last minute safety, the db crashes if one tries to store an object with a used pk
          final COMObjectEntity perObj = perObjs.get(i); // The object to be stored  // 0.255 ms
          dbBackend.getEM().persist(perObj);  // object    // 0.240 ms
        } else {
          Logger.getLogger(TransactionsProcessor.class.getName()).log(Level.SEVERE,
              "The Archive could not store the object: " + perObjs.get(i).toString(),
              new Throwable());
        }
      } else {
        dbBackend.getEM().persist(perObjs.get(i));  // object
      }

      // Flush every 1k objects...
      if (i != 0) {
        if ((i % 1000) == 0) {
          Logger.getLogger(TransactionsProcessor.class.getName()).log(Level.FINE,
              "Flushing the data after 1000 serial stores...");
          dbBackend.getEM().flush();
          dbBackend.getEM().clear();
        }
      }
    }
  }

  public void insert(final ArrayList<COMObjectEntity> perObjs, final Runnable publishEvents) {
    final boolean isSequential = this.sequencialStoring.get();
    final StoreCOMObjectsContainer container = new StoreCOMObjectsContainer(perObjs, isSequential);

    this.sequencialStoring.set(true);

    // Insert into queue with 3 retries, in case of unexpected InterruptedException
    boolean added = false;
    int retryCounter = 0;
    while (!added && (retryCounter++) < 3) {
      try {
        storeQueue.put(container);
        added = true;
      } catch (InterruptedException ex) {
        Logger.getLogger(ArchiveManager.class.getName()).log(Level.SEVERE, null, ex);
      }
    }

    dbTransactionsExecutor.execute(() -> {
      StoreCOMObjectsContainer container1 = storeQueue.poll();

      if (container1 != null) {
        try {
          dbBackend.createEntityManager();  // 0.166 ms
          dbBackend.getEM().getTransaction().begin(); // 0.480 ms
          persistObjects(container1.getPerObjs()); // store

          while (true) {
            container1 = storeQueue.peek(); // get next if there is one available
            if (container1 != null && container1.isContinuous()) {
              container1 = storeQueue.poll();
              persistObjects(container1.getPerObjs()); // store
            } else {
              break;
            }
          }

          dbBackend.safeCommit();
        } finally {
          dbBackend.closeEntityManager();
        }
      }
      if (publishEvents != null) {
        generalExecutor.submit(publishEvents);
      }
    });
  }

  public void remove(final Integer objTypeId, final Integer domainId,
      final LongList objIds, final Runnable publishEvents) {
    this.sequencialStoring.set(false); // Sequential stores can no longer happen otherwise we break order

    dbTransactionsExecutor.execute(() -> {
      try {
        dbBackend.createEntityManager();  // 0.166 ms

        // Generate the object Ids if needed and the persistence objects to be removed
        for (int i = 0; i < objIds.size(); i++) {
          final COMObjectEntityPK id
              = COMObjectEntity.generatePK(objTypeId, domainId, objIds.get(i));
          COMObjectEntity perObj = dbBackend.getEM().find(CLASS_ENTITY, id);
          dbBackend.getEM().getTransaction().begin();
          dbBackend.getEM().remove(perObj);
          dbBackend.getEM().getTransaction().commit();
        }
      } finally {
        dbBackend.closeEntityManager();
      }

      if (publishEvents != null) {
        generalExecutor.submit(publishEvents);
      }
      vacuum();
    });
  }

  /**
   * Allows to quickly remove number of objects without generating any events
   */
  public void quickRemove(final List<COMObjectEntity> objs) {
    this.sequencialStoring.set(false); // Sequential stores can no longer happen otherwise we break order

    dbTransactionsExecutor.execute(() -> {
      try {
        dbBackend.createEntityManager();
        dbBackend.getEM().getTransaction().begin();

        for (COMObjectEntity e : objs) {
          e = dbBackend.getEM().merge(e);
          dbBackend.getEM().remove(e);
        }
        dbBackend.getEM().getTransaction().commit();
      } finally {
        dbBackend.closeEntityManager();
      }
      vacuum();
    });
  }

  public void update(final List<COMObjectEntity> newObjs, final Runnable publishEvents) {
    this.sequencialStoring.set(false); // Sequential stores can no longer happen otherwise we break order

    dbTransactionsExecutor.execute(() -> {
      try {
        dbBackend.createEntityManager();  // 0.166 ms

        for (COMObjectEntity e : newObjs) {
          final COMObjectEntityPK id = e.getPrimaryKey();
          COMObjectEntity previousObj = dbBackend.getEM().find(CLASS_ENTITY, id);

          dbBackend.getEM().getTransaction().begin();
          dbBackend.getEM().remove(previousObj);
          dbBackend.getEM().getTransaction().commit();

          // Maybe we can replace the 3 lines below with a persistObjects(newObjs) after the for loop
          dbBackend.getEM().getTransaction().begin();
          dbBackend.getEM().persist(e);
          dbBackend.getEM().getTransaction().commit();
        }
      } finally {
        dbBackend.closeEntityManager();
      }
      if (publishEvents != null) {
        generalExecutor.submit(publishEvents);
      }
    });
  }

  private enum QueryType {
    SELECT("SELECT"), DELETE("DELETE");
    private final String queryPrefix;
    private QueryType(String queryPrefix) {
      this.queryPrefix = queryPrefix;
    }
    public String getQueryPrefix() {
      return queryPrefix;
    }
  }
  private class QueryCallable implements Callable {

    private final IntegerList objTypeIds;
    private final ArchiveQuery archiveQuery;
    private final IntegerList domainIds;
    private final Integer providerURIId;
    private final Integer networkId;
    private final SourceLinkContainer sourceLink;
    private final QueryFilter filter;
    private final QueryType queryType;


    public QueryCallable(final IntegerList objTypeIds,
        final ArchiveQuery archiveQuery, final IntegerList domainIds,
        final Integer providerURIId, final Integer networkId,
        final SourceLinkContainer sourceLink, final QueryFilter filter) {
      this(objTypeIds, archiveQuery, domainIds, providerURIId, networkId, sourceLink, filter, QueryType.SELECT);
    }

    public QueryCallable(final IntegerList objTypeIds,
        final ArchiveQuery archiveQuery, final IntegerList domainIds,
        final Integer providerURIId, final Integer networkId,
        final SourceLinkContainer sourceLink, final QueryFilter filter,
        final QueryType queryType) {
      this.objTypeIds = objTypeIds;
      this.archiveQuery = archiveQuery;
      this.domainIds = domainIds;
      this.providerURIId = providerURIId;
      this.networkId = networkId;
      this.sourceLink = sourceLink;
      this.filter = filter;
      this.queryType = queryType;
    }

    @Override
    public Object call() {
      final boolean relatedContainsWildcard = (archiveQuery.getRelated().equals((long) 0));
      final boolean startTimeContainsWildcard = (archiveQuery.getStartTime() == null);
      final boolean endTimeContainsWildcard = (archiveQuery.getEndTime() == null);
      final boolean providerURIContainsWildcard = (archiveQuery.getProvider() == null);
      final boolean networkContainsWildcard = (archiveQuery.getNetwork() == null);

      final boolean sourceContainsWildcard = (archiveQuery.getSource() == null);
      boolean sourceObjIdContainsWildcard = true;

      if (!sourceContainsWildcard) {
        sourceObjIdContainsWildcard
            = (archiveQuery.getSource().getKey().getInstId() == null || archiveQuery.getSource().getKey().getInstId() == 0);
      }

      // Generate the query string
      String fieldsList = "objectTypeId, objId, domainId, network, OBJ, providerURI, "
          + "relatedLink, sourceLinkDomainId, sourceLinkObjId, sourceLinkObjectTypeId, timestampArchiveDetails";
      String queryString = queryType.getQueryPrefix() + " " + (queryType == QueryType.SELECT ? fieldsList : "") + " FROM COMObjectEntity ";

      queryString += "WHERE ";

      queryString += generateQueryStringFromLists("domainId", domainIds);
      queryString += generateQueryStringFromLists("objectTypeId", objTypeIds);

      queryString += (relatedContainsWildcard) ? ""
          : "relatedLink=" + archiveQuery.getRelated() + " AND ";
      queryString += (startTimeContainsWildcard) ? ""
          : "timestampArchiveDetails>=" + archiveQuery.getStartTime().getValue() + " AND ";
      queryString += (endTimeContainsWildcard) ? ""
          : "timestampArchiveDetails<=" + archiveQuery.getEndTime().getValue() + " AND ";
      queryString += (providerURIContainsWildcard) ? ""
          : "providerURI=" + providerURIId + " AND ";
      queryString += (networkContainsWildcard) ? "" : "network=" + networkId + " AND ";

      if (!sourceContainsWildcard) {
        queryString += generateQueryStringFromLists("sourceLinkObjectTypeId",
            sourceLink.getObjectTypeIds());
        queryString += generateQueryStringFromLists("sourceLinkDomainId",
            sourceLink.getDomainIds());
        queryString += (sourceObjIdContainsWildcard) ? ""
            : "sourceLinkObjId=" + sourceLink.getObjId() + " AND ";
      }

      queryString = queryString.substring(0, queryString.length() - 4);

      // A dedicated PaginationFilter for this particular COM Archive implementation was created and implemented
      if (filter != null) {
        if (filter instanceof PaginationFilter) {
          PaginationFilter pfilter = (PaginationFilter) filter;

          // Double check if the filter fields are really not null
          if (pfilter.getLimit() != null && pfilter.getOffset() != null) {
            String sortOrder = "ASC ";
            if (archiveQuery.getSortOrder() != null) {
              sortOrder = (archiveQuery.getSortOrder()) ? "ASC " : "DESC ";
            }

            queryString += "ORDER BY timestampArchiveDetails "
                + sortOrder
                + "LIMIT "
                + pfilter.getLimit().getValue()
                + " OFFSET "
                + pfilter.getOffset().getValue();
          }
        }
      }
      List resultList = null;
      try {
        dbBackend.createEntityManager();
        final Query query = dbBackend.getEM().createNativeQuery(queryString);
        if (queryType == QueryType.DELETE) {
          // DELETE or UPDATE returns number of rows updated
          dbBackend.getEM().getTransaction().begin();
          int resultRows = query.executeUpdate();
          dbBackend.getEM().getTransaction().commit();
          return Integer.valueOf(resultRows);
        } else if (queryType == QueryType.SELECT) {
          resultList = query.getResultList();
        }
      } finally {
        dbBackend.closeEntityManager();
      }
      if (queryType == QueryType.SELECT) {
        if (resultList == null) {
          return new ArrayList<COMObjectEntity>(0);
        } else {
          // FYI: SELECT objectTypeId, objId, domainId, network, OBJ, providerURI, relatedLink,
          // sourceLinkDomainId, sourceLinkObjId, sourceLinkObjectTypeId, timestampArchiveDetails FROM COMObjectEntity
          final ArrayList<COMObjectEntity> perObjs = new ArrayList<>(resultList.size());

          // Conversion from the raw SQL response into a COMObjectEntity
          for (Object obj : resultList) {
            // (final Integer objectTypeId, final Integer domainId, final Long objId)
            final SourceLinkContainer source = new SourceLinkContainer(
                (Integer) ((Object[]) obj)[9],
                (Integer) ((Object[]) obj)[7],
                convert2Long(((Object[]) obj)[8])
            );

            perObjs.add(new COMObjectEntity(
                (Integer) ((Object[]) obj)[0],
                (Integer) ((Object[]) obj)[2],
                convert2Long(((Object[]) obj)[1]),
                convert2Long(((Object[]) obj)[10]),
                (Integer) ((Object[]) obj)[5],
                (Integer) ((Object[]) obj)[3],
                source,
                convert2Long(((Object[]) obj)[6]),
                (byte[]) ((Object[]) obj)[4]
            ));
          }
          return perObjs;
        }
      } else {
        return null;
      }
    }
  }

  public static String generateQueryStringFromLists(final String field, final IntegerList list) {
    if (list.isEmpty()) {
      return "";
    }

    if (list.size() == 1) {
      return field + "=" + list.get(0) + " AND ";
    }

    StringBuilder stringForWildcards = new StringBuilder("(");

    for (Integer id : list) {
      stringForWildcards.append(field).append("=").append(id).append(" OR ");
    }

    // Remove the " OR " par of it!
    stringForWildcards = new StringBuilder(stringForWildcards.substring(0, stringForWildcards.length() - 4));

    return stringForWildcards + ") AND ";
  }

  public ArrayList<COMObjectEntity> query(final IntegerList objTypeIds,
      final ArchiveQuery archiveQuery, final IntegerList domainIds,
      final Integer providerURIId, final Integer networkId,
      final SourceLinkContainer sourceLink, final QueryFilter filter) {
    this.sequencialStoring.set(false); // Sequential stores can no longer happen otherwise we break order
    final QueryCallable task = new QueryCallable(objTypeIds, archiveQuery,
        domainIds, providerURIId, networkId, sourceLink, filter);

    Future<Object> future = dbTransactionsExecutor.submit(task);

    try {
      return (ArrayList<COMObjectEntity>)future.get();
    } catch (InterruptedException | ExecutionException ex) {
      Logger.getLogger(TransactionsProcessor.class.getName()).log(Level.SEVERE, null, ex);
    }

    return null;
  }

  public int delete(final IntegerList objTypeIds,
      final ArchiveQuery archiveQuery, final IntegerList domainIds,
      final Integer providerURIId, final Integer networkId,
      final SourceLinkContainer sourceLink, final QueryFilter filter) {
    this.sequencialStoring.set(false); // Sequential stores can no longer happen otherwise we break order
    final QueryCallable task = new QueryCallable(objTypeIds, archiveQuery,
        domainIds, providerURIId, networkId, sourceLink, filter, QueryType.DELETE);

    Future<Object> future = dbTransactionsExecutor.submit(task);

    try {
      return (Integer)future.get();
    } catch (InterruptedException | ExecutionException ex) {
      Logger.getLogger(TransactionsProcessor.class.getName()).log(Level.SEVERE, null, ex);
    }

    return 0;
  }

  public void resetMainTable(final Callable task) {
    this.sequencialStoring.set(false); // Sequential stores can no longer happen otherwise we break order
    Future<Integer> nullValue = dbTransactionsExecutor.submit(task);
    Logger.getLogger(TransactionsProcessor.class.getName()).info("Reset table submitted!");

    try {
      Integer dummyInt = nullValue.get(); // Dummy code to Force a wait until the actual restart is done!
      Logger.getLogger(TransactionsProcessor.class.getName()).info("Reset table callback!");
    } catch (InterruptedException | ExecutionException ex) {
      Logger.getLogger(TransactionsProcessor.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public void stopInteractions(final Callable task) {
    this.sequencialStoring.set(false); // Sequential stores can no longer happen otherwise we break order
    Future<Integer> nullValue = dbTransactionsExecutor.submit(task);

    try {
      nullValue.get(); // Dummy code to Force a wait until the actual restart is done!
    } catch (InterruptedException | ExecutionException ex) {
      Logger.getLogger(TransactionsProcessor.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  /**
   * The database backend thread factory
   */
  static class DBThreadFactory implements ThreadFactory {

    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    DBThreadFactory(String prefix) {
      SecurityManager s = System.getSecurityManager();
      group = (s != null) ? s.getThreadGroup()
          : Thread.currentThread().getThreadGroup();
      namePrefix = prefix + "-thread-";
    }

    @Override
    public Thread newThread(Runnable r) {
      Thread t = new Thread(group, r,
          namePrefix + threadNumber.getAndIncrement(),
          0);
      if (t.isDaemon()) {
        t.setDaemon(false);
      }
      if (t.getPriority() != Thread.NORM_PRIORITY) {
        t.setPriority(Thread.NORM_PRIORITY);
      }
      return t;
    }
  }

  private static Long convert2Long(final Object obj) {
    if (obj == null) {
      return null;
    }

    if (obj instanceof Long) {
      return (Long) obj;
    }

    if (obj instanceof Integer) {
      return ((Integer) obj).longValue();
    }

    return null;
  }

}
