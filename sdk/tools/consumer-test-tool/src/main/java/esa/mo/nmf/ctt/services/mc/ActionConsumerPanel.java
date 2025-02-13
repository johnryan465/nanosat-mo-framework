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

import esa.mo.com.impl.provider.ArchivePersistenceObject;
import esa.mo.com.impl.util.HelperArchive;
import esa.mo.helpertools.helpers.HelperAttributes;
import esa.mo.mc.impl.consumer.ActionConsumerServiceImpl;
import esa.mo.nmf.NMFException;
import esa.mo.nmf.groundmoadapter.GroundMOAdapterImpl;
import esa.mo.tools.mowindow.MOWindow;
import java.io.InterruptedIOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.MALInteractionException;
import org.ccsds.moims.mo.mal.MALStandardError;
import org.ccsds.moims.mo.mal.structures.Attribute;
import org.ccsds.moims.mo.mal.structures.Identifier;
import org.ccsds.moims.mo.mal.structures.IdentifierList;
import org.ccsds.moims.mo.mal.structures.LongList;
import org.ccsds.moims.mo.mal.structures.UOctet;
import org.ccsds.moims.mo.mal.structures.UShort;
import org.ccsds.moims.mo.mal.transport.MALMessageHeader;
import org.ccsds.moims.mo.mc.action.ActionHelper;
import org.ccsds.moims.mo.mc.action.consumer.ActionAdapter;
import org.ccsds.moims.mo.mc.action.structures.ActionCreationRequest;
import org.ccsds.moims.mo.mc.action.structures.ActionCreationRequestList;
import org.ccsds.moims.mo.mc.action.structures.ActionDefinitionDetails;
import org.ccsds.moims.mo.mc.action.structures.ActionDefinitionDetailsList;
import org.ccsds.moims.mo.mc.action.structures.ActionInstanceDetails;
import org.ccsds.moims.mo.mc.structures.ArgumentDefinitionDetails;
import org.ccsds.moims.mo.mc.structures.ArgumentDefinitionDetailsList;
import org.ccsds.moims.mo.mc.structures.AttributeValue;
import org.ccsds.moims.mo.mc.structures.AttributeValueList;
import org.ccsds.moims.mo.mc.structures.ObjectInstancePair;
import org.ccsds.moims.mo.mc.structures.ObjectInstancePairList;

/**
 *
 * @author Cesar Coelho
 */
public class ActionConsumerPanel extends javax.swing.JPanel {

    private final ActionConsumerServiceImpl serviceMCAction;
    private final ActionTablePanel actionTable;
    private GroundMOAdapterImpl gma;

    /**
     * Creates new formAddModifyParameter ConsumerPanelArchive
     *
     * @param groundMOAdapter
     */
    public ActionConsumerPanel(GroundMOAdapterImpl groundMOAdapter) {
        initComponents();
        this.gma = groundMOAdapter;
        this.serviceMCAction = groundMOAdapter.getMCServices().getActionService();
        actionTable = new ActionTablePanel(serviceMCAction.getCOMServices().getArchiveService());
        jScrollPane2.setViewportView(actionTable);
    }
    
