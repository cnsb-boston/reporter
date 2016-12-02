package eu.isas.reporter.cli;

import com.compomics.software.settings.PathKey;
import com.compomics.software.settings.UtilitiesPathPreferences;
import com.compomics.util.Util;
import com.compomics.util.db.DerbyUtil;
import com.compomics.util.experiment.biology.EnzymeFactory;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.taxonomy.SpeciesFactory;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
import com.compomics.util.experiment.quantification.reporterion.ReporterIonQuantification;
import com.compomics.util.experiment.quantification.reporterion.ReporterMethod;
import com.compomics.util.waiting.WaitingHandler;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.preferences.UtilitiesUserPreferences;
import eu.isas.peptideshaker.PeptideShaker;
import eu.isas.peptideshaker.cmd.PeptideShakerCLI;
import static eu.isas.peptideshaker.cmd.PeptideShakerCLI.redirectErrorStream;
import eu.isas.peptideshaker.preferences.PeptideShakerPathPreferences;
import eu.isas.peptideshaker.utils.CpsParent;
import eu.isas.reporter.Reporter;
import eu.isas.reporter.calculation.QuantificationFeaturesCache;
import eu.isas.reporter.calculation.QuantificationFeaturesGenerator;
import eu.isas.reporter.io.ProjectImporter;
import eu.isas.reporter.settings.ReporterSettings;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

/**
 * This class performs the command line export of reports in command line.
 *
 * @author Marc Vaudel
 */
public class ReportCLI extends CpsParent {

    /**
     * The report command line options.
     */
    private ReportCLIInputBean reportCLIInputBean;
    /**
     * The enzyme factory.
     */
    private EnzymeFactory enzymeFactory;
    /**
     * The Progress messaging handler reports the status throughout all
     * PeptideShaker processes.
     */
    private WaitingHandler waitingHandler;
    /**
     * The PTM factory.
     */
    private PTMFactory ptmFactory;
    /**
     * The utilities user preferences.
     */
    private UtilitiesUserPreferences utilitiesUserPreferences;
    /**
     * The mgf files loaded.
     */
    private ArrayList<File> mgfFiles = new ArrayList<File>();

    /**
     * Construct a new ReportCLI runnable from a ReportCLI Bean. When
     * initialization is successful, calling "run" will open the PeptideShaker
     * project and write the desired output files.
     *
     * @param reportCLIInputBean the input bean
     */
    public ReportCLI(ReportCLIInputBean reportCLIInputBean) {
        this.reportCLIInputBean = reportCLIInputBean;
    }

