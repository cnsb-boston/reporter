package eu.isas.reporter.io;

import com.compomics.util.db.ObjectsDB;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.biology.ions.ReporterIon;
import com.compomics.util.experiment.identification.Identification;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.quantification.Quantification;
import com.compomics.util.experiment.quantification.reporterion.ReporterIonQuantification;
import com.compomics.util.experiment.quantification.reporterion.ReporterMethod;
import com.compomics.util.experiment.quantification.reporterion.ReporterMethodFactory;
import com.compomics.util.preferences.IdentificationParameters;
import com.compomics.util.preferences.LastSelectedFolder;
import com.compomics.util.waiting.WaitingHandler;
import eu.isas.peptideshaker.utils.CpsParent;
import eu.isas.reporter.Reporter;
import eu.isas.reporter.calculation.clustering.keys.PeptideClusterClassKey;
import eu.isas.reporter.calculation.clustering.keys.ProteinClusterClassKey;
import eu.isas.reporter.calculation.clustering.keys.PsmClusterClassKey;
import eu.isas.reporter.preferences.DisplayPreferences;
import eu.isas.reporter.settings.ClusteringSettings;
import eu.isas.reporter.settings.ReporterSettings;
import java.awt.Dialog;
import java.io.EOFException;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javax.swing.JOptionPane;

/**
 * Imports a project from a cps file.
 *
 * @author Marc Vaudel
 */
public class ProjectImporter {

    /**
     * The dialog owner if operated from the GUI.
     */
    private Dialog owner;

    /**
     * The last selected folder.
     */
    private LastSelectedFolder lastSelectedFolder;

    /**
     * The cps parent used to load the file.
     */
    private CpsParent cpsParent = null;

    /**
     * The reporter settings loaded from the file.
     */
    private ReporterSettings reporterSettings;

    /**
     * The reporter ion quantification object loaded from the file.
     */
    private ReporterIonQuantification reporterIonQuantification;

    /**
     * The display preferences.
     */
    private DisplayPreferences displayPreferences;

    /**
     * The default reporter ion tolerance for TMT data.
     */
    public static final double DEFAULT_REPORTER_ION_TOLERANCE_TMT = 0.0016;

    /**
     * Constructor.
     *
     * @param owner the dialog owner if operated from the GUI
     * @param lastSelectedFolder the last selected folder
     * @param cpsFile the file to import the project from
     * @param waitingHandler waiting handler used to display progress and cancel
     * the import
     */
    public ProjectImporter(Dialog owner, LastSelectedFolder lastSelectedFolder, File cpsFile, WaitingHandler waitingHandler) {
        cpsParent = new CpsParent(Reporter.getMatchesFolder());
        this.owner = owner;
        this.lastSelectedFolder = lastSelectedFolder;
        importPeptideShakerFile(cpsFile, waitingHandler);
    }

    /**
     * Constructor.
     *
     * @param lastSelectedFolder the last selected folder
     * @param cpsFile the file to import the project from
     * @param waitingHandler waiting handler used to display progress and cancel
     * the import
     */
    public ProjectImporter(LastSelectedFolder lastSelectedFolder, File cpsFile, WaitingHandler waitingHandler) {
        this(null, lastSelectedFolder, cpsFile, waitingHandler);
    }

