package craftedcart.smbworkshopexporter;


import craftedcart.smbworkshopexporter.util.LogHelper;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author CraftedCart
 *         Created on 20/09/2016 (DD/MM/YYYY)
 */
public class SMBWorkshopExporter {

    public static boolean verboseLogging = false;

    public static void main(String[] args) {

        Options options = new Options();

        Option inObj = new Option("m", "model", true, "The stage's OBJ model file path");
        inObj.setRequired(true);
        options.addOption(inObj);

        Option inConfig = new Option("c", "config", true, "The stage's configuration file path");
        inConfig.setRequired(true);
        options.addOption(inConfig);

        Option outDir = new Option("o", "output", true, "A folder to put exported files into");
        outDir.setRequired(true);
        options.addOption(outDir);

        Option verbose = new Option("v", "verbose", false, "Enables verbose logging");
        options.addOption(verbose);

        Option help = new Option("h", "help", false, "Shows this help message");
        options.addOption(help);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("java -jar smbworkshopexporter.x.y.jar -m path/to/obj -c path/to/config -o path/to/output/directory", options);

            System.exit(1);
            return;
        }

        if (cmd.hasOption("h")) { //Show help
            formatter.printHelp("java -jar smbworkshopexporter.x.y.jar -m path/to/obj -c path/to/config -o path/to/output/directory", options);
            System.exit(0);
        }

        verboseLogging = cmd.hasOption("verbose");

        String modelFilePath = cmd.getOptionValue("model");
        String configFilePath = cmd.getOptionValue("config");
        String outputFilePath = cmd.getOptionValue("output");

        LogHelper.info(SMBWorkshopExporter.class, "Parsing OBJ File...");
        ModelData modelData = new ModelData();
        try {
            modelData.parseObj(new File(modelFilePath));
        } catch (IOException e) {
            if (e instanceof FileNotFoundException) {
                LogHelper.fatal(SMBWorkshopExporter.class, "OBJ file not found!");
            } else {
                LogHelper.fatal(SMBWorkshopExporter.class, "OBJ file: IOException");
            }
            LogHelper.fatal(SMBWorkshopExporter.class, e);
            System.exit(0);
        } catch (NumberFormatException e) {
            LogHelper.fatal(SMBWorkshopExporter.class, "OBJ file: Invalid number!");
            LogHelper.fatal(SMBWorkshopExporter.class, e);
            System.exit(0);
        }

        LogHelper.info(SMBWorkshopExporter.class, "Parsing Config File...");
        ConfigData configData = new ConfigData();
        try {
            configData.parseConfig(new File(configFilePath));
        } catch (IOException e) {
            if (e instanceof FileNotFoundException) {
                LogHelper.fatal(SMBWorkshopExporter.class, "Config file not found!");
            } else {
                LogHelper.fatal(SMBWorkshopExporter.class, "Config file: IOException");
            }
            LogHelper.fatal(SMBWorkshopExporter.class, e);
            System.exit(0);
        } catch (NumberFormatException e) {
            LogHelper.fatal(SMBWorkshopExporter.class, "Config file: Invalid number!");
            LogHelper.fatal(SMBWorkshopExporter.class, e);
            System.exit(0);
        } catch (IllegalStateException e) {
            LogHelper.fatal(SMBWorkshopExporter.class, "Config file: Invalid pattern!");
            LogHelper.fatal(SMBWorkshopExporter.class, e);
            System.exit(0);
        }

        LogHelper.info(SMBWorkshopExporter.class, "Writing LZ file...");
        try {
            (new LZExporter()).writeLZ(modelData, configData, new File(outputFilePath));
        } catch (IOException e) {
            if (e instanceof FileNotFoundException) {
                LogHelper.fatal(SMBWorkshopExporter.class, "LZ file not found!");
            } else {
                LogHelper.fatal(SMBWorkshopExporter.class, "LZ file: IOException");
            }
            LogHelper.fatal(SMBWorkshopExporter.class, e);
            System.exit(0);
        }

        LogHelper.info(SMBWorkshopExporter.class, "Done!");

    }

}
