package io.dexi.service;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Result {

    private Rows rows = new Rows();

    private String nextOffset;

    public Rows getRows() {
        return rows;
    }

    public void setRows(Rows rows) {
        this.rows = rows;
    }

    public String getNextOffset() {
        return nextOffset;
    }

    public void setNextOffset(String nextOffset) {
        this.nextOffset = nextOffset;
    }
}
