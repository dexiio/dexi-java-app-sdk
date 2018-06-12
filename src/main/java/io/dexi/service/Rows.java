package io.dexi.service;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class Rows extends ArrayList<Map<String, Object>> {


    @SafeVarargs
    public static Rows of(Map<String, Object>... rows) {
        Rows out = new Rows();

        Collections.addAll(out, rows);

        return out;
    }
}
