package org.openmrs.module.episodes.service.impl;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.episodes.EpisodeAttributeType;
import org.openmrs.module.episodes.service.EpisodeAttributeTypeService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.openmrs.module.episodes.service.impl.TestHelper.exampleAttributeTypeInsuranceCaseNumber;
import static org.openmrs.module.episodes.service.impl.TestHelper.exampleAttributeTypeInsuranceId;
import static org.openmrs.module.episodes.service.impl.TestHelper.exampleAttributeTypeIsAccidentCase;

public class EpisodeAttributeTypeServiceImplITTest extends BaseModuleContextSensitiveTest {

    @Autowired
    private EpisodeAttributeTypeService attributeTypeService;

    @Test
    public void shouldCreateNewEpisodeAttributeType() {
        EpisodeAttributeType caseNumberAttributeType = exampleAttributeTypeInsuranceCaseNumber();
        EpisodeAttributeType attributeType = attributeTypeService.save(caseNumberAttributeType);
        Assert.assertTrue("Created episode attribute must have a UUID", !attributeType.getUuid().isEmpty());
    }

    @Test
    public void getAttributeTypeByUuid() {
        EpisodeAttributeType caseAttributeType = attributeTypeService.save(exampleAttributeTypeInsuranceCaseNumber());
        EpisodeAttributeType insuranceIdAttributeType = attributeTypeService.save(exampleAttributeTypeInsuranceId());
        EpisodeAttributeType attributeType = attributeTypeService.getAttributeTypeByUuid(caseAttributeType.getUuid());
        Assert.assertEquals(caseAttributeType, attributeType);

    }

    @Test
    public void shouldGetMultipleNonRetiredEpisodeAttributeTypes() {
        attributeTypeService.save(exampleAttributeTypeInsuranceCaseNumber());
        attributeTypeService.save(exampleAttributeTypeInsuranceId());
        List<EpisodeAttributeType> allAttributeTypes = attributeTypeService.getAllAttributeTypes(false);
        Assert.assertEquals(2, allAttributeTypes.size());
    }

    @Test
    public void shouldRetireAnEpisodeAttributeType() {
        EpisodeAttributeType caseAttributeType = attributeTypeService.save(exampleAttributeTypeInsuranceCaseNumber());
        Assert.assertTrue(!caseAttributeType.getRetired());
        EpisodeAttributeType retired = attributeTypeService.retire(caseAttributeType, "entered-in-error");
        Assert.assertTrue(retired.getRetired());
    }

    @Test
    public void shouldSearchEpisodeAttributeTypesByName() {
        attributeTypeService.save(exampleAttributeTypeInsuranceCaseNumber());
        attributeTypeService.save(exampleAttributeTypeInsuranceId());
        attributeTypeService.save(exampleAttributeTypeIsAccidentCase());
        List<EpisodeAttributeType> allAttributeTypes = attributeTypeService.getAttributesByName("Insurance");
        Assert.assertEquals(2, allAttributeTypes.size());
        List<EpisodeAttributeType> matches = attributeTypeService.getAttributesByName("Insurance case");
        Assert.assertEquals(1, matches.size());
    }




}