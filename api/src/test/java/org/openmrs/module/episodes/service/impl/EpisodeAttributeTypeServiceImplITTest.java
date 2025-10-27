package org.openmrs.module.episodes.service.impl;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.episodes.EpisodeAttributeType;
import org.openmrs.module.episodes.service.EpisodeAttributeTypeService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class EpisodeAttributeTypeServiceImplITTest extends BaseModuleContextSensitiveTest {

    @Autowired
    private EpisodeAttributeTypeService attributeTypeService;

    @Test
    public void shouldCreateNewEpisodeAttributeType() {
        EpisodeAttributeType caseNumberAttributeType = exampleAttributeTypeInsuranceCase();
        EpisodeAttributeType attributeType = attributeTypeService.save(caseNumberAttributeType);
        Assert.assertTrue("Created episode attribute must have a UUID", !attributeType.getUuid().isEmpty());
    }

    @Test
    public void getAttributeTypeByUuid() {
        EpisodeAttributeType caseAttributeType = attributeTypeService.save(exampleAttributeTypeInsuranceCase());
        EpisodeAttributeType insuranceIdAttributeType = attributeTypeService.save(exampleAttributeTypeInsuranceId());
        EpisodeAttributeType attributeType = attributeTypeService.getAttributeTypeByUuid(caseAttributeType.getUuid());
        Assert.assertEquals(caseAttributeType, attributeType);

    }

    @Test
    public void shouldGetMultipleNonRetiredEpisodeAttributeTypes() {
        attributeTypeService.save(exampleAttributeTypeInsuranceCase());
        attributeTypeService.save(exampleAttributeTypeInsuranceId());
        List<EpisodeAttributeType> allAttributeTypes = attributeTypeService.getAllAttributeTypes(false);
        Assert.assertEquals(2, allAttributeTypes.size());
    }

    @Test
    public void shouldRetireAnEpisodeAttributeType() {
        EpisodeAttributeType caseAttributeType = attributeTypeService.save(exampleAttributeTypeInsuranceCase());
        Assert.assertTrue(!caseAttributeType.getRetired());
        EpisodeAttributeType retired = attributeTypeService.retire(caseAttributeType, "entered-in-error");
        Assert.assertTrue(retired.getRetired());
    }

    @Test
    public void shouldSearchEpisodeAttributeTypesByName() {
        attributeTypeService.save(exampleAttributeTypeInsuranceCase());
        attributeTypeService.save(exampleAttributeTypeInsuranceId());
        List<EpisodeAttributeType> allAttributeTypes = attributeTypeService.getAttributesByName("Insurance");
        Assert.assertEquals(2, allAttributeTypes.size());
        List<EpisodeAttributeType> matches = attributeTypeService.getAttributesByName("Insurance case");
        Assert.assertEquals(1, matches.size());
    }

    private EpisodeAttributeType exampleAttributeTypeInsuranceCase() {
        EpisodeAttributeType episodeAttributeType = new EpisodeAttributeType();
        episodeAttributeType.setName("Insurance case Number");
        episodeAttributeType.setDescription("Insurance Case Number For Episode");
        episodeAttributeType.setDatatypeClassname("org.openmrs.customdatatype.datatype.FreeTextDatatype");
        episodeAttributeType.setMinOccurs(0);
        return episodeAttributeType;
    }

    private EpisodeAttributeType exampleAttributeTypeInsuranceId() {
        EpisodeAttributeType episodeAttributeType = new EpisodeAttributeType();
        episodeAttributeType.setName("Insurance ID");
        episodeAttributeType.setDescription("Insurance ID associated with Episode");
        episodeAttributeType.setDatatypeClassname("org.openmrs.customdatatype.datatype.FloatDatatype");
        episodeAttributeType.setMinOccurs(0);
        return episodeAttributeType;
    }
}