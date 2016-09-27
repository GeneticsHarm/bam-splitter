/*
 * Copyright (c) 2016 Harm Brugge [harmbrugge@gmail.com].
 * All rights reserved.
 */
package com.harmbrugge.bamtools;

import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main class for parsing the commandline arguments and initializing BamFileSplitter
 *
 * @author Harm Brugge
 * @version 0.0.1
 */
public class Main {

    private void start(String[] args) {

        String pathToBarcode;
        String pathToBam;
        Path outputPath = null;

        CommandLineParser parser = new DefaultParser();

        Options options = new Options();
        options.addOption("s", "split", false, "Input BAM should be split into singe-cell files");
        options.addOption("a", "adder", false, "Add barcode flag to input BAM files, extracted from filename");
        options.addOption("c", "samplesheet", false, "Create samplesheet for provided barcode file");
        options.addOption("i", true, "Input file BAM file or dir");
        options.addOption("o", true, "Output folder");
        options.addOption("b", true, "Input file barcode file");

        try {
            CommandLine line = parser.parse(options, args);
            if(!line.hasOption("i")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("Bam tools", options);
                System.exit(1);
            }

            pathToBam = line.getOptionValue("i");

            if (line.hasOption("o")) outputPath = Paths.get(line.getOptionValue("o"));

            if (line.hasOption("s")) {
                pathToBarcode = line.getOptionValue("b");

                BamFileSplitter bamFileSplitter = new BamFileSplitter(Paths.get(pathToBam), Paths.get(pathToBarcode), outputPath);
                bamFileSplitter.split();
                System.out.println(bamFileSplitter);

            } else if (line.hasOption("a")) {
                BamFileFlagAdder bamFileFlagAdder = new BamFileFlagAdder(Paths.get(pathToBam));
                bamFileFlagAdder.start();
            } else if (line.hasOption("c")) {
                pathToBarcode = line.getOptionValue("b");
                SampleSheetCreator sampleSheetCreator = new SampleSheetCreator(Paths.get(pathToBarcode));

                if (outputPath == null) outputPath = Paths.get(System.getProperty("user.dir"));

                sampleSheetCreator.createSamplesheet(outputPath);
            }
        }
        catch (ParseException exp) {
            System.out.println("Parse exception:" + exp.getMessage());
        } catch (IOException e) {
            System.out.println("IO exception");
        }

    }

    public static void main(String [] args) {
        Main main = new Main();
        main.start(args);
    }


}
