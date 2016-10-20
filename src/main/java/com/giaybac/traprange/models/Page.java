package com.giaybac.traprange.models;

import static com.giaybac.traprange.support.Ranges.horizontalTrapRangeOf;
import static java.util.stream.Collectors.toList;

import java.util.List;

import com.google.common.collect.Range;

public class Page {
    private List<Line> lines;
    private List<Text> texts;


    public static Page of(List<Text> texts) {
        return new Page(texts);
    }

    private Page(List<Text> texts) {
        this.texts = texts;
    }

    public List<Range<Integer>> horizontalRanges() {
        return horizontalTrapRangeOf(texts);
    }

    public List<Line> lines() {
        return lines.stream().collect(toList());
    }

//    private static Function<Range<Integer>, Line> byRange(List<TextPosition> texts) {
//        return range -> {
//            List<TextPosition> enclosedTexts = texts.stream()
//                    .filter(position -> range.encloses(verticalRangeOf(position)))
//                    .collect(toList());
//
//            return new Line(range, enclosedTexts);
//        };
//    }
}
