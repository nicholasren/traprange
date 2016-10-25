package com.giaybac.traprange.support;

import static com.giaybac.traprange.extractors.TextExtractor.with;
import static com.giaybac.traprange.support.KMeans.horizontalClustered;
import static com.giaybac.traprange.support.KMeans.verticalClustered;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import com.giaybac.traprange.models.Text;
import org.junit.Test;

public class KMeansTest {
//    private final String path = "/Users/twer/personal/lichao/pdf-text-extractor/traprange/docs/input/text-pdf-input.pdf";
    private final String path = "/Users/twer/personal/lichao/pdf-text-extractor/traprange/docs/input/test-table-noisy.pdf";

    @Test
    public void should_be_able_to_calculate_horizontal_range() {
        with(path).map(e -> e.extract(0))
                .onSuccess(texts -> assertThat(horizontalClustered(texts).size(), is(2)));
    }

    @Test
    public void should_be_able_to_calculate_vertical_range() {
        with(path).map(e -> e.extract(0))
                .onSuccess(texts -> {
                    List<List<Text>> lists = verticalClustered(texts);
                    lists.stream().map(list -> list.stream().map(Text::content).collect(Collectors.joining()))
                            .forEach(l -> {

                                System.out.println("===================");
                                System.out.println(l);
                            });
//                    assertThat(lists.size(), is(2));
                });
    }


}