package org.openmrs.module.episodes.search;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EpisodeSearchFieldRegistry {

    public static final String JOIN_GROUP_ROOT = "episode";
    public static final String JOIN_GROUP_PROGRAM = "patientProgram";
    public static final String JOIN_GROUP_STATUS_HISTORY = "statusHistory";
    public static final String JOIN_GROUP_IDENTIFIER = "patientIdentifier";
    public static final String JOIN_GROUP_ATTRIBUTE_DESTINATION_COUNTRY = "attribute.destinationCountry";
    public static final String JOIN_GROUP_ATTRIBUTE_IDENTIFIER_SOURCE = "attribute.identifierTypeSource";

    public enum Root {
        EPISODE,
        PATIENT
    }

    public enum ValueType {
        STRING,
        DATE,
        EPISODE_STATUS
    }

    public static class FieldMapping {
        public final String joinGroup;
        public final Root root;
        public final List<String> associationPath;
        public final String property;
        public final ValueType valueType;

        FieldMapping(String joinGroup, Root root, List<String> associationPath, String property, ValueType valueType) {
            this.joinGroup = joinGroup;
            this.root = root;
            this.associationPath = Collections.unmodifiableList(associationPath);
            this.property = property;
            this.valueType = valueType;
        }
    }

    private static final Map<String, FieldMapping> REGISTRY = new HashMap<>();

    private static void register(String field, String joinGroup, Root root, String property, ValueType valueType, String... hops) {
        REGISTRY.put(field, new FieldMapping(joinGroup, root, Arrays.asList(hops), property, valueType));
    }

    private static void register(String field, String joinGroup, Root root, String property, String... hops) {
        register(field, joinGroup, root, property, ValueType.STRING, hops);
    }

    static {
        register("episode.name", JOIN_GROUP_PROGRAM, Root.EPISODE, "name", "patientPrograms", "program");
        register("episode.type", JOIN_GROUP_ROOT, Root.EPISODE, "uuid", "episodeType");

        register("episode.dateStarted", JOIN_GROUP_ROOT, Root.EPISODE, "dateStarted", ValueType.DATE);
        register("episode.dateEnded", JOIN_GROUP_ROOT, Root.EPISODE, "dateEnded", ValueType.DATE);
        register("episode.mhac", JOIN_GROUP_PROGRAM, Root.EPISODE, "uuid", "patientPrograms", "location");
        register("episode.careManager", JOIN_GROUP_ROOT, Root.EPISODE, "uuid", "careManager");
        register("episode.status", JOIN_GROUP_ROOT, Root.EPISODE, "status", ValueType.EPISODE_STATUS);
        register("episode.statusDate.from", JOIN_GROUP_STATUS_HISTORY, Root.EPISODE, "dateStarted", ValueType.DATE, "statusHistory");
        register("episode.statusDate.to", JOIN_GROUP_STATUS_HISTORY, Root.EPISODE, "dateEnded", ValueType.DATE, "statusHistory");
        register("patient.identifiers.kind", JOIN_GROUP_IDENTIFIER, Root.PATIENT, "name", "identifiers", "identifierType");
        register("patient.identifiers.value", JOIN_GROUP_IDENTIFIER, Root.PATIENT, "identifier", "identifiers");
        register("episode.attribute.destinationCountry", JOIN_GROUP_ATTRIBUTE_DESTINATION_COUNTRY, Root.EPISODE,
                "valueReference", "attributes");
        register("episode.attribute.identifierTypeSource", JOIN_GROUP_ATTRIBUTE_IDENTIFIER_SOURCE, Root.EPISODE,
                "valueReference", "attributes");
    }

    private EpisodeSearchFieldRegistry() {
    }

    public static FieldMapping resolve(String field) {
        FieldMapping mapping = REGISTRY.get(field);

        if (mapping == null) {
            throw new IllegalArgumentException("Unsupported search field: " + field);
        }
        return mapping;
    }

    public static boolean isSupported(String field) {
        return REGISTRY.containsKey(field);
    }
}
