package com.giaybac.traprange.models;

import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.Range;

public class Line {
    private Range<Integer> range;
    private List<Text> texts;

    Line(Range<Integer> range, List<Text> texts) {
        this.range = range;
        this.texts = texts;
    }

    public List<Text> texts() {
        return texts;
    }

    Range<Integer> range() {
        return range;
    }


    private Predicate<Range<Integer>> isNoisyRange() {
        return r -> isGreaterThan(r, 400);
    }

    private boolean isGreaterThan(Range<Integer> r, int threshold) {
        return r.upperEndpoint() - r.lowerEndpoint() > threshold;
    }

}
