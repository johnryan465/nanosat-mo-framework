/* ----------------------------------------------------------------------------
 * Copyright (C) 2015      European Space Agency
 *                         European Space Operations Centre
 *                         Darmstadt
 *                         Germany
 * ----------------------------------------------------------------------------
 * System                : ESA NanoSat MO Framework
 * ----------------------------------------------------------------------------
 * Licensed under the European Space Agency Public License, Version 2.0
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
package esa.mo.nmf.ctt.services.sm;

import esa.mo.helpertools.connections.ConnectionConsumer;
import esa.mo.sm.impl.consumer.AppsLauncherConsumerServiceImpl;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.MALInteractionException;
import org.ccsds.moims.mo.mal.MALStandardError;
import org.ccsds.moims.mo.mal.structures.BooleanList;
import org.ccsds.moims.mo.mal.structures.Identifier;
import org.ccsds.moims.mo.mal.structures.IdentifierList;
import org.ccsds.moims.mo.mal.structures.LongList;
import org.ccsds.moims.mo.mal.structures.Subscription;
import org.ccsds.moims.mo.mal.structures.UpdateHeader;
import org.ccsds.moims.mo.mal.transport.MALMessageHeader;
import org.ccsds.moims.mo.softwaremanagement.appslauncher.AppsLauncherHelper;
import org.ccsds.moims.mo.softwaremanagement.appslauncher.body.ListAppResponse;
import org.ccsds.moims.mo.softwaremanagement.appslauncher.consumer.AppsLauncherAdapter;

/**
 *
 * @author Cesar Coelho
 */
public class AppsLauncherConsumerPanel extends javax.swing.JPanel {

    private AppsLauncherConsumerServiceImpl serviceSMAppsLauncher;
    private AppsLauncherTablePanel appsTable;
    private final HashMap<Long, JTextArea> textAreas = new HashMap<Long, JTextArea>();

