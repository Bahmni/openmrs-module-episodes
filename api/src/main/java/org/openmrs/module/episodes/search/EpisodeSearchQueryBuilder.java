/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search;

import org.openmrs.module.episodes.search.constants.SearchFields;
import org.openmrs.module.episodes.search.criteria.Condition;
import org.openmrs.module.episodes.search.criteria.FieldComparator;
import org.openmrs.module.episodes.search.exceptions.InvalidSearchCriteriaException;
import org.openmrs.module.episodes.search.exceptions.SearchResponseErrorStatus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;

public class EpisodeSearchQueryBuilder {

    private enum JoinKey { PP, PS, PI, PPA }

    private enum FieldType { TEXT, DATE }

    private static String toHqlOp(FieldComparator comparator) {
        switch (comparator) {
            case EQ: return "=";
            case GT: return ">";
            case LT: return "<";
            default: throw new InvalidSearchCriteriaException("Unsupported comparator: " + comparator, SearchResponseErrorStatus.BAD_REQUEST);
        }
    }

    private static class FieldDescriptor {
        final Set<FieldComparator> allowedComparators;
        final Set<JoinKey> requiredJoins;
        final BinaryOperator<String> fragmentFn;
        final FieldType valueType;

        FieldDescriptor(Set<FieldComparator> comparators, Set<JoinKey> joins,
                BinaryOperator<String> fn, FieldType valueType) {
            this.allowedComparators = comparators;
            this.requiredJoins = joins;
            this.fragmentFn = fn;
            this.valueType = valueType;
        }
    }

    private static final Map<String, FieldDescriptor> FIELD_REGISTRY = new HashMap<>();

    static {
        FIELD_REGISTRY.put(SearchFields.EpisodeOfCare.START_DATE, new FieldDescriptor(
                EnumSet.of(FieldComparator.GT, FieldComparator.LT),
                EnumSet.noneOf(JoinKey.class),
                (op, p) -> "e.dateStarted " + op + " :" + p,
                FieldType.DATE
        ));
        FIELD_REGISTRY.put(SearchFields.EpisodeOfCare.END_DATE, new FieldDescriptor(
                EnumSet.of(FieldComparator.GT, FieldComparator.LT),
                EnumSet.noneOf(JoinKey.class),
                (op, p) -> "e.dateEnded " + op + " :" + p,
                FieldType.DATE
        ));
        FIELD_REGISTRY.put(SearchFields.EpisodeOfCare.CARE_MANAGER, new FieldDescriptor(
                EnumSet.of(FieldComparator.EQ),
                EnumSet.noneOf(JoinKey.class),
                (op, p) -> "e.careManager.uuid " + op + " :" + p,
                FieldType.TEXT
        ));
        FIELD_REGISTRY.put(SearchFields.Program.UUID, new FieldDescriptor(
                EnumSet.of(FieldComparator.EQ),
                EnumSet.of(JoinKey.PP),
                (op, p) -> "pp.program.uuid " + op + " :" + p,
                FieldType.TEXT
        ));
        FIELD_REGISTRY.put(SearchFields.Program.TYPE, new FieldDescriptor(
                EnumSet.of(FieldComparator.EQ),
                EnumSet.of(JoinKey.PP),
                (op, p) -> "pp.program.concept.uuid " + op + " :" + p,
                FieldType.TEXT
        ));
        FIELD_REGISTRY.put(SearchFields.Program.LOCATION, new FieldDescriptor(
                EnumSet.of(FieldComparator.EQ),
                EnumSet.of(JoinKey.PP),
                (op, p) -> "pp.location.uuid " + op + " :" + p,
                FieldType.TEXT
        ));
        FIELD_REGISTRY.put(SearchFields.Program.STATUS, new FieldDescriptor(
                EnumSet.of(FieldComparator.EQ),
                EnumSet.of(JoinKey.PP, JoinKey.PS),
                (op, p) -> "ps.state.concept.uuid " + op + " :" + p,
                FieldType.TEXT
        ));
        FIELD_REGISTRY.put(SearchFields.Program.STATUS_DATE, new FieldDescriptor(
                EnumSet.of(FieldComparator.GT, FieldComparator.LT),
                EnumSet.of(JoinKey.PP, JoinKey.PS),
                (op, p) -> "ps.startDate " + op + " :" + p,
                FieldType.DATE
        ));
        FIELD_REGISTRY.put(SearchFields.Patient.IDENTIFIER_KIND, new FieldDescriptor(
                EnumSet.of(FieldComparator.EQ),
                EnumSet.of(JoinKey.PI),
                (op, p) -> "(pi.identifierType.uuid = :" + p + " OR pi.identifierType.name = :" + p + ")",
                FieldType.TEXT
        ));
        FIELD_REGISTRY.put(SearchFields.Patient.IDENTIFIER_VALUE, new FieldDescriptor(
                EnumSet.of(FieldComparator.EQ),
                EnumSet.of(JoinKey.PI),
                (op, p) -> "pi.identifier " + op + " :" + p,
                FieldType.TEXT
        ));
        FIELD_REGISTRY.put(SearchFields.Program.ATTRIBUTE_KIND, new FieldDescriptor(
                EnumSet.of(FieldComparator.EQ),
                EnumSet.of(JoinKey.PP, JoinKey.PPA),
                (op, p) -> "ppa.attributeType.uuid " + op + " :" + p,
                FieldType.TEXT
        ));
        FIELD_REGISTRY.put(SearchFields.Program.ATTRIBUTE_VALUE, new FieldDescriptor(
                EnumSet.of(FieldComparator.EQ),
                EnumSet.of(JoinKey.PP, JoinKey.PPA),
                (op, p) -> "ppa.valueReference " + op + " :" + p,
                FieldType.TEXT
        ));
    }

