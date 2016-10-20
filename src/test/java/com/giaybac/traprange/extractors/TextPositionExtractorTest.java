package com.giaybac.traprange.extractors;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

import java.util.List;
import java.util.stream.Collectors;

import com.giaybac.traprange.models.Text;
import org.junit.Test;

public class TextPositionExtractorTest {

    private final String string = "/Users/twer/personal/lichao/pdf-text-extractor/traprange/docs/input/text-pdf-input.pdf";

    @Test
    public void should_extract_texts_for_all_pages() {
        TextExtractor.with(string).map(TextExtractor::extractAll)
                .onSuccess(extracted -> {
                    assertThat(extracted.size()).isEqualTo(3);

                    Integer numberOfAllTexts = extracted.stream().collect(Collectors.summingInt(List::size));

                    assertThat(numberOfAllTexts).isEqualTo(3942);
                });
    }

    @Test
    public void should_extract_texts_with_position_from_document() {

        TextExtractor.with(string).map(extractor -> extractor.extract(1))
                .onSuccess(extracted -> {
                    Text firstText = extracted.get(0);

                    assertThat(extracted.size()).isEqualTo(619);
                    assertThat(firstText.lowerLeft().x).isCloseTo(123.8f, offset(0.1f));
                    assertThat(firstText.lowerLeft().y).isCloseTo(55.2f, offset(0.1f));
                });
    }

}