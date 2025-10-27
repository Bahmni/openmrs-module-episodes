package org.openmrs.module.episodes.service.impl;

import org.openmrs.module.episodes.EpisodeAttributeType;

public class TestHelper {
    public static EpisodeAttributeType exampleAttributeTypeInsuranceCaseNumber() {
        EpisodeAttributeType episodeAttributeType = new EpisodeAttributeType();
        episodeAttributeType.setName("Insurance case Number");
        episodeAttributeType.setDescription("Insurance Case Number For Episode");
        episodeAttributeType.setDatatypeClassname("org.openmrs.customdatatype.datatype.FreeTextDatatype");
        episodeAttributeType.setMinOccurs(0);
        return episodeAttributeType;
    }

    public static EpisodeAttributeType exampleAttributeTypeInsuranceId() {
        EpisodeAttributeType episodeAttributeType = new EpisodeAttributeType();
        episodeAttributeType.setName("Insurance Group Id");
        episodeAttributeType.setDescription("Insurance Id associated with Episode");
        episodeAttributeType.setDatatypeClassname("org.openmrs.customdatatype.datatype.FloatDatatype");
        episodeAttributeType.setMinOccurs(0);
        return episodeAttributeType;
    }

    public static EpisodeAttributeType exampleAttributeTypeIsAccidentCase() {
        EpisodeAttributeType episodeAttributeType = new EpisodeAttributeType();
        episodeAttributeType.setName("Is Accident case");
        episodeAttributeType.setDescription("Whether accident case");
        episodeAttributeType.setDatatypeClassname("org.openmrs.customdatatype.datatype.BooleanDatatype");
        episodeAttributeType.setMinOccurs(0);
        return episodeAttributeType;
    }
}
