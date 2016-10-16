package com.giaybac.traprange.result;

import java.util.List;
import java.util.stream.Collectors;

public class Table {
    private List<Row> rows;

    public Table(List<Row> rows) {
        this.rows = rows;
    }


    public String toCsv() {
        return rows.stream().map(Row::toCsv).collect(Collectors.joining(System.lineSeparator()));
    }
}