    /**
     * Calling this method will run the configured PeptideShaker process.
     *
     * @return returns 1 if the process was canceled
     */
    public Object call() {

        PathSettingsCLIInputBean pathSettingsCLIInputBean = reportCLIInputBean.getPathSettingsCLIInputBean();

        if (pathSettingsCLIInputBean.getLogFolder() != null) {
            redirectErrorStream(pathSettingsCLIInputBean.getLogFolder());
        }

        if (pathSettingsCLIInputBean.hasInput()) {
            PathSettingsCLI pathSettingsCLI = new PathSettingsCLI(pathSettingsCLIInputBean);
            pathSettingsCLI.setPathSettings();
        } else {
            try {
                setPathConfiguration();
            } catch (Exception e) {
                System.out.println("An error occurred when setting path configurations. Default paths will be used.");
                e.printStackTrace();
            }
        }

        setDbFolder(PeptideShaker.getMatchesFolder());

        try {
            ArrayList<PathKey> errorKeys = PeptideShakerPathPreferences.getErrorKeys();
            if (!errorKeys.isEmpty()) {
                System.out.println("Unable to write in the following configuration folders. Please use a temporary folder, "
                        + "the path configuration command line, or edit the configuration paths from the graphical interface.");
                for (PathKey pathKey : errorKeys) {
                    System.out.println(pathKey.getId() + ": " + pathKey.getDescription());
                }
            }
        } catch (Exception e) {
            System.out.println("Unable to load the path configurations. Default paths will be used.");
            e.printStackTrace();
        }

        // Load user preferences
        utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();

        // Instantiate factories
        PeptideShaker.instantiateFacories(utilitiesUserPreferences);
        ptmFactory = PTMFactory.getInstance();
        enzymeFactory = EnzymeFactory.getInstance();

        // Load resources files
        loadSpecies();

        // Set waiting handler
        waitingHandler = new WaitingHandlerCLIImpl();

        // Load the project from the cps file
        ProjectImporter projectImporter = new ProjectImporter();
        File selectedFile = reportCLIInputBean.getCpsFile();
        try {
            projectImporter.importPeptideShakerProject(this, mgfFiles, waitingHandler);
            projectImporter.importReporterProject(this, waitingHandler);
        } catch (OutOfMemoryError error) {
            System.out.println("Ran out of memory! (runtime.maxMemory(): " + Runtime.getRuntime().maxMemory() + ")");
            String errorText = "PeptideShaker used up all the available memory and had to be stopped.<br>"
                    + "Memory boundaries are changed in the the Welcome Dialog (Settings<br>"
                    + "& Help > Settings > Java Memory Settings) or in the Edit menu (Edit<br>"
                    + "Java Options). See also <a href=\"http://compomics.github.io/compomics-utilities/wiki/javatroubleshooting.html\">JavaTroubleShooting</a>.";
            waitingHandler.appendReport(errorText, true, true);
            error.printStackTrace();
            return 1;
        } catch (EOFException e) {
            String errorText = "An error occurred while reading:\n" + selectedFile + ".\n\n"
                    + "The file is corrupted and cannot be opened anymore.";
            waitingHandler.appendReport(errorText, true, true);
            e.printStackTrace();
            return 1;
        } catch (Exception e) {
            String errorText = "An error occurred while reading:\n" + selectedFile + ".\n\n"
                    + "Please verify that the PeptideShaker version used to create\n"
                    + "the file is compatible with your version of Reporter.";
            waitingHandler.appendReport(errorText, true, true);
            e.printStackTrace();
            return 1;
        }

        // Load project specific PTMs
        String error = PeptideShaker.loadModifications(getIdentificationParameters().getSearchParameters());
        if (error != null) {
            System.out.println(error);
        }

        // get previously set quantification settings or defaults from the identification results
        ReporterSettings reporterSettings = projectImporter.getReporterSettings();
        ReporterIonQuantification reporterIonQuantification = projectImporter.getReporterIonQuantification();
        ReporterMethod selectedMethod = reporterIonQuantification.getReporterMethod();

        // Verify that ignored PTMs are recognized
        HashSet<String> ignoredPtms = reporterSettings.getRatioEstimationSettings().getExcludingPtms();
        if (ignoredPtms != null) {
            for (String ptmName : ignoredPtms) {
                PTM ptm = ptmFactory.getPTM(ptmName);
                if (ptm == null) {
                    System.out.println("PTM " + ptmName + " not recognized.");
                    return 1;
                }
            }
        }
        
        // Create quantification features generator
        QuantificationFeaturesGenerator quantificationFeaturesGenerator = new QuantificationFeaturesGenerator(new QuantificationFeaturesCache(), getIdentification(), getIdentificationFeaturesGenerator(), reporterSettings, reporterIonQuantification,
                identificationParameters.getSearchParameters(), identificationParameters.getSequenceMatchingPreferences());

        // export report(s)
        if (reportCLIInputBean.exportNeeded()) {
            int nSurroundingAAs = 2; //@TODO: this shall not be hard coded
            for (String reportType : reportCLIInputBean.getReportTypes()) {
                try {
                    CLIExportMethods.exportReport(reportCLIInputBean, reportType, experiment.getReference(), sample.getReference(), replicateNumber, projectDetails, identification, geneMaps, identificationFeaturesGenerator, quantificationFeaturesGenerator, reporterIonQuantification, reporterSettings, identificationParameters, nSurroundingAAs, spectrumCountingPreferences, waitingHandler);
                } catch (Exception e) {
                    waitingHandler.appendReport("An error occurred while exporting the " + reportType + ".", true, true);
                    e.printStackTrace();
                    waitingHandler.setRunCanceled();
                }
            }
        }

        // export documentation(s)
        if (reportCLIInputBean.documentationExportNeeded()) {
            for (String reportType : reportCLIInputBean.getReportTypes()) {
                try {
                    CLIExportMethods.exportDocumentation(reportCLIInputBean, reportType, waitingHandler);
                } catch (Exception e) {
                    waitingHandler.appendReport("An error occurred while exporting the documentation for " + reportType + ".", true, true);
                    e.printStackTrace();
                    waitingHandler.setRunCanceled();
                }
            }
        }

        try {
            PeptideShakerCLI.closePeptideShaker(identification);
        } catch (Exception e2) {
            waitingHandler.appendReport("An error occurred while closing reporter.", true, true);
            e2.printStackTrace();
        }

        if (!waitingHandler.isRunCanceled()) {
            waitingHandler.appendReport("Report export completed.", true, true);
            System.exit(0); // @TODO: Find other ways of cancelling the process? If not cancelled searchgui will not stop.
            // Note that if a different solution is found, the DummyFrame has to be closed similar to the setVisible method in the WelcomeDialog!!
            return 0;
        } else {
            System.exit(1); // @TODO: Find other ways of cancelling the process? If not cancelled searchgui will not stop.
            // Note that if a different solution is found, the DummyFrame has to be closed similar to the setVisible method in the WelcomeDialog!!
            return 1;
        }
    }

    /**
     * Sets the path configuration.
     */
    private void setPathConfiguration() throws IOException {
        File pathConfigurationFile = new File(PeptideShaker.getJarFilePath(), UtilitiesPathPreferences.configurationFileName);
        if (pathConfigurationFile.exists()) {
            PeptideShakerPathPreferences.loadPathPreferencesFromFile(pathConfigurationFile);
        }
    }

