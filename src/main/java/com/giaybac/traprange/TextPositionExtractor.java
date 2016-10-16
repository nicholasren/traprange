package com.giaybac.traprange;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.text.TextPositionComparator;

class TextPositionExtractor extends PDFTextStripper {

    private final TextPositionComparator COMPARATOR = new TextPositionComparator();

    private final List<TextPosition> textPositions = new ArrayList<>();
    private final int pageId;

    TextPositionExtractor(PDDocument document, int pageId) throws IOException {
        super();
        super.setSortByPosition(true);
        super.document = document;
        this.pageId = pageId;
    }

    void stripPage(int pageId) throws IOException {
        this.setStartPage(pageId + 1);
        this.setEndPage(pageId + 1);
        try (Writer writer = new OutputStreamWriter(new ByteArrayOutputStream())) {
            writeText(document, writer);
        }
    }

    @Override
    protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
        this.textPositions.addAll(textPositions);
    }

    public List<TextPosition> extract() throws IOException {
        this.stripPage(pageId);
        Collections.sort(textPositions, COMPARATOR);
        return this.textPositions;
    }
}
