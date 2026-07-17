package org.openmrs.module.episodes.dao.impl;

import org.hibernate.Criteria;
import org.openmrs.module.episodes.search.EpisodeSearchFieldRegistry.FieldMapping;
import org.openmrs.module.episodes.search.EpisodeSearchFieldRegistry.Root;

import java.util.HashMap;
import java.util.Map;

/**
 * Hands out one Hibernate join alias per distinct association path for the whole search tree
 * and caches it for reuse. This is a hard Hibernate Criteria constraint, not a design choice:
 * a single query cannot create two aliases for the same association path (it throws
 * "duplicate association path"), so every leaf condition anywhere in the AND/OR tree that
 * targets, say, patient.identifiers must resolve to the very same alias.
 * <p>
 * Consequence for AND-of-same-collection-field queries (e.g. "has a PASSPORT identifier AND
 * has a DRIVING identifier"): with one shared alias both conditions are evaluated against the
 * same joined row, so this particular combination can never match. That's an inherent
 * limitation of a single Criteria join, not something introduced here - correctly supporting
 * it would require a correlated subquery per branch. Not needed by any current requirement, so
 * left as a TODO rather than solved speculatively.
 */
class AliasResolver {

    private final Criteria criteria;
    private final Map<String, String> collectionAliasCache = new HashMap<>();
    private final Map<String, String> singleValuedAliasCache = new HashMap<>();
    private int counter = 0;

    AliasResolver(Criteria criteria) {
        this.criteria = criteria;
    }

    /**
     * @return the alias to prefix the leaf property with, or null if the field is on the
     * Episode root itself (no join needed).
     */
    String resolve(FieldMapping mapping) {

        if (mapping.associationPath.isEmpty()) {
            return null;
        }

        String currentAlias = mapping.root == Root.PATIENT ? patientAlias() : null;

        for (int i = 0; i < mapping.associationPath.size(); i++) {
            String hop = mapping.associationPath.get(i);
            boolean isFirstHopOfJoinGroup = i == 0;

            Map<String, String> cache = isFirstHopOfJoinGroup ? collectionAliasCache : singleValuedAliasCache;
            String cacheKey = isFirstHopOfJoinGroup
                    ? mapping.joinGroup
                    : (currentAlias == null ? "" : currentAlias + ".") + hop;

            currentAlias = aliasFor(cache, cacheKey, currentAlias, hop);
        }
        return currentAlias;
    }

    /**
     * @return the alias for the given single-valued association hop off of baseAlias (or the
     * root entity if baseAlias is null), creating and caching a new join alias if one does not
     * already exist.
     */
    String resolveSingleValued(String baseAlias, String hop) {
        String cacheKey = (baseAlias == null ? "" : baseAlias + ".") + hop;
        return aliasFor(singleValuedAliasCache, cacheKey, baseAlias, hop);
    }

    private String aliasFor(Map<String, String> cache, String cacheKey, String baseAlias, String hop) {
        String alias = cache.get(cacheKey);
        if (alias == null) {
            alias = "a" + (++counter);

            String path = baseAlias == null ? hop : baseAlias + "." + hop;
            criteria.createAlias(path, alias);
            cache.put(cacheKey, alias);
        }
        return alias;
    }

    private String patientAlias() {
        return aliasFor(singleValuedAliasCache, "patient", null, "patient");
    }
}
