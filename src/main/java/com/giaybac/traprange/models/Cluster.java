package com.giaybac.traprange.models;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Cluster {
    private Coordinate centroid;
    private Coordinate.Order order;
    private List<Text> items;

    public Cluster(Coordinate centroid, Coordinate.Order order) {
        this.centroid = centroid;
        this.order = order;
        this.items = new ArrayList<>();
    }

    public static Comparator<? super Cluster> distanceTo(Text text, Coordinate.Order order) {
        return (Comparator<Cluster>) (cluster1, cluster2) -> {
            float distance1 = cluster1.order.distanceBetween(text.center(), cluster1.centroid());
            float distance2 = cluster2.order.distanceBetween(text.center(), cluster2.centroid());
            if (distance1 > distance2) {
                return 1;
            } else if (distance1 < distance2) {
                return -1;
            } else {
                return 0;
            }
        };
    }

    public void updateCentroid() {
        float sumX = 0;
        float sumY = 0;
        List<Text> list = this.items();
        int n_points = list.size();

        for (Text text : list) {
            sumX += text.center().x;
            sumY += text.center().y;
        }

        Coordinate centroid = this.centroid();
        if (n_points > 0) {
            float newX = sumX / n_points;
            float newY = sumY / n_points;
            centroid.x = newX;
            centroid.y = newY;
        }
    }

    public void clear() {
        this.items.clear();
    }

    public Coordinate copyCentroid() {
        return new Coordinate(centroid.x, centroid.y);
    }

    public Coordinate centroid() {
        return centroid;
    }

    void add(Text text) {
        this.items.add(text);
    }

    public List<Text> items() {
        return items.stream().sorted(order.forText()).collect(Collectors.toList());
    }
}
