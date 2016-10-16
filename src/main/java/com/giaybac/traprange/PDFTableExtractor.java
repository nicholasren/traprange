/**
 * Copyright (C) 2015, GIAYBAC
 * <p>
 * Released under the MIT license
 */
package com.giaybac.traprange;

import static com.giaybac.traprange.support.Ranges.horizontalRangeOf;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.giaybac.traprange.models.Line;
import com.giaybac.traprange.models.Lines;
import com.giaybac.traprange.result.Cell;
import com.giaybac.traprange.result.Row;
import com.giaybac.traprange.result.Table;
import com.google.common.collect.Range;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.TextPosition;

public class PDFTableExtractor {

    private InputStream inputStream;

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
        try (PDDocument document = load()) {
            List<Lines> pages = pagesIn(document);
            return pages.stream().map(this::extractTable).collect(toList());

        } catch (IOException ex) {
            throw new RuntimeException("Parse pdf file fail", ex);
        }
    }

    private List<Lines> pagesIn(PDDocument document) {
        List<Lines> pages = new ArrayList<>();
        for (int pageNumber = 0; pageNumber < document.getNumberOfPages(); pageNumber++) {
            Lines lines = Lines.of(textsIn(pageNumber, document));
            pages.add(lines);
        }
        return pages;
    }

    private PDDocument load() throws IOException {
        return PDDocument.load(inputStream);
    }

    private Table extractTable(Lines lines) {
        List<Row> rows = lines.denoisedLines().stream().map(line -> {
            List<Cell> cells = lines.horizontalRanges()
                    .stream()
                    .map(range -> toCell(line, range))
                    .collect(toList());
            return new Row(cells);
        }).collect(toList());

        return new Table(rows);
    }

    private Cell toCell(Line line, Range<Integer> range) {
        List<TextPosition> cellText = line.texts().stream().filter(text -> range.encloses(horizontalRangeOf(text))).collect(toList());
        String cellContent = cellText.stream().map(TextPosition::getUnicode).collect(Collectors.joining());
        return new Cell(cellContent);
    }

    private List<TextPosition> textsIn(int pageId, PDDocument document) {
        try {
            TextPositionExtractor extractor = new TextPositionExtractor(document, pageId);
            return extractor.extract();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
