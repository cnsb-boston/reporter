package eu.isas.reporter.gui.export;

import com.compomics.util.gui.ExportFormatSelectionDialog;
import com.compomics.util.gui.export.report.ReportEditor;
import com.compomics.util.gui.file_handling.FileAndFileFilter;
import com.compomics.util.gui.file_handling.FileChooserUtil;
import com.compomics.util.gui.waiting.waitinghandlers.ProgressDialogX;
import com.compomics.util.io.export.ExportFormat;
import com.compomics.util.io.export.ExportScheme;
import eu.isas.reporter.export.report.ReporterExportFactory;
import eu.isas.reporter.gui.ReporterGUI;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 * This dialog allows the user to select, add and edit reports.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class ReportDialog extends javax.swing.JDialog {

    /**
     * The export factory.
     */
    private ReporterExportFactory exportFactory = ReporterExportFactory.getInstance();
    /**
     * The main GUI instance.
     */
    private ReporterGUI reporterGUI;
    /**
     * A simple progress dialog.
     */
    private static ProgressDialogX progressDialog;
    /**
     * List of the available export schemes.
     */
    private ArrayList<String> exportSchemesNames;

    /**
     * Constructor.
     *
     * @param reporterGUI the main GUI instance
     */
    public ReportDialog(ReporterGUI reporterGUI) {
        super(reporterGUI, true);
        this.reporterGUI = reporterGUI;
        updateReportsList();
        initComponents();
        setUpGUI();
        setLocationRelativeTo(reporterGUI);
        setVisible(true);
    }

    /**
     * Set up the GUI.
     */
    private void setUpGUI() {
        reportsTableScrollPane.getViewport().setOpaque(false);
        reportsTable.getTableHeader().setReorderingAllowed(false);
        reportsTable.getColumn(" ").setMaxWidth(50);
        reportsTable.getColumn(" ").setMinWidth(50);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        reportDocumentationPopupMenu = new javax.swing.JPopupMenu();
        addReportMenuItem = new javax.swing.JMenuItem();
        removeReportMenuItem = new javax.swing.JMenuItem();
        editReportMenuItem = new javax.swing.JMenuItem();
        reportPopUpMenuSeparator = new javax.swing.JPopupMenu.Separator();
        reportDocumentationMenuItem = new javax.swing.JMenuItem();
        backgroundPanel = new javax.swing.JPanel();
        exitButton = new javax.swing.JButton();
        customReportsPanel = new javax.swing.JPanel();
        reportsTableScrollPane = new javax.swing.JScrollPane();
        reportsTable = new javax.swing.JTable();
        exportReportButton = new javax.swing.JButton();
        helpLabel = new javax.swing.JLabel();
        addReportLabel = new javax.swing.JLabel();

        addReportMenuItem.setText("Add");
        addReportMenuItem.setToolTipText("Add a new report type");
        addReportMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addReportMenuItemActionPerformed(evt);
            }
        });
        reportDocumentationPopupMenu.add(addReportMenuItem);

        removeReportMenuItem.setText("Remove");
        removeReportMenuItem.setToolTipText("Remove the report type");
        removeReportMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeReportMenuItemActionPerformed(evt);
            }
        });
        reportDocumentationPopupMenu.add(removeReportMenuItem);

        editReportMenuItem.setText("Edit");
        editReportMenuItem.setToolTipText("Edit the report");
        editReportMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editReportMenuItemActionPerformed(evt);
            }
        });
        reportDocumentationPopupMenu.add(editReportMenuItem);
        reportDocumentationPopupMenu.add(reportPopUpMenuSeparator);

        reportDocumentationMenuItem.setText("Documentation");
        reportDocumentationMenuItem.setToolTipText("Export the report documentation to file");
        reportDocumentationMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reportDocumentationMenuItemActionPerformed(evt);
            }
        });
        reportDocumentationPopupMenu.add(reportDocumentationMenuItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Export Ratios");
        setMinimumSize(new java.awt.Dimension(600, 350));

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

        exitButton.setText("Exit");
        exitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitButtonActionPerformed(evt);
            }
        });

        customReportsPanel.setBackground(new java.awt.Color(230, 230, 230));
        customReportsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Quantification Report"));

        reportsTable.setModel(new ReportsTableModel());
        reportsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        reportsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                reportsTableMouseClicked(evt);
            }
        });
        reportsTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                reportsTableKeyReleased(evt);
            }
        });
        reportsTableScrollPane.setViewportView(reportsTable);

        exportReportButton.setText("Export");
        exportReportButton.setEnabled(false);
        exportReportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportReportButtonActionPerformed(evt);
            }
        });

        helpLabel.setFont(helpLabel.getFont().deriveFont((helpLabel.getFont().getStyle() | java.awt.Font.ITALIC)));
        helpLabel.setText("Right click on a row in the table for additional options.");

        addReportLabel.setText("<html> <a href>Add new report type.</a> </html>");
        addReportLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addReportLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                addReportLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                addReportLabelMouseExited(evt);
            }
        });

        javax.swing.GroupLayout customReportsPanelLayout = new javax.swing.GroupLayout(customReportsPanel);
        customReportsPanel.setLayout(customReportsPanelLayout);
        customReportsPanelLayout.setHorizontalGroup(
            customReportsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(customReportsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(customReportsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(reportsTableScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 553, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, customReportsPanelLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(addReportLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(helpLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 101, Short.MAX_VALUE)
                        .addComponent(exportReportButton)))
                .addContainerGap())
        );
        customReportsPanelLayout.setVerticalGroup(
            customReportsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(customReportsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(reportsTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(customReportsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(exportReportButton)
                    .addComponent(helpLabel)
                    .addComponent(addReportLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(exitButton)
                    .addComponent(customReportsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(customReportsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(exitButton)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgroundPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgroundPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Close the dialog.
     *
     * @param evt
     */
    private void exitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitButtonActionPerformed
        try {
            exportFactory.saveFactory(exportFactory);
        } catch (Exception e) {
            reporterGUI.catchException(e);
        }
        setVisible(false);
        dispose();
    }//GEN-LAST:event_exitButtonActionPerformed

    /**
     * Export the given report or show its details.
     *
     * @param evt
     */
    private void reportsTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_reportsTableMouseClicked

        if (evt != null && reportsTable.rowAtPoint(evt.getPoint()) != -1) {
            reportsTable.setRowSelectionInterval(reportsTable.rowAtPoint(evt.getPoint()), reportsTable.rowAtPoint(evt.getPoint()));
        }

        if (evt != null && evt.getButton() == MouseEvent.BUTTON3 && reportsTable.getSelectedRow() != -1) {
            String schemeName = (String) reportsTable.getValueAt(reportsTable.getSelectedRow(), 1);
            ExportScheme exportScheme = exportFactory.getExportScheme(schemeName);
            editReportMenuItem.setVisible(exportScheme.isEditable());
            removeReportMenuItem.setVisible(exportScheme.isEditable());
            reportDocumentationPopupMenu.show(reportsTable, evt.getX(), evt.getY());
        }

        if (evt != null && evt.getButton() == MouseEvent.BUTTON1 && evt.getClickCount() == 2) {
            writeSelectedReport();
        }

        exportReportButton.setEnabled(reportsTable.getSelectedRow() != -1);
    }//GEN-LAST:event_reportsTableMouseClicked

    /**
     * Enable/disable the export and delete buttons.
     *
     * @param evt
     */
    private void reportsTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_reportsTableKeyReleased
        reportsTableMouseClicked(null);
    }//GEN-LAST:event_reportsTableKeyReleased

    /**
     * Export the selected report to file.
     *
     * @param evt
     */
    private void exportReportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportReportButtonActionPerformed
        writeSelectedReport();
    }//GEN-LAST:event_exportReportButtonActionPerformed

    /**
     * Add a new report type.
     *
     * @param evt
     */
    private void addReportLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addReportLabelMouseClicked
        addReportMenuItemActionPerformed(null);
    }//GEN-LAST:event_addReportLabelMouseClicked

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void addReportLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addReportLabelMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_addReportLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void addReportLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addReportLabelMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_addReportLabelMouseExited

    /**
     * Add a new report type.
     *
     * @param evt
     */
    private void addReportMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addReportMenuItemActionPerformed
        new ReportEditor(reporterGUI, exportFactory);
        int selectedRow = reportsTable.getSelectedRow();
        updateReportsList();
        ((DefaultTableModel) reportsTable.getModel()).fireTableDataChanged();
        if (selectedRow != -1) {
            reportsTable.setRowSelectionInterval(selectedRow, selectedRow);
        }
        reportsTableMouseClicked(null);
    }//GEN-LAST:event_addReportMenuItemActionPerformed

    /**
     * Delete the currently selected report.
     *
     * @param evt
     */
    private void removeReportMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeReportMenuItemActionPerformed
        String reportName = (String) reportsTable.getValueAt(reportsTable.getSelectedRow(), 1);
        exportFactory.removeExportScheme(reportName);
        updateReportsList();
        ((DefaultTableModel) reportsTable.getModel()).fireTableDataChanged();
        reportsTableMouseClicked(null);
    }//GEN-LAST:event_removeReportMenuItemActionPerformed

    /**
     * Edit the selected report.
     *
     * @param evt
     */
    private void editReportMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editReportMenuItemActionPerformed
        String reportName = (String) reportsTable.getValueAt(reportsTable.getSelectedRow(), 1);
        new ReportEditor(reporterGUI, exportFactory, reportName, true);
        int selectedRow = reportsTable.getSelectedRow();
        updateReportsList();
        ((DefaultTableModel) reportsTable.getModel()).fireTableDataChanged();
        if (selectedRow != -1) {
            reportsTable.setRowSelectionInterval(selectedRow, selectedRow);
        }
        reportsTableMouseClicked(null);
    }//GEN-LAST:event_editReportMenuItemActionPerformed

    /**
     * Export the report documentation to file.
     *
     * @param evt
     */
    private void reportDocumentationMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reportDocumentationMenuItemActionPerformed
        writeDocumentationOfSelectedReport();
    }//GEN-LAST:event_reportDocumentationMenuItemActionPerformed

    /**
     * Updates the reports list based on the information stored in the export
     * factory.
     */
    private void updateReportsList() {
        exportSchemesNames = new ArrayList<String>();
        exportSchemesNames.addAll(exportFactory.getDefaultExportSchemesNames());
        exportSchemesNames.addAll(exportFactory.getUserSchemesNames());
    }

    /**
     * Writes the selected report into a file.
     */
    private void writeSelectedReport() {

        final String schemeName = (String) reportsTable.getValueAt(reportsTable.getSelectedRow(), 1);
        String textFileFilterDescription = "Tab separated text file (.txt)";
        String excelFileFilterDescription = "Excel Workbook (.xls)";
        String lastSelectedFolderPath = reporterGUI.getLastSelectedFolder().getLastSelectedFolder();
        FileAndFileFilter selectedFileAndFilter = FileChooserUtil.getUserSelectedFile(this, new String[]{".xls", ".txt"},
                new String[]{excelFileFilterDescription, textFileFilterDescription}, "Export Report", lastSelectedFolderPath, schemeName, false, true, false, 1);

        if (selectedFileAndFilter != null) {

            final File selectedFile = selectedFileAndFilter.getFile();
            final ExportFormat exportFormat;
            if (selectedFileAndFilter.getFileFilter().getDescription().equalsIgnoreCase(textFileFilterDescription)) {
                exportFormat = ExportFormat.text;
            } else {
                exportFormat = ExportFormat.excel;
            }

            progressDialog = new ProgressDialogX(this, reporterGUI,
                    Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/reporter.gif")),
                    Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/reporter-orange.gif")),
                    true);
            progressDialog.setTitle("Exporting Report. Please Wait...");

            final String filePath = selectedFile.getPath();

            new Thread(new Runnable() {
                public void run() {
                    try {
                        progressDialog.setVisible(true);
                    } catch (IndexOutOfBoundsException e) {
                        // ignore
                    }
                }
            }, "ProgressDialog").start();

            new Thread("ExportThread") {
                @Override
                public void run() {

                    try {
                        ExportScheme exportScheme = exportFactory.getExportScheme(schemeName);
                        progressDialog.setTitle("Exporting. Please Wait...");
                        ReporterExportFactory.writeExport(
                                exportScheme,
                                selectedFile,
                                exportFormat,
                                reporterGUI.getProjectParameters().getProjectUniqueName(),
                                reporterGUI.getProjectDetails(),
                                reporterGUI.getIdentification(),
                                reporterGUI.getIdentificationFeaturesGenerator(),
                                reporterGUI.getSequenceProvider(),
                                reporterGUI.getSpectrumProvider(),
                                reporterGUI.getProteinDetailsProvider(),
                                reporterGUI.getGeneMaps(),
                                reporterGUI.getQuantificationFeaturesGenerator(),
                                reporterGUI.getReporterIonQuantification(),
                                reporterGUI.getReporterSettings(),
                                reporterGUI.getIdentificationParameters(),
                                null,
                                null,
                                null,
                                null,
                                reporterGUI.getIdentificationDisplayPreferences().getnAASurroundingPeptides(),
                                reporterGUI.getSpectrumCountingParameters(),
                                progressDialog
                        );

                        boolean processCancelled = progressDialog.isRunCanceled();
                        progressDialog.setRunFinished();

                        if (!processCancelled) {
                            JOptionPane.showMessageDialog(reporterGUI, "Data copied to file:\n" + filePath, "Data Exported.", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (Exception e) {
                        progressDialog.setRunFinished();
                        JOptionPane.showMessageDialog(reporterGUI, "An error occurred while generating the output.", "Output Error.", JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    /**
     * Writes the documentation related to the selected report into a file.
     */
    private void writeDocumentationOfSelectedReport() {

        ExportFormatSelectionDialog exportFormatSelectionDialog = new ExportFormatSelectionDialog(this, true);

        if (!exportFormatSelectionDialog.isCanceled()) {

            final File selectedFile;
            final ExportFormat exportFormat = exportFormatSelectionDialog.getFormat();
            final String schemeName = (String) reportsTable.getValueAt(reportsTable.getSelectedRow(), 1);

            // get the file to send the output to
            if (exportFormat == ExportFormat.text) {
                selectedFile = reporterGUI.getUserSelectedFile(schemeName + ".txt", ".txt", "Tab separated text file (.txt)", "Export...", false);
            } else {
                selectedFile = reporterGUI.getUserSelectedFile(schemeName + ".xls", ".xls", "Excel Workbook (.xls)", "Export...", false);
            }

            if (selectedFile != null) {
                progressDialog = new ProgressDialogX(this, reporterGUI,
                        Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/reporter.gif")),
                        Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/reporter-orange.gif")),
                        true);

                new Thread(new Runnable() {
                    public void run() {
                        try {
                            progressDialog.setVisible(true);
                        } catch (IndexOutOfBoundsException e) {
                            // ignore
                        }
                    }
                }, "ProgressDialog").start();

                new Thread("ExportThread") {
                    @Override
                    public void run() {
                        boolean error = false;
                        try {
                            ExportScheme exportScheme = exportFactory.getExportScheme(schemeName);
                            ReporterExportFactory.writeDocumentation(exportScheme, exportFormat, selectedFile);
                        } catch (Exception e) {
                            error = true;
                            reporterGUI.catchException(e);
                        }
                        progressDialog.setRunFinished();

                        if (!error) {
                            JOptionPane.showMessageDialog(reporterGUI, "Documentation saved to \'" + selectedFile.getAbsolutePath() + "\'.",
                                    "Documentation Saved", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }.start();
            }
        }
    }

    /**
     * Table model for the reports table.
     */
    private class ReportsTableModel extends DefaultTableModel {

        public ReportsTableModel() {
        }

        @Override
        public int getRowCount() {
            if (exportSchemesNames == null) {
                return 0;
            }
            return exportSchemesNames.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return " ";
                case 1:
                    return "Name";
                default:
                    return "";
            }
        }

        @Override
        public Object getValueAt(int row, int column) {
            switch (column) {
                case 0:
                    return row + 1;
                case 1:
                    return exportSchemesNames.get(row);
                default:
                    return "";
            }
        }

        @Override
        public Class getColumnClass(int columnIndex) {
            for (int i = 0; i < getRowCount(); i++) {
                if (getValueAt(i, columnIndex) != null) {
                    return getValueAt(i, columnIndex).getClass();
                }
            }
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel addReportLabel;
    private javax.swing.JMenuItem addReportMenuItem;
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JPanel customReportsPanel;
    private javax.swing.JMenuItem editReportMenuItem;
    private javax.swing.JButton exitButton;
    private javax.swing.JButton exportReportButton;
    private javax.swing.JLabel helpLabel;
    private javax.swing.JMenuItem removeReportMenuItem;
    private javax.swing.JMenuItem reportDocumentationMenuItem;
    private javax.swing.JPopupMenu reportDocumentationPopupMenu;
    private javax.swing.JPopupMenu.Separator reportPopUpMenuSeparator;
    private javax.swing.JTable reportsTable;
    private javax.swing.JScrollPane reportsTableScrollPane;
    // End of variables declaration//GEN-END:variables
}