    public BuiltQuery build(Condition criteria) {
        Set<JoinKey> requiredJoins = new LinkedHashSet<>();
        List<String> fragments = new ArrayList<>();
        Map<String, Object> parameters = new LinkedHashMap<>();
        int[] paramCounter = {0};
        String[] searchedAttributeTypeUuid = {null};

        processCondition(criteria, requiredJoins, fragments, parameters, paramCounter, searchedAttributeTypeUuid);

        String hql = buildHql(requiredJoins, fragments);
        return new BuiltQuery(hql, parameters, searchedAttributeTypeUuid[0]);
    }

    private void processCondition(Condition condition, Set<JoinKey> joins, List<String> fragments,
            Map<String, Object> params, int[] counter, String[] attrTypeUuid) {
        if (condition.isLeaf()) {
            processLeaf(condition, joins, fragments, params, counter, attrTypeUuid);
        } else {
            for (Condition child : condition.getConditions()) {
                processCondition(child, joins, fragments, params, counter, attrTypeUuid);
            }
        }
    }

    private void processLeaf(Condition leaf, Set<JoinKey> joins, List<String> fragments,
            Map<String, Object> params, int[] counter, String[] attrTypeUuid) {
        String field = leaf.getField();
        FieldDescriptor descriptor = FIELD_REGISTRY.get(field);
        if (descriptor == null) {
            throw new InvalidSearchCriteriaException("Unknown search field: '" + field + "'", SearchResponseErrorStatus.BAD_REQUEST);
        }

        FieldComparator comparator = leaf.getComparator();
        if (comparator == null) {
            throw new InvalidSearchCriteriaException(
                    "Unknown comparator for field '" + field + "'. Supported: eq, gt, lt", SearchResponseErrorStatus.BAD_REQUEST);
        }
        if (!descriptor.allowedComparators.contains(comparator)) {
            throw new InvalidSearchCriteriaException(
                    "Comparator '" + comparator.name().toLowerCase() + "' is not supported for field '" + field + "'", SearchResponseErrorStatus.BAD_REQUEST);
        }

        if (SearchFields.Program.ATTRIBUTE_KIND.equals(field)) {
            attrTypeUuid[0] = leaf.getValue();
        }

        joins.addAll(descriptor.requiredJoins);

        String paramName = "param" + counter[0]++;
        fragments.add(descriptor.fragmentFn.apply(toHqlOp(comparator), paramName));
        params.put(paramName, parseValue(descriptor, leaf.getValue()));
    }

    private Object parseValue(FieldDescriptor descriptor, String value) {
        if (descriptor.valueType == FieldType.DATE) {
            try {
                return new SimpleDateFormat("yyyy-MM-dd").parse(value);
            } catch (ParseException e) {
                throw new InvalidSearchCriteriaException(
                        "Invalid date format: '" + value + "'. Expected yyyy-MM-dd", SearchResponseErrorStatus.BAD_REQUEST);
            }
        }
        return value;
    }

    private String buildHql(Set<JoinKey> joins, List<String> fragments) {
        StringBuilder sb = new StringBuilder("SELECT DISTINCT e FROM Episode e");

        if (joins.contains(JoinKey.PP)) sb.append(" INNER JOIN e.patientPrograms pp");
        if (joins.contains(JoinKey.PS)) sb.append(" INNER JOIN pp.states ps");
        if (joins.contains(JoinKey.PI)) sb.append(" INNER JOIN e.patient ep INNER JOIN ep.identifiers pi");
        if (joins.contains(JoinKey.PPA)) sb.append(" INNER JOIN pp.attributes ppa");

        sb.append(" WHERE e.voided = false");

        if (joins.contains(JoinKey.PP)) sb.append(" AND pp.voided = false");
        if (joins.contains(JoinKey.PS)) sb.append(" AND ps.voided = false");
        if (joins.contains(JoinKey.PI)) sb.append(" AND pi.voided = false");
        if (joins.contains(JoinKey.PPA)) sb.append(" AND ppa.voided = false");

        for (String fragment : fragments) sb.append(" AND ").append(fragment);

        return sb.toString();
    }
}
