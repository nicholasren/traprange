package com.giaybac.traprange.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Range;

public class TrapRangeBuilder {

    private final List<Range<Integer>> ranges = new ArrayList<>();

    public TrapRangeBuilder addRange(Range<Integer> range) {
        ranges.add(range);
        return this;
    }

    public List<Range<Integer>> build() {
        List<Range<Integer>> retVal = new ArrayList<>();
        //order range by lower Bound
        Collections.sort(ranges, (a, b) -> a.lowerEndpoint().compareTo(b.lowerEndpoint()));

        for (Range<Integer> range : ranges) {
            if (retVal.isEmpty()) {
                retVal.add(range);
            } else {
                Range<Integer> lastRange = retVal.get(retVal.size() - 1);
                if (lastRange.isConnected(range)) {
                    Range newLastRange = lastRange.span(range);
                    retVal.set(retVal.size() - 1, newLastRange);
                } else {
                    retVal.add(range);
                }
            }
        }
        return retVal;
    }
}
