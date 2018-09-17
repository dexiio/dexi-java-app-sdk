package io.dexi.service;


import com.fasterxml.jackson.databind.node.ObjectNode;

public class DynamicSchemaPayload<T> {

    private T options;

    private ObjectNode inputs;

    public T getOptions() {
        return options;
    }

    public ObjectNode getInputs() {
        return inputs;
    }

    public void setInputs(ObjectNode inputs) {
        this.inputs = inputs;
    }
}
