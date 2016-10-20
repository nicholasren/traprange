/**
 * Copyright (C) 2015, GIAYBAC
 * <p>
 * Released under the MIT license
 */
package com.giaybac.traprange.extractors;

import static com.giaybac.traprange.support.Ranges.horizontalRangeOf;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.function.Function;

import com.giaybac.traprange.models.Line;
import com.giaybac.traprange.models.Page;
import com.giaybac.traprange.models.Text;
import com.giaybac.traprange.result.Cell;
import com.giaybac.traprange.result.Row;
import com.giaybac.traprange.result.Table;
import com.google.common.collect.Range;
import javaslang.control.Try;

public class PDFTableExtractor {


    private String filePath;

    public PDFTableExtractor setSource(String filePath) {
        this.filePath = filePath;
        return this;
    }

    public Try<List<Table>> extract() {
        return TextExtractor.with(filePath).map(extractor -> {
            List<Page> pages = pagesIn(extractor);

            return pages.stream()
                    .map(this::toTable)
                    .collect(toList());

        });
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

    private Function<Range<Integer>, Cell> toEnclosedTextsIn(List<Text> texts) {
        return range -> {

            List<Text> enclosedTexts = texts.stream()
                    .filter(text -> range.encloses(horizontalRangeOf(text)))
                    .collect(toList());

            return new Cell(asString(enclosedTexts));
        };
    }


    private List<Page> pagesIn(TextExtractor extractor) {
        return extractor.extractAll().stream().map(Page::of).collect(toList());
    }


    private String asString(List<Text> cellText) {
        return cellText.stream().map(Text::content).collect(joining());
    }
}
