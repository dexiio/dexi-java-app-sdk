package io.dexi.service;



/**
 * The payload received for dynamic schema requests.
 *
 * @param <T> The component configuration class
 */
public class DynamicSchemaConfig<T> {

    /**
     * The component configuration.
     */
    private T options;

    /**
     * Input connections connected to the component. Can be used to deduce things based on the incoming data types
     * such as the resulting output.
     *
     * Can be ignored if you do not need to deduce information based on the input connections - and might not always
     * be provided.
     *
     * Note that this can be null
     */
    private Schema inputConnectionSchema;

    public T getOptions() {
        return options;
    }

    public Schema getInputConnectionSchema() {
        return inputConnectionSchema;
    }
}
