package craftedcart.smbworkshopexporter;


import craftedcart.smbworkshopexporter.util.LogHelper;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author CraftedCart
 *         Created on 20/09/2016 (DD/MM/YYYY)
 */
public class SMBWorkshopExporter {

    public static boolean verboseLogging = false;

    public static void main(String[] args) throws IOException {

        Options options = new Options();

        Option inObj = new Option("m", "model", true, "The stage's OBJ model file path");
        inObj.setRequired(true);
        options.addOption(inObj);

        Option inConfig = new Option("c", "config", true, "The stage's configuration file path");
        inConfig.setRequired(true);
        options.addOption(inConfig);

        Option gameVer = new Option("g", "gamever", true, "The game version \"1\" or \"2\"");
        options.addOption(gameVer);

        Option lzOut = new Option("o", "output", true, "The path to the raw LZ output file");
        options.addOption(lzOut);

        Option compOut = new Option("s", "compressedoutput", true, "The path to the compressed LZ output file"); //S stands for small file
        options.addOption(compOut);

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
            formatter.printHelp("java -jar smbworkshopexporter.x.y.jar -m path/to/obj -c path/to/config -g [1 / 2] -s path/to/output/file", options);

            System.exit(1);
            return;
        }

        if (!cmd.hasOption("o") && !cmd.hasOption("s")) {
            System.out.println("Missing output path! Specify with --output (-o) or --compressedoutput (-s) (Or specify both)");
            formatter.printHelp("java -jar smbworkshopexporter.x.y.jar -m path/to/obj -c path/to/config -g [1 / 2] -s path/to/output/file", options);

            System.exit(1);
            return;
        }

        if (cmd.hasOption("h")) { //Show help
            formatter.printHelp("java -jar smbworkshopexporter.x.y.jar -m path/to/obj -c path/to/config -g [1 / 2] -s path/to/output/file", options);
            System.exit(0);
        }

        verboseLogging = cmd.hasOption("verbose");

        String gameVersionString = cmd.getOptionValue("gamever");
        int gameVersion;
        if (Objects.equals(gameVersionString, "1")) {
            gameVersion = 1;
        } else if (Objects.equals(gameVersionString, "2")) {
            gameVersion = 2;
        } else {
            //Error - invalid version specified
            System.out.println("Invalid game version! Specify \"1\" or \"2\"");
            formatter.printHelp("java -jar smbworkshopexporter.x.y.jar -m path/to/obj -c path/to/config -g [1 / 2] -s path/to/output/file", options);

            System.exit(1);
            return;
        }

        String modelFilePath = cmd.getOptionValue("model");
        String configFilePath = cmd.getOptionValue("config");
        File outputFile;
        if (cmd.getOptionValue("output") != null) {
            outputFile = new File(cmd.getOptionValue("output"));
        } else {
            File temp = File.createTempFile("SMBWorkshopExporter", ".lz.raw");
            temp.deleteOnExit();
            outputFile = temp;
        }
        String compressedOutputFilePath = cmd.getOptionValue("compressedoutput");

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

        LogHelper.info(SMBWorkshopExporter.class, "Writing raw LZ file...");
        try {
            if (gameVersion == 1) {
                (new SMB1LZExporter()).writeRawLZ(modelData, configData, outputFile);
            } else {
                (new SMB2LZExporter()).writeRawLZ(modelData, configData, outputFile);
            }
        } catch (IOException e) {
            if (e instanceof FileNotFoundException) {
                LogHelper.fatal(SMBWorkshopExporter.class, "Raw LZ file not found!");
            } else {
                LogHelper.fatal(SMBWorkshopExporter.class, "Raw LZ file: IOException");
            }
            LogHelper.fatal(SMBWorkshopExporter.class, e);
            System.exit(0);
        }

        if (cmd.hasOption("s") && compressedOutputFilePath != null) {
            LogHelper.info(SMBWorkshopExporter.class, "Compressing raw LZ file...");

            File outFile = new File(compressedOutputFilePath);

            if (outFile.exists()) { //Delete the file and recreate it if it exists
                if (!outFile.delete()) {
                    LogHelper.warn(SMBWorkshopExporter.class, "Failed to delete original LZ file: " + outFile.getAbsolutePath());
                    LogHelper.warn(SMBWorkshopExporter.class, "Output raw LZ file may be corrupt");
                }
            }

            try (RandomAccessFile raw = new RandomAccessFile(outputFile, "r");
                    RandomAccessFile comp = new RandomAccessFile(outFile, "rw")) {

                final List<Byte> contents = new ArrayList<>();
                while (raw.getFilePointer() < raw.length()) {
                    contents.add(raw.readByte());
                }

                final Byte[] byteArray = contents.toArray(new Byte[contents.size()]);
                List<Byte> bl = (new LZCompressor()).compress(byteArray);

                for (Byte c : bl) {
                    comp.write(c);
                }

            } catch (IOException e) {
                if (e instanceof FileNotFoundException) {
                    LogHelper.fatal(SMBWorkshopExporter.class, "LZ file not found!");
                } else {
                    LogHelper.fatal(SMBWorkshopExporter.class, "LZ file: IOException");
                }
                LogHelper.fatal(SMBWorkshopExporter.class, e);
                System.exit(0);
            }
        }

        LogHelper.info(SMBWorkshopExporter.class, "Done!");

    }

}
