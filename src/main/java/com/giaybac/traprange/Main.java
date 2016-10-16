/**
 * Copyright (C) 2016, GIAYBAC
 * <p>
 * Released under the MIT license
 */
package com.giaybac.traprange;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import com.giaybac.traprange.extractors.PDFTableExtractor;
import com.giaybac.traprange.result.Table;


public class Main {

    public static void main(String[] args) {
        String path = "/Users/twer/personal/lichao/pdf-text-extractor/traprange/docs/input/text-pdf-input.pdf";
//        String path = "/Users/twer/personal/lichao/pdf-text-extractor/traprange/docs/input/test-table-pdf-2.pdf";
//        String path = "/Users/twer/personal/lichao/pdf-text-extractor/traprange/docs/input/test-table-pdf.pdf";

//        String path = "/Users/twer/personal/lichao/pdf-text-extractor/traprange/docs/input/test-table-noisy.pdf";
        final PDFTableExtractor extractor = new PDFTableExtractor().setSource(path);

        List<Table> tables = extractor.extract();

        display(tables, "/tmp/out.csv");

    }

    private static void display(List<Table> tables, String outPath) {
        try {
            File out = new File(outPath);
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(out), "UTF-8")) {
                for (Table table : tables) {
                    writer.write(table.toCsv());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
