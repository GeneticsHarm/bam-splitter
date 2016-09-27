/*
 * Copyright (c) 2016 Harm Brugge [harmbrugge@gmail.com].
 * All rights reserved.
 */
package com.harmbrugge.bamtools;

import java.io.*;
import java.nio.file.Path;

/**
 * @author Harm Brugge
 * @version 0.0.1
 */
public class SampleSheetCreator {

    private static final String PROJECT_NAME = "SingeCell";
    private static final String FILENAME_PREFIX = "cell_";
    private static final String EXTENSION = ".bam";

    private static final String SAMPLE_SHEET_FILENAME = "samplesheet.csv";

    private int fileCount;

    private final Path pathToBarcodeFile;

    public SampleSheetCreator(Path pathToBarcodeFile) {
         this.pathToBarcodeFile = pathToBarcodeFile;
    }

    public void createSamplesheet(Path outputDir) throws IOException {

        outputDir.toFile().mkdirs();
        File outputFile = new File(outputDir.toString(), SAMPLE_SHEET_FILENAME);
        if (!outputFile.exists()) outputFile.createNewFile();

        // project,cellId,bamFile
        FileWriter fileWriter = new FileWriter(outputFile.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fileWriter);
        bw.write("project,cellId,bamFile\n");

        try (BufferedReader br = new BufferedReader(new FileReader(pathToBarcodeFile.toFile()))) {
            String barcode = br.readLine();

            while (barcode != null) {
                fileCount++;
                String filePath = "${splitBamDir}/" + FILENAME_PREFIX + fileCount + "_" + barcode + EXTENSION;
                bw.write(PROJECT_NAME + "," + FILENAME_PREFIX + fileCount + "_" + barcode + "," + filePath);
                bw.write("\n");

                barcode = br.readLine();
            }
        }

        bw.close();
    }
}
