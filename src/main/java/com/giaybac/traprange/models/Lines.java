package com.giaybac.traprange.models;

import static com.giaybac.traprange.support.Ranges.horizontal;
import static com.giaybac.traprange.support.Ranges.vertical;
import static com.giaybac.traprange.support.Ranges.verticalRangeOf;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.collect.Range;
import org.apache.pdfbox.text.TextPosition;

public class Lines {
    private List<Line> items;

    public Lines(List<Line> items) {
        this.items = items;
    }

    public static Lines of(List<TextPosition> texts) {
        List<Line> lines = vertical(texts).stream()
                .map(byRange(texts))
                .collect(toList());

        return new Lines(lines);
    }

    private static Function<Range<Integer>, Line> byRange(List<TextPosition> texts) {
        return range -> {
            List<TextPosition> enclosedTexts = texts.stream()
                    .filter(position -> range.encloses(verticalRangeOf(position)))
                    .collect(toList());

            return new Line(range, enclosedTexts);
        };
    }

    public List<TextPosition> denoisedTexts() {
        return denoisedLines()
                .map(Line::texts)
                .flatMap(List::stream)
                .collect(toList());
    }

    public List<Range<Integer>> ranges() {
        return denoisedLines()
                .map(Line::range)
                .collect(toList());
    }

    public List<Range<Integer>> columnRanges() {
        return horizontal(denoisedTexts());
    }

    private Stream<Line> denoisedLines() {
        return items.stream().filter(line -> !line.noisy());
    }


}