    /**
     * Method used to import a PeptideShaker file.
     *
     * @param psFile a PeptideShaker file
     */
    private void importPeptideShakerFile(File cpsFile, WaitingHandler waitingHandler) {

        try {
            cpsParent.setCpsFile(cpsFile);

            try {
                cpsParent.loadCpsFile(Reporter.getMatchesFolder(), waitingHandler);
            } catch (SQLException e) {
                e.printStackTrace();
                String errorText = "An error occurred while reading:\n" + cpsFile + ".\n\n"
                        + "It looks like another instance of PeptideShaker is still connected to the file.\n"
                        + "Please close all instances of PeptideShaker and try again.";
                if (owner != null) {
                    JOptionPane.showMessageDialog(owner,
                            errorText,
                            "File Input Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    throw new IllegalArgumentException(errorText);
                }
                return;
            }

            if (waitingHandler.isRunCanceled()) {
                waitingHandler.setRunFinished();
                return;
            }

            waitingHandler.setWaitingText("Loading FASTA File. Please Wait...");

            try {
                cpsParent.loadFastaFile(new File(lastSelectedFolder.getLastSelectedFolder()), waitingHandler);
            } catch (Exception e) {
                //Ignore
                e.printStackTrace();
            }

            if (waitingHandler.isRunCanceled()) {
                waitingHandler.setRunFinished();
                return;
            }

            Identification identification = cpsParent.getIdentification();
            ArrayList<String> spectrumFiles = identification.getSpectrumFiles();

            waitingHandler.setWaitingText("Loading Spectrum Files. Please Wait...");
            waitingHandler.setPrimaryProgressCounterIndeterminate(true);

            int cpt = 0, total = identification.getSpectrumFiles().size();
            for (String spectrumFileName : spectrumFiles) {

                waitingHandler.setWaitingText("Loading Spectrum Files (" + ++cpt + " of " + total + "). Please Wait...");

                try {
                    cpsParent.loadSpectrumFile(spectrumFileName, waitingHandler);
                } catch (Exception e) {
                    //Ignore
                    e.printStackTrace();
                }

                if (waitingHandler.isRunCanceled()) {
                    waitingHandler.setRunFinished();
                    break;
                }
            }

            waitingHandler.setPrimaryProgressCounterIndeterminate(true);

        } catch (OutOfMemoryError error) {
            System.out.println("Ran out of memory! (runtime.maxMemory(): " + Runtime.getRuntime().maxMemory() + ")");
            Runtime.getRuntime().gc();
            String errorText = "PeptideShaker used up all the available memory and had to be stopped.<br>"
                    + "Memory boundaries are changed in the the Welcome Dialog (Settings<br>"
                    + "& Help > Settings > Java Memory Settings) or in the Edit menu (Edit<br>"
                    + "Java Options). See also <a href=\"http://compomics.github.io/compomics-utilities/wiki/javatroubleshooting.html\">JavaTroubleShooting</a>.";
            if (owner != null) {
                JOptionPane.showMessageDialog(owner,
                        errorText,
                        "Out of Memory", JOptionPane.ERROR_MESSAGE);
            } else {
                throw new IllegalArgumentException(errorText);
            }
            waitingHandler.setRunFinished();
            error.printStackTrace();
            return;
        } catch (EOFException e) {
            e.printStackTrace();
            String errorText = "An error occurred while reading:\n" + cpsFile + ".\n\n"
                    + "The file is corrupted and cannot be opened anymore.";
            if (owner != null) {
                JOptionPane.showMessageDialog(owner,
                        errorText,
                        "Out of Memory", JOptionPane.ERROR_MESSAGE);
            } else {
                throw new IllegalArgumentException(errorText);
            }
            waitingHandler.setRunFinished();
            return;
        } catch (Exception e) {
            e.printStackTrace();
            String errorText = "An error occurred while reading:\n" + cpsFile + ".\n\n"
                    + "Please verify that the PeptideShaker version used to create\n"
                    + "the file is compatible with your version of Reporter.";
            if (owner != null) {
                JOptionPane.showMessageDialog(owner,
                        errorText,
                        "Out of Memory", JOptionPane.ERROR_MESSAGE);
            } else {
                throw new IllegalArgumentException(errorText);
            }
            waitingHandler.setRunFinished();
            return;
        }

        // Load Reporter settings files
        Identification identification = cpsParent.getIdentification();
        IdentificationParameters identificationParameters = cpsParent.getIdentificationParameters();
        ObjectsDB objectsDB = identification.getIdentificationDB().getObjectsDB();
        try {
            if (objectsDB.hasTable(ProjectSaver.REPORTER_SETTINGS_TABLE_NAME)) {
                try {
                    reporterSettings = (ReporterSettings) objectsDB.retrieveObject(ProjectSaver.REPORTER_SETTINGS_TABLE_NAME, ReporterSettings.class.getName(), true, false);
                } catch (Exception e) {
                    e.printStackTrace();
                    String errorText = "An error occurred while importing the reporter settings.";
                    if (owner != null) {
                        JOptionPane.showMessageDialog(owner,
                                errorText,
                                "Import Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        throw new IllegalArgumentException(errorText);
                    }
                    waitingHandler.setRunFinished();
                    return;
                }
                try {
                    reporterIonQuantification = (ReporterIonQuantification) objectsDB.retrieveObject(ProjectSaver.REPORTER_SETTINGS_TABLE_NAME, ReporterIonQuantification.class.getName(), true, false);
                } catch (Exception e) {
                    e.printStackTrace();
                    String errorText = "An error occurred while importing the reporter settings.";
                    if (owner != null) {
                        JOptionPane.showMessageDialog(owner,
                                errorText,
                                "Import Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        throw new IllegalArgumentException(errorText);
                    }
                    waitingHandler.setRunFinished();
                }
                try {
                    displayPreferences = (DisplayPreferences) objectsDB.retrieveObject(ProjectSaver.REPORTER_SETTINGS_TABLE_NAME, DisplayPreferences.class.getName(), true, false);
                } catch (Exception e) {
                    e.printStackTrace();
                    String errorText = "An error occurred while importing the reporter settings.";
                    if (owner != null) {
                        JOptionPane.showMessageDialog(owner,
                                errorText,
                                "Import Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        throw new IllegalArgumentException(errorText);
                    }
                    waitingHandler.setRunFinished();
                }
            }
            if (reporterSettings == null) {
                reporterSettings = getDefaultReporterSettings(identificationParameters);
            }
            if (reporterIonQuantification == null) {
                reporterIonQuantification = getDefaultReporterIonQuantification(identificationParameters);
            }
            if (displayPreferences == null) {
                displayPreferences = new DisplayPreferences();
                ClusteringSettings clusteringSettings = getDefaultClusterMetrics(identificationParameters, identification);
                displayPreferences.setClusteringSettings(clusteringSettings);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String errorText = "An error occurred while importing the quantification details from " + cpsFile + ".";
            if (owner != null) {
                JOptionPane.showMessageDialog(owner,
                        errorText,
                        "Import Error", JOptionPane.ERROR_MESSAGE);
            } else {
                throw new IllegalArgumentException(errorText);
            }
            waitingHandler.setRunFinished();
        }

        waitingHandler.setRunFinished();
    }

    /**
     * Returns the default reporter settings as inferred from the identification
     * parameters.
     *
     * @param identificationParameters the identification parameters
     *
     * @return the default reporter settings
     */
    public static ReporterSettings getDefaultReporterSettings(IdentificationParameters identificationParameters) {

        ReporterSettings reporterSettings = new ReporterSettings();

        SearchParameters searchParameters = identificationParameters.getSearchParameters();

        // try to detect the method used
        for (String ptmName : searchParameters.getPtmSettings().getAllModifications()) {
            if (ptmName.contains("iTRAQ 4-plex")) {
                reporterSettings.getReporterIonSelectionSettings().setReporterIonsMzTolerance(searchParameters.getFragmentIonAccuracyInDaltons(ReporterIon.iTRAQ4Plex_117.getTheoreticMz(1)));
                break;
            } else if (ptmName.contains("iTRAQ 8-plex")) {
                reporterSettings.getReporterIonSelectionSettings().setReporterIonsMzTolerance(searchParameters.getFragmentIonAccuracyInDaltons(ReporterIon.iTRAQ8Plex_121.getTheoreticMz(1)));
                break;
            } else if (ptmName.contains("TMT 2-plex")) {
                if (reporterSettings.getReporterIonSelectionSettings().getReporterIonsMzTolerance() > DEFAULT_REPORTER_ION_TOLERANCE_TMT) {
                    reporterSettings.getReporterIonSelectionSettings().setReporterIonsMzTolerance(DEFAULT_REPORTER_ION_TOLERANCE_TMT);
                }
                break;
            } else if (ptmName.contains("TMT") && ptmName.contains("6-plex")) {
                if (reporterSettings.getReporterIonSelectionSettings().getReporterIonsMzTolerance() > DEFAULT_REPORTER_ION_TOLERANCE_TMT) {
                    reporterSettings.getReporterIonSelectionSettings().setReporterIonsMzTolerance(DEFAULT_REPORTER_ION_TOLERANCE_TMT);
                }
                break;
            } else if (ptmName.contains("TMT") && ptmName.contains("10-plex")) {
                if (reporterSettings.getReporterIonSelectionSettings().getReporterIonsMzTolerance() > DEFAULT_REPORTER_ION_TOLERANCE_TMT) {
                    reporterSettings.getReporterIonSelectionSettings().setReporterIonsMzTolerance(DEFAULT_REPORTER_ION_TOLERANCE_TMT);
                }
                break;
            }
        }

        return reporterSettings;
    }

    /**
     * Returns the default reporter ion quantification based on the
     * identification parameters.
     *
     * @param identificationParameters the identification parameters
     *
     * @return the default reporter ion quantification
     */
    public static ReporterIonQuantification getDefaultReporterIonQuantification(IdentificationParameters identificationParameters) {

        ReporterMethod selectedMethod = null;

        SearchParameters searchParameters = identificationParameters.getSearchParameters();
        ReporterMethodFactory reporterMethodFactory = ReporterMethodFactory.getInstance();

        // try to detect the method used
        for (String ptmName : searchParameters.getPtmSettings().getAllModifications()) {
            if (ptmName.contains("iTRAQ 4-plex")) {
                selectedMethod = reporterMethodFactory.getReporterMethod("iTRAQ 4-plex");
                break;
            } else if (ptmName.contains("iTRAQ 8-plex")) {
                selectedMethod = reporterMethodFactory.getReporterMethod("iTRAQ 8-plex");
                break;
            } else if (ptmName.contains("TMT 2-plex")) {
                selectedMethod = reporterMethodFactory.getReporterMethod("TMT 2-plex");
                break;
            } else if (ptmName.contains("TMT") && ptmName.contains("6-plex")) {
                if (searchParameters.getIonSearched1() == PeptideFragmentIon.Y_ION
                        || searchParameters.getIonSearched2() == PeptideFragmentIon.Y_ION) {
                    selectedMethod = reporterMethodFactory.getReporterMethod("TMT 6-plex (HCD)");
                } else {
                    selectedMethod = reporterMethodFactory.getReporterMethod("TMT 6-plex (ETD)");
                }
                break;
            } else if (ptmName.contains("TMT") && ptmName.contains("10-plex")) {
                selectedMethod = reporterMethodFactory.getReporterMethod("TMT 10-plex");
                break;
            }
        }

        // no method detected, default to the first
        if (selectedMethod == null) {
            selectedMethod = reporterMethodFactory.getReporterMethod(reporterMethodFactory.getMethodsNames().get(0));
        }

        ReporterIonQuantification reporterIonQuantification = new ReporterIonQuantification(Quantification.QuantificationMethod.REPORTER_IONS);
        reporterIonQuantification.setMethod(selectedMethod);
        return reporterIonQuantification;
    }

    /**
     * Returns the cluster metrics for a given project.
     *
     * @param identificationParameters the identification parameters
     * @param identification the identification
     *
     * @return the cluster metrics for a given project
     */
    public static ClusteringSettings getDefaultClusterMetrics(IdentificationParameters identificationParameters, Identification identification) {

        ArrayList<ProteinClusterClassKey> proteinClasses = new ArrayList<ProteinClusterClassKey>(1);
        ProteinClusterClassKey proteinClusterClassKey = new ProteinClusterClassKey();
        proteinClasses.add(proteinClusterClassKey);
        ProteinClusterClassKey defaultClusterClass = proteinClusterClassKey;
        proteinClusterClassKey = new ProteinClusterClassKey();
        proteinClusterClassKey.setStarred(Boolean.TRUE);
        proteinClasses.add(proteinClusterClassKey);

        PtmSettings ptmSettings = identificationParameters.getSearchParameters().getPtmSettings();
        ArrayList<PeptideClusterClassKey> peptideClasses = new ArrayList<PeptideClusterClassKey>(4);
        PeptideClusterClassKey peptidelusterClassKey = new PeptideClusterClassKey();
        peptideClasses.add(peptidelusterClassKey);
        peptidelusterClassKey = new PeptideClusterClassKey();
        peptidelusterClassKey.setStarred(Boolean.TRUE);
        peptideClasses.add(peptidelusterClassKey);
        peptidelusterClassKey = new PeptideClusterClassKey();
        peptidelusterClassKey.setnTerm(Boolean.TRUE);
        peptideClasses.add(peptidelusterClassKey);
        peptidelusterClassKey = new PeptideClusterClassKey();
        peptidelusterClassKey.setcTerm(Boolean.TRUE);
        peptideClasses.add(peptidelusterClassKey);
        peptidelusterClassKey = new PeptideClusterClassKey();
        peptidelusterClassKey.setNotModified(Boolean.TRUE);
        peptideClasses.add(peptidelusterClassKey);
        PTMFactory ptmFactory = PTMFactory.getInstance();
        HashMap<Double, ArrayList<String>> ptmMap = new HashMap<Double, ArrayList<String>>();
        for (String ptmName : ptmSettings.getAllNotFixedModifications()) {
            PTM ptm = ptmFactory.getPTM(ptmName);
            Double ptmMass = ptm.getMass();
            ArrayList<String> ptms = ptmMap.get(ptmMass);
            if (ptms == null) {
                ptms = new ArrayList<String>(2);
                ptmMap.put(ptmMass, ptms);
            }
            ptms.add(ptmName);
        }
        for (ArrayList<String> ptms : ptmMap.values()) {
            Collections.sort(ptms);
            peptidelusterClassKey = new PeptideClusterClassKey();
            peptidelusterClassKey.setPossiblePtms(ptms);
            peptideClasses.add(peptidelusterClassKey);
        }

        ArrayList<PsmClusterClassKey> psmClasses = new ArrayList<PsmClusterClassKey>(1);
        PsmClusterClassKey psmClusterClassKey = new PsmClusterClassKey();
        psmClasses.add(psmClusterClassKey);
        psmClusterClassKey = new PsmClusterClassKey();
        psmClusterClassKey.setStarred(Boolean.TRUE);
        psmClasses.add(psmClusterClassKey);
        ArrayList<String> spectrumFiles = identification.getOrderedSpectrumFileNames();
        if (spectrumFiles.size() > 1) {
            for (String spectrumFile : spectrumFiles) {
                psmClusterClassKey = new PsmClusterClassKey();
                psmClusterClassKey.setFile(spectrumFile);
                psmClasses.add(psmClusterClassKey);
            }
        }

        ClusteringSettings clusteringSettings = new ClusteringSettings();
        clusteringSettings.setProteinClassKeys(proteinClasses);
        clusteringSettings.setPeptideClassKeys(peptideClasses);
        clusteringSettings.setPsmClassKeys(psmClasses);
        clusteringSettings.addProteinClass(defaultClusterClass.toString());
        return clusteringSettings;
    }

    /**
     * Returns the cps parent used to import the file.
     *
     * @return the cps parent used to import the file
     */
    public CpsParent getCpsParent() {
        return cpsParent;
    }

    /**
     * Returns the reporter settings loaded from the file.
     *
     * @return the reporter settings loaded from the file
     */
    public ReporterSettings getReporterSettings() {
        return reporterSettings;
    }

    /**
     * Returns the reporter ion quantification object loaded from the file.
     *
     * @return the reporter ion quantification object loaded from the file
     */
    public ReporterIonQuantification getReporterIonQuantification() {
        return reporterIonQuantification;
    }

    /**
     * Returns the display preferences.
     *
     * @return the display preferences
     */
    public DisplayPreferences getDisplayPreferences() {
        return displayPreferences;
    }
}
