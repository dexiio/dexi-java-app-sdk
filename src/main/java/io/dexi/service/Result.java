package io.dexi.service;


public class Result extends Rows {

    private String nextOffset;

    public String getNextOffset() {
        return nextOffset;
    }

    public void setNextOffset(String nextOffset) {
        this.nextOffset = nextOffset;
    }
}
