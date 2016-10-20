package com.giaybac.traprange.support;

import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import com.giaybac.traprange.models.Text;
import com.google.common.base.Throwables;
import com.google.common.collect.Range;

public class Ranges {

    public static List<Range<Integer>> horizontalTrapRangeOf(Collection<Text> texts) {
        //TODO:  better solution to classify ranges
        TrapRangeBuilder rangesBuilder = new TrapRangeBuilder();

        for (Text text : texts) {
            Range<Integer> range = horizontalRangeOf(text);
            rangesBuilder.addRange(range);
        }
        return rangesBuilder.build();
    }

    public static List<Range<Integer>> verticalTrapRangesOf(List<Text> texts) {
        //TODO:  better solution to classify ranges
        TrapRangeBuilder lineTrapRangeBuilder = new TrapRangeBuilder();

        for (Text text : texts) {
            Range<Integer> lineRange = verticalRangeOf(text);
            lineTrapRangeBuilder.addRange(lineRange);
        }

        return lineTrapRangeBuilder.build();
    }

    public static Range<Integer> verticalRangeOf(Text text) {
        return Range.closed(lowerBoundOf(text), upperBoundOf(text));
    }

    public static Range<Integer> horizontalRangeOf(Text text) {
        return Range.closed(leftBoundOf(text), rightBoundOf(text));
    }

    public static void persist(List<Range<Integer>> ranges) {
        File file = createIfNotExist("./docs/result/ranges.csv");

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(ranges.stream().map(toRenderScripts()).collect(joining(lineSeparator())));
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    private static File createIfNotExist(String pathname) {
        File file = new File(pathname);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Throwables.propagate(e);
            }
        }
        return file;
    }

    private static int upperBoundOf(Text textPosition) {
        return (int) textPosition.upperLeft().y;
    }

    private static int lowerBoundOf(Text textPosition) {
        return (int) textPosition.lowerLeft().y;
    }

    private static int rightBoundOf(Text text) {
        return (int) text.lowerRight().x;
    }

    private static int leftBoundOf(Text text) {
        return (int) text.lowerLeft().x;
    }

    private static Function<Range<Integer>, String> toRenderScripts() {
        AtomicLong y = new AtomicLong(0);
        return r -> {
            long newY = y.getAndIncrement();
            return "line(" + r.lowerEndpoint() + ", " + newY + ", " + r.upperEndpoint() + ", " + newY + ")";
        };
    }
}
