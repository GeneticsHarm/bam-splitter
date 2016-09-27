/*
 * Copyright (c) 2016 Harm Brugge [harmbrugge@gmail.com].
 * All rights reserved.
 */
package com.harmbrugge.bamtools;

import htsjdk.samtools.*;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Harm Brugge
 * @version 0.0.1
 */
public class BamFileFlagAdder {

    private final static String FLAG_NAME = "CB";

    private String outputDir;

    private final Matcher matcher;

    private File[] bamFiles;


    public BamFileFlagAdder(Path pathToBamFiles) {

        this.outputDir = pathToBamFiles.getParent() + "/output/";
        this.matcher = Pattern.compile("_([ATCG]{14})\\.").matcher("");

        File file = pathToBamFiles.toFile();

        bamFiles = file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".bam");
            }
        });

    }

    public void start() throws IOException {
        File file = new File(outputDir);
        if (!file.exists()) file.mkdirs();

        for (final File bamFile : bamFiles) {
            convert(bamFile);
        }
    }

    private void convert(File bamFile) throws IOException {

        SamReader bamReader = SamReaderFactory.makeDefault().open(bamFile);
        String barcode = getBarcodeFromFilename(bamFile.getName());

        File outputFile = new File(outputDir + bamFile.getName());

        final SAMFileWriter outputBam = new SAMFileWriterFactory().makeBAMWriter(bamReader.getFileHeader(),
                false, outputFile);

        for (final SAMRecord bamRecord : bamReader) {
            bamRecord.setAttribute(FLAG_NAME, barcode);
            outputBam.addAlignment(bamRecord);
        }

        outputBam.close();
        bamReader.close();

    }

    private String getBarcodeFromFilename(String filename) {
        matcher.reset(filename);

        if (matcher.find()) return matcher.group(1);
        return null;
    }

}
