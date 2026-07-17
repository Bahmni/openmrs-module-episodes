package org.openmrs.module.episodes.search;

import java.util.List;

public class EpisodeSearchCriteria {

    private String operator;
    private List<EpisodeSearchCriteria> conditions;

    private String field;
    private String comparator;
    private String value;
    private String EpisodeSearchFieldRegistry;

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public List<EpisodeSearchCriteria> getConditions() {
        return conditions;
    }

    public void setConditions(List<EpisodeSearchCriteria> conditions) {
        this.conditions = conditions;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getComparator() {
        return comparator;
    }

    public void setComparator(String comparator) {
        this.comparator = comparator;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return true if this node is a group (AND/OR) node with child conditions, false if it is a
     * leaf condition node.
     */
    public boolean isGroup() {
        return operator != null && conditions != null && !conditions.isEmpty();
    }

    public boolean isOr() {
        return "OR".equalsIgnoreCase(operator);
    }
}
