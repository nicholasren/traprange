package com.giaybac.traprange.models;

import java.util.Comparator;
import java.util.Random;

import com.google.common.base.MoreObjects;

public class Coordinate {

    public Coordinate(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float x;
    public float y;

    public static float distanceBetween(Coordinate from, Coordinate to, Order order) {
        return (float) Math.sqrt(Math.pow((from.y - to.y), 2) + Math.pow((from.x - to.x), 2));
    }

    public enum Order {
        X() {
            @Override
            public Coordinate random(Coordinate min, Coordinate max) {
                Random r = new Random();
                float x = min.x + (max.x - min.x) * r.nextFloat();
                return new Coordinate(x, 0);
            }

            @Override
            public Comparator<Coordinate> forCoordinate() {
                return (o1, o2) -> {
                    if (o1.x - o2.x > 0) {
                        return 1;
                    } else if (o1.x - o2.x < 0) {
                        return -1;
                    } else {
                        return 0;
                    }
                };
            }

            @Override
            public float distanceBetween(Coordinate from, Coordinate to) {
                return Math.abs(from.x - to.x);

            }

            @Override
            public Comparator<? super Text> forText() {
                return (o1, o2) -> {
                    if (o1.x() - o2.x() > 0) {
                        return 1;
                    } else if (o1.x() - o2.x() < 0) {
                        return -1;
                    } else {
                        return 0;
                    }
                };
            }
        },
        Y {
            @Override
            public Coordinate random(Coordinate min, Coordinate max) {
                Random r = new Random();
                float y = min.y + (max.y - min.y) * r.nextFloat();
                return new Coordinate(0, y);

            }

            @Override
            public Comparator<Coordinate> forCoordinate() {
                return (o1, o2) -> {
                    if (o1.y - o2.y > 0) {
                        return 1;
                    } else if (o1.y - o2.y < 0) {
                        return -1;
                    } else {
                        return 0;
                    }
                };
            }

            @Override
            public float distanceBetween(Coordinate from, Coordinate to) {
                return Math.abs(from.y - to.y);
            }

            @Override
            public Comparator<? super Text> forText() {
                return (o1, o2) -> {
                    if (o1.y() - o2.y() > 0) {
                        return 1;
                    } else if (o1.y() - o2.y() < 0) {
                        return -1;
                    } else {
                        return 0;
                    }
                };
            }
        };

        public abstract Coordinate random(Coordinate min, Coordinate max);

        public abstract Comparator<Coordinate> forCoordinate();

        public abstract float distanceBetween(Coordinate from, Coordinate to);

        public abstract Comparator<? super Text> forText();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("x", x)
                .add("y", y)
                .toString();
    }
}
