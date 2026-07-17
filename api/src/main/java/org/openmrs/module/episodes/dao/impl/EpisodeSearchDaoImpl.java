package org.openmrs.module.episodes.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Restrictions;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.dao.EpisodeSearchDao;
import org.openmrs.module.episodes.search.EpisodeSearchCriteria;
import org.openmrs.module.episodes.search.EpisodeSearchFieldRegistry;
import org.openmrs.module.episodes.search.EpisodeSearchFieldRegistry.FieldMapping;
import org.openmrs.module.episodes.search.EpisodeSearchFieldRegistry.ValueType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;


public class EpisodeSearchDaoImpl implements EpisodeSearchDao {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private final SessionFactory sessionFactory;

    public EpisodeSearchDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<Episode> search(EpisodeSearchCriteria criteria) {
        Criteria hibernateCriteria = sessionFactory.getCurrentSession().createCriteria(Episode.class);
        hibernateCriteria.add(Restrictions.eq("voided", false));
        hibernateCriteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        if (criteria != null) {
            AliasResolver aliasResolver = new AliasResolver(hibernateCriteria);
            hibernateCriteria.add(buildRestriction(criteria, aliasResolver));
        }

        return hibernateCriteria.list();
    }

    private Criterion buildRestriction(EpisodeSearchCriteria node, AliasResolver aliasResolver) {
        if (node.isGroup()) {
            Junction junction = node.isOr() ? Restrictions.disjunction() : Restrictions.conjunction();
            for (EpisodeSearchCriteria child : node.getConditions()) {
                junction.add(buildRestriction(child, aliasResolver));
            }
            return junction;
        }
        return buildLeafRestriction(node, aliasResolver);
    }


    private Criterion buildLeafRestriction(EpisodeSearchCriteria leaf, AliasResolver aliasResolver) {
        FieldMapping mapping = EpisodeSearchFieldRegistry.resolve(leaf.getField());
        String alias = aliasResolver.resolve(mapping);
        String propertyPath = alias == null ? mapping.property : alias + "." + mapping.property;
        Object value = coerce(mapping.valueType, leaf.getValue());
        Criterion userRestriction = toRestriction(propertyPath, leaf.getComparator(), value);

        Criterion discriminator = attributeDiscriminatorRestriction(mapping, alias, aliasResolver);
        return discriminator == null ? userRestriction : Restrictions.and(discriminator, userRestriction);
    }

    /**
     * episode_attribute stores every attribute type in one table, disambiguated by
     * attribute_type_id. destinationCountry/identifierTypeSource fields therefore need a second,
     * user-invisible condition on the same joined row asserting which attribute type it is - see
     * design discussion. Special-cased here for these two known fields rather than generalized
     * into the registry, since there are only two of them today.
     * <p>
     * TODO(tech debt): matching by attributeType.name is fragile - it silently breaks if an
     * implementer renames the "Destination Country" / "Identifier Type Source" attribute type in
     * Manage Episode Attribute Types, and it requires a code change for any new attribute-backed
     * criterion. Preferred fix: generalize this into a single keyType-driven polymorphic field
     * (episode.attribute + keyType = attributeType UUID), mirroring how patient.identifiers
     * already carries its type via field.keyType per the widget config schema. Deferred for now.
     */
    private Criterion attributeDiscriminatorRestriction(FieldMapping mapping, String alias, AliasResolver aliasResolver) {
        String attributeTypeName = attributeTypeNameFor(mapping.joinGroup);
        if (attributeTypeName == null) {
            return null;
        }
        String attributeTypeAlias = aliasResolver.resolveSingleValued(alias, "attributeType");
        return Restrictions.eq(attributeTypeAlias + ".name", attributeTypeName);
    }


    private String attributeTypeNameFor(String joinGroup) {
        if (EpisodeSearchFieldRegistry.JOIN_GROUP_ATTRIBUTE_DESTINATION_COUNTRY.equals(joinGroup)) {
            return "Destination Country";
        }
        if (EpisodeSearchFieldRegistry.JOIN_GROUP_ATTRIBUTE_IDENTIFIER_SOURCE.equals(joinGroup)) {
            return "Identifier Type Source";
        }
        return null;
    }

    private Object coerce(ValueType valueType, String rawValue) {
        switch (valueType) {
            case DATE:
                try {
                    return DATE_FORMAT.parse(rawValue);
                } catch (ParseException e) {
                    throw new IllegalArgumentException("Invalid date value: " + rawValue, e);
                }
            case EPISODE_STATUS:
                return Episode.Status.valueOf(rawValue);
            default:
                return rawValue;
        }
    }

    private Criterion toRestriction(String propertyPath, String comparator, Object value) {
        if (comparator == null) {
            comparator = "eq";
        }
        switch (comparator) {
            case "eq":
                return Restrictions.eq(propertyPath, value);
            case "ne":
                return Restrictions.ne(propertyPath, value);
            case "gt":
                return Restrictions.gt(propertyPath, value);
            case "lt":
                return Restrictions.lt(propertyPath, value);
            case "ge":
                return Restrictions.ge(propertyPath, value);
            case "le":
                return Restrictions.le(propertyPath, value);
            default:
                throw new IllegalArgumentException("Unsupported comparator: " + comparator);
        }
    }
}


