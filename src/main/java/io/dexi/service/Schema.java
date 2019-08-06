package io.dexi.service;


import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * This class provides the structure needed to communicate data and configuration structure to dexi.
 */
public class Schema extends HashMap<String, Schema.Field> {

    public static class Field {

        private String title;

        private boolean required;

        private boolean secret;

        private String type;

        private String description;

        private JsonNode options;

        private int sortOrder;

        private JsonNode defaultValue;

        private Set<String> dependsOn = new HashSet<>();

        private JsonNode configuration;

        private Schema properties;

        private Field items;

        public Schema getProperties() {
            return properties;
        }

        public void setProperties(Schema properties) {
            this.properties = properties;
        }

        public Field getItems() {
            return items;
        }

        public void setItems(Field items) {
            this.items = items;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        public boolean isSecret() {
            return secret;
        }

        public void setSecret(boolean secret) {
            this.secret = secret;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public JsonNode getOptions() {
            return options;
        }

        public void setOptions(JsonNode options) {
            this.options = options;
        }

        public int getSortOrder() {
            return sortOrder;
        }

        public void setSortOrder(int sortOrder) {
            this.sortOrder = sortOrder;
        }

        public JsonNode getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(JsonNode defaultValue) {
            this.defaultValue = defaultValue;
        }

        public Set<String> getDependsOn() {
            return dependsOn;
        }

        public void setDependsOn(Set<String> dependsOn) {
            this.dependsOn = dependsOn;
        }

        public JsonNode getConfiguration() {
            return configuration;
        }

        public void setConfiguration(JsonNode configuration) {
            this.configuration = configuration;
        }
    }

}
