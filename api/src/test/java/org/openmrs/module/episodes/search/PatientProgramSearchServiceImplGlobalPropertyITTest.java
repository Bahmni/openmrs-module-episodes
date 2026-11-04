/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark of OpenMRS
 * and the OpenMRS graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search;

import org.openmrs.module.episodes.service.EpisodeSearchService;

import org.bahmni.search.model.SearchCondition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.episodes.search.dto.EpisodeSearchResponse;
import org.openmrs.module.episodes.search.dto.SearchRequest;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;

/**
 * Platform 2.6.11+ requires the "Get Global Properties" privilege for AdministrationService
 * global-property reads. The search service reads two pagination global properties on every
 * search() call. This test proves whether a user holding only the @Authorized privileges of
 * search() can still complete a search, or whether the GP reads need a proxy privilege.
 */
public class PatientProgramSearchServiceImplGlobalPropertyITTest extends BaseModuleContextSensitiveTest {

    private static final String TEST_PASSWORD = "Admin123!";

    private static final String[] REQUIRED_PRIVILEGES = resolveRequiredPrivileges();

    private static String[] resolveRequiredPrivileges() {
        try {
            return EpisodeSearchService.class
                    .getMethod("search", SearchRequest.class)
                    .getAnnotation(Authorized.class)
                    .value();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not resolve @Authorized privileges from EpisodeSearchService", e);
        }
    }

    @Autowired
    private EpisodeSearchService searchService;

    @Autowired
    private UserService userService;

    private User userWithOnlySearchPrivileges;

    @Before
    public void setUpUserWithOnlySearchPrivileges() {
        // The in-memory test database seeds no privileges at all (the admin user passes
        // authorization only via the superuser bypass), so a role granted "exactly the
        // @Authorized privileges" needs those privilege objects created first.
        for (String priv : REQUIRED_PRIVILEGES) {
            if (userService.getPrivilege(priv) == null) {
                userService.savePrivilege(new Privilege(priv, "Privilege required by EpisodeSearchService.search"));
            }
        }

        Person person = new Person();
        PersonName personName = new PersonName();
        personName.setGivenName("Test");
        personName.setFamilyName("User");
        person.addName(personName);
        person.setGender("M");
        Context.getPersonService().savePerson(person);

        Role role = new Role();
        role.setRole("TestRole_" + UUID.randomUUID());
        role.setDescription("Exactly the @Authorized privileges of EpisodeSearchService.search, nothing more");

        Set<Privilege> privileges = new HashSet<>();
        for (String priv : REQUIRED_PRIVILEGES) {
            Privilege privilege = userService.getPrivilege(priv);
            if (privilege != null) {
                privileges.add(privilege);
            }
        }
        role.setPrivileges(privileges);
        userService.saveRole(role);

        User user = new User(person);
        user.setUsername("test_user_" + UUID.randomUUID());
        user.setSystemId("test_" + UUID.randomUUID());
        user.addRole(role);
        userWithOnlySearchPrivileges = userService.createUser(user, TEST_PASSWORD);
    }

    @After
    public void restoreAdminSession() {
        Context.authenticate("admin", "test");
    }

    @Test
    public void shouldCompleteSearchWithOnlyTheAuthorizedPrivileges() {
        Context.authenticate(userWithOnlySearchPrivileges.getUsername(), TEST_PASSWORD);
        EpisodeSearchResponse response = searchService.search(validRequest());
        assertNotNull("search must complete and return a response for a user holding only the "
                + "@Authorized privileges (the pagination global-property reads must not require "
                + "a privilege the search caller does not have)", response);
    }

    private SearchRequest validRequest() {
        SearchCondition leaf = new SearchCondition();
        leaf.setField("episodeOfCare.startDate");
        leaf.setComparator("gt");
        leaf.setValue("2024-01-01");

        SearchCondition criteria = new SearchCondition();
        criteria.setOperator("and");
        criteria.setConditions(Collections.singletonList(leaf));

        SearchRequest request = new SearchRequest();
        request.setEntity("patientProgram");
        request.setCriteria(criteria);
        return request;
    }
}
