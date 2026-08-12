/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search.builder;

import org.bahmni.search.builder.SearchFieldPredicate;
import org.openmrs.module.episodes.search.SearchKeyConstants;
import org.openmrs.module.episodes.search.SearchFields;
import org.bahmni.search.exceptions.InvalidSearchCriteriaException;
import org.bahmni.search.exceptions.SearchResponseErrorStatus;
import org.bahmni.search.model.ConditionOperator;
import org.bahmni.search.model.FieldComparator;
import org.bahmni.search.model.FieldType;
import org.bahmni.search.model.SearchCondition;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class PatientProgramCriteriaBuilder {

    private static final DateTimeFormatter ISO_DATETIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private final PatientProgramJoinResolver joinResolver = new PatientProgramJoinResolver();
    private final Map<String, SearchFieldPredicate> fieldRegistry;

    public PatientProgramCriteriaBuilder() {
        this.fieldRegistry = Collections.unmodifiableMap(buildFieldRegistry());
    }

    public void apply(EpisodeQueryContext queryContext, SearchCondition criteria) {
        Predicate predicate = buildCriterion(queryContext, criteria);
        if (predicate != null) {
            queryContext.predicates.add(predicate);
        }
    }

    private Map<String, SearchFieldPredicate> buildFieldRegistry() {
        Map<String, SearchFieldPredicate> registry = new HashMap<>();

        registry.put(SearchFields.EOC_START_DATE,
                createFieldPredicate(queryContext -> queryContext.episodeRoot, SearchKeyConstants.EPISODE_DATE_STARTED, FieldType.DATE));
        registry.put(SearchFields.EOC_END_DATE,
                createFieldPredicate(queryContext -> queryContext.episodeRoot, SearchKeyConstants.EPISODE_DATE_ENDED, FieldType.DATE));
        registry.put(SearchFields.EOC_CARE_MANAGER,
                createFieldPredicate(joinResolver::joinCareManager, SearchKeyConstants.COMMON_UUID, FieldType.STRING));

        registry.put(SearchFields.PROGRAM_ENROLLMENT_DATE,
                createFieldPredicate(queryContext -> queryContext.patientProgramJoin, SearchKeyConstants.ENROLLMENT_DATE_ENROLLED, FieldType.DATE));
        registry.put(SearchFields.PROGRAM_COMPLETION_DATE,
                createFieldPredicate(queryContext -> queryContext.patientProgramJoin, SearchKeyConstants.ENROLLMENT_DATE_COMPLETED, FieldType.DATE));

        registry.put(SearchFields.PROGRAM_UUID,
                createFieldPredicate(joinResolver::joinProgram, SearchKeyConstants.COMMON_UUID, FieldType.STRING));
        registry.put(SearchFields.PROGRAM_TYPE,
                createFieldPredicate(joinResolver::joinProgramConcept, SearchKeyConstants.COMMON_UUID, FieldType.STRING));
        registry.put(SearchFields.PROGRAM_LOCATION,
                createFieldPredicate(joinResolver::joinLocation, SearchKeyConstants.COMMON_UUID, FieldType.STRING));

        registry.put(SearchFields.PROGRAM_STATUS,
                createFieldPredicate(joinResolver::joinStateConcept, SearchKeyConstants.COMMON_UUID, FieldType.STRING));
        registry.put(SearchFields.PROGRAM_STATUS_DATE,
                createFieldPredicate(joinResolver::joinStates, SearchKeyConstants.STATE_START_DATE, FieldType.DATE));

        registry.put(SearchFields.PATIENT_IDENTIFIER_KIND, this::buildIdentifierKindPredicate);
        registry.put(SearchFields.PATIENT_IDENTIFIER_VALUE,
                createFieldPredicate(joinResolver::joinPatientIdentifiers, SearchKeyConstants.IDENTIFIER_VALUE, FieldType.STRING));

        registry.put(SearchFields.PROGRAM_ATTRIBUTE_KIND,
                createFieldPredicate(joinResolver::joinAttributeType, SearchKeyConstants.COMMON_UUID, FieldType.STRING));
        registry.put(SearchFields.PROGRAM_ATTRIBUTE_VALUE,
                createFieldPredicate(joinResolver::joinAttributes, SearchKeyConstants.ATTRIBUTE_VALUE_REFERENCE, FieldType.STRING));

        return registry;
    }

    private SearchFieldPredicate createFieldPredicate(Function<EpisodeQueryContext, From<?, ?>> joinFunction,
                                                     String propertyName, FieldType fieldType) {
        return (queryContext, fieldName, comparator, value, operator) -> {
            validateComparator(fieldName, comparator, fieldType);
            Path<?> fieldPath = joinFunction.apply((EpisodeQueryContext) queryContext).get(propertyName);
            return buildPredicate(queryContext.criteriaBuilder, fieldPath, comparator, value);
        };
    }

    private Predicate buildIdentifierKindPredicate(org.bahmni.search.builder.QueryContext<?> ctx, String fieldName,
                                                    FieldComparator comparator, String value,
                                                    ConditionOperator operator) {
        validateComparator(fieldName, comparator, FieldType.STRING);
        EpisodeQueryContext queryContext = (EpisodeQueryContext) ctx;
        From<?, ?> identifierTypeJoin = joinResolver.joinIdentifierType(queryContext);

        Predicate uuidMatch = queryContext.criteriaBuilder.equal(identifierTypeJoin.get(SearchKeyConstants.COMMON_UUID), value);
        Predicate nameMatch = queryContext.criteriaBuilder.equal(identifierTypeJoin.get(SearchKeyConstants.COMMON_NAME), value);

        ConditionOperator effectiveOperator = operator != null ? operator : ConditionOperator.OR;
        return effectiveOperator == ConditionOperator.AND
                ? queryContext.criteriaBuilder.and(uuidMatch, nameMatch)
                : queryContext.criteriaBuilder.or(uuidMatch, nameMatch);
    }

    private Predicate buildCriterion(EpisodeQueryContext queryContext, SearchCondition criteria) {
        if (criteria == null) {
            return null;
        }
        if (criteria.isLeaf()) {
            return buildLeafCriterion(queryContext, criteria);
        }
        return combineChildPredicates(queryContext, criteria);
    }

    private Predicate buildLeafCriterion(EpisodeQueryContext queryContext, SearchCondition leafCriteria) {
        String fieldName = leafCriteria.getField();
        FieldComparator comparator = leafCriteria.getComparator();

        SearchFieldPredicate fieldPredicate = fieldRegistry.get(fieldName);
        if (fieldPredicate == null) {
            throw new InvalidSearchCriteriaException(
                    "Unknown search field: '" + fieldName + "'",
                    SearchResponseErrorStatus.BAD_REQUEST);
        }

        return fieldPredicate.build(queryContext, fieldName, comparator,
                leafCriteria.getValue(), leafCriteria.getOperator());
    }

    private Predicate combineChildPredicates(EpisodeQueryContext queryContext, SearchCondition parentCriteria) {
        List<Predicate> childPredicates = new ArrayList<>();
        if (parentCriteria.getConditions() != null) {
            for (SearchCondition childCriteria : parentCriteria.getConditions()) {
                Predicate resolvedPredicate = buildCriterion(queryContext, childCriteria);
                if (resolvedPredicate != null) {
                    childPredicates.add(resolvedPredicate);
                }
            }
        }

        if (childPredicates.isEmpty()) {
            return null;
        }
        if (childPredicates.size() == 1) {
            return childPredicates.get(0);
        }

        Predicate[] predicateArray = childPredicates.toArray(new Predicate[0]);
        return parentCriteria.getOperator() == ConditionOperator.OR
                ? queryContext.criteriaBuilder.or(predicateArray)
                : queryContext.criteriaBuilder.and(predicateArray);
    }

    @SuppressWarnings("unchecked")
    private Predicate buildPredicate(CriteriaBuilder criteriaBuilder, Path<?> fieldPath,
                                     FieldComparator comparator, String value) {
        switch (comparator) {
            case EQ: return criteriaBuilder.equal(fieldPath, value);
            case GT: return criteriaBuilder.greaterThan((Path<Date>) fieldPath, parseDate(value));
            case LT: return criteriaBuilder.lessThan((Path<Date>) fieldPath, parseDate(value));
            case GE: return criteriaBuilder.greaterThanOrEqualTo((Path<Date>) fieldPath, parseDate(value));
            case LE: return criteriaBuilder.lessThanOrEqualTo((Path<Date>) fieldPath, parseDate(value));
            default:
                throw new InvalidSearchCriteriaException(
                        "Unsupported comparator: " + comparator,
                        SearchResponseErrorStatus.BAD_REQUEST);
        }
    }

    private void validateComparator(String fieldName, FieldComparator comparator, FieldType fieldType) {
        if (!fieldType.supports(comparator)) {
            throw new InvalidSearchCriteriaException(
                    "Comparator '" + comparator.name().toLowerCase()
                            + "' is not supported for field '" + fieldName
                            + "'. Supported: " + fieldType.getSupportedComparators().toString().toLowerCase(),
                    SearchResponseErrorStatus.BAD_REQUEST);
        }
    }

    private Date parseDate(String dateValue) {
        try {
            return Date.from(OffsetDateTime.parse(dateValue, ISO_DATETIME_FORMAT).toInstant());
        } catch (DateTimeParseException exception) {
            throw new InvalidSearchCriteriaException(
                    "Invalid date format: '" + dateValue
                            + "'. Expected yyyy-MM-dd'T'HH:mm:ss.SSSZ (e.g. 2024-01-01T10:30:00.000+0530)",
                    SearchResponseErrorStatus.BAD_REQUEST);
        }
    }
}
