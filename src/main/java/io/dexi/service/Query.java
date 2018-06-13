package io.dexi.service;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class Query {
    private static final Pattern NUMBER = Pattern.compile("^[0-9]+$");

    private List<Order> sortOrder = new ArrayList<>();

    private Set<String> fields = new HashSet<>();

    private List<Statement> statements = new ArrayList<>();

    private String offset;

    private int limit = 30;

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

    public Set<String> getFields() {
        return fields;
    }

    public void setFields(Set<String> fields) {
        this.fields = fields;
    }

    public List<Statement> getStatements() {
        return statements;
    }

    public void setStatements(List<Statement> statements) {
        this.statements = statements;
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

    public List<Order> getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(List<Order> sortOrder) {
        this.sortOrder = sortOrder;
    }

    public static class Condition {
        private ConditionType type = ConditionType.EQ;

        private String field;

        private Object from;

        private Object to;

        private Object value;

        private List<Object> list;

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public ConditionType getType() {
            return type;
        }

        public void setType(ConditionType type) {
            this.type = type;
        }

        public Object getFrom() {
            return from;
        }

        public void setFrom(Object from) {
            this.from = from;
        }

        public Object getTo() {
            return to;
        }

        public void setTo(Object to) {
            this.to = to;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public List<Object> getList() {
            return list;
        }

        public void setList(List<Object> list) {
            this.list = list;
        }
    }

    public static class Statement {

        private StatementType type = StatementType.AND;

        private List<Condition> conditions = new ArrayList<>();

        public StatementType getType() {
            return type;
        }

        public void setType(StatementType type) {
            this.type = type;
        }

        public List<Condition> getConditions() {
            return conditions;
        }

        public void setConditions(List<Condition> conditions) {
            this.conditions = conditions;
        }
    }

    public static class Order {
        private String field;

        private OrderDirection direction;

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public OrderDirection getDirection() {
            return direction;
        }

        public void setDirection(OrderDirection direction) {
            this.direction = direction;
        }
    }

    public enum StatementType {
        OR, AND
    }

    public enum OrderDirection {
        ASC, DESC
    }

    public enum ConditionType {
        EQ,
        NEQ,
        GT,
        GTE,
        LT,
        LTE,
        IN,
        NIN,
        CONTAINS,
        STARTS_WITH,
        ENDS_WITH,
        BETWEEN
    }
}