    public void init(){
        this.listDefinitionAllButtonActionPerformed(null);
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
        jScrollPane2 = new javax.swing.JScrollPane();
        actionDefinitionsTable = new javax.swing.JTable();
        parameterTab = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        submitAction = new javax.swing.JButton();
        preCheckActionButton = new javax.swing.JButton();
        listDefinitionButton = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        addDefinitionButton = new javax.swing.JButton();
        updateDefinitionButton = new javax.swing.JButton();
        removeDefinitionButton = new javax.swing.JButton();
        listDefinitionAllButton = new javax.swing.JButton();
        removeDefinitionAllButton = new javax.swing.JButton();

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("Action Service - Definitions");
        jLabel6.setToolTipText("");

        jScrollPane2.setHorizontalScrollBar(null);
        jScrollPane2.setPreferredSize(new java.awt.Dimension(796, 380));
        jScrollPane2.setRequestFocusEnabled(false);

        actionDefinitionsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, Boolean.TRUE, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Identity", "Obj Inst Id", "name", "description", "rawType", "rawUnit", "generationEnabled", "updateInterval"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.String.class, java.lang.Boolean.class, java.lang.Float.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        actionDefinitionsTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        actionDefinitionsTable.setAutoscrolls(false);
        actionDefinitionsTable.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        actionDefinitionsTable.setMaximumSize(null);
        actionDefinitionsTable.addContainerListener(new java.awt.event.ContainerAdapter() {
            public void componentAdded(java.awt.event.ContainerEvent evt) {
                actionDefinitionsTableComponentAdded(evt);
            }
        });
        jScrollPane2.setViewportView(actionDefinitionsTable);

        parameterTab.setLayout(new java.awt.GridLayout(2, 1));

        submitAction.setText("submitAction");
        submitAction.addActionListener(this::submitActionActionPerformed);
        jPanel1.add(submitAction);

        preCheckActionButton.setText("preCheckAction");
        preCheckActionButton.addActionListener(this::preCheckActionButtonActionPerformed);
        jPanel1.add(preCheckActionButton);

        listDefinitionButton.setText("listDefinition()");
        listDefinitionButton.addActionListener(this::listDefinitionButtonActionPerformed);
        jPanel1.add(listDefinitionButton);

        parameterTab.add(jPanel1);

        addDefinitionButton.setText("addDefinition");
        addDefinitionButton.addActionListener(this::addDefinitionButtonActionPerformed);
        jPanel5.add(addDefinitionButton);

        updateDefinitionButton.setText("updateDefinition");
        updateDefinitionButton.addActionListener(this::updateDefinitionButtonActionPerformed);
        jPanel5.add(updateDefinitionButton);

        removeDefinitionButton.setText("removeDefinition");
        removeDefinitionButton.addActionListener(this::removeDefinitionButtonActionPerformed);
        jPanel5.add(removeDefinitionButton);

        listDefinitionAllButton.setText("listDefinition(\"*\")");
        listDefinitionAllButton.addActionListener(this::listDefinitionAllButtonActionPerformed);
        jPanel5.add(listDefinitionAllButton);

        removeDefinitionAllButton.setText("removeDefinition(0)");
        removeDefinitionAllButton.addActionListener(this::removeDefinitionAllButtonActionPerformed);
        jPanel5.add(removeDefinitionAllButton);

        parameterTab.add(jPanel5);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(parameterTab, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 882, Short.MAX_VALUE)
            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(parameterTab, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void submitActionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitActionActionPerformed
        if (actionTable.getSelectedRow() == -1) { // The row is not selected?
            return;  // Well, then nothing to be done here folks!
        }

        ArchivePersistenceObject comObject = actionTable.getSelectedCOMObject();
        Long objIdDef = comObject.getObjectId();

        ActionDefinitionDetails actDef = (ActionDefinitionDetails) comObject.getObject();
        AttributeValueList argumentValueList = new AttributeValueList();
        ArgumentDefinitionDetailsList arguments = actDef.getArguments();

        if (arguments != null){
            for (int i = 0; i < arguments.size(); i++) {
                if (arguments.get(i) == null) {  // If the argument is null, then please jump over it
                    argumentValueList.add(null);
                    continue;
                }

                String attributeName = HelperAttributes.typeShortForm2attributeName(arguments.get(i).getRawType().intValue());
                Object aaa = HelperAttributes.attributeName2object(attributeName);
                Attribute elem = (Attribute) HelperAttributes.javaType2Attribute(aaa);

                AttributeValue argumentValue = new AttributeValue();
                argumentValue.setValue(elem);

                argumentValueList.add(argumentValue);
            }
        }

        // Allow the user to specify the arguments
        MOWindow moWindow = new MOWindow(argumentValueList, true, "Action arguments list");
        try {
            argumentValueList = (AttributeValueList) moWindow.getObject();
        } catch (InterruptedIOException ex) {
            return;
        }

        try {
            gma.launchAction(actionTable.getSelectedDefinitionObjId(), argumentValueList, new ActionAdapter() {
                @Override
                public void submitActionAckReceived(MALMessageHeader msgHeader, Map qosProperties) {
                    super.submitActionAckReceived(msgHeader, qosProperties);
                    JOptionPane.showMessageDialog(null, "The action instance was successfully submitted.", "Success", JOptionPane.PLAIN_MESSAGE);
                }

                @Override
                public void submitActionErrorReceived(MALMessageHeader msgHeader, MALStandardError error, Map qosProperties) {
                    super.submitActionErrorReceived(msgHeader, error, qosProperties);
                    JOptionPane.showMessageDialog(null, "The action submittal has failed.", "Error", JOptionPane.PLAIN_MESSAGE);
                }
            });

        } catch (NMFException ex) {
            JOptionPane.showMessageDialog(null, "There was an error with the submitted action.", "Error", JOptionPane.PLAIN_MESSAGE);
            Logger.getLogger(ActionConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_submitActionActionPerformed

    private void listDefinitionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listDefinitionButtonActionPerformed
        IdentifierList actionNames = new IdentifierList();
        MOWindow actionNamesWindow = new MOWindow(actionNames, true);

        try {
            ObjectInstancePairList objIds;
            try {
                objIds = this.serviceMCAction.getActionStub().listDefinition((IdentifierList) actionNamesWindow.getObject());
            } catch (InterruptedIOException ex) {
                return;
            }

            StringBuilder str = new StringBuilder("Object instance identifiers on the provider: \n");
            if (objIds != null) {
                for (ObjectInstancePair objId : objIds) {
                    str.append("ObjId Def: ").append(objId.getObjDefInstanceId().toString()).append(" Identity: ").append(objId.getObjIdentityInstanceId().toString()).append("\n");
                }
            }

            JOptionPane.showMessageDialog(null, str.toString(), "Returned List from the Provider", JOptionPane.PLAIN_MESSAGE);

        } catch (MALInteractionException | MALException ex) {
            Logger.getLogger(ActionConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_listDefinitionButtonActionPerformed

    private void addDefinitionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addDefinitionButtonActionPerformed
        // Create and Show the Action Definition to the user
        ActionDefinitionDetails actionDefinition = new ActionDefinitionDetails();
        actionDefinition.setDescription("The action takes a picture and saves it in the 'picture' parameter.");
        actionDefinition.setCategory(new UOctet((short) 0));
        actionDefinition.setProgressStepCount(new UShort(1));

        ArgumentDefinitionDetails details = new ArgumentDefinitionDetails();
        details.setRawType((byte) 1);
        details.setArgId(new Identifier("0"));

        ArgumentDefinitionDetailsList detailsList = new ArgumentDefinitionDetailsList();
        detailsList.add(null);
        actionDefinition.setArguments(detailsList);

        ActionCreationRequest creation = new ActionCreationRequest();
        creation.setActionDefDetails(actionDefinition);
        creation.setName(new Identifier("Take_Picture"));
        MOWindow actionDefinitionWindow = new MOWindow(creation, true);

        ActionCreationRequestList requestList = new ActionCreationRequestList();
        requestList.add(creation);

        try {
            requestList.add((ActionCreationRequest) actionDefinitionWindow.getObject());
        } catch (InterruptedIOException ex) {
            return;
        }

        try {
            ObjectInstancePairList objIds = this.serviceMCAction.getActionStub().addAction(requestList);

            if (objIds.isEmpty()) {
                return;
            }

            Thread.sleep(500);
            // Get the stored Action Definition from the Archive
            ArchivePersistenceObject comObject = HelperArchive.getArchiveCOMObject(this.serviceMCAction.getCOMServices().getArchiveService().getArchiveStub(),
                    ActionHelper.ACTIONDEFINITION_OBJECT_TYPE, serviceMCAction.getConnectionDetails().getDomain(), objIds.get(0).getObjDefInstanceId());

            // Add the Action Definition to the table
            actionTable.addEntry(requestList.get(0).getName(), comObject);
        } catch (MALInteractionException | MALException ex) {
            JOptionPane.showMessageDialog(null, "There was an error with the submitted action instance.", "Error", JOptionPane.PLAIN_MESSAGE);
            Logger.getLogger(ActionConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(ActionConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_addDefinitionButtonActionPerformed

    private void updateDefinitionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateDefinitionButtonActionPerformed
        if (actionTable.getSelectedRow() == -1) { // The row is not selected?
            return;  // Well, then nothing to be done here folks!
        }

        ArchivePersistenceObject obj = actionTable.getSelectedCOMObject();
        MOWindow moObject = new MOWindow(obj.getObject(), true);

        LongList objIds = new LongList();
        objIds.add(actionTable.getSelectedIdentityObjId());

        ActionDefinitionDetailsList defs = new ActionDefinitionDetailsList();
        try {
            defs.add((ActionDefinitionDetails) moObject.getObject());
        } catch (InterruptedIOException ex) {
            return;
        }

        try {
            this.serviceMCAction.getActionStub().updateDefinition(objIds, defs);
            this.listDefinitionAllButtonActionPerformed(null);
        } catch (MALInteractionException | MALException ex) {
            Logger.getLogger(ActionConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_updateDefinitionButtonActionPerformed

    private void removeDefinitionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeDefinitionButtonActionPerformed
        if (actionTable.getSelectedRow() == -1) { // The row is not selected?
            return;  // Well, then nothing to be done here folks!
        }

        LongList longlist = new LongList();
        longlist.add(actionTable.getSelectedIdentityObjId());

        try {
            this.serviceMCAction.getActionStub().removeAction(longlist);

            actionTable.removeSelectedEntry();
        } catch (MALInteractionException | MALException ex) {
            Logger.getLogger(ActionConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_removeDefinitionButtonActionPerformed

    private void listDefinitionAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listDefinitionAllButtonActionPerformed
        IdentifierList idList = new IdentifierList();
        idList.add(new Identifier("*"));

        /*
        ObjectInstancePairList output;

        try {
            output = this.serviceMCAction.getActionStub().listDefinition(idList);
            actionTable.refreshTableWithIds(output, serviceMCAction.getConnectionDetails().getDomain(), ActionHelper.ACTIONDEFINITION_OBJECT_TYPE);
        } catch (MALInteractionException ex) {
            JOptionPane.showMessageDialog(null, "There was an error during the listDefinition operation.", "Error", JOptionPane.PLAIN_MESSAGE);
            Logger.getLogger(ActionConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
            return;
        } catch (MALException ex) {
            JOptionPane.showMessageDialog(null, "There was an error during the listDefinition operation.", "Error", JOptionPane.PLAIN_MESSAGE);
            Logger.getLogger(ActionConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        Logger.getLogger(ActionConsumerPanel.class.getName()).log(Level.INFO, "listDefinition(\"*\") returned {0} object instance identifiers", output.size());
*/
        try {
            this.serviceMCAction.getActionStub().asyncListDefinition(idList, new ActionAdapter() {
                @Override
                public void listDefinitionResponseReceived(MALMessageHeader msgHeader, ObjectInstancePairList actionInstIds, Map qosProperties) {
                    actionTable.refreshTableWithIds(actionInstIds, serviceMCAction.getConnectionDetails().getDomain(), ActionHelper.ACTIONDEFINITION_OBJECT_TYPE);
                    Logger.getLogger(ActionConsumerPanel.class.getName()).log(Level.INFO, "listDefinition(\"*\") returned {0} object instance identifiers", actionInstIds.size());
                }

                @Override
                public void listDefinitionErrorReceived(MALMessageHeader msgHeader, MALStandardError error, Map qosProperties) {
                    JOptionPane.showMessageDialog(null, "There was an error during the listDefinition operation.", "Error", JOptionPane.PLAIN_MESSAGE);
                    Logger.getLogger(ActionConsumerPanel.class.getName()).log(Level.SEVERE, null, error);
                }
            }
            );
        } catch (MALInteractionException | MALException ex) {
            Logger.getLogger(ActionConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }


    }//GEN-LAST:event_listDefinitionAllButtonActionPerformed

    private void removeDefinitionAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeDefinitionAllButtonActionPerformed
        Long objId = (long) 0;
        LongList longlist = new LongList();
        longlist.add(objId);

        try {
            this.serviceMCAction.getActionStub().removeAction(longlist);
            actionTable.removeAllEntries();
        } catch (MALInteractionException | MALException ex) {
            Logger.getLogger(ActionConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_removeDefinitionAllButtonActionPerformed

    private void preCheckActionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_preCheckActionButtonActionPerformed
        if (actionTable.getSelectedRow() == -1) { // The row is not selected?
            return;  // Well, then nothing to be done here folks!
        }

        ActionInstanceDetails actionInstanceDetails = new ActionInstanceDetails();
        actionInstanceDetails.setDefInstId(actionTable.getSelectedDefinitionObjId());
        MOWindow genericObject = new MOWindow(actionInstanceDetails, true);
        try {
            actionInstanceDetails = (ActionInstanceDetails) genericObject.getObject();
        } catch (InterruptedIOException ex) {
            return;
        }

        try {
            this.serviceMCAction.getActionStub().preCheckAction(actionInstanceDetails);
        } catch (MALInteractionException | MALException ex) {
            JOptionPane.showMessageDialog(null, "There was an error with the submitted action instance.", "Error", JOptionPane.PLAIN_MESSAGE);
            Logger.getLogger(ActionConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

        JOptionPane.showMessageDialog(null, "The action instance pre-check has passed successfully.", "Success", JOptionPane.PLAIN_MESSAGE);
    }//GEN-LAST:event_preCheckActionButtonActionPerformed

    private void actionDefinitionsTableComponentAdded(java.awt.event.ContainerEvent evt) {//GEN-FIRST:event_actionDefinitionsTableComponentAdded
        // TODO add your handling code here:
    }//GEN-LAST:event_actionDefinitionsTableComponentAdded


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable actionDefinitionsTable;
    private javax.swing.JButton addDefinitionButton;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton listDefinitionAllButton;
    private javax.swing.JButton listDefinitionButton;
    private javax.swing.JPanel parameterTab;
    private javax.swing.JButton preCheckActionButton;
    private javax.swing.JButton removeDefinitionAllButton;
    private javax.swing.JButton removeDefinitionButton;
    private javax.swing.JButton submitAction;
    private javax.swing.JButton updateDefinitionButton;
    // End of variables declaration//GEN-END:variables
}
