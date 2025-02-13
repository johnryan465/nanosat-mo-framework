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
package esa.mo.nmf.ctt.services.mc;

import esa.mo.com.impl.consumer.ArchiveConsumerServiceImpl;
import esa.mo.com.impl.provider.ArchivePersistenceObject;
import esa.mo.nmf.ctt.utils.SharedTablePanel;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ccsds.moims.mo.mal.structures.Identifier;
import org.ccsds.moims.mo.mc.action.structures.ActionDefinitionDetails;

/**
 *
 * @author Cesar Coelho
 */
public class ActionTablePanel extends SharedTablePanel {

    public ActionTablePanel(ArchiveConsumerServiceImpl archiveService) {
        super(archiveService);
    }

    @Override
    public void addEntry(final Identifier name, final ArchivePersistenceObject comObject) {
        if (comObject == null) {
            Logger.getLogger(SharedTablePanel.class.getName()).log(Level.SEVERE, "The table cannot process a null COM Object.");
            return;
        }

        try {
            semaphore.acquire();
        } catch (InterruptedException ex) {
            Logger.getLogger(SharedTablePanel.class.getName()).log(Level.SEVERE, null, ex);
        }

        ActionDefinitionDetails pDef = (ActionDefinitionDetails) comObject.getObject();

        tableData.addRow(new Object[]{
            comObject.getArchiveDetails().getDetails().getRelated(),
            comObject.getArchiveDetails().getInstId(),
            name.toString(),
            pDef.getDescription(),
            String.valueOf(pDef.getCategory().getValue()),
            pDef.getProgressStepCount().toString()
        });

        comObjects.add(comObject);
        semaphore.release();
    }

    @Override
    public void defineTableContent() {
        String[] tableCol = new String[]{
            "Identity", "Obj Inst Id", "name", "description", "Category", "progressStepCount"};

        tableData = new javax.swing.table.DefaultTableModel(
                new Object[][]{}, tableCol) {
            Class[] types = new Class[]{
                java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, 
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            @Override               //all cells false
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };

        super.getTable().setModel(tableData);
    }

}
