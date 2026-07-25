/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search;

import org.openmrs.module.episodes.service.SearchService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.episodes.search.model.SearchCondition;
import org.openmrs.module.episodes.search.model.SearchRequest;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.fail;

public class PatientProgramSearchServiceImplPrivilegeITTest extends BaseModuleContextSensitiveTest {

    private static final String TEST_PASSWORD = "Admin123!";

    private static final String[] REQUIRED_PRIVILEGES = resolveRequiredPrivileges();

    private static String[] resolveRequiredPrivileges() {
        try {
            return SearchService.class
                    .getMethod("search", SearchRequest.class)
                    .getAnnotation(Authorized.class)
                    .value();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not resolve @Authorized privileges from SearchService", e);
        }
    }

    @Autowired
    private SearchService searchService;

    @Autowired
    private UserService userService;

    private final Map<String, User> usersWithoutPrivilege = new HashMap<>();

    @Before
    public void setUpRestrictedUsers() {
        for (String privilege : REQUIRED_PRIVILEGES) {
            usersWithoutPrivilege.put(privilege, createUserWithout(privilege));
        }
    }

    @After
    public void restoreAdminSession() {
        Context.authenticate("admin", "test");
    }

    @Test
    public void shouldDenyAccessWhenEachRequiredPrivilegeIsMissing() {
        for (String missingPrivilege : REQUIRED_PRIVILEGES) {
            User user = usersWithoutPrivilege.get(missingPrivilege);
            Context.authenticate(user.getUsername(), TEST_PASSWORD);
            try {
                searchService.search(validRequest());
                fail("Expected APIAuthenticationException when missing privilege: " + missingPrivilege);
            } catch (APIAuthenticationException e) {
                // expected
            }
            Context.authenticate("admin", "test");
        }
    }

    private User createUserWithout(String missingPrivilege) {
        Person person = new Person();
        PersonName personName = new PersonName();
        personName.setGivenName("Test");
        personName.setFamilyName("User");
        person.addName(personName);
        person.setGender("M");
        Context.getPersonService().savePerson(person);

        Role role = new Role();
        role.setRole("TestRole_" + UUID.randomUUID());
        role.setDescription("All required privileges except: " + missingPrivilege);

        Set<Privilege> privileges = new HashSet<>();
        for (String priv : REQUIRED_PRIVILEGES) {
            if (!priv.equals(missingPrivilege)) {
                Privilege privilege = userService.getPrivilege(priv);
                if (privilege != null) {
                    privileges.add(privilege);
                }
            }
        }
        role.setPrivileges(privileges);
        userService.saveRole(role);

        User user = new User(person);
        user.setUsername("test_user_" + UUID.randomUUID());
        user.setSystemId("test_" + UUID.randomUUID());
        user.addRole(role);
        userService.createUser(user, TEST_PASSWORD);
        return user;
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
