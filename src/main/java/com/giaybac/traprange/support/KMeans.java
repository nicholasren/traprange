package com.giaybac.traprange.support;

import static com.giaybac.traprange.models.Coordinate.distanceBetween;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import com.giaybac.traprange.models.Cluster;
import com.giaybac.traprange.models.Coordinate;
import com.giaybac.traprange.models.Text;

public class KMeans {

    public static List<List<Text>> verticalClustered(List<Text> texts) {
        return cluster(texts, Coordinate.Order.Y);
    }


    public static List<List<Text>> horizontalClustered(List<Text> texts) {
        return cluster(texts, Coordinate.Order.X);
    }

    private static List<List<Text>> cluster(List<Text> texts, Coordinate.Order order) {
        List<Cluster> clusters = createClustersWith(texts, order);

        boolean finish = false;

        while (!finish) {
            clusters.forEach(Cluster::clear);

            List<Coordinate> lastCentroids = copyCentroidsOf(clusters);

            assign(texts, clusters, order);

            clusters.forEach(Cluster::updateCentroid);

            List<Coordinate> currentCentroids = copyCentroidsOf(clusters);

            double distance = totalDistanceBetween(lastCentroids, currentCentroids, order);

            if (distance == 0) {
                finish = true;
            }

        }
        return clusters.stream().map(Cluster::items).collect(toList());
    }

    private static void assign(List<Text> texts, List<Cluster> clusters, Coordinate.Order order) {
        for (Text text : texts) {
            Cluster closest = clusters.stream().min(Cluster.distanceTo(text, order)).get();
            text.clusterTo(closest);
        }
    }

    private static List<Coordinate> copyCentroidsOf(List<Cluster> clusters) {
        return clusters.stream().map(Cluster::copyCentroid).collect(toList());
    }

    private static double totalDistanceBetween(List<Coordinate> from, List<Coordinate> to, Coordinate.Order order) {
        double distance = 0;
        for (int i = 0; i < from.size(); i++) {
            distance += distanceBetween(from.get(i), to.get(i), order);
        }
        return distance;
    }

    private static List<Cluster> createClustersWith(List<Text> texts, Coordinate.Order order) {
        Coordinate min = texts.stream().map(Text::center).min(order.forCoordinate()).get();
        Coordinate max = texts.stream().map(Text::center).max(order.forCoordinate()).get();

        int numberOfCluster = 20;
        List<Cluster> clusters = new ArrayList<>(numberOfCluster);
        for (int i = 0; i < numberOfCluster; i++) {
            Coordinate centroid = order.random(min, max);
            Cluster cluster = new Cluster(centroid, order);
            clusters.add(cluster);
        }
        return clusters;
    }

}
