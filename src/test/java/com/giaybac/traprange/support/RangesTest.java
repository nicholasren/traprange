package com.giaybac.traprange.support;

import static com.giaybac.traprange.extractors.TextExtractor.with;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class RangesTest {
    private final String path = "/Users/twer/personal/lichao/pdf-text-extractor/traprange/docs/input/text-pdf-input.pdf";

    @Test
    public void should_be_able_to_calculate_horizontal_range() {
        with(path).map(e -> e.extract(0))
                .onSuccess(texts -> assertThat(Ranges.horizontalTrapRangeOf(texts), is(2)));
    }

}