    /**
     * PeptideShaker report CLI header message when printing the usage.
     */
    private static String getHeader() {
        return System.getProperty("line.separator")
                + "The PeptideShaker report command line takes a cpsx file and generates various types of reports." + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "For further help see http://compomics.github.io/projects/peptide-shaker.html and http://compomics.github.io/peptide-shaker/wiki/peptideshakercli.html." + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "Or contact the developers at https://groups.google.com/group/peptide-shaker." + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "----------------------"
                + System.getProperty("line.separator")
                + "OPTIONS"
                + System.getProperty("line.separator")
                + "----------------------" + System.getProperty("line.separator")
                + System.getProperty("line.separator");
    }

    /**
     * Verifies the command line start parameters.
     *
     * @return true if the startup was valid
     */
    private static boolean isValidStartup(CommandLine aLine) throws IOException {

        if (aLine.getOptions().length == 0) {
            return false;
        }
        if (!aLine.hasOption(ReportCLIParams.CPS_FILE.id) || ((String) aLine.getOptionValue(ReportCLIParams.CPS_FILE.id)).equals("")) {
            System.out.println("\n" + ReportCLIParams.CPS_FILE.description + " not specified.\n");
            return false;
        } else {
            String fileTxt = aLine.getOptionValue(ReportCLIParams.CPS_FILE.id);
            File testFile = new File(fileTxt.trim());
            if (!testFile.exists()) {
                System.out.println("\n" + ReportCLIParams.CPS_FILE.description + " \'" + testFile.getAbsolutePath() + "\' not found.\n");
                return false;
            }
        }
        if (!aLine.hasOption(ReportCLIParams.EXPORT_FOLDER.id) || ((String) aLine.getOptionValue(ReportCLIParams.EXPORT_FOLDER.id)).equals("")) {
            System.out.println("\n" + ReportCLIParams.EXPORT_FOLDER.description + " not specified.\n");
            return false;
        } else {
            String fileTxt = aLine.getOptionValue(ReportCLIParams.EXPORT_FOLDER.id);
            File testFile = new File(fileTxt.trim());
            if (!testFile.exists()) {
                System.out.println("\n" + ReportCLIParams.EXPORT_FOLDER.description + " \'" + testFile.getAbsolutePath() + "\' not found.\n");
                return false;
            }
        }

        return true;
    }

    /**
     * Starts the launcher by calling the launch method. Use this as the main
     * class in the jar file.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try {
            Options lOptions = new Options();
            ReportCLIParams.createOptionsCLI(lOptions);
            BasicParser parser = new BasicParser();
            CommandLine line = parser.parse(lOptions, args);

            if (!isValidStartup(line)) {
                PrintWriter lPrintWriter = new PrintWriter(System.out);
                lPrintWriter.print(System.getProperty("line.separator") + "===============================================" + System.getProperty("line.separator"));
                lPrintWriter.print("PeptideShaker Report Exporter - Command Line" + System.getProperty("line.separator"));
                lPrintWriter.print("===============================================" + System.getProperty("line.separator"));
                lPrintWriter.print(getHeader());
                lPrintWriter.print(ReportCLIParams.getOptionsAsString());
                lPrintWriter.flush();
                lPrintWriter.close();

                System.exit(0);
            } else {
                ReportCLIInputBean lCLIBean = new ReportCLIInputBean(line);
                ReportCLI cli = new ReportCLI(lCLIBean);
                cli.call();
            }
        } catch (OutOfMemoryError e) {
            System.out.println("<CompomicsError>PeptideShaker used up all the memory and had to be stopped. See the PeptideShaker log for details.</CompomicsError>");
            System.err.println("Ran out of memory!");
            System.err.println("Memory given to the Java virtual machine: " + Runtime.getRuntime().maxMemory() + ".");
            System.err.println("Memory used by the Java virtual machine: " + Runtime.getRuntime().totalMemory() + ".");
            System.err.println("Free memory in the Java virtual machine: " + Runtime.getRuntime().freeMemory() + ".");
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            System.out.print("<CompomicsError>PeptideShaker processing failed. See the PeptideShaker log for details.</CompomicsError>");
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public String toString() {
        return "FollowUpCLI{" + ", cliInputBean=" + reportCLIInputBean + '}';
    }

    /**
     * Close the PeptideShaker instance by clearing up factories and cache.
     *
     * @throws IOException thrown of IOException occurs
     * @throws SQLException thrown if SQLException occurs
     */
    public void closePeptideShaker() throws IOException, SQLException {

        SpectrumFactory.getInstance().closeFiles();
        SequenceFactory.getInstance().closeFile();
        identification.close();

        File matchFolder = PeptideShaker.getMatchesFolder();

        DerbyUtil.closeConnection();

        File[] tempFiles = matchFolder.listFiles();

        if (tempFiles != null) {
            for (File currentFile : tempFiles) {
                Util.deleteDir(currentFile);
            }
        }
    }

    /**
     * Loads the species from the species file into the species factory.
     */
    private void loadSpecies() {
        try {
            SpeciesFactory speciesFactory = SpeciesFactory.getInstance();
            speciesFactory.initiate(PeptideShaker.getJarFilePath());
        } catch (Exception e) {
            System.out.println("An error occurred while loading the species.");
            e.printStackTrace();
        }
    }
}
