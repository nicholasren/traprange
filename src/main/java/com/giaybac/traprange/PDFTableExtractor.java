/**
 * Copyright (C) 2015, GIAYBAC
 * <p>
 * Released under the MIT license
 */
package com.giaybac.traprange;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.giaybac.traprange.entity.Table;
import com.giaybac.traprange.entity.TableCell;
import com.giaybac.traprange.entity.TableRow;
import com.giaybac.traprange.models.Lines;
import com.giaybac.traprange.support.Ranges;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.text.TextPositionComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author THOQ LUONG Mar 22, 2015 3:34:29 PM
 */
public class PDFTableExtractor {

    private final Logger logger = LoggerFactory.getLogger(PDFTableExtractor.class);

    private InputStream inputStream;

    private PDDocument document;

    public PDFTableExtractor setSource(InputStream inputStream) {
        this.inputStream = inputStream;
        return this;
    }

    public PDFTableExtractor setSource(File file) {
        try {
            return this.setSource(new FileInputStream(file));
        } catch (FileNotFoundException ex) {
            throw new RuntimeException("Invalid pdf file", ex);
        }
    }

    public PDFTableExtractor setSource(String filePath) {
        return this.setSource(new File(filePath));
    }

    public List<Table> extract() {
        List<Table> retVal = new ArrayList<>();
        Multimap<Integer, Range<Integer>> pageIdNLineRangesMap = LinkedListMultimap.create();
        Multimap<Integer, TextPosition> pageIdNTextsMap = LinkedListMultimap.create();

        try {
            this.document = load();

            List<Range<Integer>> columnRanges = null;
            for (int pageNumber = 0; pageNumber < document.getNumberOfPages(); pageNumber++) {

                Lines lines = Lines.of(textsIn(pageNumber, document));
                columnRanges = lines.horizontalRanges();

                pageIdNLineRangesMap.putAll(pageNumber, lines.verticalRanges());
                pageIdNTextsMap.putAll(pageNumber, lines.denoisedTexts());
            }

            for (int pageId : pageIdNTextsMap.keySet()) {
                Table table = buildTable(pageId, (List) pageIdNTextsMap.get(pageId), (List) pageIdNLineRangesMap.get(pageId), columnRanges);
                retVal.add(table);
                //debug
                logger.debug("Found " + table.getRows().size() + " row(s) and " + columnRanges.size()
                        + " column(s) of a table in page " + pageId);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Parse pdf file fail", ex);
        } finally {
            if (this.document != null) {
                try {
                    this.document.close();
                } catch (IOException ex) {
                    logger.error(null, ex);
                }
            }
        }
        //return
        return retVal;
    }


    private String asString(List<TextPosition> value) {
        return value.stream().map(TextPosition::getUnicode).collect(Collectors.joining(""));
    }


    private PDDocument load() throws IOException {
        return PDDocument.load(inputStream);
    }

    //--------------------------------------------------------------------------
    //  Implement N Override
    //--------------------------------------------------------------------------
    //  Utils

    /**
     * Texts in tableContent have been ordered by .getY() ASC
     *
     * @param pageIdx
     * @param tableContent
     * @param rowTrapRanges
     * @param columnTrapRanges
     * @return
     */
    private Table buildTable(int pageIdx, List<TextPosition> tableContent,
                             List<Range<Integer>> rowTrapRanges, List<Range<Integer>> columnTrapRanges) {
        Table retVal = new Table(pageIdx, columnTrapRanges.size());
        int idx = 0;
        int rowIdx = 0;
        List<TextPosition> rowContent = new ArrayList<>();
        while (idx < tableContent.size()) {
            TextPosition textPosition = tableContent.get(idx);

            Range<Integer> rowTrapRange = rowTrapRanges.get(rowIdx);
            Range<Integer> textRange = Ranges.verticalRangeOf(textPosition);

            if (rowTrapRange.encloses(textRange)) {
                rowContent.add(textPosition);
                idx++;
            } else {
                TableRow row = buildRow(rowIdx, rowContent, columnTrapRanges);
                retVal.getRows().add(row);
                //next row: clear rowContent
                rowContent.clear();
                rowIdx++;

            }

        }
        //last row
        if (!rowContent.isEmpty() && rowIdx < rowTrapRanges.size()) {
            TableRow row = buildRow(rowIdx, rowContent, columnTrapRanges);
            retVal.getRows().add(row);
        }
        //return
        return retVal;
    }

    private TableRow buildRow(int rowIdx, List<TextPosition> rowContent, List<Range<Integer>> columnTrapRanges) {
        TableRow retVal = new TableRow(rowIdx);
        //Sort rowContent
        Collections.sort(rowContent, new Comparator<TextPosition>() {
            @Override
            public int compare(TextPosition o1, TextPosition o2) {
                int retVal = 0;
                if (o1.getX() < o2.getX()) {
                    retVal = -1;
                } else if (o1.getX() > o2.getX()) {
                    retVal = 1;
                }
                return retVal;
            }
        });
        int idx = 0;
        int columnIdx = 0;
        List<TextPosition> cellContent = new ArrayList<>();
        while (idx < rowContent.size()) {
            TextPosition textPosition = rowContent.get(idx);
            Range<Integer> columnTrapRange = columnTrapRanges.get(columnIdx);
            Range<Integer> textRange = Range.closed(Ranges.leftBoundOf(textPosition),
                    Ranges.rightBoundOf(textPosition));
            if (columnTrapRange.encloses(textRange)) {
                cellContent.add(textPosition);
                idx++;
            } else {
                TableCell cell = buildCell(columnIdx, cellContent);
                retVal.getCells().add(cell);
                //next column: clear cell content
                cellContent.clear();
                columnIdx++;
            }
        }
        if (!cellContent.isEmpty() && columnIdx < columnTrapRanges.size()) {
            TableCell cell = buildCell(columnIdx, cellContent);
            retVal.getCells().add(cell);
        }
        //return
        return retVal;
    }

    private TableCell buildCell(int columnIdx, List<TextPosition> cellContent) {
        Collections.sort(cellContent, new Comparator<TextPosition>() {
            @Override
            public int compare(TextPosition o1, TextPosition o2) {
                int retVal = 0;
                if (o1.getX() < o2.getX()) {
                    retVal = -1;
                } else if (o1.getX() > o2.getX()) {
                    retVal = 1;
                }
                return retVal;
            }
        });
        //String cellContentString = Joiner.on("").join(cellContent.stream().map(e -> e.getCharacter()).iterator());
        StringBuilder cellContentBuilder = new StringBuilder();
        for (TextPosition textPosition : cellContent) {
            cellContentBuilder.append(textPosition.getUnicode());
        }
        String cellContentString = cellContentBuilder.toString();
        return new TableCell(columnIdx, cellContentString);
    }

    private List<TextPosition> textsIn(int pageId, PDDocument document) throws IOException {
        TextPositionExtractor extractor = new TextPositionExtractor(document, pageId);
        return extractor.extract();
    }


    private static class TextPositionExtractor extends PDFTextStripper {

        private final TextPositionComparator COMPARATOR = new TextPositionComparator();

        private final List<TextPosition> textPositions = new ArrayList<>();
        private final int pageId;

        private TextPositionExtractor(PDDocument document, int pageId) throws IOException {
            super();
            super.setSortByPosition(true);
            super.document = document;
            this.pageId = pageId;
        }

        void stripPage(int pageId) throws IOException {
            this.setStartPage(pageId + 1);
            this.setEndPage(pageId + 1);
            try (Writer writer = new OutputStreamWriter(new ByteArrayOutputStream())) {
                writeText(document, writer);
            }
        }

        @Override
        protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
            this.textPositions.addAll(textPositions);
        }

        private List<TextPosition> extract() throws IOException {
            this.stripPage(pageId);
            Collections.sort(textPositions, COMPARATOR);
            return this.textPositions;
        }
    }
}
