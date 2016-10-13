package com.giaybac.traprange.models;

import static com.giaybac.traprange.support.Ranges.horizontal;

import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.Range;
import org.apache.pdfbox.text.TextPosition;

public class Line {
    private Range<Integer> range;
    private List<TextPosition> texts;

    public Line(Range<Integer> range, List<TextPosition> texts) {
        this.range = range;
        this.texts = texts;
    }

    List<TextPosition> texts() {
        return texts;
    }

    Range<Integer> range() {
        return range;
    }

    Boolean noisy() {
        return horizontal(texts).stream().filter(noise()).count() > 0;
    }

    private Predicate<Range<Integer>> noise() {
        return r -> isGreaterThan(r, 400);
    }

    private boolean isGreaterThan(Range<Integer> r, int threshold) {
        return r.upperEndpoint() - r.lowerEndpoint() > threshold;
    }

}