    /**
     *
     * @param serviceSMAppsLauncher
     */
    public AppsLauncherConsumerPanel(AppsLauncherConsumerServiceImpl serviceSMAppsLauncher) {
        initComponents();

        appsTable = new AppsLauncherTablePanel(serviceSMAppsLauncher.getCOMServices().getArchiveService());

        appsTable.getTable().addMouseListener(new MouseListener() {
            @Override
            public void mousePressed(MouseEvent evt) {
            }

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // If there is a concrete row selected...
                if (appsTable.getSelectedRow() != -1) {
                    final Long objId = appsTable.getCOMObjects().get(appsTable.getSelectedRow()).getArchiveDetails().getInstId();
                    jScrollPane1.setViewportView(textAreas.get(objId));
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

        jScrollPane2.setViewportView(appsTable);

        this.serviceSMAppsLauncher = serviceSMAppsLauncher;
        this.listAppAllButtonActionPerformed(null);

        // Subscribe to Apps
        Subscription subscription = ConnectionConsumer.subscriptionWildcard();
        try {
            serviceSMAppsLauncher.getAppsLauncherStub().monitorExecutionRegister(subscription, new AppsLauncherConsumerAdapter());
        } catch (MALInteractionException ex) {
            Logger.getLogger(AppsLauncherConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MALException ex) {
            Logger.getLogger(AppsLauncherConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * This method is called from within the constructor to initialize the
     * formAddModifyParameter. WARNING: Do NOT modify this code. The content of
     * this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel6 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        appVerboseTextArea = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        defaultTable = new javax.swing.JTable();
        parameterTab = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        runAppButton = new javax.swing.JButton();
        stopAppButton = new javax.swing.JButton();
        killAppButton = new javax.swing.JButton();
        listAppAllButton = new javax.swing.JButton();

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("Apps Launcher Service");
        jLabel6.setToolTipText("");

        appVerboseTextArea.setColumns(20);
        appVerboseTextArea.setFont(new java.awt.Font("Courier New", 0, 12)); // NOI18N
        appVerboseTextArea.setRows(5);
        jScrollPane1.setViewportView(appVerboseTextArea);

        jScrollPane2.setHorizontalScrollBar(null);
        jScrollPane2.setPreferredSize(new java.awt.Dimension(796, 380));
        jScrollPane2.setRequestFocusEnabled(false);

        defaultTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null,  new Boolean(true), null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Obj Inst Id", "name", "description", "category", "runAtStartup", "running", "Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        defaultTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        defaultTable.setAutoscrolls(false);
        defaultTable.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        defaultTable.setMaximumSize(null);
        defaultTable.setMinimumSize(null);
        defaultTable.setPreferredSize(null);
        defaultTable.addContainerListener(new java.awt.event.ContainerAdapter() {
            public void componentAdded(java.awt.event.ContainerEvent evt) {
                defaultTableComponentAdded(evt);
            }
        });
        jScrollPane2.setViewportView(defaultTable);

        parameterTab.setLayout(new javax.swing.BoxLayout(parameterTab, javax.swing.BoxLayout.LINE_AXIS));

        jPanel1.setMinimumSize(new java.awt.Dimension(419, 23));
        jPanel1.setPreferredSize(new java.awt.Dimension(419, 23));

        runAppButton.setText("runApp");
        runAppButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runAppButtonActionPerformed(evt);
            }
        });
        jPanel1.add(runAppButton);

        stopAppButton.setText("stopApp");
        stopAppButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopAppButtonActionPerformed(evt);
            }
        });
        jPanel1.add(stopAppButton);

        killAppButton.setText("killApp");
        killAppButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                killAppButtonActionPerformed(evt);
            }
        });
        jPanel1.add(killAppButton);

        listAppAllButton.setText("listApp(\"*\")");
        listAppAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listAppAllButtonActionPerformed(evt);
            }
        });
        jPanel1.add(listAppAllButton);

        parameterTab.add(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(parameterTab, javax.swing.GroupLayout.DEFAULT_SIZE, 893, Short.MAX_VALUE)
            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(parameterTab, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void listAppAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listAppAllButtonActionPerformed
        IdentifierList idList = new IdentifierList();
        idList.add(new Identifier("*"));

/*
        ListAppResponse output;
        try {
            output = this.serviceSMAppsLauncher.getAppsLauncherStub().listApp(idList, new Identifier("*"));
            appsTable.refreshTableWithIds(output.getBodyElement0(), 
                    serviceSMAppsLauncher.getConnectionDetails().getDomain(), 
                    AppsLauncherHelper.APP_OBJECT_TYPE);

            for (int i = 0; i < output.getBodyElement0().size(); i++) {
                Long objId = output.getBodyElement0().get(i);

                if (textAreas.get(objId) == null) {
                    javax.swing.JTextArea textArea = new javax.swing.JTextArea();
                    textAreas.put(objId, textArea);
                    textArea.setColumns(20);
                    textArea.setFont(new java.awt.Font("Courier New", 0, 12)); // NOI18N
                    textArea.setRows(5);
                }
            }

            Logger.getLogger(AppsLauncherConsumerPanel.class.getName()).log(Level.INFO, "listDefinition(\"*\") returned {0} object instance identifiers", output.getBodyElement0().size());
        } catch (MALInteractionException ex) {
            JOptionPane.showMessageDialog(null, "There was an error during the listDefinition operation.", "Error", JOptionPane.PLAIN_MESSAGE);
            Logger.getLogger(AppsLauncherConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
            return;
        } catch (MALException ex) {
            JOptionPane.showMessageDialog(null, "There was an error during the listDefinition operation.", "Error", JOptionPane.PLAIN_MESSAGE);
            Logger.getLogger(AppsLauncherConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
*/

        try {
            this.serviceSMAppsLauncher.getAppsLauncherStub().asyncListApp(idList, new Identifier("*"), new AppsLauncherAdapter() {
                @Override
                public void listAppResponseReceived(MALMessageHeader msgHeader, LongList appInstIds, BooleanList running, Map qosProperties) {
                    appsTable.refreshTableWithIds(appInstIds, 
                        serviceSMAppsLauncher.getConnectionDetails().getDomain(), 
                        AppsLauncherHelper.APP_OBJECT_TYPE);
                    
                    for (int i = 0; i < appInstIds.size(); i++) {
                        Long objId = appInstIds.get(i);

                        if (textAreas.get(objId) == null) {
                            javax.swing.JTextArea textArea = new javax.swing.JTextArea();
                            textAreas.put(objId, textArea);
                            textArea.setColumns(20);
                            textArea.setFont(new java.awt.Font("Courier New", 0, 12)); // NOI18N
                            textArea.setRows(5);
                        }
                    }

                    Logger.getLogger(AppsLauncherConsumerPanel.class.getName()).log(Level.INFO, "listApp(\"*\") returned {0} object instance identifiers", appInstIds.size());
                }

                @Override
                public void listAppErrorReceived(MALMessageHeader msgHeader, MALStandardError error, Map qosProperties) {
                    JOptionPane.showMessageDialog(null, "There was an error during the listApp operation.", "Error", JOptionPane.PLAIN_MESSAGE);
                    Logger.getLogger(AppsLauncherConsumerPanel.class.getName()).log(Level.SEVERE, null, error);
                }
            }
            );
        } catch (MALInteractionException ex) {
            Logger.getLogger(AppsLauncherConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MALException ex) {
            Logger.getLogger(AppsLauncherConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_listAppAllButtonActionPerformed

    private void killAppButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_killAppButtonActionPerformed
        if (appsTable.getSelectedRow() == -1) { // The row is not selected?
            return;  // Well, then nothing to be done here folks!
        }

        LongList ids = new LongList();
        Long objId = appsTable.getSelectedCOMObject().getArchiveDetails().getInstId();
        ids.add(objId);

        try {
            this.serviceSMAppsLauncher.getAppsLauncherStub().killApp(ids);
            appsTable.switchEnabledstatus(false);
            
            for(Long id : ids){
                appsTable.reportStatus("Killed!", id.intValue());
            }
        } catch (MALInteractionException ex) {
            JOptionPane.showMessageDialog(null, "Error!\nException:\n" + ex + "\n" + ex.getMessage(), "Error!", JOptionPane.PLAIN_MESSAGE);
            Logger.getLogger(AppsLauncherConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MALException ex) {
            JOptionPane.showMessageDialog(null, "Error!\nException:\n" + ex + "\n" + ex.getMessage(), "Error!", JOptionPane.PLAIN_MESSAGE);
            Logger.getLogger(AppsLauncherConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_killAppButtonActionPerformed

    private void stopAppButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopAppButtonActionPerformed
        if (appsTable.getSelectedRow() == -1) { // The row is not selected?
            return;  // Well, then nothing to be done here folks!
        }

        LongList ids = new LongList();
        Long objId = appsTable.getSelectedCOMObject().getArchiveDetails().getInstId();
        ids.add(objId);

        try {
            this.serviceSMAppsLauncher.getAppsLauncherStub().stopApp(ids, new StopAdapter());
            appsTable.switchEnabledstatus(false);
            
            for(Long id : ids){
                appsTable.reportStatus("Stop request sent", id.intValue());
            }
        } catch (MALInteractionException ex) {
            JOptionPane.showMessageDialog(null, "Error!\nException:\n" + ex + "\n" + ex.getMessage(), "Error!", JOptionPane.PLAIN_MESSAGE);
            Logger.getLogger(AppsLauncherConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MALException ex) {
            JOptionPane.showMessageDialog(null, "Error!\nException:\n" + ex + "\n" + ex.getMessage(), "Error!", JOptionPane.PLAIN_MESSAGE);
            Logger.getLogger(AppsLauncherConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_stopAppButtonActionPerformed

    private void runAppButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runAppButtonActionPerformed
        if (appsTable.getSelectedRow() == -1) { // The row is not selected?
            return;  // Well, then nothing to be done here folks!
        }

        LongList ids = new LongList();
        Long objId = appsTable.getSelectedCOMObject().getArchiveDetails().getInstId();
        ids.add(objId);

        try {
            this.serviceSMAppsLauncher.getAppsLauncherStub().runApp(ids);
            appsTable.switchEnabledstatus(true);
            for(Long id : ids){
                appsTable.reportStatus("Starting...", id.intValue());
            }
        } catch (MALInteractionException ex) {
            JOptionPane.showMessageDialog(null, "Error!\nException:\n" + ex + "\n" + ex.getMessage(), "Error!", JOptionPane.PLAIN_MESSAGE);
            Logger.getLogger(AppsLauncherConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MALException ex) {
            JOptionPane.showMessageDialog(null, "Error!\nException:\n" + ex + "\n" + ex.getMessage(), "Error!", JOptionPane.PLAIN_MESSAGE);
            Logger.getLogger(AppsLauncherConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_runAppButtonActionPerformed

    private void defaultTableComponentAdded(java.awt.event.ContainerEvent evt) {//GEN-FIRST:event_defaultTableComponentAdded
        // TODO add your handling code here:
    }//GEN-LAST:event_defaultTableComponentAdded

    public class AppsLauncherConsumerAdapter extends AppsLauncherAdapter {

        @Override
        public void monitorExecutionNotifyReceived(org.ccsds.moims.mo.mal.transport.MALMessageHeader msgHeader,
                org.ccsds.moims.mo.mal.structures.Identifier _Identifier0,
                org.ccsds.moims.mo.mal.structures.UpdateHeaderList updateHeaderList,
                org.ccsds.moims.mo.mal.structures.StringList outputStream, java.util.Map qosProperties) {

            for (int i = 0; i < updateHeaderList.size(); i++) {
                final String out = outputStream.get(i);
                final UpdateHeader updateHeader = updateHeaderList.get(i);

                javax.swing.JTextArea textArea = textAreas.get(updateHeader.getKey().getSecondSubKey());
                textArea.append(out);
                textArea.setCaretPosition(textArea.getDocument().getLength());
            }
        }
    }

    public class StopAdapter extends AppsLauncherAdapter {

        @Override
        public void stopAppAckReceived(org.ccsds.moims.mo.mal.transport.MALMessageHeader msgHeader, java.util.Map qosProperties) {
            appsTable.reportStatus("Stopping...", 1);
        }

        @Override
        public void stopAppUpdateReceived(org.ccsds.moims.mo.mal.transport.MALMessageHeader msgHeader, Long appClosing, java.util.Map qosProperties) {
            appsTable.reportStatus("Stopped!", appClosing.intValue());
        }

        @Override
        public void stopAppAckErrorReceived(org.ccsds.moims.mo.mal.transport.MALMessageHeader msgHeader, 
                org.ccsds.moims.mo.mal.MALStandardError error, java.util.Map qosProperties){
            appsTable.reportStatus("Unable to stop... :S", 1);
        }

        @Override
        public void stopAppResponseReceived(org.ccsds.moims.mo.mal.transport.MALMessageHeader msgHeader, java.util.Map qosProperties) {
            appsTable.reportStatus("(4) All apps have been closed!", 1);
        }

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea appVerboseTextArea;
    private javax.swing.JTable defaultTable;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton killAppButton;
    private javax.swing.JButton listAppAllButton;
    private javax.swing.JPanel parameterTab;
    private javax.swing.JButton runAppButton;
    private javax.swing.JButton stopAppButton;
    // End of variables declaration//GEN-END:variables
}