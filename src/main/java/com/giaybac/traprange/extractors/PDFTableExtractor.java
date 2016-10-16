/**
 * Copyright (C) 2015, GIAYBAC
 * <p>
 * Released under the MIT license
 */
package com.giaybac.traprange.extractors;

import static com.giaybac.traprange.support.Ranges.horizontalRangeOf;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.giaybac.traprange.models.Line;
import com.giaybac.traprange.models.Page;
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
            List<Page> pages = pagesIn(document);

            return pages.stream()
                    .map(this::toTable)
                    .collect(toList());

        } catch (IOException ex) {
            throw new RuntimeException("Parse pdf file fail", ex);
        }
    }

    private Table toTable(Page page) {
        List<Row> rows = page.lines().stream()
                .map(toRowDividedBy(page.horizontalRanges()))
                .collect(toList());

        return new Table(rows);
    }

    private Function<Line, Row> toRowDividedBy(List<Range<Integer>> ranges) {
        return line -> {
            List<Cell> cells = ranges.stream()
                    .map(toEnclosedTextsIn(line.texts()))
                    .collect(toList());
            return new Row(cells);
        };
    }

    private Function<Range<Integer>, Cell> toEnclosedTextsIn(List<TextPosition> texts) {
        return range -> {

            List<TextPosition> enclosedTexts = texts.stream()
                    .filter(text -> range.encloses(horizontalRangeOf(text)))
                    .collect(toList());

            return new Cell(asString(enclosedTexts));
        };
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

    private List<Page> pagesIn(PDDocument document) {
        List<Page> pages = new ArrayList<>();
        for (int pageNumber = 0; pageNumber < document.getNumberOfPages(); pageNumber++) {
            Page page = Page.of(textsIn(pageNumber, document));
            pages.add(page);
        }
        return pages;
    }

    private PDDocument load() throws IOException {
        return PDDocument.load(inputStream);
    }

    private String asString(List<TextPosition> cellText) {
        return cellText.stream().map(TextPosition::getUnicode).collect(Collectors.joining());
    }
}
