package com.kc1vmz.netlog;

/*
    NetLog
    Copyright (c) 2026 John Rokicki KC1VMZ

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
    
    http://www.kc1vmz.com
*/

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.kc1vmz.netlog.controller.UIController;
import com.kc1vmz.netlog.enums.*;
import com.kc1vmz.netlog.record.*;
import com.kc1vmz.netlog.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.DynamicTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MigrationIntegrationTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 11, 2, 19, 30);

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) throws Exception {
        Path database = Files.createTempFile("netlog-migration-", ".db");
        database.toFile().deleteOnExit();
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + database);
    }

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private SectionRepository sections;
    @Autowired
    private OperatorRepository operators;
    @Autowired
    private RecurringEventRepository recurringEvents;
    @Autowired
    private EventRepository events;
    @Autowired
    private ParticipantRepository participants;
    @Autowired
    private OperatorSectionRepository memberships;
    @Autowired
    private LocationCountryRepository countries;
    @Autowired
    private LocationStateRepository states;
    @Autowired
    private LocationCountyRepository counties;
    @Autowired
    private LocationMunicipalityRepository municipalities;
    @Autowired
    private DistrictRepository districts;
    @Autowired
    private DistrictLocationRepository districtLocations;

    @BeforeEach
    void seed() {
        for (String table : List.of("participant", "event", "recurring_event", "operator_section",
                "operator", "district_location", "district", "section", "location_municipality",
                "location_county", "location_state", "location_country")) {
            jdbc.update("DELETE FROM " + table);
        }
        countries.save(new LocationCountryRecord("country", "United States", "US"));
        states.save(new LocationStateRecord("state", "country", "Massachusetts", "MA"));
        counties.save(new LocationCountyRecord("county", "country", "state", "Test County"));
        municipalities.save(new LocationMunicipalityRecord("location", "country", "state", "county", "Test Town"));
        sections.save(new SectionRecord("section", "Test Section", "Description", true));
        operators.save(new OperatorRecord("operator", "KC1VMZ", "Test <operator>", true, true, true, "location"));
        operators.save(new OperatorRecord("unlocated", "W1AW", "No location", false, false, false, null));
        memberships.save(new OperatorSectionRecord("membership", "section", "operator", MembershipType.LEADERSHIP));
        recurringEvents.save(new RecurringEventRecord("recurring", "section", "Weekly Net", "Description",
                EventType.NET, "Monday", "Radio", "KC1VMZ", true));
        events.save(new EventRecord("event", "Test Event", "Description", "recurring", "section", EventType.NET,
                "Radio", START, null, false, "KC1VMZ", true, EventState.STARTED.ordinal()));
        participants.save(new ParticipantRecord("participant", "event", "operator", START, null,
                ElectricalPowerType.BATTERY, ElectricalPowerType.COMMERCIAL, 50,
                CommunicationPlatform.UNKNOWN, CommunicationBand.UNKNOWN));
        districts.save(new DistrictRecord("district", "section", "Test District", "Description", "Leader", "Contact"));
        districtLocations.save(new DistrictLocationRecord("district-location", "section", "district", "location"));
    }

    @Test
    void sectionOperatorsIncludeResidentsAcrossAllDistrictsWithoutDuplicates() throws Exception {
        districts.save(new DistrictRecord("second-district", "section", "Second District", "Description", "", ""));
        municipalities.save(new LocationMunicipalityRecord("second-town", "country", "state", "county", "Second Town"));
        districtLocations.save(new DistrictLocationRecord("second-town-link", "section", "second-district", "second-town"));
        districtLocations.save(new DistrictLocationRecord("shared-town-link", "section", "second-district", "location"));
        operators.save(new OperatorRecord("section-resident-member", "W1MEM", "Member resident", true, false, true, "second-town"));
        operators.save(new OperatorRecord("section-resident-nonmember", "W1NON", "Nonmember resident", false, true, false, "location"));
        memberships.save(new OperatorSectionRecord("resident-membership", "section", "section-resident-member", MembershipType.MEMBER));
        sections.save(new SectionRecord("other-section", "Other Section", "Description", true));
        districts.save(new DistrictRecord("other-district", "other-section", "Other District", "Description", "", ""));
        municipalities.save(new LocationMunicipalityRecord("outside-town", "country", "state", "county", "Outside Town"));
        districtLocations.save(new DistrictLocationRecord("outside-town-link", "other-section", "other-district", "outside-town"));
        operators.save(new OperatorRecord("outside-operator", "W1OUT", "Outside resident", false, false, false, "outside-town"));
        memberships.save(new OperatorSectionRecord("outside-membership", "section", "outside-operator", MembershipType.MEMBER));
        operators.save(new OperatorRecord("other-section-resident", "W1OTHER", "Other section leader", false, false, true, "second-town"));
        memberships.save(new OperatorSectionRecord("other-membership", "other-section", "other-section-resident", MembershipType.LEADERSHIP));

        String page = mvc.perform(get("/section/section")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(page).contains("Operators (4 total)", "KC1VMZ", "W1MEM", "W1NON", "W1OTHER",
                "<th>NTS</th>", "<th>RACES</th>", "<th>SKYWARN</th>", "<td>Yes</td>", "<td>No</td>")
                .doesNotContain("W1OUT", "No location", "<td>true</td>", "<td>false</td>");
        for (var expected : java.util.Map.of("operator", "Leadership", "section-resident-member", "Member",
                "section-resident-nonmember", "Non-member", "other-section-resident", "Non-member").entrySet()) {
            int start = page.indexOf("<tr id=\"" + expected.getKey() + "\"");
            assertThat(start).isGreaterThanOrEqualTo(0);
            assertThat(page.substring(start, page.indexOf("</tr>", start)))
                    .contains("<td>" + expected.getValue() + "</td>");
        }
        mvc.perform(get("/api/v1/sections/section/operators")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("W1OUT")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("W1NON"))));
        jdbc.update("DELETE FROM operator_section WHERE section_id = 'section'");
        String withoutMemberships = mvc.perform(get("/section/section")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(withoutMemberships).contains("Operators (4 total)", "KC1VMZ", "W1MEM", "W1NON", "W1OTHER")
                .doesNotContain("W1OUT");
        assertThat(memberships.findBysectionId("section")).isEmpty();
        sections.save(new SectionRecord("no-districts", "No districts", "Description", true));
        mvc.perform(get("/section/no-districts")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Operators (0 total)")));
    }

    @Test
    void districtOperatorsIncludeAllResidentsAndUseMembershipOnlyForTheirLabels() throws Exception {
        municipalities.save(new LocationMunicipalityRecord("second-town", "country", "state", "county", "Second Town"));
        districtLocations.save(new DistrictLocationRecord("second-district-town", "section", "district", "second-town"));
        operators.save(new OperatorRecord("district-member", "W1MEM", "Member", false, true, false, "second-town"));
        operators.save(new OperatorRecord("district-nonmember", "W1NON", "Nonmember", true, false, true, "location"));
        operators.save(new OperatorRecord("other-section-member", "W1OTHER", "Other section leader", false, false, false, "second-town"));
        memberships.save(new OperatorSectionRecord("district-membership", "section", "district-member", MembershipType.MEMBER));
        sections.save(new SectionRecord("other-section", "Other Section", "Description", true));
        memberships.save(new OperatorSectionRecord("other-section-membership", "other-section", "other-section-member", MembershipType.LEADERSHIP));
        municipalities.save(new LocationMunicipalityRecord("outside-town", "country", "state", "county", "Outside Town"));
        operators.save(new OperatorRecord("outside-resident", "W1OUT", "Outside district", false, false, false, "outside-town"));
        memberships.save(new OperatorSectionRecord("outside-membership", "section", "outside-resident", MembershipType.MEMBER));

        String page = mvc.perform(get("/district/district")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(page).contains("Operators (4 total)", "KC1VMZ", "W1MEM", "W1NON", "W1OTHER",
                "<th>NTS</th>", "<th>RACES</th>", "<th>SKYWARN</th>", "<td>Yes</td>", "<td>No</td>",
                "aria-label=\"District properties\"", "aria-label=\"District editing actions\"",
                "aria-label=\"District navigation\"")
                .doesNotContain("W1OUT", "No location", "<td>true</td>", "<td>false</td>", "NTS?", "Skywarn?");
        for (var expected : java.util.Map.of("operator", "Leadership", "district-member", "Member",
                "district-nonmember", "Non-member", "other-section-member", "Non-member").entrySet()) {
            int start = page.indexOf("<tr id=\"" + expected.getKey() + "\"");
            assertThat(start).isGreaterThanOrEqualTo(0);
            String row = page.substring(start, page.indexOf("</tr>", start));
            assertThat(row).contains("<td>" + expected.getValue() + "</td>");
        }
        assertThat(page.indexOf("KC1VMZ")).isLessThan(page.indexOf("W1MEM"));
        assertThat(page.indexOf("W1MEM")).isLessThan(page.indexOf("W1NON"));
        assertThat(memberships.findBysectionId("section")).hasSize(3);

        jdbc.update("DELETE FROM operator_section WHERE section_id = 'section'");
        String withoutMemberships = mvc.perform(get("/district/district")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(withoutMemberships).contains("Operators (4 total)", "KC1VMZ", "W1MEM", "W1NON", "W1OTHER")
                .doesNotContain("W1OUT");
        for (String operatorId : List.of("operator", "district-member", "district-nonmember", "other-section-member")) {
            int start = withoutMemberships.indexOf("<tr id=\"" + operatorId + "\"");
            String row = withoutMemberships.substring(start, withoutMemberships.indexOf("</tr>", start));
            assertThat(row).contains("<td>Non-member</td>");
        }
        assertThat(memberships.findBysectionId("section")).isEmpty();
    }

    @Test
    void editingSectionPropertiesWithoutMembershipDataPreservesSectionAndDistrictOperators() throws Exception {
        mvc.perform(post("/section-edit-action/section").param("name", "Updated Section")
                        .param("description", "Updated description").param("leaderName", "New leader")
                        .param("leaderContact", "leader@example.test"))
                .andExpect(status().isSeeOther());
        assertThat(memberships.findBysectionId("section")).hasSize(1);
        assertThat(memberships.findById("membership").orElseThrow().membershipType()).isEqualTo(MembershipType.LEADERSHIP);
        for (String route : List.of("/section/section", "/district/district")) {
            mvc.perform(get(route)).andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("KC1VMZ")))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Operators (1 total)")));
        }
    }

    @Test
    void sectionEditFormPreservesUnchangedMembersAndStillAllowsExplicitMembershipChanges() throws Exception {
        String firstId = "11111111-1111-4111-8111-111111111111";
        String secondId = "22222222-2222-4222-8222-222222222222";
        jdbc.update("DELETE FROM operator_section");
        operators.save(new OperatorRecord(firstId, "W1KEEP", "Keep operator", true, true, true, "location"));
        operators.save(new OperatorRecord(secondId, "W1ADD", "Add operator", false, false, false, "location"));
        memberships.save(new OperatorSectionRecord("keep-membership", "section", firstId, MembershipType.LEADERSHIP));
        String form = mvc.perform(get("/section-edit/section")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var field = java.util.regex.Pattern.compile("<input[^>]*name=\"callsignMemberIds\"[^>]*value=\"([^\"]*)\"")
                .matcher(form);
        assertThat(field.find()).isTrue();
        String selection = field.group(1);
        assertThat(selection).isEqualTo(firstId + "3");
        assertThat(form).contains("<button type=\"button\"", "newButton.type = 'button'");
        mvc.perform(post("/section-edit-action/section").param("name", "Test Section")
                        .param("description", "Description").param("callsignMemberIds", selection))
                .andExpect(status().isSeeOther());
        assertThat(memberships.findById("keep-membership")).isPresent();
        for (String route : List.of("/section/section", "/district/district")) {
            mvc.perform(get(route)).andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("W1KEEP")));
        }
        for (String invalid : List.of("bad-selection", firstId + "9", "33333333-3333-4333-8333-3333333333332")) {
            mvc.perform(post("/section-edit-action/section").param("name", "Must not save")
                            .param("description", "Description").param("callsignMemberIds", invalid))
                    .andExpect(status().isBadRequest());
            assertThat(memberships.findById("keep-membership")).isPresent();
            assertThat(sections.findById("section").orElseThrow().name()).isEqualTo("Test Section");
        }
        mvc.perform(post("/section-edit-action/section").param("name", "Test Section")
                        .param("description", "Description").param("callsignMemberIds", selection + secondId + "2"))
                .andExpect(status().isSeeOther());
        assertThat(memberships.findBysectionId("section")).hasSize(2);
        mvc.perform(post("/section-edit-action/section").param("name", "Test Section")
                        .param("description", "Description").param("callsignMemberIds", secondId + "2"))
                .andExpect(status().isSeeOther());
        assertThat(memberships.findBysectionId("section")).hasSize(1);
        assertThat(memberships.findBysectionId("section").getFirst().operatorId()).isEqualTo(secondId);
        mvc.perform(post("/section-edit-action/section").param("name", "Test Section")
                        .param("description", "Description").param("callsignMemberIds", ""))
                .andExpect(status().isSeeOther());
        assertThat(memberships.findBysectionId("section")).isEmpty();
        assertThat(operators.findById(firstId)).isPresent();
        assertThat(operators.findById(secondId)).isPresent();
    }

    @Test
    void sectionLeadershipFormsPersistDisplayAndValidateOptionalFields() throws Exception {
        for (String route : List.of("/section-add", "/section-edit/section")) {
            String form = mvc.perform(get(route)).andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            assertThat(form).contains("name=\"leaderName\"", "name=\"leaderContact\"", "maxlength=\"100\"");
        }
        String redirect = mvc.perform(post("/section-add-action")
                        .param("name", "New leadership section").param("description", "Description")
                        .param("leaderName", "Pat Leader").param("leaderContact", "pat@example.test"))
                .andExpect(status().isSeeOther()).andReturn().getResponse().getHeader("Location");
        mvc.perform(get(redirect)).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Pat Leader")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("pat@example.test")));
        mvc.perform(post("/section-edit-action/section")
                        .param("name", "Test Section").param("description", "Description")
                        .param("leaderName", "Updated Leader").param("leaderContact", "Radio or phone"))
                .andExpect(status().isSeeOther());
        SectionRecord saved = sections.findById("section").orElseThrow();
        assertThat(saved.leaderName()).isEqualTo("Updated Leader");
        assertThat(saved.leaderContact()).isEqualTo("Radio or phone");
        String edit = mvc.perform(get("/section-edit/section")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(edit).contains("value=\"Updated Leader\"", "value=\"Radio or phone\"");
        mvc.perform(post("/section-edit-action/section").param("name", "Test Section")
                        .param("description", "Description").param("leaderName", "x".repeat(101)))
                .andExpect(status().isBadRequest());
        assertThat(sections.findById("section").orElseThrow().leaderName()).isEqualTo("Updated Leader");
    }

    @Test
    void sectionLeadershipApiSupportsOlderClientsAndExplicitClearing() throws Exception {
        String body = mvc.perform(post("/api/v1/sections").contentType("application/json")
                        .content("""
                                {"name":"API section","description":"Description","leaderName":"API Leader",
                                 "leaderContact":"leader@example.test"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.leaderName").value("API Leader"))
                .andExpect(jsonPath("$.leaderContact").value("leader@example.test"))
                .andReturn().getResponse().getContentAsString();
        String id = new com.fasterxml.jackson.databind.ObjectMapper().readTree(body).get("id").asText();
        mvc.perform(get("/api/v1/sections")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("API Leader")));
        mvc.perform(put("/api/v1/sections/" + id).contentType("application/json")
                        .content("{\"name\":\"Older client edit\",\"description\":\"Description\",\"active\":true}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.leaderName").value("API Leader"));
        mvc.perform(put("/api/v1/sections/" + id).contentType("application/json")
                        .content("{\"name\":\"Clear leader\",\"description\":\"Description\",\"active\":true,\"leaderName\":\"\",\"leaderContact\":\"\"}"))
                .andExpect(status().isOk());
        assertThat(sections.findById(id).orElseThrow().leaderName()).isEmpty();
        assertThat(sections.findById(id).orElseThrow().leaderContact()).isEmpty();
        mvc.perform(post("/api/v1/sections").contentType("application/json")
                        .content("{\"name\":\"Legacy create\",\"description\":\"Description\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.leaderName").doesNotExist());
        mvc.perform(put("/api/v1/sections/" + id).contentType("application/json")
                        .content("{\"name\":\"Invalid\",\"description\":\"Description\",\"leaderContact\":\"" + "x".repeat(101) + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void countyDetailsIncludesOperatorsAcrossItsMunicipalitiesOnly() throws Exception {
        municipalities.save(new LocationMunicipalityRecord("second-town", "country", "state", "county", "Second Town"));
        operators.save(new OperatorRecord("county-operator", "W1COUNTY", "County operator", true, true, false, "second-town"));
        counties.save(new LocationCountyRecord("other-county", "country", "state", "Other County"));
        municipalities.save(new LocationMunicipalityRecord("other-town", "country", "state", "other-county", "Other Town"));
        operators.save(new OperatorRecord("other-operator", "W1OTHER", "Other operator", false, false, false, "other-town"));
        String page = mvc.perform(get("/location-county/county")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(page).contains("aria-label=\"County location\"", "Municipalities (2 total)", "Operators (2)",
                "aria-label=\"County actions\"", "id=\"county-municipalities-list\"", "id=\"county-operators-list\"",
                "<th>Callsign</th>", "<th>Location</th>", "<th>NTS</th>", "<th>RACES</th>", "<th>SKYWARN</th>",
                "KC1VMZ", "W1COUNTY", "Second Town", "/operators?selected=county-operator",
                "data-dialog-title=\"Add municipality\"", "data-dialog-title=\"Add municipalities in bulk\"",
                "data-dialog-title=\"Remove municipality from county\"", "data-dialog-title=\"Edit county details\"",
                "data-dialog-title=\"Delete county\"")
                .doesNotContain("W1OTHER", "No location", "/operators?selected=unlocated");
        int rowStart = page.lastIndexOf("<tr", page.indexOf("/operators?selected=county-operator"));
        String row = page.substring(rowStart, page.indexOf("</tr>", rowStart));
        assertThat(row).containsPattern("(?s)<td>Yes</td>\\s*<td>No</td>\\s*<td>Yes</td>");
        counties.save(new LocationCountyRecord("empty-county", "country", "state", "Empty County"));
        mvc.perform(get("/location-county/empty-county")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("No operators from municipalities in this county.")));
        mvc.perform(get("/location-county/missing")).andExpect(status().isNotFound());
    }

    @Test
    void municipalityDetailsListsOnlyLocalOperatorsAndTheirAffiliations() throws Exception {
        operators.save(new OperatorRecord("local-second", "W1LOCAL", "Local operator", true, false, true, "location"));
        String page = mvc.perform(get("/location-location/location")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(page).contains("aria-label=\"Municipality properties\"", "Operators (2)",
                "aria-label=\"Municipality actions\"", "<th>SKYWARN</th>", "<th>RACES</th>", "<th>NTS</th>",
                "/operators?selected=operator", "/operators?selected=local-second", "KC1VMZ", "Test &lt;operator&gt;",
                "data-dialog-title=\"Edit municipality details\"", "data-dialog-title=\"Delete municipality\"",
                "Return to state").doesNotContain("No location", "/operators?selected=unlocated");
        int rowStart = page.lastIndexOf("<tr", page.indexOf("/operators?selected=local-second"));
        String row = page.substring(rowStart, page.indexOf("</tr>", rowStart));
        assertThat(row).containsPattern("(?s)<td>No</td>\\s*<td>Yes</td>\\s*<td>Yes</td>");
        municipalities.save(new LocationMunicipalityRecord("empty-town", "country", "state", "county", "Empty Town"));
        mvc.perform(get("/location-location/empty-town")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("No operators from this municipality.")));
        mvc.perform(get("/location-location/missing")).andExpect(status().isNotFound());
    }

    @Test
    void operatorLocationSelectionKeepsQueryParametersAndUpdatesTheChosenMunicipality() throws Exception {
        String editPage = mvc.perform(get("/operator-edit/operator")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(editPage).contains("/js/action-dialog.js", "/css/action-dialog.css",
                "<dialog id=\"action-dialog\"", "data-action-dialog=\"steps\"",
                "data-dialog-title=\"Change operator location\"", "data-dialog-subject=\"KC1VMZ\"");
        municipalities.save(new LocationMunicipalityRecord("new-location", "country", "state", "county", "New Town"));
        String initial = mvc.perform(get("/operator-edit-changelocation/operator"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(initial).contains("value=\"country\" selected", "name=\"countryId\"");
        String countryStep = mvc.perform(post("/operator-edit-changelocation-action/operator")
                        .param("callsign", "KC1VMZ").param("countryId", "country"))
                .andExpect(status().isSeeOther())
                .andExpect(header().string("Location", "/operator-edit-changelocation/operator?countryId=country"))
                .andReturn().getResponse().getHeader("Location");
        mvc.perform(get(countryStep)).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"stateId\"")));
        String stateStep = mvc.perform(post("/operator-edit-changelocation-action/operator")
                        .param("callsign", "KC1VMZ").param("stateId", "state"))
                .andExpect(status().isSeeOther())
                .andExpect(header().string("Location", "/operator-edit-changelocation/operator?countryId=country&stateId=state"))
                .andReturn().getResponse().getHeader("Location");
        mvc.perform(get(stateStep)).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("New Town")));
        mvc.perform(post("/operator-edit-changelocation-action/operator")
                        .param("callsign", "KC1VMZ").param("locationId", "new-location"))
                .andExpect(status().isSeeOther())
                .andExpect(header().string("Location", "/operator-edit/operator"));
        assertThat(operators.findById("operator").orElseThrow().location_municipality_id()).isEqualTo("new-location");
        mvc.perform(get("/operator-edit-changelocation/unlocated")).andExpect(status().isOk());
        mvc.perform(get("/operator-edit-changelocation/missing")).andExpect(status().isNotFound());
        mvc.perform(post("/operator-edit-changelocation-action/missing")
                        .param("callsign", "W1TEST").param("countryId", "country"))
                .andExpect(status().isNotFound());
    }

    @Test
    void editingAnActiveEventReturnsToTheUpdatedSelectedEvent() throws Exception {
        String redirect = mvc.perform(post("/activeEvent-edit-action/event")
                        .param("name", "Updated net").param("description", "Updated description")
                        .param("type", "1").param("location", "Updated radio location")
                        .param("netControlCallsign", "W1AW").param("startTimeStr", START.plusHours(1).toString()))
                .andExpect(status().isSeeOther())
                .andExpect(header().string("Location", "/activeEvents?selected=event"))
                .andReturn().getResponse().getHeader("Location");
        String page = mvc.perform(get(redirect)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(page).contains("data-active-event-id=\"event\"", "is-selected", "aria-current=\"true\"",
                "Active Event Details - Updated net", "Updated description", "Updated radio location", "W1AW",
                "2026-11-02 20:30").doesNotContain("Back to active events");
        assertThat(events.findById("event").orElseThrow().name()).isEqualTo("Updated net");
    }

    @Test
    void structuredCheckInSavesEachParticipantsCapturedDefaultsAndValidatesBeforeSaving() throws Exception {
        jdbc.update("DELETE FROM participant");
        String form = mvc.perform(get("/activeEvent-checkin/event")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(form).contains("Participation defaults", "data-checkin-payload",
                "data-participation-default=\"transmitPower\"",
                "data-participation-default=\"primaryPowerSource\"",
                "data-participation-default=\"secondaryPowerSource\"",
                "data-participation-default=\"communicationPlatform\"",
                "data-participation-default=\"communicationBand\"");
        mvc.perform(post("/activeEvent-checkin-action/event")
                        .param("participants[0].operatorId", "operator")
                        .param("participants[0].checkInTime", START.toString())
                        .param("participants[0].transmitPower", "25")
                        .param("participants[0].primaryPowerSource", "BATTERY")
                        .param("participants[0].secondaryPowerSource", "SOLAR")
                        .param("participants[0].communicationPlatform", "RADIO_VOICE")
                        .param("participants[0].communicationBand", "M_2")
                        .param("participants[1].operatorId", "unlocated")
                        .param("participants[1].checkInTime", START.plusMinutes(5).toString())
                        .param("participants[1].transmitPower", "")
                        .param("participants[1].primaryPowerSource", "UNKNOWN")
                        .param("participants[1].secondaryPowerSource", "UNKNOWN")
                        .param("participants[1].communicationPlatform", "UNKNOWN")
                        .param("participants[1].communicationBand", "UNKNOWN"))
                .andExpect(status().isSeeOther())
                .andExpect(header().string("Location", "/activeEvents?selected=event"));
        List<ParticipantRecord> saved = participants.findByeventId("event");
        assertThat(saved).hasSize(2);
        ParticipantRecord first = saved.stream().filter(item -> item.operatorId().equals("operator")).findFirst().orElseThrow();
        assertThat(first.transmitPower()).isEqualTo(25);
        assertThat(first.primaryPower()).isEqualTo(ElectricalPowerType.BATTERY);
        assertThat(first.backupPower()).isEqualTo(ElectricalPowerType.SOLAR);
        assertThat(first.communicationPlatform()).isEqualTo(CommunicationPlatform.RADIO_VOICE);
        assertThat(first.communicationBand()).isEqualTo(CommunicationBand.M_2);
        ParticipantRecord second = saved.stream().filter(item -> item.operatorId().equals("unlocated")).findFirst().orElseThrow();
        assertThat(second.transmitPower()).isZero();
        assertThat(second.primaryPower()).isEqualTo(ElectricalPowerType.UNKNOWN);
        assertThat(second.communicationBand()).isEqualTo(CommunicationBand.UNKNOWN);
        jdbc.update("DELETE FROM participant");
        for (String invalidField : List.of("transmitPower", "communicationBand", "operatorId")) {
            var submission = post("/activeEvent-checkin-action/event")
                    .param("participants[0].operatorId", invalidField.equals("operatorId") ? "INVALID" : "operator")
                    .param("participants[0].checkInTime", START.toString())
                    .param("participants[0].primaryPowerSource", "UNKNOWN")
                    .param("participants[0].secondaryPowerSource", "UNKNOWN")
                    .param("participants[0].communicationPlatform", "UNKNOWN")
                    .param("participants[0].communicationBand", invalidField.equals("communicationBand") ? "INVALID" : "UNKNOWN");
            if (invalidField.equals("transmitPower")) {
                submission.param("participants[0].transmitPower", "-1");
            }
            mvc.perform(submission).andExpect(status().isBadRequest());
            assertThat(participants.findByeventId("event")).isEmpty();
        }
    }

    @Test
    void participantCheckInAndOutReturnToTheUpdatedSelectedActiveEvent() throws Exception {
        for (String action : List.of("checkin", "checkout")) {
            String redirect = mvc.perform(post("/activeEvent-" + action + "-action/event")
                            .param("participantIds", "unlocated," + START.plusMinutes(20) + ","))
                    .andExpect(status().isSeeOther())
                    .andExpect(header().string("Location", "/activeEvents?selected=event"))
                    .andReturn().getResponse().getHeader("Location");
            String page = mvc.perform(get(redirect)).andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            assertThat(page).contains("data-active-event-id=\"event\"", "is-selected", "aria-current=\"true\"",
                    "Active Event Details - Test Event", "W1AW", "Participants (2)")
                    .doesNotContain("Back to active events");
            var participant = participants.findByeventId("event").stream()
                    .filter(item -> item.operatorId().equals("unlocated")).findFirst().orElseThrow();
            assertThat(participant.startTime()).isEqualTo(START.plusMinutes(20));
            if (action.equals("checkout")) {
                assertThat(participant.endTime()).isEqualTo(START.plusMinutes(20));
            } else {
                assertThat(participant.endTime()).isNull();
            }
        }
    }

    @Test
    void startingAScheduledEventReturnsToTheUpdatedSelectedActiveEvent() throws Exception {
        jdbc.update("UPDATE event SET state = ? WHERE id = 'event'", EventState.PENDING.ordinal());
        LocalDateTime actualStart = START.plusHours(2);
        String redirect = mvc.perform(post("/activeEvent-start-action/event")
                        .param("startTimeStr", actualStart.toString()))
                .andExpect(status().isSeeOther())
                .andExpect(header().string("Location", "/activeEvents?selected=event"))
                .andReturn().getResponse().getHeader("Location");
        EventRecord started = events.findById("event").orElseThrow();
        assertThat(started.state()).isEqualTo(EventState.STARTED.ordinal());
        assertThat(started.startTime()).isEqualTo(actualStart);
        String page = mvc.perform(get(redirect)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(page).contains("data-active-event-id=\"event\"", "is-selected", "aria-current=\"true\"",
                "Active Event Details - Test Event", "STARTED", "2026-11-02 21:30",
                "Check-in operators", "Check-out operators", "href=\"/activeEvent-secure/event\"")
                .doesNotContain("href=\"/activeEvent-start/event\"", "Back to active events");
    }

    @Test
    void oneTimeEventsCanStartImmediatelyOrBeScheduledWithoutRecurringTemplates() throws Exception {
        String page = mvc.perform(get("/activeEvents")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(page).contains("href=\"/activeEvent-add\"", "data-dialog-title=\"Add event\"",
                "/js/active-event-form.js");
        String form = mvc.perform(get("/activeEvent-add")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(form).contains("Test Section", "name=\"sectionId\"", "name=\"name\"",
                "name=\"description\"", "name=\"type\"", "name=\"location\"",
                "name=\"netControlCallsign\"", "Start immediately", "Schedule for a specific time",
                "data-scheduled-start hidden", "name=\"startTimeStr\"");
        int templateCount = recurringEvents.findAll().size();
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        for (String mode : List.of("STARTED", "PENDING")) {
            String redirect = mvc.perform(post("/activeEvent-add-action")
                            .param("sectionId", "section").param("name", "Single " + mode)
                            .param("description", "One-off description").param("type", "2")
                            .param("location", "Test radio").param("netControlCallsign", "W1AW")
                            .param("startMode", mode).param("startTimeStr", START.plusDays(2).toString()))
                    .andExpect(status().isSeeOther()).andReturn().getResponse().getHeader("Location");
            String id = redirect.substring(redirect.indexOf("selected=") + 9);
            EventRecord created = events.findById(id).orElseThrow();
            assertThat(created.name()).isEqualTo("Single " + mode);
            assertThat(created.description()).isEqualTo("One-off description");
            assertThat(created.type()).isEqualTo(EventType.TRAINING);
            assertThat(created.location()).isEqualTo("Test radio");
            assertThat(created.netControlCallsign()).isEqualTo("W1AW");
            assertThat(created.recurringEventId()).isEmpty();
            assertThat(created.state()).isEqualTo(EventState.valueOf(mode).ordinal());
            assertThat(created.activeSection()).isTrue();
            if (mode.equals("STARTED")) {
                assertThat(created.startTime()).isAfter(before).isBefore(LocalDateTime.now().plusSeconds(1));
            } else {
                assertThat(created.startTime()).isEqualTo(START.plusDays(2));
            }
            mvc.perform(get(redirect)).andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Single " + mode)));
            mvc.perform(get("/activeEvent-checkin/" + id)).andExpect(status().isOk());
        }
        assertThat(recurringEvents.findAll()).hasSize(templateCount);
        int eventCount = events.findAll().size();
        mvc.perform(post("/activeEvent-add-action").param("sectionId", "section")
                        .param("name", "Bad time").param("type", "1").param("startMode", "PENDING"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/activeEvent-add-action").param("sectionId", "missing")
                        .param("name", "Bad section").param("type", "1").param("startMode", "STARTED"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/activeEvent-add-action").param("sectionId", "section")
                        .param("name", "Bad mode").param("type", "1").param("startMode", "SECURE"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/activeEvent-add-action").param("sectionId", "section")
                        .param("name", "Bad type").param("type", "99").param("startMode", "STARTED"))
                .andExpect(status().isBadRequest());
        assertThat(events.findAll()).hasSize(eventCount);
        jdbc.update("UPDATE section SET active = 0 WHERE id = 'section'");
        mvc.perform(post("/activeEvent-add-action").param("sectionId", "section")
                        .param("name", "Inactive section").param("type", "1").param("startMode", "STARTED"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void districtMunicipalitySelectionKeepsQueryParametersSeparateFromThePath() throws Exception {
        jdbc.update("DELETE FROM district_location");
        mvc.perform(get("/district-municipality-add/district"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"countryId\"")));
        String countryStep = mvc.perform(post("/district-municipality-add-action/district")
                        .param("countryId", "country"))
                .andExpect(status().isSeeOther())
                .andExpect(header().string("Location", "/district-municipality-add/district?countryId=country"))
                .andReturn().getResponse().getHeader("Location");
        mvc.perform(get(countryStep))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"stateId\"")));
        String stateStep = mvc.perform(post("/district-municipality-add-action/district")
                        .param("stateId", "state"))
                .andExpect(status().isSeeOther())
                .andExpect(header().string("Location", "/district-municipality-add/district?countryId=country&stateId=state"))
                .andReturn().getResponse().getHeader("Location");
        mvc.perform(get(stateStep))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"locationId\"")));
        mvc.perform(post("/district-municipality-add-action/district").param("locationId", "location"))
                .andExpect(status().isSeeOther())
                .andExpect(header().string("Location", "/district/district"));
        assertThat(districtLocations.findAll()).hasSize(1);
        assertThat(districtLocations.findAll().getFirst().location_id()).isEqualTo("location");
        mvc.perform(get("/district-municipality-add/missing")).andExpect(status().isNotFound());
        mvc.perform(post("/district-municipality-add-action/missing").param("countryId", "country"))
                .andExpect(status().isNotFound());
    }

    @TestFactory
    Stream<DynamicTest> everyPageRendersWithExistingData() {
        List<DynamicTest> tests = new ArrayList<>();
        for (Method method : UIController.class.getDeclaredMethods()) {
            GetMapping mapping = method.getAnnotation(GetMapping.class);
            if (mapping == null || method.getReturnType() != ModelAndView.class) {
                continue;
            }
            String route = mapping.value()[0];
            String url = pageUrl(route);
            tests.add(DynamicTest.dynamicTest(route, () -> {
                String html = mvc.perform(get(url))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString();
                assertThat(html).doesNotContain("[[${", "{{", "th:block", "th:each");
                assertThat(html).contains("Welcome", "Active Events", "Recurring Events", "Archives",
                        "Sections", "Operators", "Locations");
                assertThat(html).contains("src=\"/js/theme.js\"", "href=\"/css/theme.css\"",
                        "class=\"theme-toggle\"", "theme-icon--moon", "theme-icon--sun",
                        "aria-label=\"Switch to dark mode\"");
            }));
        }
        return tests.stream();
    }

    private String pageUrl(String route) {
        String id = "event";
        if (route.startsWith("/section") || route.startsWith("/district-add")) {
            id = "section";
        } else if (route.startsWith("/district")) {
            id = "district";
        } else if (route.startsWith("/operator")) {
            id = "operator";
        } else if (route.startsWith("/participant")) {
            id = "participant";
        } else if (route.startsWith("/recurring")) {
            id = "recurring";
        } else if (route.startsWith("/location-country") || route.startsWith("/location-state-add")) {
            id = "country";
        } else if (route.startsWith("/location-state") || route.startsWith("/location-county-add")
                || route.startsWith("/location-location-add")) {
            id = "state";
        } else if (route.startsWith("/location-county")) {
            id = "county";
        } else if (route.startsWith("/location-location")) {
            id = "location";
        }
        return route.replace("{id}", id).replace("{countyId}", "county")
                .replace("{locationId}", "location");
    }

    @Test
    void activeDialogsReuseOriginalFormsAndParticipantSelection() throws Exception {
        String page = mvc.perform(get("/activeEvents").param("selected", "event"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(page).contains("/js/action-dialog.js", "/js/participant-selection.js",
                "<dialog id=\"action-dialog\"", "data-dialog-title=\"Edit active event\"",
                "data-dialog-title=\"Secure active event\"", "data-dialog-title=\"Check-in operators\"",
                "data-dialog-title=\"Check-out operators\"", "data-dialog-title=\"Cancel active event\"",
                "data-dialog-title=\"Delete active event\"", "data-dialog-title=\"Update participant details\"",
                "data-dialog-title=\"Remove participant\"");
        for (String action : List.of("edit", "secure", "start", "cancel", "delete", "checkin", "checkout")) {
            String form = mvc.perform(get("/activeEvent-" + action + "/event"))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            assertThat(form).contains("action=\"/activeEvent-" + action + "-action/event\"");
            if (action.equals("checkin") || action.equals("checkout")) {
                assertThat(form).contains("data-participant-selection=\"" + action + "\"",
                        "data-participant-action=\"add\"", "type=\"button\"", "name=\"participantIds\"")
                        .doesNotContain("onclick=", "new Function");
            }
        }
        mvc.perform(post("/activeEvent-checkin-action/event")
                        .param("participantIds", "unlocated," + START + ","))
                .andExpect(status().isSeeOther());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM participant WHERE event_id = 'event'", Integer.class))
                .isEqualTo(2);
        mvc.perform(post("/participant-remove-action/participant"))
                .andExpect(status().isSeeOther()).andExpect(header().string("Location", "/activeEvent/event"));
        assertThat(participants.findById("participant")).isEmpty();
        assertThat(operators.findById("operator")).isPresent();
    }

    @Test
    void archivedDialogsReuseFormsAndDeleteOnlyTheSelectedEvent() throws Exception {
        jdbc.update("UPDATE event SET state = ?, secure = 1, end_time = ? WHERE id = 'event'",
                EventState.SECURE.ordinal(), JdbcValues.parameter(START.plusHours(1)));
        jdbc.update("UPDATE participant SET end_time = ? WHERE id = 'participant'",
                JdbcValues.parameter(START.plusHours(1)));
        String page = mvc.perform(get("/archives").param("selected", "event"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(page).contains("<dialog id=\"action-dialog\"", "/js/action-dialog.js", "/css/action-dialog.css",
                "data-dialog-title=\"Generate monthly summary report\"",
                "data-dialog-title=\"Generate monthly participation report\"",
                "data-dialog-title=\"Generate monthly data export\"",
                "data-dialog-title=\"Generate quarterly summary report\"",
                "data-dialog-title=\"Generate quarterly participation report\"",
                "data-dialog-title=\"Generate archived event report\"",
                "data-dialog-title=\"Delete archived event\"",
                "data-dialog-title=\"Update participant details\"");

        for (String route : List.of("/archivedEvents-monthlyreport", "/archivedEvents-monthlyparticipationreport",
                "/archivedEvents-monthlyexport", "/archivedEvents-quarterlyreport",
                "/archivedEvents-quarterlyparticipationreport")) {
            String form = mvc.perform(get(route)).andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            assertThat(form).contains("action=\"" + route + "-action\"", "name=\"sectionId\"", "name=\"year\"");
            assertThat(form).contains(route.contains("quarterly") ? "name=\"quarter\"" : "name=\"month\"");
        }
        mvc.perform(get("/archivedEvent-report/event"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("action=\"/archivedEvent-report-action/event\"")));
        mvc.perform(get("/archivedEvent-delete/event"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("action=\"/archivedEvent-delete-action/event\"")));
        String participantForm = mvc.perform(get("/participant-edittimes/participant/"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(participantForm).contains("name=\"startTimeStr\"", "name=\"endTimeStr\"", "name=\"transmitPower\"",
                "name=\"primaryPowerSource\"", "name=\"secondaryPowerSource\"",
                "name=\"communicationPlatform\"", "name=\"communicationBand\"");

        events.save(new EventRecord("other-archived", "Another Event", "Description", "recurring", "section",
                EventType.NET, "Radio", START.minusDays(7), START.minusDays(7).plusHours(1), true,
                "KC1VMZ", true, EventState.SECURE.ordinal()));
        mvc.perform(post("/archivedEvent-delete-action/event"))
                .andExpect(status().isSeeOther())
                .andExpect(header().string("Location", "/archives"));
        assertThat(events.findById("event")).isEmpty();
        assertThat(participants.findById("participant")).isEmpty();
        assertThat(events.findById("other-archived")).isPresent();
        assertThat(recurringEvents.findById("recurring")).isPresent();
        assertThat(operators.findById("operator")).isPresent();
    }

    @Test
    void recurringReportDialogRetainsSectionTypeAndDownload() throws Exception {
        String list = mvc.perform(get("/recurringEvents"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(list).contains("data-action-dialog=\"report\"", "href=\"/recurringEvents-report\"");
        String report = mvc.perform(get("/recurringEvents-report"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(report).contains("action=\"/recurringEvents-report-action\"", "name=\"sectionId\"",
                "Test Section", "value=\"ALL\"", "value=\"NETS\" selected", "Nets only");
        for (String type : List.of("ALL", "NETS")) {
            String location = mvc.perform(post("/recurringEvents-report-action")
                            .param("sectionId", "section").param("type", type))
                    .andExpect(status().isSeeOther())
                    .andReturn().getResponse().getHeader("Location");
            mvc.perform(get(location))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/pdf"))
                    .andExpect(header().exists("Content-Disposition"));
            String filename = location.substring(location.lastIndexOf('/') + 1);
            Files.deleteIfExists(Path.of(System.getProperty("java.io.tmpdir"), filename));
        }
    }

    @Test
    void recurringEditDialogUsesAllOriginalFieldsAndSaveHandler() throws Exception {
        String selected = mvc.perform(get("/recurringEvents").param("selected", "recurring"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(selected).contains("data-action-dialog=\"edit\"", "data-dialog-subject=\"Weekly Net\"");
        String edit = mvc.perform(get("/recurringEvent-edit/recurring"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(edit).contains("action=\"/recurringEvent-edit-action/recurring\"", "Test Section",
                "name=\"name\"", "name=\"description\"", "name=\"type\"", "name=\"schedule\"",
                "name=\"location\"", "name=\"netControlCallsign\"", "value=\"Weekly Net\"",
                "value=\"Monday\"", "value=\"Radio\"", "value=\"KC1VMZ\"");
        mvc.perform(post("/recurringEvent-edit-action/recurring")
                        .param("name", "Updated Net").param("description", "Updated description")
                        .param("type", "2").param("schedule", "Tuesday 19:30")
                        .param("location", "Updated location").param("netControlCallsign", "W1AW"))
                .andExpect(status().isSeeOther())
                .andExpect(header().string("Location", "/recurringEvent/recurring"));
        RecurringEventRecord updated = recurringEvents.findById("recurring").orElseThrow();
        assertThat(updated.name()).isEqualTo("Updated Net");
        assertThat(updated.description()).isEqualTo("Updated description");
        assertThat(updated.type()).isEqualTo(EventType.TRAINING);
        assertThat(updated.schedule()).isEqualTo("Tuesday 19:30");
        assertThat(updated.location()).isEqualTo("Updated location");
        assertThat(updated.netControlCallsign()).isEqualTo("W1AW");
        assertThat(updated.sectionId()).isEqualTo("section");
    }

    @Test
    void recurringDialogsReuseOriginalFormsAndDeletionBehavior() throws Exception {
        String selected = mvc.perform(get("/recurringEvents").param("selected", "recurring"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(selected).contains("<dialog id=\"action-dialog\"",
                "data-action-dialog=\"schedule\"", "data-action-dialog=\"start\"",
                "data-action-dialog=\"delete\"", "data-dialog-subject=\"Weekly Net\"");
        mvc.perform(get("/recurringEvent-schedule/recurring"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("weeks", 1))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("action=\"/recurringEvent-schedule-action/recurring\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("min=\"1\" max=\"5\"")));
        mvc.perform(get("/recurringEvent-start/recurring"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("startTimeStr"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("action=\"/recurringEvent-start-action/recurring\"")));
        mvc.perform(get("/recurringEvent-delete/recurring"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("action=\"/recurringEvent-delete-action/recurring\"")));

        events.save(new EventRecord("archived-history", "Previous Net", "Description", "recurring", "section",
                EventType.NET, "Radio", START.minusDays(7), START.minusDays(7).plusHours(1), true,
                "KC1VMZ", true, EventState.SECURE.ordinal()));
        participants.save(new ParticipantRecord("archived-participant", "archived-history", "operator",
                START.minusDays(7), START.minusDays(7).plusHours(1), ElectricalPowerType.UNKNOWN,
                ElectricalPowerType.UNKNOWN, 0, CommunicationPlatform.UNKNOWN, CommunicationBand.UNKNOWN));
        mvc.perform(post("/recurringEvent-delete-action/recurring"))
                .andExpect(status().isSeeOther())
                .andExpect(header().string("Location", "/recurringEvents"));
        assertThat(recurringEvents.findById("recurring")).isEmpty();
        assertThat(events.findById("event")).isEmpty();
        assertThat(participants.findById("participant")).isEmpty();
        assertThat(events.findById("archived-history")).isPresent();
        assertThat(participants.findById("archived-participant")).isPresent();
    }

    @Test
    void checkoutDefaultsToEachCheckInAndAcceptsAnEditedTime() throws Exception {
        LocalDateTime secondCheckIn = START.plusMinutes(17).plusSeconds(37);
        participants.save(new ParticipantRecord("second-participant", "event", "unlocated", secondCheckIn, null,
                ElectricalPowerType.UNKNOWN, ElectricalPowerType.UNKNOWN, 0,
                CommunicationPlatform.UNKNOWN, CommunicationBand.UNKNOWN));

        String html = mvc.perform(get("/activeEvent-checkout/event"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        for (var entry : java.util.Map.of("operator", START, "unlocated", secondCheckIn).entrySet()) {
            int start = html.indexOf("<tr id=\"" + entry.getKey() + "\"");
            assertThat(start).isGreaterThanOrEqualTo(0);
            String row = html.substring(start, html.indexOf("</tr>", start));
            assertThat(row).contains("value=\"" + entry.getValue() + "\"", "step=\"any\"")
                    .doesNotContain("readonly");
        }

        LocalDateTime editedCheckOut = secondCheckIn.plusMinutes(12);
        mvc.perform(post("/activeEvent-checkout-action/event")
                        .param("participantIds", "operator," + START + ",unlocated," + editedCheckOut + ","))
                .andExpect(status().isSeeOther());
        assertThat(participants.findById("participant").orElseThrow().endTime()).isEqualTo(START);
        assertThat(participants.findById("second-participant").orElseThrow().endTime()).isEqualTo(editedCheckOut);
    }

    @Test
    void recurringEventSelectionKeepsEventActionsAndReportAvailable() throws Exception {
        String initial = mvc.perform(get("/recurringEvents"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(initial).contains("Select a recurring event to view details.", "/recurringEvents?selected=recurring",
                "/recurringEvent-add", "href=\"/recurringEvents-report\"", "recurring-events-report-actions")
                .doesNotContain("<th>Actions</th>", "href=\"/recurringEvent-delete/");
        String selected = mvc.perform(get("/recurringEvents").param("selected", "recurring"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(selected).contains("is-selected", "Recurring Event Details - Weekly Net", "Monday", "Radio",
                "Test Section", "KC1VMZ", "recurring-event-actions", "href=\"/recurringEvent-edit/recurring\"",
                "href=\"/recurringEvent-schedule/recurring\"", "href=\"/recurringEvent-start/recurring\"",
                "href=\"/recurringEvent-delete/recurring\"", "href=\"/recurringEvents-report\"")
                .doesNotContain("Select a recurring event to view details.", "Back to recurring events", "<th>Actions</th>");
        String fragment = mvc.perform(get("/recurringEvent/recurring/details-pane"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(fragment).contains("Recurring Event Details - Weekly Net", "Schedule", "Start", "Delete")
                .doesNotContain("class=\"navbar\"", "<html", "Back to recurring events");
        mvc.perform(get("/recurringEvent/recurring"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Back to recurring events")));
        mvc.perform(get("/recurringEvent/missing/details-pane")).andExpect(status().isNotFound());
        mvc.perform(get("/recurringEvents").param("selected", "missing"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("This recurring event is no longer available.")));
        jdbc.update("UPDATE recurring_event SET active_section = 0 WHERE id = 'recurring'");
        mvc.perform(get("/recurringEvent/recurring/details-pane")).andExpect(status().isNotFound());
    }

    @Test
    void activeEventSelectionShowsActionsForStartedAndScheduledEvents() throws Exception {
        String initial = mvc.perform(get("/activeEvents"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(initial).contains("Select an active event to view details.", "/activeEvents?selected=event")
                .doesNotContain("<th>Actions</th>");
        String selected = mvc.perform(get("/activeEvents").param("selected", "event"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(selected).contains("is-selected", "Active Event Details - Test Event", "KC1VMZ",
                "Test Section", "active-event-actions", "href=\"/activeEvent-edit/event\"",
                "href=\"/activeEvent-secure/event\"", "href=\"/activeEvent-checkin/event\"",
                "href=\"/activeEvent-checkout/event\"", "href=\"/activeEvent-cancel/event\"",
                "href=\"/activeEvent-delete/event\"", "href=\"/participant-edittimes/participant/\"")
                .doesNotContain("Back to active events", "href=\"/activeEvent-start/event\"");
        assertThat(selected.substring(0, selected.indexOf("id=\"active-event-details\"")))
                .doesNotContain("<th>Actions</th>");

        String started = mvc.perform(get("/activeEvent/event/details-pane"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(started).contains("Active Event Details - Test Event", "Check-in operators", "Check-out operators")
                .doesNotContain("class=\"navbar\"", "<html", "Back to active events");
        jdbc.update("UPDATE event SET state = ? WHERE id = 'event'", EventState.PENDING.ordinal());
        String scheduled = mvc.perform(get("/activeEvent/event/details-pane"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(scheduled).contains("href=\"/activeEvent-start/event\"", "Edit details", "Cancel Event", "Delete")
                .doesNotContain("Check-in operators", "Check-out operators", "href=\"/activeEvent-secure/event\"");
        mvc.perform(get("/activeEvent/event"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Back to active events")));
        mvc.perform(get("/activeEvent/missing/details-pane")).andExpect(status().isNotFound());
        mvc.perform(get("/activeEvents").param("selected", "missing"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("This active event is no longer available.")));
        jdbc.update("UPDATE event SET state = ? WHERE id = 'event'", EventState.SECURE.ordinal());
        mvc.perform(get("/activeEvent/event/details-pane")).andExpect(status().isNotFound());
    }

    @Test
    void archivedEventSelectionKeepsReportsAndEventActionsAvailable() throws Exception {
        jdbc.update("UPDATE event SET state = ?, secure = ?, end_time = ? WHERE id = 'event'",
                EventState.SECURE.ordinal(), true, JdbcValues.parameter(START.plusHours(1)));
        String initial = mvc.perform(get("/archives"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(initial).contains("Select an archived event to view details.", "/archives?selected=event",
                "archives-report-actions", "/archivedEvents-monthlyreport",
                "/archivedEvents-monthlyparticipationreport", "/archivedEvents-monthlyexport",
                "/archivedEvents-quarterlyreport", "/archivedEvents-quarterlyparticipationreport")
                .doesNotContain("<th>Actions</th>", "href=\"/archivedEvent-delete/");

        String selected = mvc.perform(get("/archives").param("selected", "event"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(selected).contains("is-selected", "Archived Event Details - Test Event", "KC1VMZ",
                "Test Section", "Participants (1 total)", "archived-event-actions",
                "href=\"/archivedEvent-report/event\"", "href=\"/archivedEvent-delete/event\"",
                "href=\"/participant-edittimes/participant/\"")
                .doesNotContain("Select an archived event to view details.", "Back to archived events");
        assertThat(selected.substring(0, selected.indexOf("id=\"archived-event-details\"")))
                .doesNotContain("<th>Actions</th>");

        String fragment = mvc.perform(get("/archivedEvent/event/details-pane"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(fragment).contains("Archived Event Details - Test Event", "KC1VMZ", "Generate report", "Delete")
                .doesNotContain("class=\"navbar\"", "<html", "Back to archived events");
        mvc.perform(get("/archivedEvent/missing/details-pane")).andExpect(status().isNotFound());
        mvc.perform(get("/archives").param("selected", "missing"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("This archived event is no longer available.")));
        mvc.perform(get("/archivedEvent/event"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Back to archived events")));
        jdbc.update("UPDATE event SET state = ? WHERE id = 'event'", EventState.STARTED.ordinal());
        mvc.perform(get("/archivedEvent/event/details-pane")).andExpect(status().isNotFound());
    }

    @Test
    void operatorsSelectionRendersSharedDetailsWithoutListActions() throws Exception {
        String initial = mvc.perform(get("/operators"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(initial).contains("Select an operator to view details.", "/operators?selected=operator")
                .doesNotContain("<th>Actions</th>", "href=\"/operator-delete/");

        String selected = mvc.perform(get("/operators").param("selected", "operator"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(selected).contains("is-selected", "Operator Details - KC1VMZ", "Test Section", "LEADERSHIP",
                "href=\"/operator-edit/operator\"", "href=\"/operator-delete/operator\"",
                "<dialog id=\"action-dialog\"", "/js/action-dialog.js", "/css/action-dialog.css",
                "data-action-dialog=\"delete\"", "data-dialog-title=\"Delete operator\"",
                "data-dialog-subject=\"KC1VMZ\"")
                .doesNotContain("Select an operator to view details.", "Back to operators", "<th>Actions</th>");

        String fragment = mvc.perform(get("/operator/operator/details-pane"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(fragment).contains("Operator Details - KC1VMZ", "NTS affiliated", "Test Section", "LEADERSHIP")
                .doesNotContain("class=\"navbar\"", "<html", "Back to operators");
        mvc.perform(get("/operator/missing/details-pane")).andExpect(status().isNotFound());

        String missing = mvc.perform(get("/operators").param("selected", "missing"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(missing).contains("This operator is no longer available.");
        mvc.perform(get("/operator/operator"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Back to operators")));
    }

    @Test
    void restJsonRetainsBooleanNamesAndOmitsNulls() throws Exception {
        mvc.perform(get("/api/v1/operators/operator"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.NTS").value(true))
                .andExpect(jsonPath("$.RACES").value(true))
                .andExpect(jsonPath("$.skywarn").value(true))
                .andExpect(jsonPath("$.nts").doesNotExist());
        mvc.perform(get("/api/v1/operators/unlocated"))
                .andExpect(jsonPath("$.location").doesNotExist());
        mvc.perform(get("/api/v1/events/event"))
                .andExpect(jsonPath("$.endTime").doesNotExist())
                .andExpect(jsonPath("$.prettyEndTime").doesNotExist());
        mvc.perform(get("/api/v1/operators/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Not Found"))
                .andExpect(jsonPath("$._embedded.errors[0].message").value("Page Not Found"));
        mvc.perform(delete("/api/v1/operators/missing")).andExpect(status().isOk());
    }

    @Test
    void jdbcPreservesEpochTimestampsEnumsAndRepositoryMethods() {
        assertThat(events.findById("event").orElseThrow().startTime()).isEqualTo(START);
        assertThat(jdbc.queryForObject("SELECT typeof(start_time) FROM event WHERE id = 'event'", String.class))
                .isEqualTo("integer");
        assertThat(jdbc.queryForObject("SELECT type FROM event WHERE id = 'event'", String.class)).isEqualTo("NET");
        assertThat(operators.findBycallsign("KC1VMZ").isNTS()).isTrue();
        assertThat(countries.findByname("United States")).isPresent();
        assertThat(districtLocations.findBydistrict_id("district")).hasSize(1);
        assertThat(memberships.findBysectionId("section")).hasSize(1);
        assertThat(participants.findByeventId("event")).hasSize(1);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM DATABASECHANGELOG", Integer.class)).isEqualTo(11);
    }

    @Test
    void formsBindRecordsAndRedirectWith303() throws Exception {
        mvc.perform(post("/operator-add-action")
                        .param("callsign", "W1TEST")
                        .param("name", "New Operator")
                        .param("isNTS", "on"))
                .andExpect(status().isSeeOther())
                .andExpect(header().string("Location", org.hamcrest.Matchers.startsWith("/operator/")));
        assertThat(operators.findBycallsign("W1TEST").isNTS()).isTrue();
        mvc.perform(post("/recurringEvent-schedule-action/recurring")
                        .param("startTimeStr", "2026-11-09T19:30")
                        .param("weeks", "2"))
                .andExpect(status().isSeeOther());
        assertThat(events.findByrecurringEventId("recurring")).hasSize(3);
    }

    @Test
    void startingAnEventBindsOmittedPrimitiveFormFields() throws Exception {
        mvc.perform(post("/recurringEvent-start-action/recurring")
                        .param("startTimeStr", "2026-11-09T19:30"))
                .andExpect(status().isSeeOther())
                .andExpect(header().string("Location", org.hamcrest.Matchers.startsWith("/activeEvent/")));
        assertThat(events.findByrecurringEventId("recurring")).hasSize(2);
    }

    @Test
    void downloadRedirectsNormalizeAndEncodePaths() {
        assertThat(com.kc1vmz.netlog.web.WebUris.path("/report//Test Report.pdf").toString())
                .isEqualTo("/report/Test%20Report.pdf");
    }

    @TestFactory
    Stream<DynamicTest> reportsAndExportsGenerateDownloadableFiles() {
        List<String> routes = List.of(
                "/archivedEvent-report-action/event",
                "/archivedEvents-monthlyreport-action",
                "/archivedEvents-monthlyparticipationreport-action",
                "/archivedEvents-quarterlyreport-action",
                "/archivedEvents-quarterlyparticipationreport-action",
                "/section-memberreport-action/section",
                "/section-ntsreport-action/section",
                "/section-racesreport-action/section",
                "/section-skywarnreport-action/section",
                "/section-leadershipreport-action/section",
                "/section-monthlynonparticipationreport-action",
                "/section-quarterlynonparticipationreport-action",
                "/recurringEvents-report-action",
                "/archivedEvents-monthlyexport-action",
                "/section-monthlynonparticipationexport-action",
                "/section-quarterlynonparticipationexport-action");
        return routes.stream().map(route -> DynamicTest.dynamicTest(route, () -> {
            jdbc.update("UPDATE event SET state = ?, secure = ?, end_time = ? WHERE id = 'event'",
                    EventState.SECURE.ordinal(), true, JdbcValues.parameter(START.plusHours(1)));
            jdbc.update("UPDATE participant SET end_time = ? WHERE id = 'participant'",
                    JdbcValues.parameter(START.plusHours(1)));
            String location = mvc.perform(post(route)
                            .param("sectionId", "section")
                            .param("month", "11")
                            .param("year", "2026")
                            .param("quarter", "4")
                            .param("type", "ALL"))
                    .andExpect(status().isSeeOther())
                    .andReturn().getResponse().getHeader("Location");
            byte[] file = mvc.perform(get(location))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Content-Disposition"))
                    .andReturn().getResponse().getContentAsByteArray();
            assertThat(file.length).isGreaterThan(20);
            if (location.endsWith(".pdf")) {
                assertThat(new String(file, 0, 5, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
            }
            String filename = location.substring(location.lastIndexOf('/') + 1);
            Files.deleteIfExists(Path.of(System.getProperty("java.io.tmpdir"), filename));
        }));
    }
}
