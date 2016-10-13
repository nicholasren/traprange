/**
 * Copyright (C) 2015, GIAYBAC
 * <p>
 * Released under the MIT license
 */
package com.giaybac.traprange;

import static java.util.stream.Collectors.toList;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.giaybac.traprange.entity.Table;
import com.giaybac.traprange.entity.TableCell;
import com.giaybac.traprange.entity.TableRow;
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

                List<TextPosition> texts = extractTextPositions(pageNumber);//sorted by .getY() ASC

                //extract line ranges
                List<Range<Integer>> rowRanges = getLineRanges(texts);

                Map<Range<Integer>, List<TextPosition>> textsByRow = dividedByRow(texts, rowRanges);

//
                List<TextPosition> textContent = columnedTextFrom(textsByRow);

                columnRanges = calculateColumnRangeVia(textsByRow);
                rowRanges = textsByRow.entrySet().stream()
                        .filter(e -> isNotNoisyRow().test(e.getValue())).map(e -> e.getKey())
                        .collect(toList());
                //extract column ranges
//                List<TextPosition> textContent = getTextsByRowRanges(rowRanges, texts);


                pageIdNLineRangesMap.putAll(pageNumber, rowRanges);
                pageIdNTextsMap.putAll(pageNumber, textContent);

            }
            //Calculate columnRanges
//            columnRanges = getColumnRanges(pageIdNTextsMap.values());

            System.out.println("=================" + columnRanges);
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
            Range<Integer> textRange = enclosedRangeFor(textPosition);

            if (rowTrapRange.encloses(textRange)) {
                rowContent.add(textPosition);
                idx++;
            } else {
                TableRow row = buildRow(rowIdx, rowContent, columnTrapRanges);
                print(row);
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

    private void print(TableRow row) {
        System.out.println(row.getCells().stream().map(TableCell::getContent).collect(Collectors.joining("   ")) + "========================");
    }

    private int lowerBoundOf(TextPosition textPosition) {
        return (int) textPosition.getY();
    }

    /**
     * @param rowIdx
     * @param rowContent
     * @param columnTrapRanges
     * @return
     */
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
            Range<Integer> textRange = Range.closed(leftBoundOf(textPosition),
                    rightBoundOf(textPosition));
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

    private List<TextPosition> extractTextPositions(int pageId) throws IOException {
        TextPositionExtractor extractor = new TextPositionExtractor(document, pageId);
        return extractor.extract();
    }


    private Map<Range<Integer>, List<TextPosition>> dividedByRow(List<TextPosition> positions, List<Range<Integer>> rowRanges) {
        Map<Range<Integer>, List<TextPosition>> result = new HashMap<>();

        for (Range rowRange : rowRanges) {
            positions.stream().filter(position -> rowRange.encloses(enclosedRangeFor(position)))
                    .forEach(position -> {
                        List<TextPosition> positionsOfRow = result.getOrDefault(rowRange, new ArrayList<>());
                        positionsOfRow.add(position);
                        result.put(rowRange, positionsOfRow);
                    });
        }
        return result;
    }

    /**
     * @param texts
     * @return
     */
    private List<Range<Integer>> getColumnRanges(Collection<TextPosition> texts) {
        TrapRangeBuilder rangesBuilder = new TrapRangeBuilder();
        for (TextPosition text : texts) {
            Range<Integer> range = Range.closed(leftBoundOf(text), rightBoundOf(text));
            rangesBuilder.addRange(range);
        }

        return rangesBuilder.build();
    }

    private List<Range<Integer>> calculateColumnRangeVia(Map<Range<Integer>, List<TextPosition>> textsByRow) {
        return getColumnRanges(columnedTextFrom(textsByRow));
    }

    private List<TextPosition> columnedTextFrom(Map<Range<Integer>, List<TextPosition>> textsByRow) {
        return textsByRow.values().stream()
                .filter(isNotNoisyRow())
                .flatMap(List::stream)
                .collect(toList());
    }


    private Predicate<List<TextPosition>> isNotNoisyRow() {
        return e -> getColumnRanges(e).stream().filter(r -> isGreaterThan(r, 400)).collect(toList()).size() <= 0;
    }


    private boolean isGreaterThan(Range<Integer> r, int threshold) {
        return r.upperEndpoint() - r.lowerEndpoint() > threshold;
    }


    private List<Range<Integer>> getLineRanges(List<TextPosition> pageContent) {
        TrapRangeBuilder lineTrapRangeBuilder = new TrapRangeBuilder();
        for (TextPosition textPosition : pageContent) {
            Range<Integer> lineRange = enclosedRangeFor(textPosition);
            //add to builder
            lineTrapRangeBuilder.addRange(lineRange);
        }
        List<Range<Integer>> lineTrapRanges = lineTrapRangeBuilder.build();
        return lineTrapRanges;
    }

    private int rightBoundOf(TextPosition text) {
        return (int) (text.getX() + text.getWidth());
    }

    private int leftBoundOf(TextPosition text) {
        return (int) text.getX();
    }

    //--------------------------------------------------------------------------
    //  Inner class
    private Range<Integer> enclosedRangeFor(TextPosition textPosition) {
        return Range.closed(lowerBoundOf(textPosition), upperBoundOf(textPosition));
    }

    private int upperBoundOf(TextPosition textPosition) {
        return (int) (textPosition.getY() + textPosition.getHeight());
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

//        static class TextPositionComparator implements Comparator<TextPosition> {
//
//            @Override
//            public int compare(TextPosition o1, TextPosition o2) {
//                if (o1.getY() > o2.getY()) {
//                    return 1;
//                } else if (o1.getY() < o2.getY()) {
//                    return -1;
//                } else {
//                    if (o1.getX() > o2.getX()) {
//                        return 1;
//                    } else if (o1.getX() < o2.getX()) {
//                        return -1;
//                    } else {
//                        return 0;
//                    }
//                }
//            }
//        }
    }
}
