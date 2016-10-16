package com.giaybac.traprange.models;

import static com.giaybac.traprange.support.Ranges.horizontalTrapRangeOf;
import static com.giaybac.traprange.support.Ranges.verticalTrapRangesOf;
import static com.giaybac.traprange.support.Ranges.verticalRangeOf;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.function.Function;

import com.google.common.collect.Range;
import org.apache.pdfbox.text.TextPosition;

public class Page {
    private List<Line> items;

    public static Page of(List<TextPosition> texts) {
        List<Line> lines = verticalTrapRangesOf(texts).stream()
                .map(byRange(texts))
                .filter(line -> !line.noisy())
                .collect(toList());

        return new Page(lines);
    }

    public List<Range<Integer>> horizontalRanges() {
        return horizontalTrapRangeOf(texts());
    }

    public List<Line> lines() {
        return items.stream().collect(toList());
    }

    private List<TextPosition> texts() {
        return items.stream()
                .map(Line::texts)
                .flatMap(List::stream)
                .collect(toList());
    }

    private Page(List<Line> items) {
        this.items = items;
    }

    private static Function<Range<Integer>, Line> byRange(List<TextPosition> texts) {
        return range -> {
            List<TextPosition> enclosedTexts = texts.stream()
                    .filter(position -> range.encloses(verticalRangeOf(position)))
                    .collect(toList());

            return new Line(range, enclosedTexts);
        };
    }
}
