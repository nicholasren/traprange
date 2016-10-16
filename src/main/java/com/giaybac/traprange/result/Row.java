package com.giaybac.traprange.result;

import java.util.List;
import java.util.stream.Collectors;

public class Row {
    private List<Cell> cells;

    public Row(List<Cell> cells) {
        this.cells = cells;
    }

    public String toCsv() {
        return this.cells.stream().map(Cell::content).collect(Collectors.joining(", "));
    }
}
