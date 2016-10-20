package com.giaybac.traprange.models;

import org.apache.pdfbox.text.TextPosition;

public class Text {
    private TextPosition underlying;
    private Coordinate lowerLeft;
    private Coordinate lowerRight;
    private Coordinate upperLeft;
    private Coordinate upperRight;

    public Text(TextPosition textPosition) {
        underlying = textPosition;
        lowerLeft = new Coordinate(underlying.getX(), underlying.getY() + underlying.getHeight());
        lowerRight = new Coordinate(underlying.getX() + underlying.getWidth(), underlying.getY() + underlying.getHeight());
        upperLeft = new Coordinate(underlying.getX(), underlying.getY());
        upperRight = new Coordinate(underlying.getX() + underlying.getWidth(), underlying.getY());
    }

    //lower left
    public Coordinate lowerLeft() {
        return lowerLeft;
    }

    //lower right
    public Coordinate lowerRight() {
        return lowerRight;
    }

    //upper left
    public Coordinate upperLeft() {
        return upperLeft;
    }

    //upper right
    public Coordinate upperRight() {
        return upperRight;
    }

    public String content(){
        return underlying.getUnicode();
    }

}
