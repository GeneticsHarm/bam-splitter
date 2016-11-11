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

    private static final int GROUP_SIZE = 50;

    private int fileCount;
    private int groupCount;

    private final Path pathToBarcodeFile;

    public SampleSheetCreator(Path pathToBarcodeFile) {
         this.pathToBarcodeFile = pathToBarcodeFile;
    }

    public void create(Path outputDir) throws IOException {

        outputDir.toFile().mkdirs();
        File outputFile = new File(outputDir.toString(), SAMPLE_SHEET_FILENAME);
        if (!outputFile.exists()) outputFile.createNewFile();

        // project,cellId,bamFile
        FileWriter fileWriter = new FileWriter(outputFile.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fileWriter);
        bw.write("project,cellId,bamFile,cellGroup\n");

        try (BufferedReader br = new BufferedReader(new FileReader(pathToBarcodeFile.toFile()))) {
            String barcode = br.readLine();

            while (barcode != null) {
                fileCount++;

                if (fileCount % GROUP_SIZE == 0) {
                    groupCount++;
                }

                String cellId = FILENAME_PREFIX + fileCount + "_" + barcode;
                String filePath = groupCount + "/" + cellId + EXTENSION;
                bw.write(PROJECT_NAME + ",");
                bw.write(cellId + ",");
                bw.write(filePath + ",");
                bw.write(groupCount + "\n");

                barcode = br.readLine();
            }
        }

        bw.close();
    }
}
