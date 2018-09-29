package eu.isas.reporter.cli;

import com.compomics.software.settings.UtilitiesPathPreferences;
import eu.isas.reporter.preferences.ReporterPathPreferences;
import java.util.ArrayList;
import org.apache.commons.cli.Options;

/**
 * Parameters for the path settings command line.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public enum PathSettingsCLIParams {

    ALL("temp_folder", "A folder for temporary file storage. Use only if you encounter problems with the default configuration."),
    LOG("log", "Folder where to write log files.");
    /**
     * The id of the command line option.
     */
    public String id;
    /**
     * The description of the command line option.
     */
    public String description;

    /**
     * Constructor.
     *
     * @param id the id of the command line option
     * @param description the description of the command line option
     */
    private PathSettingsCLIParams(String id, String description) {
        this.id = id;
        this.description = description;
    }

    /**
     * Creates the options for the command line interface based on the possible
     * values.
     *
     * @param aOptions the options object where the options will be added
     */
    public static void createOptionsCLI(Options aOptions) {
        for (PathSettingsCLIParams pathSettingsCLIParam : values()) {
            aOptions.addOption(pathSettingsCLIParam.id, true, pathSettingsCLIParam.description);
        }
        for (ReporterPathPreferences.ReporterPathKey reporterPathKey : ReporterPathPreferences.ReporterPathKey.values()) {
            aOptions.addOption(reporterPathKey.getId(), true, reporterPathKey.getDescription());
        }
        for (UtilitiesPathPreferences.UtilitiesPathKey utilitiesPathKey : UtilitiesPathPreferences.UtilitiesPathKey.values()) {
            aOptions.addOption(utilitiesPathKey.getId(), true, utilitiesPathKey.getDescription());
        }
    }
    
    /**
     * Returns the list of supported command line options.
     * 
     * @return the list of supported command line options
     */
    public static ArrayList<String> getOptionIDs() {
        
        ArrayList<String> options = new ArrayList<String>();
        
        for (PathSettingsCLIParams pathSettingsCLIParam : values()) {
            options.add("-" + pathSettingsCLIParam.id);
        }
        for (ReporterPathPreferences.ReporterPathKey reporterPathKey : ReporterPathPreferences.ReporterPathKey.values()) {
            options.add("-" + reporterPathKey.getId());
        }
        for (UtilitiesPathPreferences.UtilitiesPathKey utilitiesPathKey : UtilitiesPathPreferences.UtilitiesPathKey.values()) {
            options.add("-" + utilitiesPathKey.getId());
        }
        
        return options;
    }

    /**
     * Returns the options as a string.
     *
     * @return the options as a string
     */
    public static String getOptionsAsString() {

        String output = "";
        String formatter = "%-35s";

        output += "Log Folder:\n\n";
        output += "-" + String.format(formatter, LOG.id) + " " + LOG.description + "\n";

        output += "Generic Temporary Folder:\n\n";
        output += "-" + String.format(formatter, ALL.id) + " " + ALL.description + "\n";

        output += "\n\nSpecific Path Setting:\n\n";
        for (ReporterPathPreferences.ReporterPathKey reporterPathKey : ReporterPathPreferences.ReporterPathKey.values()) {
            output += "-" + String.format(formatter, reporterPathKey.getId()) + " " + reporterPathKey.getDescription() + System.getProperty("line.separator");
        }
        for (UtilitiesPathPreferences.UtilitiesPathKey utilitiesPathKey : UtilitiesPathPreferences.UtilitiesPathKey.values()) {
            output += "-" + String.format(formatter, utilitiesPathKey.getId()) + " " + utilitiesPathKey.getDescription() + System.getProperty("line.separator");
        }

        return output;
    }
}
