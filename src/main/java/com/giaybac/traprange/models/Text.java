package com.giaybac.traprange.models;

import com.google.common.base.MoreObjects;
import org.apache.pdfbox.text.TextPosition;

public class Text {

    private final Coordinate center;
    private final Coordinate lowerLeft;
    private final Coordinate lowerRight;
    private final Coordinate upperLeft;
    private final Coordinate upperRight;
    private final String content;
    private Cluster cluster;

    public Text(TextPosition textPosition) {
        lowerLeft = new Coordinate(textPosition.getX(), textPosition.getY() + textPosition.getHeight());
        lowerRight = new Coordinate(textPosition.getX() + textPosition.getWidth(), textPosition.getY() + textPosition.getHeight());
        upperLeft = new Coordinate(textPosition.getX(), textPosition.getY());
        upperRight = new Coordinate(textPosition.getX() + textPosition.getWidth(), textPosition.getY());
        center = new Coordinate(textPosition.getX() + textPosition.getWidth() / 2, textPosition.getY() + textPosition.getHeight() / 2);
        content = textPosition.getUnicode();
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


    public float x() {
        return center.x;
    }

    public float y() {
        return center.y;
    }

    public String content() {
        return content;
    }

    public Coordinate center() {
        return center;
    }

    public void clusterTo(Cluster cluster) {
        this.cluster = cluster;
        cluster.add(this);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("center", center)
                .add("content", content)
                .toString();
    }


}
