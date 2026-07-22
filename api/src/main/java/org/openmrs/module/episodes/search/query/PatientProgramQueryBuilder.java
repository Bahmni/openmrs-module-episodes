/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search.query;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.openmrs.module.episodes.search.criteria.Condition;
import org.openmrs.module.episodes.search.criteria.ConditionOperator;
import org.openmrs.module.episodes.search.criteria.FieldComparator;
import org.openmrs.module.episodes.search.exceptions.InvalidSearchCriteriaException;
import org.openmrs.module.episodes.search.exceptions.SearchResponseErrorStatus;
import org.openmrs.module.episodes.search.query.PatientProgramFieldRegistry.AliasDescriptor;
import org.openmrs.module.episodes.search.query.PatientProgramFieldRegistry.FieldDescriptor;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PatientProgramQueryBuilder {

    private static final DateTimeFormatter DATE_INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Translates the Condition tree into Hibernate Criterion and applies it (along with
     * any required aliases and voided-filters) to the given Criteria.
     */
    public void applyCondition(Criteria criteria, Condition condition) {
        Set<String> createdAliases = new HashSet<>();
        Criterion criterion = buildCriterion(criteria, condition, createdAliases);
        if (criterion != null) {
            criteria.add(criterion);
        }
    }

    /**
     * Recursively walks the Condition tree. Leaf nodes become field-level Criterion via the
     * registry; group nodes combine their children with AND / OR.
     */
    private Criterion buildCriterion(Criteria criteria, Condition condition, Set<String> createdAliases) {
        if (condition.isLeaf()) {
            return buildLeafCriterion(criteria, condition, createdAliases);
        }

        List<Criterion> children = new ArrayList<>();
        for (Condition child : condition.getConditions()) {
            Criterion c = buildCriterion(criteria, child, createdAliases);
            if (c != null) {
                children.add(c);
            }
        }
        if (children.isEmpty()) {
            return null;
        }
        if (children.size() == 1) {
            return children.get(0);
        }
        Criterion[] arr = children.toArray(new Criterion[0]);
        return condition.getOperator() == ConditionOperator.OR
                ? Restrictions.or(arr)
                : Restrictions.and(arr);
    }

    private Criterion buildLeafCriterion(Criteria criteria, Condition leaf, Set<String> createdAliases) {
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

        ensureAliases(criteria, descriptor.requiredAliases, createdAliases);

        Object value = parseValue(descriptor, leaf.getValue(), field);
        return descriptor.criterionFn.build(comparator, value);
    }

    /**
     * Creates Hibernate aliases on the Criteria, driven entirely by the field descriptor's
     * alias list. Each alias is created at most once. Voided-filters are added automatically
     * for aliases that require them.
     */
    private void ensureAliases(Criteria criteria, List<AliasDescriptor> aliases, Set<String> createdAliases) {
        for (AliasDescriptor alias : aliases) {
            if (createdAliases.add(alias.alias)) {
                criteria.createAlias(alias.associationPath, alias.alias, alias.joinType);
                if (alias.filterVoided) {
                    criteria.add(Restrictions.eq(alias.alias + ".voided", false));
                }
            }
        }
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
