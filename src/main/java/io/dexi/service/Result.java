package io.dexi.service;


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
