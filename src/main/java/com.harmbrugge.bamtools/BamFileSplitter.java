/*
 * Copyright (c) 2016 Harm Brugge [harmbrugge@gmail.com].
 * All rights reserved.
 */
package com.harmbrugge.bamtools;

import htsjdk.samtools.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * BamFileSplitter splits a BAM files generated in cell-ranger pipeline from 10xGenomics into a file per singe cell.
 * <p>
 * Multimappers are removed by discarding records with a NH flag > 1.
 *
 * @author Harm Brugge
 * @version 0.0.1
 */
public class BamFileSplitter {

    private static final String FILE_NAME_PREFIX = "cell_";
    private static final String EXTENSION = ".bam";
    private static final int GROUP_SIZE = 50;

    private final Log logger = LogFactory.getLog(this.getClass());
    private final Path pathToBarcodeFile;
    private final String outputDir;

    private int fileCount;
    private int groupCount;
    private int recordCount;
    private int absentBarcodeCount;
    private int invalidBarcodeCount;
    private int multiMappedCount;

    private SamReader samReader;
    private SAMFileWriterFactory writerFactory = new SAMFileWriterFactory();

    /**
     * Map holding a FileWriter for every output BAM.
     * The key is the barcode of the cell.
     */
    private Map<String, SAMFileWriter> outputBams = new HashMap<>();

    public BamFileSplitter(Path pathToBamFile, Path pathToBarcodeFile, Path outputPath) {
        if (outputPath == null) outputDir = pathToBamFile.getParent() + "/output/";
        else outputDir = outputPath.toString();

        File file = new File(outputDir + "/0/");
        if (!file.exists()) file.mkdirs();

        this.pathToBarcodeFile = pathToBarcodeFile;
        this.samReader = SamReaderFactory.makeDefault().open(pathToBamFile.toFile());
    }

    public BamFileSplitter(Path pathToBamFile, Path pathToBarcodeFile) {
        this(pathToBamFile, pathToBarcodeFile, null);
    }

    /**
     * Start the splitting of the BAM file into single cell files.
     *
     * @throws IOException if reading or writing to the bam files fails.
     */
    public void split() throws IOException {
        this.populateKeys();

        for (SAMRecord samRecord : samReader) {
            recordCount++;

            if (isMultimapped(samRecord)) continue;

            Object Umi = samRecord.getAttribute("UR");
            if (Umi != null) samRecord.setReadName(samRecord.getReadName() + "_" + Umi);

            Object barcodeObject = samRecord.getAttribute("CB");
            if (barcodeObject != null) {
                String barcode = barcodeObject.toString();

                if (outputBams.containsKey(barcode)) {
                    addRecordToBam(barcode, samRecord);
                } else {
//                            logger.warn("Barcode not present in barcode file, for record: " + samRecord.toString());
                    invalidBarcodeCount++;
                }
            } else {
//                        logger.warn("No barcode found for SAMRecord: " + samRecord.toString());
                absentBarcodeCount++;
            }
        }

        this.printSummary();

        closeOutputWriters();
        samReader.close();
    }

    private void populateKeys() throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(pathToBarcodeFile.toFile()))) {
            String barcode = br.readLine();

            while (barcode != null) {
                outputBams.put(barcode, createBamWriter(barcode));

                barcode = br.readLine();
            }
        }
    }

    private boolean isMultimapped(SAMRecord samRecord) {
        Object nhTag = samRecord.getAttribute("NH");

        if (nhTag != null) {
            try {
                int hitsCount = (int) nhTag;

                if (hitsCount > 1) {
                    multiMappedCount++;
                    return true;
                }
            } catch (ClassCastException e) {
                logger.warn("Skipped record: " + samRecord + " Invalid NH tag (not an integer)");
                return true;
            }
        }

        return false;
    }

    private void addRecordToBam(String barcode, SAMRecord samRecord) {
        SAMFileWriter outputBam = outputBams.get(barcode);

        List<SAMReadGroupRecord> readGroup = outputBam.getFileHeader().getReadGroups();
        if (readGroup.size() > 0) {
            String readGroupId = readGroup.get(0).getId();
            samRecord.setAttribute("RG", readGroupId);
        }

        outputBam.addAlignment(samRecord);
    }

    private File createBamFile(String barcode) {
        fileCount++;

        if (fileCount % GROUP_SIZE == 0) {
            groupCount++;
            File file = new File(outputDir + "/" + groupCount);
            if (!file.exists()) file.mkdirs();
        }

        String filePath = outputDir + "/" + groupCount  + "/" + FILE_NAME_PREFIX + fileCount + "_" + barcode + EXTENSION;
        return new File(filePath);
    }

    private SAMFileHeader createBamHeader(String barcode) {
        SAMFileHeader samHeader = samReader.getFileHeader();

        SAMReadGroupRecord readGroup = new SAMReadGroupRecord(String.valueOf(fileCount));
        readGroup.setSample("cell_" + fileCount + "_" + barcode);
        readGroup.setPlatform("ILLUMINA");

        List<SAMReadGroupRecord> readGroups = new ArrayList<>();
        readGroups.add(readGroup);

        samHeader.setReadGroups(readGroups);

        return samHeader;
    }

    private SAMFileWriter createBamWriter(String barcode) {
        File bamFile = this.createBamFile(barcode);
        SAMFileHeader bamHeader = createBamHeader(barcode);

        return writerFactory.makeSAMOrBAMWriter(bamHeader, true, bamFile);
    }

    private void closeOutputWriters() {
        for (Map.Entry<String, SAMFileWriter> entry : outputBams.entrySet()) {
            SAMFileWriter samFileWriter = entry.getValue();
            samFileWriter.close();
        }
    }

    private void printSummary() {
        System.out.println("BamFileSplitter: \n" +
                "fileCount=" + fileCount +
                ", recordCount=" + recordCount +
                ", absentBarcodeCount=" + absentBarcodeCount +
                ", invalidBarcodeCount=" + invalidBarcodeCount +
                ", multimappers=" + multiMappedCount);
    }

    @Override
    public String toString() {
        return "BamFileSplitter{" +
                "fileCount=" + fileCount +
                ", recordCount=" + recordCount +
                ", absentBarcodeCount=" + absentBarcodeCount +
                ", invalidBarcodeCount=" + invalidBarcodeCount +
                '}';
    }
}