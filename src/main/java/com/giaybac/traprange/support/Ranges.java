package com.giaybac.traprange.support;

import java.util.Collection;
import java.util.List;

import com.giaybac.traprange.TrapRangeBuilder;
import com.google.common.collect.Range;
import org.apache.pdfbox.text.TextPosition;

public class Ranges {

    public static List<Range<Integer>> horizontal(Collection<TextPosition> texts) {
        TrapRangeBuilder rangesBuilder = new TrapRangeBuilder();

        for (TextPosition text : texts) {
            Range<Integer> range = horizontalRangeOf(text);
            rangesBuilder.addRange(range);
        }
        return rangesBuilder.build();
    }

    public static List<Range<Integer>> vertical(List<TextPosition> pageContent) {
        TrapRangeBuilder lineTrapRangeBuilder = new TrapRangeBuilder();

        for (TextPosition textPosition : pageContent) {
            Range<Integer> lineRange = verticalRangeOf(textPosition);
            lineTrapRangeBuilder.addRange(lineRange);
        }

        return lineTrapRangeBuilder.build();
    }

    public static Range<Integer> verticalRangeOf(TextPosition textPosition) {
        return Range.closed(lowerBoundOf(textPosition), upperBoundOf(textPosition));
    }

    public static Range<Integer> horizontalRangeOf(TextPosition text) {
        return Range.closed(leftBoundOf(text), rightBoundOf(text));
    }

    private static int upperBoundOf(TextPosition textPosition) {
        return (int) (textPosition.getY() + textPosition.getHeight());
    }

    private static int lowerBoundOf(TextPosition textPosition) {
        return (int) textPosition.getY();
    }

    private static int rightBoundOf(TextPosition text) {
        return (int) (text.getX() + text.getWidth());
    }

    private static int leftBoundOf(TextPosition text) {
        return (int) text.getX();
    }
}
