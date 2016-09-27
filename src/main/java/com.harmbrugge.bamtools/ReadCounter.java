/*
 * Copyright (c) 2016 Harm Brugge [harmbrugge@gmail.com].
 * All rights reserved.
 */
package com.harmbrugge.bamtools;

import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Harm Brugge
 * @version 0.0.1
 */
public class ReadCounter {

    private final static String PATH_TO_BAM = "/Volumes/Macintosh HD/Users/harmbrugge/Documents/Stage/Bams/pbmc6k/cells/cell_1036_AGGTTCGATAGACC-1.bam";
    private int nhGreaterThan1Count;
    private int gxGreaterThan1Count;
    private int total;

    private SamReader samReader;

    public ReadCounter(Path pathToBamFile) {
        this.samReader = SamReaderFactory.makeDefault().open(pathToBamFile.toFile());
    }

    public void read() {
        for (final SAMRecord samRecord : samReader) {

            int nhTag = (int) samRecord.getAttribute("NH");
            if (nhTag > 1) nhGreaterThan1Count++;
            
            Object gxTag = samRecord.getAttribute("GX");
            if (gxTag != null) {
                int gxCount = String.valueOf(gxTag).split(";").length;
                if (gxCount > 1) {
                    gxGreaterThan1Count++;
                }
            }

            total++;
        }

        System.out.println("NH > 1: " + nhGreaterThan1Count);
        System.out.println("GX > 1: " + gxGreaterThan1Count);
        System.out.println("Percentage NH > 1: " + ((double) nhGreaterThan1Count / total * 100) );
        System.out.println("Percentage GX > 1: " + ((double) gxGreaterThan1Count / total * 100) );

    }

    public static void main(String [] args) {
        ReadCounter readCounter = new ReadCounter(Paths.get(PATH_TO_BAM));
        readCounter.read();;
    }


}
