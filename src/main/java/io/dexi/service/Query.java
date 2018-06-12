package io.dexi.service;


import java.util.regex.Pattern;

public class Query {
    private static final Pattern NUMBER = Pattern.compile("^[0-9]+$");

    private String query;

    private String offset;

    private int limit = 30;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getOffset() {
        return offset;
    }

    public long  getOffsetAsLong() {
        if (offset == null || "".equals(offset.trim())) {
            return 0;
        }
        if (NUMBER.matcher(offset).matches()) {
            return Integer.valueOf(offset);
        }

        return 0; //Unknown number
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
