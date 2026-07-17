package org.openmrs.module.episodes.dao.impl;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientProgram;
import org.openmrs.Provider;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.ProviderService;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.EpisodeAttribute;
import org.openmrs.module.episodes.EpisodeAttributeType;
import org.openmrs.module.episodes.dao.EpisodeDAO;
import org.openmrs.module.episodes.dao.EpisodeSearchDao;
import org.openmrs.module.episodes.service.EpisodeAttributeTypeService;
import org.openmrs.module.episodes.search.EpisodeSearchCriteria;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class EpisodeSearchDaoImplTest extends BaseModuleContextSensitiveTest {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    private EpisodeSearchDao episodeSearchDao;

    @Autowired
    private EpisodeDAO episodeDAO;

    @Autowired
    private ProgramWorkflowService programWorkflowService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private ProviderService providerService;

    @Autowired
    private EpisodeAttributeTypeService episodeAttributeTypeService;

    private Episode activeEpisodeWithHivProgram;
    private Episode onHoldEpisodeWithMdrTbProgram;

    @Before
    public void setUp() {
        activeEpisodeWithHivProgram = new Episode();
        activeEpisodeWithHivProgram.setStatus(Episode.Status.ACTIVE);
        activeEpisodeWithHivProgram.setDateStarted(parseDate("2024-01-10"));
        activeEpisodeWithHivProgram.setPatient(patientService.getPatient(2));
        activeEpisodeWithHivProgram.addPatientProgram(programWorkflowService.getPatientProgram(1));
        activeEpisodeWithHivProgram.setCareManager(providerService.getProvider(1));
        episodeDAO.save(activeEpisodeWithHivProgram);

        onHoldEpisodeWithMdrTbProgram = new Episode();
        onHoldEpisodeWithMdrTbProgram.setStatus(Episode.Status.ONHOLD);
        onHoldEpisodeWithMdrTbProgram.setDateStarted(parseDate("2024-02-15"));
        onHoldEpisodeWithMdrTbProgram.setPatient(patientService.getPatient(2));
        onHoldEpisodeWithMdrTbProgram.addPatientProgram(programWorkflowService.getPatientProgram(2));
        episodeDAO.save(onHoldEpisodeWithMdrTbProgram);
    }

    private Date parseDate(String value) {
        try {
            return DATE_FORMAT.parse(value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private EpisodeSearchCriteria leaf(String field, String comparator, String value) {
        EpisodeSearchCriteria criteria = new EpisodeSearchCriteria();
        criteria.setField(field);
        criteria.setComparator(comparator);
        criteria.setValue(value);
        return criteria;
    }

    private EpisodeSearchCriteria group(String operator, EpisodeSearchCriteria... conditions) {
        EpisodeSearchCriteria criteria = new EpisodeSearchCriteria();
        criteria.setOperator(operator);
        criteria.setConditions(new ArrayList<>(Arrays.asList(conditions)));
        return criteria;
    }

    @Test
    public void shouldReturnAllNonVoidedEpisodesWhenCriteriaIsNull() {
        List<Episode> results = episodeSearchDao.search(null);

        assertThat(results, hasSize(2));
        assertThat(results, containsInAnyOrder(activeEpisodeWithHivProgram, onHoldEpisodeWithMdrTbProgram));
    }

    @Test
    public void shouldFilterByASingleEqualityLeafCondition() {
        EpisodeSearchCriteria criteria = leaf("episode.status", "eq", "ACTIVE");

        List<Episode> results = episodeSearchDao.search(criteria);

        assertThat(results, hasSize(1));
        assertThat(results.get(0), is(activeEpisodeWithHivProgram));
    }

    @Test
    public void shouldCombineSiblingConditionsWithAnd() {
        EpisodeSearchCriteria criteria = group("AND",
                leaf("episode.status", "eq", "ACTIVE"),
                leaf("episode.dateStarted", "ge", "2024-01-01"));

        List<Episode> results = episodeSearchDao.search(criteria);

        assertThat(results, hasSize(1));
        assertThat(results.get(0), is(activeEpisodeWithHivProgram));
    }

    @Test
    public void shouldReturnNoResultsWhenAndConditionsCannotBothBeSatisfied() {
        EpisodeSearchCriteria criteria = group("AND",
                leaf("episode.status", "eq", "ACTIVE"),
                leaf("episode.status", "eq", "ONHOLD"));

        List<Episode> results = episodeSearchDao.search(criteria);

        assertThat(results, empty());
    }

    @Test
    public void shouldCombineSiblingConditionsWithOr() {
        EpisodeSearchCriteria criteria = group("OR",
                leaf("episode.status", "eq", "ACTIVE"),
                leaf("episode.status", "eq", "ONHOLD"));

        List<Episode> results = episodeSearchDao.search(criteria);

        assertThat(results, hasSize(2));
        assertThat(results, containsInAnyOrder(activeEpisodeWithHivProgram, onHoldEpisodeWithMdrTbProgram));
    }

    @Test
    public void shouldFilterUsingADateRangeExpressedAsTwoAndedComparators() {
        EpisodeSearchCriteria criteria = group("AND",
                leaf("episode.dateStarted", "ge", "2024-02-01"),
                leaf("episode.dateStarted", "le", "2024-02-28"));

        List<Episode> results = episodeSearchDao.search(criteria);

        assertThat(results, hasSize(1));
        assertThat(results.get(0), is(onHoldEpisodeWithMdrTbProgram));
    }

    @Test
    public void shouldFilterByProgrammeNameThroughTheJoinToPatientProgramAndProgram() {
        EpisodeSearchCriteria criteria = leaf("episode.name", "eq", "MDR-TB PROGRAM");

        List<Episode> results = episodeSearchDao.search(criteria);

        assertThat(results, hasSize(1));
        assertThat(results.get(0), is(onHoldEpisodeWithMdrTbProgram));
    }

    @Test
    public void shouldFilterByCareManagerUuid() {
        Provider careManager = providerService.getProvider(1);
        EpisodeSearchCriteria criteria = leaf("episode.careManager", "eq", careManager.getUuid());

        List<Episode> results = episodeSearchDao.search(criteria);

        assertThat(results, hasSize(1));
        assertThat(results.get(0), is(activeEpisodeWithHivProgram));
    }

    @Test
    public void shouldMatchAnIdentifierKindAndValuePairOnTheSameJoinedRow() {
        PatientIdentifierType identifierType = patientService.getPatientIdentifierType(1);
        EpisodeSearchCriteria criteria = group("AND",
                leaf("patient.identifiers.kind", "eq", identifierType.getName()),
                leaf("patient.identifiers.value", "eq", "101-6"));

        List<Episode> results = episodeSearchDao.search(criteria);

        assertThat(results, hasSize(2));
        assertThat(results, containsInAnyOrder(activeEpisodeWithHivProgram, onHoldEpisodeWithMdrTbProgram));
    }

    @Test
    public void shouldNotMatchWhenIdentifierKindAndValueComeFromDifferentIdentifierRows() {
        PatientIdentifierType wrongIdentifierType = patientService.getPatientIdentifierType(2);
        EpisodeSearchCriteria criteria = group("AND",
                leaf("patient.identifiers.kind", "eq", wrongIdentifierType.getName()),
                leaf("patient.identifiers.value", "eq", "101-6"));

        List<Episode> results = episodeSearchDao.search(criteria);

        assertThat(results, empty());
    }

    @Test
    public void shouldTreatEachSiblingOrBranchAsMatchingAPotentiallyDifferentIdentifierRow() {
        EpisodeSearchCriteria criteria = group("OR",
                group("AND",
                        leaf("patient.identifiers.kind", "eq", "Old Identification Number"),
                        leaf("patient.identifiers.value", "eq", "101")),
                group("AND",
                        leaf("patient.identifiers.kind", "eq", "OpenMRS Identification Number"),
                        leaf("patient.identifiers.value", "eq", "101-6")));

        List<Episode> results = episodeSearchDao.search(criteria);

        assertThat(results, hasSize(2));
        assertThat(results, containsInAnyOrder(activeEpisodeWithHivProgram, onHoldEpisodeWithMdrTbProgram));
    }

    @Test
    public void shouldFilterEpisodesByDestinationCountryAttributeWithoutMatchingOtherAttributeTypes() {
        EpisodeAttributeType destinationCountryType = new EpisodeAttributeType();
        destinationCountryType.setName("Destination Country");
        destinationCountryType.setDatatypeClassname("org.openmrs.customdatatype.datatype.FreeTextDatatype");
        destinationCountryType.setMinOccurs(0);
        episodeAttributeTypeService.save(destinationCountryType);

        EpisodeAttributeType identifierSourceType = new EpisodeAttributeType();
        identifierSourceType.setName("Identifier Type Source");
        identifierSourceType.setDatatypeClassname("org.openmrs.customdatatype.datatype.FreeTextDatatype");
        identifierSourceType.setMinOccurs(0);
        episodeAttributeTypeService.save(identifierSourceType);

        addAttribute(activeEpisodeWithHivProgram, destinationCountryType, "New Zealand");
        addAttribute(activeEpisodeWithHivProgram, identifierSourceType, "New Zealand");
        addAttribute(onHoldEpisodeWithMdrTbProgram, destinationCountryType, "Fiji");
        episodeDAO.save(activeEpisodeWithHivProgram);
        episodeDAO.save(onHoldEpisodeWithMdrTbProgram);

        EpisodeSearchCriteria criteria = leaf("episode.attribute.destinationCountry", "eq", "New Zealand");

        List<Episode> results = episodeSearchDao.search(criteria);

        assertThat(results, hasSize(1));
        assertThat(results.get(0), is(activeEpisodeWithHivProgram));
    }

    private void addAttribute(Episode episode, EpisodeAttributeType attributeType, String value) {
        EpisodeAttribute attribute = new EpisodeAttribute();
        attribute.setAttributeType(attributeType);
        attribute.setValueReferenceInternal(value);
        episode.addAttribute(attribute);
    }
}
