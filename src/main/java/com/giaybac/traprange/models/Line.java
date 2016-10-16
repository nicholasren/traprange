package com.giaybac.traprange.models;

import static com.giaybac.traprange.support.Ranges.horizontalTrapRangeOf;

import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.Range;
import org.apache.pdfbox.text.TextPosition;

public class Line {
    private Range<Integer> range;
    private List<TextPosition> texts;

    Line(Range<Integer> range, List<TextPosition> texts) {
        this.range = range;
        this.texts = texts;
    }

    public List<TextPosition> texts() {
        return texts;
    }

    Range<Integer> range() {
        return range;
    }

    Boolean noisy() {
        return horizontalTrapRangeOf(texts).stream()
                .filter(isNoisyRange())
                .count() > 0;
    }

    private Predicate<Range<Integer>> isNoisyRange() {
        return r -> isGreaterThan(r, 400);
    }

    private boolean isGreaterThan(Range<Integer> r, int threshold) {
        return r.upperEndpoint() - r.lowerEndpoint() > threshold;
    }

}
