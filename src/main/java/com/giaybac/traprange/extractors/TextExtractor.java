package com.giaybac.traprange.extractors;

import static java.util.stream.Collectors.toList;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.giaybac.traprange.models.Text;
import com.google.common.base.Throwables;
import javaslang.control.Try;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.text.TextPositionComparator;

public class TextExtractor extends PDFTextStripper {

    private final TextPositionComparator COMPARATOR = new TextPositionComparator();

    private final List<TextPosition> textPositions = new ArrayList<>();

    public static Try<TextExtractor> with(String path) {
        return Try.of(() -> new TextExtractor(path));
    }

    TextExtractor(String path) throws IOException {
        super();
        super.setSortByPosition(true);
        super.document = PDDocument.load(new FileInputStream(new File(path)));
    }

    void stripPage(int pageId) {
        this.setStartPage(pageId + 1);
        this.setEndPage(pageId + 1);
        try (Writer writer = new OutputStreamWriter(new ByteArrayOutputStream())) {
            writeText(document, writer);
        } catch (IOException e) {
            Throwables.propagate(e);
        }
    }

    @Override
    protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
        this.textPositions.addAll(textPositions);
    }

    public List<Text> extract(Integer pageNumber) {
        stripPage(pageNumber);
        Collections.sort(textPositions, COMPARATOR);
        return textPositions.stream().map(Text::new).collect(toList());
    }

    public List<List<Text>> extractAll() {

        List<List<Text>> texts = new ArrayList<>();
        for (int i = 0; i < document.getNumberOfPages(); i++) {
            texts.add(this.extract(i));
        }
        return texts;
    }
}
