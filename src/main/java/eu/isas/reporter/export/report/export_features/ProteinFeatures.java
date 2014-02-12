/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.isas.reporter.export.report.export_features;

import com.compomics.util.io.export.ExportFeature;
import eu.isas.reporter.export.report.ReporterExportFeature;
import java.util.ArrayList;

/**
 * This enum lists all the protein export features available from reporter complementarily to the ones available in PeptideShaker
 *
 * @author Marc
 */
public enum ProteinFeatures implements ReporterExportFeature {

    ratio("Ratios", "The ratios of this protein group.", true),
    spread("Spread", "The spread of the peptide ratios of this protein group.", true);

    /**
     * The title of the feature which will be used for column heading.
     */
    private String title;
    /**
     * The description of the feature.
     */
    private String description;
    /**
     * Indicates whether the feature is channel dependent
     */
    private boolean hasChannels;
    /**
     * The type of export feature.
     */
    public final static String type = "Protein Reporter Quantification Summary";

    /**
     * Constructor.
     *
     * @param title title of the feature
     * @param description description of the feature
     * @param hasChannels indicates whether the feature is channel dependent
     */
    private ProteinFeatures(String title, String description, boolean hasChannels) {
        this.title = title;
        this.description = description;
        this.hasChannels = hasChannels;
    }

    @Override
    public String[] getTitles() {
        return new String[]{title};
    }
    
    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getFeatureFamily() {
        return type;
    }

    @Override
    public ArrayList<ExportFeature> getExportFeatures() {
        ArrayList<ExportFeature> result = eu.isas.peptideshaker.export.exportfeatures.ProteinFeatures.accession.getExportFeatures();
        result.add(ratio);
        result.add(spread);
        return result;
    }
    
    @Override
    public boolean hasChannels() {
        return hasChannels;
    }
    
}