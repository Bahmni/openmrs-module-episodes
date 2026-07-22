/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search.query;

import org.openmrs.PatientProgram;
import org.openmrs.module.episodes.search.criteria.Condition;
import org.openmrs.module.episodes.search.criteria.FieldComparator;
import org.openmrs.module.episodes.search.exceptions.InvalidSearchCriteriaException;
import org.openmrs.module.episodes.search.exceptions.SearchResponseErrorStatus;
import org.openmrs.module.episodes.search.query.PatientProgramFieldRegistry.FieldDescriptor;
import org.openmrs.module.episodes.search.query.PatientProgramFieldRegistry.JoinKey;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatientProgramQueryBuilder {

    private static final DateTimeFormatter DATE_INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public List<Predicate> buildPredicates(CriteriaBuilder cb, Root<PatientProgram> root,
            CriteriaQuery<?> cq, List<Predicate> voidedFilters, Condition criteria) {
        Map<JoinKey, Join<?, ?>> joins = new HashMap<>();
        List<Predicate> predicates = new ArrayList<>();
        processCondition(criteria, cb, root, cq, joins, voidedFilters, predicates);
        return predicates;
    }

    private void processCondition(Condition condition, CriteriaBuilder cb, Root<PatientProgram> root,
            CriteriaQuery<?> cq, Map<JoinKey, Join<?, ?>> joins,
            List<Predicate> voidedFilters, List<Predicate> predicates) {
        if (condition.isLeaf()) {
            processLeaf(condition, cb, root, cq, joins, voidedFilters, predicates);
        } else {
            for (Condition child : condition.getConditions()) {
                processCondition(child, cb, root, cq, joins, voidedFilters, predicates);
            }
        }
    }

    private void processLeaf(Condition leaf, CriteriaBuilder cb, Root<PatientProgram> root,
            CriteriaQuery<?> cq, Map<JoinKey, Join<?, ?>> joins,
            List<Predicate> voidedFilters, List<Predicate> predicates) {
        String field = leaf.getField();
        FieldDescriptor descriptor = PatientProgramFieldRegistry.get(field);
        if (descriptor == null) {
            throw new InvalidSearchCriteriaException(
                    "Unknown search field: '" + field + "'", SearchResponseErrorStatus.BAD_REQUEST);
        }

        FieldComparator comparator = leaf.getComparator();
        if (comparator == null) {
            throw new InvalidSearchCriteriaException(
                    "Unknown comparator for field '" + field + "'. Supported: eq, gt, lt",
                    SearchResponseErrorStatus.BAD_REQUEST);
        }
        if (!descriptor.allowedComparators.contains(comparator)) {
            throw new InvalidSearchCriteriaException(
                    "Comparator '" + comparator.name().toLowerCase() + "' is not supported for field '" + field + "'",
                    SearchResponseErrorStatus.BAD_REQUEST);
        }

        for (JoinKey joinKey : descriptor.requiredJoins) {
            addJoinIfAbsent(joinKey, cb, root, joins, voidedFilters);
        }

        Object value = parseValue(descriptor, leaf.getValue(), field);
        predicates.add(descriptor.predicateFn.build(cb, root, cq, joins, comparator, value));
    }

    private void addJoinIfAbsent(JoinKey joinKey, CriteriaBuilder cb, Root<PatientProgram> root,
            Map<JoinKey, Join<?, ?>> joins, List<Predicate> voidedFilters) {
        if (joins.containsKey(joinKey)) return;

        Join<?, ?> join;
        switch (joinKey) {
            case PS:
                join = root.join("states", JoinType.INNER);
                voidedFilters.add(cb.equal(join.get("voided"), false));
                break;
            case PI:
                join = root.join("patient", JoinType.INNER).join("identifiers", JoinType.INNER);
                voidedFilters.add(cb.equal(join.get("voided"), false));
                break;
            case PPA:
                join = root.join("attributes", JoinType.INNER);
                voidedFilters.add(cb.equal(join.get("voided"), false));
                break;
            default:
                throw new InvalidSearchCriteriaException(
                        "Unknown join key: " + joinKey, SearchResponseErrorStatus.BAD_REQUEST);
        }
        joins.put(joinKey, join);
    }

    private Object parseValue(FieldDescriptor descriptor, String value, String field) {
        if (descriptor.valueType == PatientProgramFieldRegistry.FieldType.DATE) {
            try {
                LocalDate localDate = LocalDate.parse(value, DATE_INPUT_FORMATTER);
                return Date.from(localDate.atStartOfDay(ZoneOffset.UTC).toInstant());
            } catch (DateTimeParseException e) {
                throw new InvalidSearchCriteriaException(
                        "Invalid date format: '" + value + "'. Expected yyyy-MM-dd",
                        SearchResponseErrorStatus.BAD_REQUEST);
            }
        }
        return value;
    }
}
