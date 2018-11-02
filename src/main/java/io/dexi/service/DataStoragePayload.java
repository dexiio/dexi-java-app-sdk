package io.dexi.service;


public class DataStoragePayload<T> extends DynamicSchemaPayload {

    private T config;

    private Rows rows;

    public T getConfig() {
        return config;
    }

    public void setConfig(T config) {
        this.config = config;
    }

    public Rows getRows() {
        return rows;
    }

    public void setRows(Rows rows) {
        this.rows = rows;
    }
}
