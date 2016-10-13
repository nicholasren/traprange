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

import com.giaybac.traprange.entity.Table;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        PropertyConfigurator.configure(Main.class.getResource("/com/giaybac/traprange/log4j.properties"));

//        String path = "/Users/twer/personal/lichao/pdf-text-extractor/traprange/docs/input/text-pdf-input.pdf";

//        String path = "/Users/twer/personal/lichao/pdf-text-extractor/traprange/docs/input/test-table-pdf-2.pdf";
        String path = "/Users/twer/personal/lichao/pdf-text-extractor/traprange/docs/input/test-table-noisy.pdf";
//        String path = "/Users/twer/personal/lichao/pdf-text-extractor/traprange/docs/input/test-table-pdf.pdf";
        extractTables(path);

    }

    private static void extractTables(String path) {
        try {
            PDFTableExtractor extractor = new PDFTableExtractor().setSource(path);

            List<Table> tables = extractor.extract();

            File out = new File("/tmp/out.html");
            Writer writer = new OutputStreamWriter(new FileOutputStream(out), "UTF-8");
            try {
                for (Table table : tables) {
                    writer.write(table.toHtml());
                }
            } finally {
                try {
                    writer.close();
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            logger.error(null, e);
        }
    }
}
