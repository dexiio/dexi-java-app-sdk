package io.dexi.service;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class Rows {

    private ArrayList<Map<String, Object>> rows = new ArrayList<>();

    public ArrayList<Map<String, Object>> getRows() {
        return rows;
    }

    public void setRows(ArrayList<Map<String, Object>> rows) {
        this.rows = rows;
    }

    @SafeVarargs
    public static Rows of(Map<String, Object>... rows) {
        Rows out = new Rows();

        Collections.addAll(out.rows, rows);

        return out;
    }

    public boolean isEmpty() {
        return rows.isEmpty();
    }
}
