package com.kc1vmz.netlog.controller;

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

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import com.kc1vmz.netlog.web.WebUris;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import com.kc1vmz.netlog.web.ViewModels;


import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kc1vmz.netlog.accessor.EventAccessor;
import com.kc1vmz.netlog.accessor.LocationAccessor;
import com.kc1vmz.netlog.accessor.NonParticipationExportAccessor;
import com.kc1vmz.netlog.accessor.NonParticipationReportAccessor;
import com.kc1vmz.netlog.accessor.OperatorAccessor;
import com.kc1vmz.netlog.accessor.OperatorAffiliationReportAccessor;
import com.kc1vmz.netlog.accessor.ParticipantAccessor;
import com.kc1vmz.netlog.accessor.ParticipationReportAccessor;
import com.kc1vmz.netlog.accessor.RecurringEventAccessor;
import com.kc1vmz.netlog.accessor.RecurringEventReportAccessor;
import com.kc1vmz.netlog.accessor.ReportAccessor;
import com.kc1vmz.netlog.accessor.SectionAccessor;
import com.kc1vmz.netlog.accessor.SummaryReportAccessor;
import com.kc1vmz.netlog.accessor.SummaryExportAccessor;
import com.kc1vmz.netlog.enums.CommunicationBand;
import com.kc1vmz.netlog.enums.CommunicationPlatform;
import com.kc1vmz.netlog.enums.ElectricalPowerType;
import com.kc1vmz.netlog.enums.EventState;
import com.kc1vmz.netlog.enums.EventType;
import com.kc1vmz.netlog.enums.MembershipType;
import com.kc1vmz.netlog.enums.OperatorAffiliationReportType;
import com.kc1vmz.netlog.object.District;
import com.kc1vmz.netlog.object.Event;
import com.kc1vmz.netlog.object.Location;
import com.kc1vmz.netlog.object.LocationCountry;
import com.kc1vmz.netlog.object.LocationCounty;
import com.kc1vmz.netlog.object.LocationState;
import com.kc1vmz.netlog.object.Operator;
import com.kc1vmz.netlog.object.Participant;
import com.kc1vmz.netlog.object.RecurringEvent;
import com.kc1vmz.netlog.object.Section;
import com.kc1vmz.netlog.object.SectionOperator;
import com.kc1vmz.netlog.request.ActiveEventCheckInOutRequest;
import com.kc1vmz.netlog.request.ActiveEventCheckInRequest;
import com.kc1vmz.netlog.request.ParticipantCheckInRequest;
import com.kc1vmz.netlog.request.ActiveEventEditRequest;
import com.kc1vmz.netlog.request.ActiveEventScheduleRequest;
import com.kc1vmz.netlog.request.ParticipantStartEndRequest;
import com.kc1vmz.netlog.request.DistrictCreateRequest;
import com.kc1vmz.netlog.request.DistrictLocationEditRequest;
import com.kc1vmz.netlog.request.DistrictMunicipalityBulkCreateRequest;
import com.kc1vmz.netlog.request.LocationCountryCreateRequest;
import com.kc1vmz.netlog.request.LocationCountyChangeRequest;
import com.kc1vmz.netlog.request.LocationStateCreateRequest;
import com.kc1vmz.netlog.request.MonthlyReportRequest;
import com.kc1vmz.netlog.request.OperatorBulkCreateRequest;
import com.kc1vmz.netlog.request.OperatorCreateRequest;
import com.kc1vmz.netlog.request.OperatorLocationEditRequest;
import com.kc1vmz.netlog.request.QuarterlyReportRequest;
import com.kc1vmz.netlog.request.RecurringEventCreateRequest;
import com.kc1vmz.netlog.request.ActiveEventCreateRequest;
import com.kc1vmz.netlog.request.RecurringEventReportRequest;
import com.kc1vmz.netlog.request.SectionCreateRequest;


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

import org.springframework.beans.factory.annotation.Autowired;
import jakarta.validation.Valid;

@Controller
public class UIController {

    @Autowired
    private SectionAccessor sectionAccessor;
    @Autowired
    private RecurringEventAccessor recurringEventAccessor;
    @Autowired
    private EventAccessor eventAccessor;
    @Autowired
    private OperatorAccessor operatorAccessor;
    @Autowired
    private ParticipantAccessor participantAccessor;
    @Autowired
    private ReportAccessor reportAccessor;
    @Autowired
    private SummaryReportAccessor summaryReportAccessor;
    @Autowired
    private SummaryExportAccessor summaryExportAccessor;
    @Autowired
    private OperatorAffiliationReportAccessor operatorAffiliationReportAccessor;
    @Autowired
    private ParticipationReportAccessor participationReportAccessor;
    @Autowired
    private RecurringEventReportAccessor recurringEventReportAccessor;
    @Autowired
    private NonParticipationReportAccessor nonParticipationReportAccessor;
    @Autowired
    private NonParticipationExportAccessor nonParticipationExportAccessor;
    @Autowired
    private LocationAccessor locationAccessor;

    private static final Logger logger = LogManager.getLogger(UIController.class);



    @GetMapping("/archives")
    public ModelAndView archiveEvents(HttpServletRequest request) {
        List<Event> events = eventAccessor.listSecured(null);
        hydrateActiveEvents(events);
        String selectedId = request.getParameter("selected");
        Map<String, Object> model = ViewModels.mapOf("events", events, "selectedId", selectedId);
        if (selectedId != null) {
            boolean available = events.stream().anyMatch(event -> selectedId.equals(event.getId()));
            if (available) {
                model.putAll(archivedEventDetailsModel(selectedId, false));
            } else {
                model.put("selectionUnavailable", true);
            }
        }
        return new ModelAndView("archives", model);
    }


    @GetMapping("/archivedEvent/{id}")
    public ModelAndView archiveEventsDetails(HttpServletRequest request, @PathVariable String id) {
        return new ModelAndView("archivedEvent", archivedEventDetailsModel(id, true));
    }

    @GetMapping("/archivedEvent/{id}/details-pane")
    public String archivedEventDetailsPane(@PathVariable String id, Model model) {
        Map<String, Object> details = archivedEventDetailsModel(id, false);
        Event event = (Event) details.get("event");
        if (event == null || (event.getState() != EventState.SECURE && event.getState() != EventState.NOT_HELD)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        model.addAllAttributes(details);
        return "fragments/archived-event-details :: content";
    }

    private Map<String, Object> archivedEventDetailsModel(String id, boolean standalone) {
        Event event = eventAccessor.get(id);
        if (event == null) {
            return ViewModels.mapOf("event", null);
        }
        hydrateActiveEvent(event);

        List<Participant> participants = participantAccessor.listParticipants(event, false);
        hydrateParticipants(event, participants);

        Collections.sort(participants, new Comparator<Participant>() {
            @Override
            public int compare(Participant obj1, Participant obj2) {
                return obj1.getOperator().getCallsign().compareTo(obj2.getOperator().getCallsign());
            }
        });

        return ViewModels.mapOf("event", event, "participants", participants,
                "participantCount", participants.size(), "standaloneArchivedDetails", standalone);
    }


    @GetMapping("/archivedEvent-report/{id}")
    public ModelAndView archivedEventReport(HttpServletRequest request,  @PathVariable String id) {
        Event event = eventAccessor.get(id);
        return new ModelAndView("archivedEvent-report", ViewModels.mapOf("event", event));
    }


    @PostMapping("/archivedEvent-report-action/{id}")
    ResponseEntity<?> archivedEventReportAction(HttpServletRequest request, @PathVariable String id) {
        Event event = eventAccessor.get(id);
        List<Participant> participants = participantAccessor.listParticipants(event, false);

        String filename = reportAccessor.generateReport(event, participants);
        if (filename == null) {
            return ResponseEntity.internalServerError().body("Could not create report");
        }

        return ResponseEntity.status(303).location(WebUris.path("/archivedEvent-report-pdf/"+id+"/"+filename)).build();
    }

    @GetMapping(path = "/archivedEvent-report-pdf/{id}/{filename}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadArchivedEventReport(HttpServletRequest request, @PathVariable String id, @PathVariable String filename) {
        Event event = eventAccessor.get(id);

        try {
            filename = reportAccessor.getTempReportDir()+filename;
            byte[] fileBytes = Files.readAllBytes(Paths.get(filename));
            String newFilename = String.format("NetLog-%s-%s.pdf", event.getName(), event.getStartTime());
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\""+newFilename+"\"")
                    .body(fileBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/archivedEvents-monthlyreport")
    public ModelAndView archivedEventsMonthlyReport(HttpServletRequest request) {
        List<Section> sections = sectionAccessor.list();
        return new ModelAndView("archivedEvents-monthlyreport", ViewModels.mapOf("sections", sections));
    }

    @GetMapping("/section-monthlyreport/{id}")
    public ModelAndView sectionMonthlyReport(HttpServletRequest request, @PathVariable String id) {
        Section section = sectionAccessor.get(id);
        List<Section> sections = new ArrayList<>();
        sections.add(section);
        return new ModelAndView("archivedEvents-monthlyreport", ViewModels.mapOf("sections", sections));
    }


    @PostMapping("/archivedEvents-monthlyreport-action")
    ResponseEntity<?> archivedEventsMonthlyReportAction(HttpServletRequest request, @Valid @ModelAttribute MonthlyReportRequest actionData) {

        Section section = sectionAccessor.get(actionData.sectionId());
        List<SectionOperator> members = operatorAccessor.listOperators(section);
        Map<Event, List<Participant>> eventParticipants = getEvents(section, actionData.month(), actionData.year(), members);
        String dateStr = actionData.month()+"-"+actionData.year();

        String filename = summaryReportAccessor.generateReport(section, eventParticipants, dateStr, "MONTHLY SUMMARY");
        if (filename == null) {
            return ResponseEntity.internalServerError().body("Could not create report");
        }

        return ResponseEntity.status(303).location(WebUris.path("/archivedEvents-monthlyreport-pdf/"+section.getId()+"/"+dateStr+"/"+filename)).build();
    }

    @GetMapping(path = "/archivedEvents-monthlyreport-pdf/{sectionId}/{dateStr}/{filename}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadArchivedEventMonthlyReport(HttpServletRequest request, @PathVariable String sectionId, @PathVariable String dateStr, @PathVariable String filename) {

        try {
            Section section = sectionAccessor.get(sectionId);
            filename = summaryReportAccessor.getTempReportDir()+filename;
            byte[] fileBytes = Files.readAllBytes(Paths.get(filename));
            String newFilename = String.format("NetLog-MonthlySummaryReport-%s-%s.pdf", section.getName(), dateStr);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\""+newFilename+"\"")
                    .body(fileBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/archivedEvents-monthlyparticipationreport")
    public ModelAndView archivedEventsMonthlyParticipationReport(HttpServletRequest request) {
        List<Section> sections = sectionAccessor.list();
        return new ModelAndView("archivedEvents-monthlyparticipationreport", ViewModels.mapOf("sections", sections));
    }

    @GetMapping("/section-monthlyparticipationreport/{id}")
    public ModelAndView sectionMonthlyParticipationReport(HttpServletRequest request,  @PathVariable String id) {
        List<Section> sections = new ArrayList<>();
        Section section = sectionAccessor.get(id);
        sections.add(section);
        return new ModelAndView("archivedEvents-monthlyparticipationreport", ViewModels.mapOf("sections", sections));
    }


    @PostMapping("/archivedEvents-monthlyparticipationreport-action")
    ResponseEntity<?> archivedEventsMonthlyParticipationReportAction(HttpServletRequest request, @Valid @ModelAttribute MonthlyReportRequest actionData) {

        Section section = sectionAccessor.get(actionData.sectionId());
        List<SectionOperator> members = operatorAccessor.listOperators(section);
        Map<Event, List<Participant>> eventParticipants = getEvents(section, actionData.month(), actionData.year(), members);
        String dateStr = actionData.month()+"-"+actionData.year();

        String filename = participationReportAccessor.generateReport(section, eventParticipants, members, dateStr, "MONTHLY PARTICIPATION");
        if (filename == null) {
            return ResponseEntity.internalServerError().body("Could not create report");
        }

        return ResponseEntity.status(303).location(WebUris.path("/archivedEvents-monthlyparticipationreport-pdf/"+section.getId()+"/"+dateStr+"/"+filename)).build();
    }

    @GetMapping(path = "/archivedEvents-monthlyparticipationreport-pdf/{sectionId}/{dateStr}/{filename}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadArchivedEventMonthlyParticipationReport(HttpServletRequest request, @PathVariable String sectionId, @PathVariable String dateStr, @PathVariable String filename) {

        try {
            Section section = sectionAccessor.get(sectionId);
            filename = participationReportAccessor.getTempReportDir()+filename;
            byte[] fileBytes = Files.readAllBytes(Paths.get(filename));
            String newFilename = String.format("NetLog-MonthlyParticipationReport-%s-%s.pdf", section.getName(), dateStr);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\""+newFilename+"\"")
                    .body(fileBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/archivedEvents-quarterlyreport")
    public ModelAndView archivedEventsQuarterlyReport(HttpServletRequest request) {
        List<Section> sections = sectionAccessor.list();
        return new ModelAndView("archivedEvents-quarterlyreport", ViewModels.mapOf("sections", sections));
    }

    @GetMapping("/section-quarterlyreport/{id}")
    public ModelAndView sectionQuarterlyReport(HttpServletRequest request,  @PathVariable String id) {
        List<Section> sections = new ArrayList<>();
        Section section = sectionAccessor.get(id);
        sections.add(section);
        return new ModelAndView("archivedEvents-quarterlyreport", ViewModels.mapOf("sections", sections));
    }


    @PostMapping("/archivedEvents-quarterlyreport-action")
    ResponseEntity<?> archivedEventsMonthlyReportAction(HttpServletRequest request, @Valid @ModelAttribute QuarterlyReportRequest actionData) {

        Section section = sectionAccessor.get(actionData.sectionId());
        List<SectionOperator> members = operatorAccessor.listOperators(section);
        Map<Event, List<Participant>> eventParticipants = getEvents(section, actionData.quarter(), actionData.year(), members);
        String dateStr = actionData.quarter()+"-"+actionData.year();

        String filename = summaryReportAccessor.generateReport(section, eventParticipants, dateStr, "QUARTERLY SUMMARY");
        if (filename == null) {
            return ResponseEntity.internalServerError().body("Could not create report");
        }

        return ResponseEntity.status(303).location(WebUris.path("/archivedEvents-quarterlyreport-pdf/"+section.getId()+"/"+dateStr+"/"+filename)).build();
    }

    @GetMapping("/section-memberreport/{id}")
    public ModelAndView sectionMemberReport(HttpServletRequest request, @PathVariable String id) {
        Section section = sectionAccessor.get(id);
        return new ModelAndView("section-memberreport", ViewModels.mapOf("section", section));
    }


    @PostMapping("/section-memberreport-action/{id}")
    ResponseEntity<?> sectionMemberReportAction(HttpServletRequest request, @PathVariable String id) {

        Section section = sectionAccessor.get(id);
        List<SectionOperator> members = operatorAccessor.listOperators(section);
        String dateStr = LocalDateTime.now().toString().substring(0, 16);

        String filename = operatorAffiliationReportAccessor.generateReport(section, members, OperatorAffiliationReportType.MEMBERS,  "MEMBERS");
        if (filename == null) {
            return ResponseEntity.internalServerError().body("Could not create report");
        }

        return ResponseEntity.status(303).location(WebUris.path("/section-memberreport-pdf/"+section.getId()+"/"+dateStr+"/"+filename)).build();
    }

    @GetMapping(path = "/section-memberreport-pdf/{sectionId}/{dateStr}/{filename}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadSectionMemberReport(HttpServletRequest request, @PathVariable String sectionId, @PathVariable String dateStr, @PathVariable String filename) {

        try {
            Section section = sectionAccessor.get(sectionId);
            filename = summaryReportAccessor.getTempReportDir()+filename;
            byte[] fileBytes = Files.readAllBytes(Paths.get(filename));
            String newFilename = String.format("NetLog-SectionMemberReport-%s-%s.pdf", section.getName(), dateStr);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\""+newFilename+"\"")
                    .body(fileBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/section-ntsreport/{id}")
    public ModelAndView sectionMemberNTSReport(HttpServletRequest request, @PathVariable String id) {
        Section section = sectionAccessor.get(id);
        return new ModelAndView("section-ntsreport", ViewModels.mapOf("section", section));
    }


    @PostMapping("/section-ntsreport-action/{id}")
    ResponseEntity<?> sectionMemberNTSReportAction(HttpServletRequest request, @PathVariable String id) {

        Section section = sectionAccessor.get(id);
        List<SectionOperator> members = operatorAccessor.listOperators(section);
        String dateStr = LocalDateTime.now().toString().substring(0, 16);

        String filename = operatorAffiliationReportAccessor.generateReport(section, members, OperatorAffiliationReportType.NTS, "NTS MEMBERS");
        if (filename == null) {
            return ResponseEntity.internalServerError().body("Could not create report");
        }

        return ResponseEntity.status(303).location(WebUris.path("/section-ntsreport-pdf/"+section.getId()+"/"+dateStr+"/"+filename)).build();
    }

    @GetMapping(path = "/section-ntsreport-pdf/{sectionId}/{dateStr}/{filename}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadSectionMemberNTSReport(HttpServletRequest request, @PathVariable String sectionId, @PathVariable String dateStr, @PathVariable String filename) {

        try {
            Section section = sectionAccessor.get(sectionId);
            filename = summaryReportAccessor.getTempReportDir()+filename;
            byte[] fileBytes = Files.readAllBytes(Paths.get(filename));
            String newFilename = String.format("NetLog-SectionMemberNTSReport-%s-%s.pdf", section.getName(), dateStr);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\""+newFilename+"\"")
                    .body(fileBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/section-racesreport/{id}")
    public ModelAndView sectionMemberRACESReport(HttpServletRequest request, @PathVariable String id) {
        Section section = sectionAccessor.get(id);
        return new ModelAndView("section-racesreport", ViewModels.mapOf("section", section));
    }


    @PostMapping("/section-racesreport-action/{id}")
    ResponseEntity<?> sectionMemberRACESReportAction(HttpServletRequest request, @PathVariable String id) {

        Section section = sectionAccessor.get(id);
        List<SectionOperator> members = operatorAccessor.listOperators(section);
        String dateStr = LocalDateTime.now().toString().substring(0, 16);

        String filename = operatorAffiliationReportAccessor.generateReport(section, members, OperatorAffiliationReportType.RACES, "RACES MEMBERS");
        if (filename == null) {
            return ResponseEntity.internalServerError().body("Could not create report");
        }

        return ResponseEntity.status(303).location(WebUris.path("/section-racesreport-pdf/"+section.getId()+"/"+dateStr+"/"+filename)).build();
    }

    @GetMapping(path = "/section-racesreport-pdf/{sectionId}/{dateStr}/{filename}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadSectionMemberRACESReport(HttpServletRequest request, @PathVariable String sectionId, @PathVariable String dateStr, @PathVariable String filename) {

        try {
            Section section = sectionAccessor.get(sectionId);
            filename = summaryReportAccessor.getTempReportDir()+filename;
            byte[] fileBytes = Files.readAllBytes(Paths.get(filename));
            String newFilename = String.format("NetLog-SectionMemberRACESReport-%s-%s.pdf", section.getName(), dateStr);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\""+newFilename+"\"")
                    .body(fileBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/section-skywarnreport/{id}")
    public ModelAndView sectionMemberSkywarnReport(HttpServletRequest request, @PathVariable String id) {
        Section section = sectionAccessor.get(id);
        return new ModelAndView("section-skywarnreport", ViewModels.mapOf("section", section));
    }


    @PostMapping("/section-skywarnreport-action/{id}")
    ResponseEntity<?> sectionMemberSkywarnReportAction(HttpServletRequest request, @PathVariable String id) {

        Section section = sectionAccessor.get(id);
        List<SectionOperator> members = operatorAccessor.listOperators(section);
        String dateStr = LocalDateTime.now().toString().substring(0, 16);

        String filename = operatorAffiliationReportAccessor.generateReport(section, members, OperatorAffiliationReportType.SKYWARN, "SKYWARN MEMBERS");
        if (filename == null) {
            return ResponseEntity.internalServerError().body("Could not create report");
        }

        return ResponseEntity.status(303).location(WebUris.path("/section-skywarnreport-pdf/"+section.getId()+"/"+dateStr+"/"+filename)).build();
    }

    @GetMapping(path = "/section-skywarnreport-pdf/{sectionId}/{dateStr}/{filename}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadSectionMemberSkywarnReport(HttpServletRequest request, @PathVariable String sectionId, @PathVariable String dateStr, @PathVariable String filename) {

        try {
            Section section = sectionAccessor.get(sectionId);
            filename = summaryReportAccessor.getTempReportDir()+filename;
            byte[] fileBytes = Files.readAllBytes(Paths.get(filename));
            String newFilename = String.format("NetLog-SectionMemberSkywarnReport-%s-%s.pdf", section.getName(), dateStr);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\""+newFilename+"\"")
                    .body(fileBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/section-leadershipreport/{id}")
    public ModelAndView sectionMemberLeadershipReport(HttpServletRequest request, @PathVariable String id) {
        Section section = sectionAccessor.get(id);
        return new ModelAndView("section-leadershipreport", ViewModels.mapOf("section", section));
    }


    @PostMapping("/section-leadershipreport-action/{id}")
    ResponseEntity<?> sectionMemberLeadershipReportAction(HttpServletRequest request, @PathVariable String id) {

        Section section = sectionAccessor.get(id);
        List<SectionOperator> members = operatorAccessor.listOperators(section);
        String dateStr = LocalDateTime.now().toString().substring(0, 16);

        String filename = operatorAffiliationReportAccessor.generateReport(section, members, OperatorAffiliationReportType.LEADERSHIP, "LEADERSHIP");
        if (filename == null) {
            return ResponseEntity.internalServerError().body("Could not create report");
        }

        return ResponseEntity.status(303).location(WebUris.path("/section-leadershipreport-pdf/"+section.getId()+"/"+dateStr+"/"+filename)).build();
    }

    @GetMapping(path = "/section-leadershipreport-pdf/{sectionId}/{dateStr}/{filename}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadSectionMemberLeadershipReport(HttpServletRequest request, @PathVariable String sectionId, @PathVariable String dateStr, @PathVariable String filename) {

        try {
            Section section = sectionAccessor.get(sectionId);
            filename = summaryReportAccessor.getTempReportDir()+filename;
            byte[] fileBytes = Files.readAllBytes(Paths.get(filename));
            String newFilename = String.format("NetLog-SectionMemberLeadershipReport-%s-%s.pdf", section.getName(), dateStr);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\""+newFilename+"\"")
                    .body(fileBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(path = "/archivedEvents-quarterlyreport-pdf/{sectionId}/{dateStr}/{filename}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadArchivedEventQuarterlyReport(HttpServletRequest request, @PathVariable String sectionId, @PathVariable String dateStr, @PathVariable String filename) {

        try {
            Section section = sectionAccessor.get(sectionId);
            filename = summaryReportAccessor.getTempReportDir()+filename;
            byte[] fileBytes = Files.readAllBytes(Paths.get(filename));
            String newFilename = String.format("NetLog-QuarterlySummaryReport-%s-%s.pdf", section.getName(), dateStr);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\""+newFilename+"\"")
                    .body(fileBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/archivedEvents-quarterlyparticipationreport")
    public ModelAndView archivedEventsQuarterlyParticipationReport(HttpServletRequest request) {
        List<Section> sections = sectionAccessor.list();
        return new ModelAndView("archivedEvents-quarterlyparticipationreport", ViewModels.mapOf("sections", sections));
    }

    @GetMapping("/section-quarterlyparticipationreport/{id}")
    public ModelAndView sectionQuarterlyParticipationReport(HttpServletRequest request, @PathVariable String id) {
        List<Section> sections = new ArrayList<>();
        Section section = sectionAccessor.get(id);
        sections.add(section);
        return new ModelAndView("archivedEvents-quarterlyparticipationreport", ViewModels.mapOf("sections", sections));
    }


    @PostMapping("/archivedEvents-quarterlyparticipationreport-action")
    ResponseEntity<?> archivedEventsQuarterlyParticipationReportAction(HttpServletRequest request, @Valid @ModelAttribute QuarterlyReportRequest actionData) {

        Section section = sectionAccessor.get(actionData.sectionId());
        List<SectionOperator> members = operatorAccessor.listOperators(section);
        Map<Event, List<Participant>> eventParticipants = getEvents(section, actionData.quarter(), actionData.year(), members);
        String dateStr = actionData.quarter()+"-"+actionData.year();

        String filename = participationReportAccessor.generateReport(section, eventParticipants, members, dateStr, "QUARTERLY PARTICIPATION");
        if (filename == null) {
            return ResponseEntity.internalServerError().body("Could not create report");
        }

        return ResponseEntity.status(303).location(WebUris.path("/archivedEvents-quarterlyparticipationreport-pdf/"+section.getId()+"/"+dateStr+"/"+filename)).build();
    }

    @GetMapping(path = "/archivedEvents-quarterlyparticipationreport-pdf/{sectionId}/{dateStr}/{filename}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadArchivedEventQuarterlyParticipationReport(HttpServletRequest request, @PathVariable String sectionId, @PathVariable String dateStr, @PathVariable String filename) {

        try {
            Section section = sectionAccessor.get(sectionId);
            filename = participationReportAccessor.getTempReportDir()+filename;
            byte[] fileBytes = Files.readAllBytes(Paths.get(filename));
            String newFilename = String.format("NetLog-QuarterlyParticipationReport-%s-%s.pdf", section.getName(), dateStr);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\""+newFilename+"\"")
                    .body(fileBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/archivedEvent-delete/{id}")
    public ModelAndView archivedEventDelete(HttpServletRequest request,  @PathVariable String id) {
        Event event = eventAccessor.get(id);
        return new ModelAndView("archivedEvent-delete", ViewModels.mapOf("event", event));
    }


    @PostMapping("/archivedEvent-delete-action/{id}")
    ResponseEntity<?> archivedEventDeleteAction(HttpServletRequest request, @PathVariable String id) {
        Event event = eventAccessor.get(id);

        // delete participants
        List<Participant> participants = participantAccessor.listParticipants(event, false);
        for (Participant participant : participants) {
            participantAccessor.delete(participant.getId());
        }

        eventAccessor.delete(id);

        return ResponseEntity.status(303).location(WebUris.path("/archives")).build();
    }

    @GetMapping("/")
    public ModelAndView home(HttpServletRequest request) {
        List<Section> sections = sectionAccessor.list();
        List<Operator> operators = operatorAccessor.list();
        List<Event> activeEvents = eventAccessor.list(true, true);
        List<RecurringEvent> recurringEvents = recurringEventAccessor.list(true);

        int sectionCount = 0;
        int operatorCount = 0;
        int activeEventsCount = 0;
        int recurringEventsCount = 0;
        int pendingEventsCount = 0;

        if (sections != null) {
            sectionCount = sections.size();
        }
        if (operators != null) {
            operatorCount = operators.size();
        }
        if (activeEvents != null) {
            activeEventsCount = activeEvents.size();
            for (Event event : activeEvents) {
                if (event.getState().equals(EventState.PENDING)) {
                    pendingEventsCount++;
                }
            }
        }
        if (recurringEvents != null) {
            recurringEventsCount = recurringEvents.size();
        }
        return new ModelAndView("home", ViewModels.mapOf("sectionCount", sectionCount, "operatorCount", operatorCount, "activeEventsCount", activeEventsCount,
                                                        "pendingEventsCount", pendingEventsCount, "recurringEventsCount", recurringEventsCount));
    }

    @GetMapping("/license")
    public ModelAndView license(HttpServletRequest request) {
        return new ModelAndView("license");
    }

    @GetMapping("/sections")
    public ModelAndView sections(HttpServletRequest request) {
       List<Section> sections = sectionAccessor.list();
       return new ModelAndView("sections", ViewModels.mapOf("sections", sections));
    }

    @GetMapping("/section/{id}")
    public ModelAndView sectionDetails(HttpServletRequest request, @PathVariable String id) {
        Section section = sectionAccessor.get(id);
        if (section == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        List<District> districts = sectionAccessor.listDistricts(section);
        List<Location> municipalities = new ArrayList<>();
        for (District district : districts) {
            municipalities.addAll(sectionAccessor.listLocations(district));
        }
        List<SectionOperator> operators = operatorAccessor.listOperatorsInLocations(section, municipalities);
        return new ModelAndView("section", ViewModels.mapOf("section", section, "operators", operators, "operatorCount", operators.size(),
                                                                "districts", districts, "districtsCount", districts.size()));
    }

    @GetMapping("/section-edit/{id}")
    public ModelAndView sectionEdit(HttpServletRequest request,  @PathVariable String id) {
        Section section = sectionAccessor.get(id);
        List<SectionOperator> sectionOperators = operatorAccessor.listOperators(section);
        List<Operator> operators = operatorAccessor.list();
        removeMemberOperators(operators, sectionOperators);
        String sectionOperatorsString = buildSectionOperatorsString(sectionOperators);

        return new ModelAndView("section-edit", ViewModels.mapOf("section", section, "sectionOperators", sectionOperators, "sectionOperatorsString", sectionOperatorsString, "operators", operators));
    }


    @PostMapping("/section-edit-action/{id}")
    ResponseEntity<?> sectionEditAction(HttpServletRequest request, @Valid @ModelAttribute SectionCreateRequest actionData,  @PathVariable String id) {
        Section section = sectionAccessor.get(id);
        if (section == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        List<SectionOperator> requestedMembers = actionData.callsignMemberIds() == null
                ? null : convertToSectionOperatorList(actionData.callsignMemberIds());
        section.setName(actionData.name());
        section.setDescription(actionData.description());
        section.setLeaderName(actionData.leaderName());
        section.setLeaderContact(actionData.leaderContact());

        sectionAccessor.update(id, section);

        if (requestedMembers != null) {
            addMembers(section, requestedMembers);
        }

        return ResponseEntity.status(303).location(WebUris.path("/section/"+id)).build();
    }

    @GetMapping("/section-delete/{id}")
    public ModelAndView sectionDelete(HttpServletRequest request,  @PathVariable String id) {
        Section section = sectionAccessor.get(id);
        return new ModelAndView("section-delete", ViewModels.mapOf("section", section));
    }


    @PostMapping("/section-delete-action/{id}")
    ResponseEntity<?> sectionDeleteAction(HttpServletRequest request,  @PathVariable String id) {
        boolean softFlag = true;

        Section section = sectionAccessor.get(id);

        if (softFlag) {
            section.setActive(false);
            sectionAccessor.update(id, section);

            List<Event> events = eventAccessor.listBySection(section, true, false);
            for (Event event : events) {
                event.setSectionActive(false);
                eventAccessor.update(event.getId(), event);
            }

            List<RecurringEvent> recurringEvents = recurringEventAccessor.listBySection(section, true);
            for (RecurringEvent recurringEvent : recurringEvents) {
                recurringEvent.setSectionActive(false);
                recurringEventAccessor.update(recurringEvent.getId(), recurringEvent);
            }
        } else {
            sectionAccessor.delete(section.getId(), true);

            List<Event> events = eventAccessor.listBySection(section, true, false);
            for (Event event : events) {
                eventAccessor.delete(event.getId());
            }

            List<RecurringEvent> recurringEvents = recurringEventAccessor.listBySection(section, true);
            for (RecurringEvent recurringEvent : recurringEvents) {
                recurringEventAccessor.delete(recurringEvent.getId());
            }
        }

        return ResponseEntity.status(303).location(WebUris.path("/sections")).build();
    }

    @GetMapping("/section-add")
    public ModelAndView sectionAdd(HttpServletRequest request) {
        return new ModelAndView("section-add", ViewModels.mapOf("form", ""));
    }


    @PostMapping("/section-add-action")
    ResponseEntity<?> sectionAddAction(HttpServletRequest request, @Valid @ModelAttribute SectionCreateRequest actionData) {
        Section section = new Section();
        section.setName(actionData.name());
        section.setDescription(actionData.description());
        section.setLeaderName(actionData.leaderName());
        section.setLeaderContact(actionData.leaderContact());

        Section sectionNew = sectionAccessor.create(section);
        if (sectionNew != null) {
            return ResponseEntity.status(303).location(WebUris.path("/section/"+sectionNew.getId())).build();
        }

        return ResponseEntity.status(303).location(WebUris.path("/sections")).build();
    }


    @GetMapping("/section-monthlynonparticipationreport/{id}")
    public ModelAndView sectionMonthlyNonParticipationReport(HttpServletRequest request, @PathVariable String id) {
        Section section = sectionAccessor.get(id);
        return new ModelAndView("section-monthlynonparticipationreport", ViewModels.mapOf("section", section));
    }


    @PostMapping("/section-monthlynonparticipationreport-action")
    ResponseEntity<?> sectionMonthlyNonParticipationReportAction(HttpServletRequest request, @Valid @ModelAttribute MonthlyReportRequest actionData) {

        Section section = sectionAccessor.get(actionData.sectionId());
        List<SectionOperator> members = operatorAccessor.listOperators(section);
        Map<Event, List<Participant>> eventParticipants = getEvents(section, actionData.month(), actionData.year(), members);
        String dateStr = actionData.month()+"-"+actionData.year();

        String filename = nonParticipationReportAccessor.generateReport(section, members, eventParticipants, dateStr, "MONTHLY NON-PARTICIPATION");
        if (filename == null) {
            return ResponseEntity.internalServerError().body("Could not create report");
        }

        return ResponseEntity.status(303).location(WebUris.path("/section-monthlynonparticipationreport-pdf/"+section.getId()+"/"+dateStr+"/"+filename)).build();
    }

    @GetMapping(path = "/section-monthlynonparticipationreport-pdf/{sectionId}/{dateStr}/{filename}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadSectionMonthlyNonParticipationReport(HttpServletRequest request, @PathVariable String sectionId, @PathVariable String dateStr, @PathVariable String filename) {

        try {
            Section section = sectionAccessor.get(sectionId);
            filename = nonParticipationReportAccessor.getTempReportDir()+filename;
            byte[] fileBytes = Files.readAllBytes(Paths.get(filename));
            String newFilename = String.format("NetLog-MonthlyNonParticipationReport-%s-%s.pdf", section.getName(), dateStr);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\""+newFilename+"\"")
                    .body(fileBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/section-quarterlynonparticipationreport/{id}")
    public ModelAndView sectionQuarterlyNonParticipationReport(HttpServletRequest request, @PathVariable String id) {
        Section section = sectionAccessor.get(id);
        return new ModelAndView("section-quarterlynonparticipationreport", ViewModels.mapOf("section", section));
    }


    @PostMapping("/section-quarterlynonparticipationreport-action")
    ResponseEntity<?> sectionQuarterlyNonParticipationReportAction(HttpServletRequest request, @Valid @ModelAttribute QuarterlyReportRequest actionData) {
        Section section = sectionAccessor.get(actionData.sectionId());
        List<SectionOperator> members = operatorAccessor.listOperators(section);
        Map<Event, List<Participant>> eventParticipants = getEvents(section, actionData.quarter(), actionData.year(), members);
        String dateStr = actionData.quarter()+"-"+actionData.year();

        String filename = nonParticipationReportAccessor.generateReport(section, members, eventParticipants, dateStr, "QUARTERLY NON-PARTICIPATION");
        if (filename == null) {
            return ResponseEntity.internalServerError().body("Could not create report");
        }

        return ResponseEntity.status(303).location(WebUris.path("/section-quarterlynonparticipationreport-pdf/"+section.getId()+"/"+dateStr+"/"+filename)).build();
    }

    @GetMapping(path = "/section-quarterlynonparticipationreport-pdf/{sectionId}/{dateStr}/{filename}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadSectionQuarterlyNonParticipationReport(HttpServletRequest request, @PathVariable String sectionId, @PathVariable String dateStr, @PathVariable String filename) {
        try {
            Section section = sectionAccessor.get(sectionId);
            filename = nonParticipationReportAccessor.getTempReportDir()+filename;
            byte[] fileBytes = Files.readAllBytes(Paths.get(filename));
            String newFilename = String.format("NetLog-QuarterlyNonParticipationReport-%s-%s.pdf", section.getName(), dateStr);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\""+newFilename+"\"")
                    .body(fileBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/operators")
    public ModelAndView operators(HttpServletRequest request) {
        List<Operator> operators = operatorAccessor.list();
        String selectedId = request.getParameter("selected");
        Map<String, Object> model = ViewModels.mapOf("operators", operators, "selectedId", selectedId);
        if (selectedId != null) {
            boolean available = operators.stream().anyMatch(operator -> selectedId.equals(operator.getId()));
            if (available) {
                model.putAll(operatorDetailsModel(selectedId, false));
            } else {
                model.put("selectionUnavailable", true);
            }
        }
        return new ModelAndView("operators", model);
    }

    @GetMapping("/operator/{id}")
    public ModelAndView operatorDetails(HttpServletRequest request, @PathVariable String id) {
        return new ModelAndView("operator", operatorDetailsModel(id, true));
    }

    @GetMapping("/operator/{id}/details-pane")
    public String operatorDetailsPane(@PathVariable String id, Model model) {
        Map<String, Object> details = operatorDetailsModel(id, false);
        if (details.get("operator") == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        model.addAllAttributes(details);
        return "fragments/operator-details :: content";
    }

    private Map<String, Object> operatorDetailsModel(String id, boolean standalone) {
        Operator operator = operatorAccessor.get(id);
        List<SectionOperator> sectionOperators = operatorAccessor.listSections(operator);
        hydrateSectionOperators(sectionOperators);
        return ViewModels.mapOf("operator", operator, "sectionOperators", sectionOperators,
                "standaloneDetails", standalone);
    }

    @GetMapping("/operator-edit/{id}")
    public ModelAndView operatorEdit(HttpServletRequest request,  @PathVariable String id) {
        Operator operator = operatorAccessor.get(id);
        return new ModelAndView("operator-edit", ViewModels.mapOf("operator", operator,
                                     "isSkywarnChecked", operator.isSkywarn() ? "yes" : null,
                                     "isNTSChecked", operator.isNTS() ? "yes" : null,
                                     "isRACESChecked", operator.isRACES() ? "yes" : null));
    }


    @PostMapping("/operator-edit-action/{id}")
    ResponseEntity<?> operatorEditAction(HttpServletRequest request, @Valid @ModelAttribute OperatorCreateRequest actionData,  @PathVariable String id) {
        Operator operator = operatorAccessor.get(id);
        operator.setName(actionData.name());
        operator.setCallsign(actionData.callsign());

        boolean isNTS = false;
        if (actionData.isNTS() != null) {
            isNTS = true;
        }
        boolean isSkywarn = false;
        if (actionData.isSkywarn() != null) {
            isSkywarn = true;
        }
        boolean isRACES = false;
        if (actionData.isRACES() != null) {
            isRACES = true;
        }
        operator.setNTS(isNTS);
        operator.setSkywarn(isSkywarn);
        operator.setRACES(isRACES);

        operatorAccessor.update(id, operator);

        return ResponseEntity.status(303).location(WebUris.path("/operator/"+id)).build();
    }

    @GetMapping("/operator-add")
    public ModelAndView operatorAdd(HttpServletRequest request) {
        return new ModelAndView("operator-add", ViewModels.mapOf("form", ""));
    }


    @PostMapping("/operator-add-action")
    ResponseEntity<?> operatorAddAction(HttpServletRequest request, @Valid @ModelAttribute OperatorCreateRequest actionData) {
        Operator operator = new Operator();
        operator.setCallsign(actionData.callsign());
        operator.setName(actionData.name());

        boolean isNTS = false;
        if (actionData.isNTS() != null) {
            isNTS = true;
        }
        boolean isSkywarn = false;
        if (actionData.isSkywarn() != null) {
            isSkywarn = true;
        }
        boolean isRACES = false;
        if (actionData.isRACES() != null) {
            isRACES = true;
        }
        operator.setNTS(isNTS);
        operator.setSkywarn(isSkywarn);
        operator.setRACES(isRACES);

        Operator operatorNew = operatorAccessor.create(operator);
        if (operatorNew != null) {
            return ResponseEntity.status(303).location(WebUris.path("/operator/"+operatorNew.getId())).build();
        }

        return ResponseEntity.status(303).location(WebUris.path("/operators")).build();
    }

    @GetMapping("/operator-addbulk")
    public ModelAndView operatorAddBulk(HttpServletRequest request) {
        return new ModelAndView("operator-addbulk", ViewModels.mapOf("form", ""));
    }


    @PostMapping("/operator-addbulk-action")
    ResponseEntity<?> operatorAddBulkAction(HttpServletRequest request, @Valid @ModelAttribute OperatorBulkCreateRequest actionData) {

        addAllOperators(actionData.bulkList());

        return ResponseEntity.status(303).location(WebUris.path("/operators")).build();
    }

    private void addAllOperators(String bulkList) {
        String [] lines = bulkList.lines().toArray(String[]::new);
        for (String line : lines) {
            if (line.length() == 0) {
                continue;
            }
            String [] fields = line.split(",");
            Operator operator = new Operator();
            try {
                if (fields.length == 1) {
                    operator.setCallsign(line.replace("\"", ""));
                } else if (fields.length == 2) {
                    String callsign = fields[0].replace("\"", "");
                    String name = fields[1].replace("\"", "");
                    operator.setCallsign(callsign);
                    operator.setName(name);
                } else if (fields.length > 2) {
                    int index = line.indexOf(",");
                    String callsign = line.substring(0, index).replace("\"", "");
                    String name = line.substring(index+1).replace("\"", "");
                    operator.setCallsign(callsign);
                    operator.setName(name);
                }
                Operator newOperator = operatorAccessor.create(operator);
                if ((newOperator != null) && (!newOperator.getName().equalsIgnoreCase(operator.getName()))) {
                    // this was previously created without a name  - set the name
                    newOperator.setName(operator.getName());
                    operatorAccessor.update(newOperator.getId(), newOperator);
                }
            } catch (Exception e) {
            }
        }
    }

    @GetMapping("/operator-delete/{id}")
    public ModelAndView operatorDelete(HttpServletRequest request,  @PathVariable String id) {
        Operator operator = operatorAccessor.get(id);
        return new ModelAndView("operator-delete", ViewModels.mapOf("operator", operator));
    }


    @PostMapping("/operator-delete-action/{id}")
    ResponseEntity<?> operatorDeleteAction(HttpServletRequest request, @PathVariable String id) {
        operatorAccessor.get(id);
        operatorAccessor.delete(id);

        return ResponseEntity.status(303).location(WebUris.path("/operators")).build();
    }

    @GetMapping("/recurringEvents")
    public ModelAndView recurringEvents(HttpServletRequest request) {
        List<RecurringEvent> recurringEvents = recurringEventAccessor.list(true);
        hydrateRecurringEvents(recurringEvents);

        String selectedId = request.getParameter("selected");
        Map<String, Object> model = ViewModels.mapOf("recurringEvents", recurringEvents, "selectedId", selectedId);
        if (selectedId != null) {
            boolean available = recurringEvents.stream().anyMatch(event -> selectedId.equals(event.getId()));
            if (available) {
                model.putAll(recurringEventDetailsModel(selectedId, false));
            } else {
                model.put("selectionUnavailable", true);
            }
        }
        return new ModelAndView("recurringEvents", model);
    }

    @GetMapping("/recurringEvents-report")
    public ModelAndView recurringEventsReport(HttpServletRequest request) {
        List<Section> sections = sectionAccessor.list();
        return new ModelAndView("recurringEvents-report", ViewModels.mapOf("sections", sections));
    }


    @PostMapping("/recurringEvents-report-action")
    ResponseEntity<?> recurringEventsReportAction(HttpServletRequest request, @Valid @ModelAttribute RecurringEventReportRequest actionData) {

        Section section = sectionAccessor.get(actionData.sectionId());
        String title = "EVENTS";
        List<RecurringEvent> recurringEvents = recurringEventAccessor.listBySection(section, true);
        if (actionData.type().equals("NETS")) {
            List<RecurringEvent> recurringEventsNetsOnly = new ArrayList<>();
            for (RecurringEvent recurringEvent : recurringEvents) {
                if (recurringEvent.getType().equals(EventType.NET)) {
                    recurringEventsNetsOnly.add(recurringEvent);
                }
            }
            recurringEvents = recurringEventsNetsOnly;
            title = "NETS";
        }

        String filename = recurringEventReportAccessor.generateReport(section, recurringEvents, title);
        if (filename == null) {
            return ResponseEntity.internalServerError().body("Could not create report");
        }

        return ResponseEntity.status(303).location(WebUris.path("/recurringEvents-report-pdf/"+section.getId()+"/"+filename)).build();
    }

    @GetMapping(path = "/recurringEvents-report-pdf/{sectionId}/{filename}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadRecurringEventsReport(HttpServletRequest request, @PathVariable String sectionId, @PathVariable String filename) {

        try {
            Section section = sectionAccessor.get(sectionId);
            filename = participationReportAccessor.getTempReportDir()+filename;
            byte[] fileBytes = Files.readAllBytes(Paths.get(filename));
            String newFilename = String.format("NetLog-EventSchedule-%s.pdf", section.getName());
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\""+newFilename+"\"")
                    .body(fileBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/recurringEvent/{id}")
    public ModelAndView recurringEventsDetails(HttpServletRequest request, @PathVariable String id) {
        return new ModelAndView("recurringEvent", recurringEventDetailsModel(id, true));
    }

    @GetMapping("/recurringEvent/{id}/details-pane")
    public String recurringEventDetailsPane(@PathVariable String id, Model model) {
        Map<String, Object> details = recurringEventDetailsModel(id, false);
        RecurringEvent event = (RecurringEvent) details.get("recurringEvent");
        if (event == null || !event.isSectionActive()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        model.addAllAttributes(details);
        return "fragments/recurring-event-details :: content";
    }

    private Map<String, Object> recurringEventDetailsModel(String id, boolean standalone) {
        RecurringEvent recurringEvent = recurringEventAccessor.get(id);
        if (recurringEvent != null) {
            hydrateRecurringEvent(recurringEvent);
        }
        return ViewModels.mapOf("recurringEvent", recurringEvent, "standaloneRecurringDetails", standalone);
    }

    @GetMapping("/recurringEvent-edit/{id}")
    public ModelAndView recurringEventEdit(HttpServletRequest request,  @PathVariable String id) {
        RecurringEvent recurringEvent = recurringEventAccessor.get(id);
        hydrateRecurringEvent(recurringEvent);
        return new ModelAndView("recurringEvent-edit", ViewModels.mapOf("recurringEvent", recurringEvent, "recurringEventType"+recurringEvent.getType().ordinal(), "yes"));
    }


    @PostMapping("/recurringEvent-edit-action/{id}")
    ResponseEntity<?> recurringEventEditAction(HttpServletRequest request, @Valid @ModelAttribute RecurringEventCreateRequest actionData,  @PathVariable String id) {
        RecurringEvent recurringEvent = recurringEventAccessor.get(id);
        recurringEvent.setName(actionData.name());
        recurringEvent.setDescription(actionData.description());
        recurringEvent.setLocation(actionData.location());
        recurringEvent.setSchedule(actionData.schedule());
        recurringEvent.setType(EventType.values()[actionData.type()]);
        recurringEvent.setNetControlCallsign(actionData.netControlCallsign());

        recurringEventAccessor.update(id, recurringEvent);

        return ResponseEntity.status(303).location(WebUris.path("/recurringEvent/"+id)).build();
    }

    @GetMapping("/recurringEvent-add")
    public ModelAndView recurringEventAdd(HttpServletRequest request) {
        List<Section> sections = sectionAccessor.list();

        return new ModelAndView("recurringEvent-add", ViewModels.mapOf("sections", sections));
    }


    @PostMapping("/recurringEvent-add-action")
    ResponseEntity<?> recurringEventAddAction(HttpServletRequest request, @Valid @ModelAttribute RecurringEventCreateRequest actionData) {
        Section section = sectionAccessor.get(actionData.sectionId());

        RecurringEvent recurringEvent = new RecurringEvent();
        recurringEvent.setName(actionData.name());
        recurringEvent.setDescription(actionData.description());
        recurringEvent.setLocation(actionData.location());
        recurringEvent.setSchedule(actionData.schedule());
        recurringEvent.setType(EventType.values()[actionData.type()]);
        recurringEvent.setNetControlCallsign(actionData.netControlCallsign());
        recurringEvent.setSectionActive(section.isActive());
        recurringEvent.setSection(section);

        RecurringEvent recurringEventNew = recurringEventAccessor.createBySection(section, recurringEvent);
        if (recurringEventNew != null) {
            return ResponseEntity.status(303).location(WebUris.path("/recurringEvent/"+recurringEventNew.getId())).build();
        }

        return ResponseEntity.status(303).location(WebUris.path("/recurringEvents")).build();
    }

    @GetMapping("/recurringEvent-delete/{id}")
    public ModelAndView recurringEventDelete(HttpServletRequest request,  @PathVariable String id) {
        RecurringEvent recurringEvent = recurringEventAccessor.get(id);
        return new ModelAndView("recurringEvent-delete", ViewModels.mapOf("recurringEvent", recurringEvent));
    }


    @PostMapping("/recurringEvent-delete-action/{id}")
    ResponseEntity<?> recurringEventDeleteAction(HttpServletRequest request, @PathVariable String id) {

        RecurringEvent recurringEvent = recurringEventAccessor.get(id);

        List<Event> events = eventAccessor.listByRecurringEvent(recurringEvent, true, false);
        for (Event event : events) {
            List<Participant> participants = participantAccessor.listParticipants(event, false);
            for (Participant participant : participants) {
                participantAccessor.delete(participant.getId());
            }
            eventAccessor.delete(event.getId());
        }
        recurringEventAccessor.delete(id);

        return ResponseEntity.status(303).location(WebUris.path("/recurringEvents")).build();
    }

    @GetMapping("/recurringEvent-start/{id}")
    public ModelAndView recurringEventStart(HttpServletRequest request,  @PathVariable String id) {
        RecurringEvent recurringEvent = recurringEventAccessor.get(id);
        return new ModelAndView("recurringEvent-start", ViewModels.mapOf("recurringEvent", recurringEvent, "startTimeStr", LocalDateTime.now().toString().substring(0, 16)));
    }


    @PostMapping("/recurringEvent-start-action/{id}")
    ResponseEntity<?> recurringEventStartAction(HttpServletRequest request, @Valid @ModelAttribute ParticipantStartEndRequest actionData, @PathVariable String id) {
        RecurringEvent recurringEvent = recurringEventAccessor.get(id);

        Event eventNew = createEvent(recurringEvent, actionData, EventState.STARTED);
        if (eventNew != null) {
            return ResponseEntity.status(303).location(WebUris.path("/activeEvent/"+eventNew.getId())).build();
        }

        return ResponseEntity.status(303).location(WebUris.path("/recurringEvents")).build();
    }

    @GetMapping("/recurringEvent-schedule/{id}")
    public ModelAndView recurringEventSchedule(HttpServletRequest request,  @PathVariable String id) {
        RecurringEvent recurringEvent = recurringEventAccessor.get(id);
        return new ModelAndView("recurringEvent-schedule", ViewModels.mapOf("recurringEvent", recurringEvent, "startTimeStr", LocalDateTime.now().toString().substring(0, 16), "weeks", 1));
    }


    @PostMapping("/recurringEvent-schedule-action/{id}")
    ResponseEntity<?> recurringEventScheduleAction(HttpServletRequest request, @Valid @ModelAttribute ActiveEventScheduleRequest actionData, @PathVariable String id) {
        RecurringEvent recurringEvent = recurringEventAccessor.get(id);

        createEvents(recurringEvent, actionData, EventState.PENDING);

        return ResponseEntity.status(303).location(WebUris.path("/activeEvents")).build();
    }

    private Event createEvent(RecurringEvent recurringEvent, ParticipantStartEndRequest actionData, EventState state) {
        Event event = new Event();
        event.setDescription(recurringEvent.getDescription());
        event.setLocation(recurringEvent.getLocation());
        event.setName(recurringEvent.getName());
        event.setRecurringEvent(recurringEvent);
        event.setSection(recurringEvent.getSection());
        event.setState(state);
        event.setStartTime(LocalDateTime.parse(actionData.startTimeStr()));
        event.setType(recurringEvent.getType());
        event.setNetControlCallsign(recurringEvent.getNetControlCallsign());
        event.setSectionActive(true);

        Event eventNew = eventAccessor.create(recurringEvent.getSection(), recurringEvent, event);
        return eventNew;
    }

    private List<Event> createEvents(RecurringEvent recurringEvent, ActiveEventScheduleRequest actionData, EventState state) {
        List<Event> ret = new ArrayList<>();
        LocalDateTime startTime = LocalDateTime.parse(actionData.startTimeStr());

        for (int i = 0; i < actionData.weeks(); i++) {
            Event event = new Event();
            event.setDescription(recurringEvent.getDescription());
            event.setLocation(recurringEvent.getLocation());
            event.setName(recurringEvent.getName());
            event.setRecurringEvent(recurringEvent);
            event.setSection(recurringEvent.getSection());
            event.setState(state);
            event.setStartTime(startTime);
            event.setType(recurringEvent.getType());
            event.setNetControlCallsign(recurringEvent.getNetControlCallsign());
            event.setSectionActive(true);

            Event eventNew = eventAccessor.create(recurringEvent.getSection(), recurringEvent, event);
            ret.add(eventNew);

            startTime = startTime.plusDays(7);
        }
        return ret;
    }

    @GetMapping("/activeEvent-add")
    public ModelAndView activeEventAdd() {
        List<Section> sections = new ArrayList<>(sectionAccessor.list());
        sections.removeIf(section -> !section.isActive());
        return new ModelAndView("activeEvent-add", ViewModels.mapOf("sections", sections));
    }

    @PostMapping("/activeEvent-add-action")
    public ResponseEntity<?> activeEventAddAction(@Valid @ModelAttribute ActiveEventCreateRequest actionData) {
        Section section = sectionAccessor.get(actionData.sectionId());
        if (section == null || !section.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose an active section");
        }

        EventState state = EventState.valueOf(actionData.startMode());
        LocalDateTime startTime = LocalDateTime.now();
        if (state == EventState.PENDING) {
            try {
                startTime = LocalDateTime.parse(actionData.startTimeStr());
            } catch (java.time.format.DateTimeParseException | NullPointerException exception) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a scheduled start time");
            }
        }

        Event event = new Event();
        event.setName(actionData.name());
        event.setDescription(actionData.description());
        event.setType(EventType.values()[actionData.type()]);
        event.setLocation(actionData.location());
        event.setNetControlCallsign(actionData.netControlCallsign());
        event.setState(state);
        event.setStartTime(startTime);
        event.setSectionActive(true);
        Event created = eventAccessor.createOneTime(section, event);
        if (created == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to create event");
        }
        return ResponseEntity.status(303)
                .location(UriComponentsBuilder.fromPath("/activeEvents")
                        .queryParam("selected", created.getId()).build().encode().toUri()).build();
    }

    @GetMapping("/activeEvents")
    public ModelAndView activeEvents(HttpServletRequest request) {
        List<Event> activeEvents = eventAccessor.list(true, true);

        hydrateActiveEvents(activeEvents);
        String selectedId = request.getParameter("selected");
        Map<String, Object> model = ViewModels.mapOf("activeEvents", activeEvents, "selectedId", selectedId);
        if (selectedId != null) {
            boolean available = activeEvents.stream().anyMatch(event -> selectedId.equals(event.getId()));
            if (available) {
                model.putAll(activeEventDetailsModel(selectedId, false));
            } else {
                model.put("selectionUnavailable", true);
            }
        }
        return new ModelAndView("activeEvents", model);
    }

    @GetMapping("/activeEvent/{id}")
    public ModelAndView activeEventsDetails(HttpServletRequest request, @PathVariable String id) {
        return new ModelAndView("activeEvent", activeEventDetailsModel(id, true));
    }

    @GetMapping("/activeEvent/{id}/details-pane")
    public String activeEventDetailsPane(@PathVariable String id, Model model) {
        Map<String, Object> details = activeEventDetailsModel(id, false);
        Event event = (Event) details.get("event");
        if (event == null || !event.isSectionActive()
                || event.getState() == EventState.SECURE || event.getState() == EventState.NOT_HELD) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        model.addAllAttributes(details);
        return "fragments/active-event-details :: content";
    }

    private Map<String, Object> activeEventDetailsModel(String id, boolean standalone) {
        Event event = eventAccessor.get(id);
        if (event == null) {
            return ViewModels.mapOf("event", null);
        }
        hydrateActiveEvent(event);

        List<Participant> participants = participantAccessor.listParticipants(event, false);
        hydrateParticipants(event, participants);

        Collections.sort(participants, new Comparator<Participant>() {
            @Override
            public int compare(Participant obj1, Participant obj2) {
                return obj1.getOperator().getCallsign().compareTo(obj2.getOperator().getCallsign());
            }
        });

        boolean isScheduled = event.getState().equals(EventState.PENDING);
        boolean isStarted = event.getState().equals(EventState.STARTED);
        return ViewModels.mapOf("event", event, "participants", participants, "participantCount", participants.size(),
                "isScheduled", isScheduled, "isStarted", isStarted, "standaloneActiveDetails", standalone);
    }

    @GetMapping("/activeEvent-edit/{id}")
    public ModelAndView activeEventEdit(HttpServletRequest request,  @PathVariable String id) {
        Event event = eventAccessor.get(id);
        hydrateActiveEvent(event);

        List<Participant> participants = participantAccessor.listParticipants(event, true);

        Collections.sort(participants, new Comparator<Participant>() {
            @Override
            public int compare(Participant obj1, Participant obj2) {
                return obj1.getOperator().getCallsign().compareTo(obj2.getOperator().getCallsign());
            }
        });

        return new ModelAndView("activeEvent-edit", ViewModels.mapOf("event", event, "eventType"+event.getType().ordinal(), "yes",  "participants", participants,
                                        "checkInTimeStr", event.getStartTime().toString()));
    }


    @PostMapping("/activeEvent-edit-action/{id}")
    ResponseEntity<?> activeEventEditAction(HttpServletRequest request, @Valid @ModelAttribute ActiveEventEditRequest actionData,  @PathVariable String id) {
        Event event = eventAccessor.get(id);
        event.setName(actionData.name());
        event.setDescription(actionData.description());
        event.setLocation(actionData.location());
        event.setStartTime(LocalDateTime.parse(actionData.startTimeStr()));
        // no setting secure here
        event.setType(EventType.values()[actionData.type()]);
        event.setNetControlCallsign(actionData.netControlCallsign());

        event = eventAccessor.update(id, event);

        return ResponseEntity.status(303)
                .location(UriComponentsBuilder.fromPath("/activeEvents")
                        .queryParam("selected", id).build().encode().toUri()).build();
    }

    @GetMapping("/activeEvent-checkin/{id}")
    public ModelAndView activeEventCheckin(HttpServletRequest request,  @PathVariable String id) {
        Event event = eventAccessor.get(id);
        hydrateActiveEvent(event);

        List<Operator> operators = operatorAccessor.list();
        List<Participant> participants = participantAccessor.listParticipants(event, true);
        removeMemberParticipants(operators, participants);
        List<Operator> previousOperators = determinePreviousParticipants(event, operators);

        Collections.sort(participants, new Comparator<Participant>() {
            @Override
            public int compare(Participant obj1, Participant obj2) {
                return obj1.getOperator().getCallsign().compareTo(obj2.getOperator().getCallsign());
            }
        });

        Collections.sort(previousOperators, new Comparator<Operator>() {
            @Override
            public int compare(Operator obj1, Operator obj2) {
                return obj1.getCallsign().compareTo(obj2.getCallsign());
            }
        });

        return new ModelAndView("activeEvent-checkin", ViewModels.mapOf("event", event, "checkInTimeStr", event.getStartTime().toString(), "operators", operators, "previousOperators", previousOperators));
    }


    @PostMapping("/activeEvent-checkin-action/{id}")
    ResponseEntity<?> activeEventCheckinAction(HttpServletRequest request, @Valid @ModelAttribute ActiveEventCheckInRequest actionData,  @PathVariable String id) {
        Event event = eventAccessor.get(id);

        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        if (actionData.participants() != null) {
            // Validate all operators before creating any participant records.
            for (ParticipantCheckInRequest participant : actionData.participants()) {
                if (participant == null || operatorAccessor.get(participant.operatorId()) == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose an existing operator");
                }
            }
            for (ParticipantCheckInRequest participant : actionData.participants()) {
                participantAccessor.checkInParticipant(event, operatorAccessor.get(participant.operatorId()),
                        participant.checkInTime(), participant.transmitPower() == null ? 0 : participant.transmitPower(),
                        participant.primaryPowerSource(), participant.secondaryPowerSource(),
                        participant.communicationPlatform(), participant.communicationBand());
            }
        } else {
            // Preserve submissions from the original operator/time-pair form.
            checkInParticipants(event, actionData.participantIds());
        }

        return ResponseEntity.status(303)
                .location(UriComponentsBuilder.fromPath("/activeEvents")
                        .queryParam("selected", id).build().encode().toUri()).build();
    }

    @GetMapping("/activeEvent-checkout/{id}")
    public ModelAndView activeEventCheckout(HttpServletRequest request,  @PathVariable String id) {
        Event event = eventAccessor.get(id);
        hydrateActiveEvent(event);

        List<Participant> participants = participantAccessor.listParticipants(event, true);
        Collections.sort(participants, new Comparator<Participant>() {
            @Override
            public int compare(Participant obj1, Participant obj2) {
                return obj1.getOperator().getCallsign().compareTo(obj2.getOperator().getCallsign());
            }
        });

        return new ModelAndView("activeEvent-checkout", ViewModels.mapOf("event", event, "participants", participants));
    }


    @PostMapping("/activeEvent-checkout-action/{id}")
    ResponseEntity<?> activeEventCheckoutAction(HttpServletRequest request, @Valid @ModelAttribute ActiveEventCheckInOutRequest actionData,  @PathVariable String id) {
        Event event = eventAccessor.get(id);

        checkOutParticipants(event, actionData.participantIds());

        return ResponseEntity.status(303)
                .location(UriComponentsBuilder.fromPath("/activeEvents")
                        .queryParam("selected", id).build().encode().toUri()).build();
    }

    @GetMapping("/activeEvent-start/{id}")
    public ModelAndView eventStart(HttpServletRequest request,  @PathVariable String id) {
        Event event = eventAccessor.get(id);
        LocalDateTime startTime = LocalDateTime.now();
        if (event.getStartTime() != null) {
            startTime = event.getStartTime();
        }
        return new ModelAndView("activeEvent-start", ViewModels.mapOf("event", event, "startTimeStr", startTime.toString().substring(0, 16)));
    }


    @PostMapping("/activeEvent-start-action/{id}")
    ResponseEntity<?> eventStartAction(HttpServletRequest request, @Valid @ModelAttribute ParticipantStartEndRequest actionData, @PathVariable String id) {
        Event event = eventAccessor.get(id);
        event.setStartTime(LocalDateTime.parse(actionData.startTimeStr()));
        event.setState(EventState.STARTED);
        Event eventNew = eventAccessor.update(id, event);
        if (eventNew != null) {
            return ResponseEntity.status(303)
                    .location(UriComponentsBuilder.fromPath("/activeEvents")
                            .queryParam("selected", eventNew.getId()).build().encode().toUri()).build();
        }

        return ResponseEntity.status(303).location(WebUris.path("/activeEvents")).build();
    }

    @GetMapping("/participant-edittimes/{id}/")
    public ModelAndView participantEdit(HttpServletRequest request,  @PathVariable String id) {
        Participant participant = participantAccessor.get(id);

        return new ModelAndView("participant-edittimes", ViewModels.mapOf("participant", participant, "showEndTime" , (participant.getEndTime() == null) ? null : "true",
                                        "primaryPowerSource"+participant.getPrimaryPower().ordinal(), "yes",
                                        "secondaryPowerSource"+participant.getBackupPower().ordinal(), "yes",
                                        "communicationPlatform"+participant.getCommunicationPlatform().ordinal(), "yes",
                                        "communicationBand"+participant.getCommunicationBand().ordinal(), "yes"));
    }


    @PostMapping("/participant-edittimes-action/{id}")
    ResponseEntity<?> participantEditAction(HttpServletRequest request, @Valid @ModelAttribute ParticipantStartEndRequest actionData,  @PathVariable String id) {
        Participant participant = participantAccessor.get(id);
        participant.setStartTime(LocalDateTime.parse(actionData.startTimeStr()));
        if (participant.getEndTime() != null) {
            participant.setEndTime(LocalDateTime.parse(actionData.endTimeStr()));
        }
        participant.setPrimaryPower(ElectricalPowerType.values()[actionData.primaryPowerSource()]);
        participant.setBackupPower(ElectricalPowerType.values()[actionData.secondaryPowerSource()]);
        participant.setCommunicationPlatform(CommunicationPlatform.values()[actionData.communicationPlatform()]);
        participant.setCommunicationBand(CommunicationBand.values()[actionData.communicationBand()]);
        participant.setTransmitPower(actionData.transmitPower());

        participantAccessor.update(id, participant);

        return ResponseEntity.status(303).location(WebUris.path("/activeEvent/"+participant.getEvent().getId())).build();
    }

    @GetMapping("/participant-remove/{id}")
    public ModelAndView participantRemove(HttpServletRequest request,  @PathVariable String id) {
        Participant participant = participantAccessor.get(id);
        Event event = eventAccessor.get(participant.getEvent().getId());
        participant.setEvent(event);
        return new ModelAndView("participant-remove", ViewModels.mapOf("participant", participant));
    }


    @PostMapping("/participant-remove-action/{id}")
    ResponseEntity<?> participantRemoveAction(HttpServletRequest request, @PathVariable String id) {
        Participant participant = participantAccessor.get(id);
        String eventId = participant.getEvent().getId();
        participantAccessor.delete(id);

        return ResponseEntity.status(303).location(WebUris.path("/activeEvent/"+eventId)).build();
    }

    @GetMapping("/activeEvent-secure/{id}")
    public ModelAndView activeEventSecure(HttpServletRequest request,  @PathVariable String id) {
        Event event = eventAccessor.get(id);
        return new ModelAndView("activeEvent-secure", ViewModels.mapOf("event", event, "endTimeStr", LocalDateTime.now().toString().substring(0, 16)));
    }


    @PostMapping("/activeEvent-secure-action/{id}")
    ResponseEntity<?> activeEventSecureAction(HttpServletRequest request, @Valid @ModelAttribute ParticipantStartEndRequest actionData, @PathVariable String id) {
        Event event = eventAccessor.get(id);

        // check out participants
        List<Participant> participants = participantAccessor.listParticipants(event, true);
        for (Participant participant : participants) {
            participantAccessor.checkOutParticipant(event, participant.getOperator(), LocalDateTime.parse(actionData.endTimeStr()));
        }

        event.setState(EventState.SECURE);
        event.setEndTime(LocalDateTime.parse(actionData.endTimeStr()));
        eventAccessor.update(id, event);

        return ResponseEntity.status(303).location(WebUris.path("/activeEvents")).build();
    }

    @GetMapping("/activeEvent-cancel/{id}")
    public ModelAndView activeEventCancel(HttpServletRequest request,  @PathVariable String id) {
        Event event = eventAccessor.get(id);
        return new ModelAndView("activeEvent-cancel", ViewModels.mapOf("event", event, "endTimeStr", LocalDateTime.now().toString().substring(0, 16)));
    }


    @PostMapping("/activeEvent-cancel-action/{id}")
    ResponseEntity<?> activeEventCancelAction(HttpServletRequest request, @PathVariable String id) {
        Event event = eventAccessor.get(id);

        // remove any participants if present
        List<Participant> participants = participantAccessor.listParticipants(event, true);
        for (Participant participant : participants) {
            participantAccessor.delete(participant.getId());
        }

        event.setState(EventState.NOT_HELD);
        event.setEndTime(LocalDateTime.now());
        eventAccessor.update(id, event);

        return ResponseEntity.status(303).location(WebUris.path("/activeEvents")).build();
    }

    @GetMapping("/activeEvent-delete/{id}")
    public ModelAndView activeEventDelete(HttpServletRequest request,  @PathVariable String id) {
        Event event = eventAccessor.get(id);
        return new ModelAndView("activeEvent-delete", ViewModels.mapOf("event", event));
    }


    @PostMapping("/activeEvent-delete-action/{id}")
    ResponseEntity<?> activeEventDeleteAction(HttpServletRequest request, @PathVariable String id) {
        Event event = eventAccessor.get(id);

        // delete participants
        List<Participant> participants = participantAccessor.listParticipants(event, false);
        for (Participant participant : participants) {
            participantAccessor.delete(participant.getId());
        }

        eventAccessor.delete(id);

        return ResponseEntity.status(303).location(WebUris.path("/activeEvents")).build();
    }

    private void hydrateRecurringEvents(List<RecurringEvent> recurringEvents) {
        List <Section> sections = sectionAccessor.list();
        if (sections == null) {
            return;
        }
        if (recurringEvents == null) {
            return;
        }

        for (RecurringEvent recurringEvent : recurringEvents) {
            if ((recurringEvent.getSection() == null) || (recurringEvent.getSection().getId() == null)) {
                continue;
            }
            for (Section section : sections) {
                if (recurringEvent.getSection().getId().equals(section.getId())) {
                    recurringEvent.setSection(section);
                    break;
                }
            }
        }
    }

    private void hydrateRecurringEvent(RecurringEvent recurringEvent) {
        if ((recurringEvent == null) || (recurringEvent.getSection() == null) || (recurringEvent.getSection().getId() == null)) {
            return;
        }

        List<RecurringEvent> events = new ArrayList<>();
        events.add(recurringEvent);
        hydrateRecurringEvents(events);
    }

    private void hydrateActiveEvents(List<Event> events) {
        List <Section> sections = sectionAccessor.list();
        if (sections == null) {
            return;
        }
        if (events == null) {
            return;
        }

        for (Event event : events) {
            if ((event.getSection() == null) || (event.getSection().getId() == null)) {
                continue;
            }
            for (Section section : sections) {
                if (event.getSection().getId().equals(section.getId())) {
                    event.setSection(section);
                    break;
                }
            }
        }
    }

    private void hydrateActiveEvent(Event event) {
        if ((event == null) || (event.getSection() == null) || (event.getSection().getId() == null)) {
            return;
        }

        List<Event> events = new ArrayList<>();
        events.add(event);
        hydrateActiveEvents(events);
    }

    private void removeMemberOperators(List<Operator> operators, List<SectionOperator> sectionOperators) {
        if ((operators == null) || (operators.isEmpty())) {
            return; // no operators
        }
        if ((sectionOperators == null) || (sectionOperators.isEmpty())) {
            return; // no one to remove
        }
        for (SectionOperator sectionOperator : sectionOperators) {
            for (Operator operator : operators) {
                if (operator.getCallsign().equals(sectionOperator.getCallsign())) {
                    operators.remove(operator);
                    break;
                }
            }
        }
    }

    private void addMembers(Section section, List<SectionOperator> newIds) {
        List<SectionOperator> existingOperators = operatorAccessor.listOperators(section);
        List<SectionOperator> addIds = new ArrayList<>();
        List<SectionOperator> removeOperators = new ArrayList<>();

        // remove those not in new list from old list
        for (SectionOperator existingOperator : existingOperators) {
            boolean found = false;
            for (SectionOperator newId : newIds) {
                if (newId.getId().equals(existingOperator.getId())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                removeOperators.add(existingOperator);
            }
        }
        for (SectionOperator newId : newIds) {
            boolean found = false;
            for (SectionOperator existingOperator : existingOperators) {
                if (newId.getId().equals(existingOperator.getId())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                addIds.add(newId);
            }
        }
        if (!removeOperators.isEmpty()) {
            for (SectionOperator removeOperator: removeOperators) {
                operatorAccessor.removeOperator(section, removeOperator);
            }
        }
        if (!addIds.isEmpty()) {
            for (SectionOperator addId: addIds) {
                Operator operator = operatorAccessor.get(addId.getId());
                operatorAccessor.addOperator(section, operator, addId.getMembershipType());
            }
        }
    }

    private List<SectionOperator> convertToSectionOperatorList(String callsignMemberIds) {
        List<SectionOperator> members = new ArrayList<>();
        if (callsignMemberIds.isEmpty()) {
            return members;
        }
        if (callsignMemberIds.length() % 37 != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid section membership selection");
        }
        for (int offset = 0; offset < callsignMemberIds.length(); offset += 37) {
            String operatorId = callsignMemberIds.substring(offset, offset + 36);
            int type = Character.digit(callsignMemberIds.charAt(offset + 36), 10);
            if (type < 0 || type >= MembershipType.values().length || operatorAccessor.get(operatorId) == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid section membership selection");
            }
            SectionOperator member = new SectionOperator();
            member.setId(operatorId);
            member.setMembershipType(MembershipType.values()[type]);
            members.add(member);
        }
        return members;
    }

    private String buildSectionOperatorsString(List<SectionOperator> sectionOperators) {
        String ret = "";
        if (sectionOperators != null) {
            for (SectionOperator sectionOperator : sectionOperators) {
                ret += (sectionOperator.getId()+String.format("%s", sectionOperator.getMembershipType().ordinal()));
            }
        }
        return ret;
    }

    private void removeMemberParticipants(List<Operator> operators, List<Participant> participants) {
        if ((operators == null) || (operators.isEmpty())) {
            return; // no operators
        }
        if ((participants == null) || (participants.isEmpty())) {
            return; // no one to remove
        }
        for (Participant participant : participants) {
            for (Operator operator : operators) {
                if (operator.getCallsign().equals(participant.getOperator().getCallsign())) {
                    operators.remove(operator);
                    break;
                }
            }
        }
    }

    private void checkInParticipants(Event event, String participantIds) {
        List<Operator> newIds = convertToOperatorList(participantIds);
        List<LocalDateTime> checkInTimes = convertToLocalTimes(participantIds);

        if (newIds.size() != checkInTimes.size()) {
            // error - should have two identical sized lists
            return;
        }

        int max = newIds.size();
        for (int i = 0; i < max; i++) {
            Operator operator = newIds.get(i);
            LocalDateTime time = checkInTimes.get(i);
            participantAccessor.checkInParticipant(event, operator, time);
        }
    }

    private void checkOutParticipants(Event event, String participantIds) {
        List<Operator> newIds = convertToOperatorList(participantIds);
        List<LocalDateTime> times = convertToLocalTimes(participantIds);

        if (newIds.size() != times.size()) {
            // error - should have two identical sized lists
            return;
        }

        int max = newIds.size();
        for (int i = 0; i < max; i++) {
            Operator operator = newIds.get(i);
            LocalDateTime time = times.get(i);
            participantAccessor.checkOutParticipant(event, operator, time);
        }
    }

    private List<Operator> convertToOperatorList(String ids) {
        List<Operator> ret = new ArrayList<>();
        if ((ids == null) || (ids.isEmpty())) {
            return ret;
        }
        String [] values = ids.split(",");
        for (int i = 0; i < values.length; i += 2) {
            Operator operator = new Operator();
            operator.setId(values[i]);
            ret.add(operator);
        }

        return ret;
    }

    private List<LocalDateTime> convertToLocalTimes(String ids) {
        List<LocalDateTime> ret = new ArrayList<>();
        if ((ids == null) || (ids.isEmpty())) {
            return ret;
        }
        String [] values = ids.split(",");
        for (int i = 1; i < values.length; i += 2) {
            LocalDateTime time = LocalDateTime.parse(values[i]);
            ret.add(time);
        }

        return ret;
    }

    private void hydrateParticipants(Event event, List<Participant> participants) {
        if ((participants.isEmpty())) {
            return;
        }
        List<SectionOperator> sectionOperators = operatorAccessor.listOperators(event.getSection());

        for (Participant participant : participants) {
            participant.setEvent(event);
            if (participant.getOperator() != null) {
                Operator operator = operatorAccessor.get(participant.getOperator().getId());
                participant.setOperator(operator);
            } else {
                continue;
            }
            participant.setMembershipType(MembershipType.NON_MEMBER);
            for (SectionOperator sectionOperator : sectionOperators) {
                if (sectionOperator.getId().equals(participant.getOperator().getId())) {
                    participant.setMembershipType(sectionOperator.getMembershipType());
                    break;
                }
            }
        }
    }

    private void hydrateSectionOperators(List<SectionOperator> sectionOperators) {
        for (SectionOperator sectionOperator : sectionOperators) {
            try {
                Section section = sectionAccessor.get(sectionOperator.getSection().getId());
                if (section != null) {
                    sectionOperator.setSection(section);
                }
            } catch (Exception e) {
                logger.error("Exception caught", e);
            }
        }
    }

    private Map<Event, List<Participant>> getEvents(Section section, String monthStr, String yearStr, List<SectionOperator> members) {
        if (monthStr.startsWith("Q")) {
            return getEventsQuarterly(section, monthStr, yearStr, members);
        }
        Map<Event, List<Participant>> eventParticipants = new TreeMap<>();
        List<Event> events = eventAccessor.listSecured(section);

        int year = Integer.parseInt(yearStr);
        int month = Integer.parseInt(monthStr);

        for (Event event : events) {
            if ((event.getStartTime().getMonthValue() == month) && (event.getStartTime().getYear() == year)) {
                List<Participant> participants = participantAccessor.listParticipants(event, false);
                if (members != null) {
                    if (participants != null) {
                        for (Participant participant : participants) {
                            boolean found = false;
                            for (SectionOperator member : members) {
                                if (participant.getOperator().getCallsign().equals(member.getCallsign())) {
                                    participant.setMembershipType(member.getMembershipType());
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                participant.setMembershipType(MembershipType.NON_MEMBER);
                            }
                        }
                    }
                }
                Collections.sort(participants, new Comparator<Participant>() {
                    @Override
                    public int compare(Participant obj1, Participant obj2) {
                        return obj1.getOperator().getCallsign().compareTo(obj2.getOperator().getCallsign());
                    }
                });
                eventParticipants.put(event, participants);
            }
        }
        return eventParticipants;
    }

    private Map<Event, List<Participant>> getEventsQuarterly(Section section, String monthStr, String yearStr, List<SectionOperator> members) {
        Map<Event, List<Participant>> eventParticipants = new TreeMap<>();
        List<Event> events = eventAccessor.listSecured(section);

        int year = Integer.parseInt(yearStr);
        int startMonth = 0;
        int endMonth = 0;

        if (monthStr.equals("Q1")) {
            startMonth = 1;
            endMonth = 3;
        } else if (monthStr.equals("Q2")) {
            startMonth = 4;
            endMonth = 6;
        } else if (monthStr.equals("Q3")) {
            startMonth = 7;
            endMonth = 9;
        } else if (monthStr.equals("Q4")) {
            startMonth = 10;
            endMonth = 12;
        } else if (monthStr.equals("QALL")) {
            startMonth = 1;
            endMonth = 12;
        }

        for (Event event : events) {
            if ((event.getStartTime().getMonthValue() >= startMonth) && (event.getStartTime().getMonthValue() <= endMonth) && (event.getStartTime().getYear() == year)) {
                List<Participant> participants = participantAccessor.listParticipants(event, false);
                if (members != null) {
                    if (participants != null) {
                        for (Participant participant : participants) {
                            boolean found = false;
                            for (SectionOperator member : members) {
                                if (participant.getOperator().getCallsign().equals(member.getCallsign())) {
                                    participant.setMembershipType(member.getMembershipType());
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                participant.setMembershipType(MembershipType.NON_MEMBER);
                            }
                        }
                    }
                }
                Collections.sort(participants, new Comparator<Participant>() {
                    @Override
                    public int compare(Participant obj1, Participant obj2) {
                        return obj1.getOperator().getCallsign().compareTo(obj2.getOperator().getCallsign());
                    }
                });
                eventParticipants.put(event, participants);
            }
        }
        return eventParticipants;
    }

    private List<Operator> determinePreviousParticipants(Event event, List<Operator> operators) {
         List<Operator> ret = new ArrayList<>();

        // get all past events
        RecurringEvent recurringEvent = event.getRecurringEvent();
        if (recurringEvent == null || recurringEvent.getId() == null || recurringEvent.getId().isBlank()) {
            return ret;
        }
        List<Event> pastEvents = eventAccessor.listByRecurringEvent(recurringEvent, false, false);

        // get a unique set of the last operator objects for each participant in all events
        Map<String, Operator> mapUniqueOperators = new HashMap<>();
        for (Event pastEvent : pastEvents) {
            List<Participant> eventParticipants = participantAccessor.listParticipants(pastEvent, false);
            for (Participant eventParticipant : eventParticipants) {
                mapUniqueOperators.put(eventParticipant.getOperator().getCallsign(), eventParticipant.getOperator());
            }
        }

        // put in a list
        for (Map.Entry<String,Operator> mapEntry : mapUniqueOperators.entrySet()) {
            ret.add(mapEntry.getValue());
        }

        // filter past participants out of the complete list
        for (Operator previousOperator : ret) {
            for (Operator operator : operators) {
                if (operator.getCallsign().equalsIgnoreCase(previousOperator.getCallsign())) {
                    operators.remove(operator);
                    break;
                }
            }
        }
        return ret;
    }




    @GetMapping("/archivedEvents-monthlyexport")
    public ModelAndView archivedEventsMonthlyExport(HttpServletRequest request) {
        List<Section> sections = sectionAccessor.list();
        return new ModelAndView("archivedEvents-monthlyexport", ViewModels.mapOf("sections", sections));
    }

    @GetMapping("/section-monthlyexport/{id}")
    public ModelAndView sectionMonthlyExport(HttpServletRequest request, @PathVariable String id) {
        Section section = sectionAccessor.get(id);
        List<Section> sections = new ArrayList<>();
        sections.add(section);
        return new ModelAndView("archivedEvents-monthlyexport", ViewModels.mapOf("sections", sections));
    }


    @PostMapping("/archivedEvents-monthlyexport-action")
    public ResponseEntity<?> archivedEventsMonthlyExportAction(HttpServletRequest request, @Valid @ModelAttribute MonthlyReportRequest actionData) {

        Section section = sectionAccessor.get(actionData.sectionId());
        List<SectionOperator> members = operatorAccessor.listOperators(section);
        Map<Event, List<Participant>> eventParticipants = getEvents(section, actionData.month(), actionData.year(), members);
        Map<String, String> locationDistricts = getLocationDistricts(section);
        String dateStr = actionData.month()+"-"+actionData.year();

        String filename = summaryExportAccessor.generateReport(section, eventParticipants, members, locationDistricts, dateStr, "MONTHLY SUMMARY");
        if (filename == null) {
            return ResponseEntity.internalServerError().body("Could not create export");
        }

        return ResponseEntity.status(303).location(WebUris.path("/archivedEvents-monthlyexport-csv/"+section.getId()+"/"+dateStr+"/"+filename)).build();
    }

    @GetMapping(path = "/archivedEvents-monthlyexport-csv/{sectionId}/{dateStr}/{filename}", produces = "text/csv")
    public ResponseEntity<byte[]> downloadArchivedEventMonthlyExport(HttpServletRequest request, @PathVariable String sectionId, @PathVariable String dateStr, @PathVariable String filename) {

        try {
            Section section = sectionAccessor.get(sectionId);
            filename = summaryExportAccessor.getTempReportDir()+filename;
            byte[] fileBytes = Files.readAllBytes(Paths.get(filename));
            String newFilename = String.format("NetLog-MonthlySummaryExport-%s-%s.csv", section.getName(), dateStr);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\""+newFilename+"\"")
                    .body(fileBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/section-monthlynonparticipationexport/{id}")
    public ModelAndView sectionMonthlyNonParticipationExport(HttpServletRequest request, @PathVariable String id) {
        Section section = sectionAccessor.get(id);
        return new ModelAndView("section-monthlynonparticipationexport", ViewModels.mapOf("section", section));
    }


    @PostMapping("/section-monthlynonparticipationexport-action")
    ResponseEntity<?> sectionMonthlyNonParticipationExportAction(HttpServletRequest request, @Valid @ModelAttribute MonthlyReportRequest actionData) {

        Section section = sectionAccessor.get(actionData.sectionId());
        List<SectionOperator> members = operatorAccessor.listOperators(section);
        Map<Event, List<Participant>> eventParticipants = getEvents(section, actionData.month(), actionData.year(), members);
        Map<String, String> locationDistricts = getLocationDistricts(section);
        String dateStr = actionData.month()+"-"+actionData.year();

        String filename = nonParticipationExportAccessor.generateReport(section, members, eventParticipants, locationDistricts, dateStr, "MONTHLY NON-PARTICIPATION");
        if (filename == null) {
            return ResponseEntity.internalServerError().body("Could not create report");
        }

        return ResponseEntity.status(303).location(WebUris.path("/section-monthlynonparticipationexport-pdf/"+section.getId()+"/"+dateStr+"/"+filename)).build();
    }

    @GetMapping(path = "/section-monthlynonparticipationexport-pdf/{sectionId}/{dateStr}/{filename}", produces = "text/csv")
    public ResponseEntity<byte[]> downloadSectionMonthlyNonParticipationExport(HttpServletRequest request, @PathVariable String sectionId, @PathVariable String dateStr, @PathVariable String filename) {

        try {
            Section section = sectionAccessor.get(sectionId);
            filename = nonParticipationExportAccessor.getTempReportDir()+filename;
            byte[] fileBytes = Files.readAllBytes(Paths.get(filename));
            String newFilename = String.format("NetLog-MonthlyNonParticipationExport-%s-%s.csv", section.getName(), dateStr);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\""+newFilename+"\"")
                    .body(fileBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/section-quarterlynonparticipationexport/{id}")
    public ModelAndView sectionQuarterlyNonParticipationExport(HttpServletRequest request, @PathVariable String id) {
        Section section = sectionAccessor.get(id);
        return new ModelAndView("section-quarterlynonparticipationexport", ViewModels.mapOf("section", section));
    }


    @PostMapping("/section-quarterlynonparticipationexport-action")
    ResponseEntity<?> sectionQuarterlyNonParticipationExportAction(HttpServletRequest request, @Valid @ModelAttribute QuarterlyReportRequest actionData) {

        Section section = sectionAccessor.get(actionData.sectionId());
        List<SectionOperator> members = operatorAccessor.listOperators(section);
        Map<Event, List<Participant>> eventParticipants = getEvents(section, actionData.quarter(), actionData.year(), members);
        Map<String, String> locationDistricts = getLocationDistricts(section);
        String dateStr = actionData.quarter()+"-"+actionData.year();

        String filename = nonParticipationExportAccessor.generateReport(section, members, eventParticipants, locationDistricts, dateStr, "QUARTERLY NON-PARTICIPATION");
        if (filename == null) {
            return ResponseEntity.internalServerError().body("Could not create report");
        }

        return ResponseEntity.status(303).location(WebUris.path("/section-quarterlynonparticipationexport-pdf/"+section.getId()+"/"+dateStr+"/"+filename)).build();
    }

    @GetMapping(path = "/section-quarterlynonparticipationexport-pdf/{sectionId}/{dateStr}/{filename}", produces = "text/csv")
    public ResponseEntity<byte[]> downloadSectionQuarterlyNonParticipationExport(HttpServletRequest request, @PathVariable String sectionId, @PathVariable String dateStr, @PathVariable String filename) {

        try {
            Section section = sectionAccessor.get(sectionId);
            filename = nonParticipationExportAccessor.getTempReportDir()+filename;
            byte[] fileBytes = Files.readAllBytes(Paths.get(filename));
            String newFilename = String.format("NetLog-QuarterlyNonParticipationExport-%s-%s.csv", section.getName(), dateStr);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\""+newFilename+"\"")
                    .body(fileBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/operator-edit-changelocation/{id}")
    public ModelAndView operatorEditLocation(HttpServletRequest request,  @PathVariable String id, @RequestParam Optional<String> countryId, @RequestParam Optional<String> stateId, @RequestParam Optional<String> locationId) {
        Operator operator = operatorAccessor.get(id);
        if (operator == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        List<LocationCountry> countries = null;
        LocationCountry country = null;
        List<LocationState> states = null;
        LocationState state = null;
        List<Location> locations = null;
        Location location = null;
        boolean done = false;
        LocationCountry existingCountry = (operator.getLocation() != null && operator.getLocation().getCountry() != null) ? operator.getLocation().getCountry() : null;
        LocationState existingState = (operator.getLocation() != null && operator.getLocation().getState() != null) ? operator.getLocation().getState() : null;
        Location existingLocation = (operator.getLocation() != null) ? operator.getLocation() : null;

        if (!countryId.isPresent()) {
            countries = locationAccessor.listCountries();
            if (existingCountry != null) {
                for (LocationCountry countryIter : countries) {
                    if (countryIter.getId().equals(existingCountry.getId())) {
                        countries.remove(countryIter);
                        break;
                    }
                }
            }
        } else {
            country = locationAccessor.getCountry(countryId.get());
        }
        if (!stateId.isPresent()) {
            if (country != null) {
                states = locationAccessor.listStates(country);
                if (existingState != null) {
                    for (LocationState stateIter : states) {
                        if (stateIter.getId().equals(existingState.getId())) {
                            states.remove(stateIter);
                            break;
                        }
                    }
                }
            }
        } else {
            state = locationAccessor.getState(stateId.get());
        }
        if (!locationId.isPresent()) {
            if (state != null) {
                locations = locationAccessor.listLocations(state);
                if (existingLocation != null) {
                    for (Location locationIter : locations) {
                        if (locationIter.getId().equals(existingLocation.getId())) {
                            locations.remove(locationIter);
                            break;
                        }
                    }
                }
            }
        } else {
            location = locationAccessor.getLocation(locationId.get());
            done = true;
        }
        return new ModelAndView("operator-edit-changelocation", ViewModels.mapOf("operator", operator,
                                     "countryId", (countryId.isPresent() ? countryId.get() : null),
                                     "country", country,
                                     "countries", countries,
                                     "stateId", (stateId.isPresent() ? stateId.get() : null),
                                     "state", state,
                                     "states", states,
                                     "locationId", (locationId.isPresent() ? locationId.get() : null),
                                     "locations", locations,
                                     "location", location,
                                     "done", done,
                                     "existingCountry", existingCountry,
                                     "existingState", existingState,
                                     "existingLocation", existingLocation));
    }


    @PostMapping("/operator-edit-changelocation-action/{id}")
    ResponseEntity<?> operatorEditLocationAction(HttpServletRequest request, @Valid @ModelAttribute OperatorLocationEditRequest actionData,  @PathVariable String id) {
        Operator operator = operatorAccessor.get(id);
        if (operator == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        String countryId = actionData.countryId();
        String stateId = actionData.stateId();
        String locationId = actionData.locationId();

        if ((locationId != null) && (stateId == null)) {
            Location location = locationAccessor.getLocation(locationId);
            stateId = (location.getState() != null) ? location.getState().getId() : null;
            countryId = (location.getCountry() != null) ? location.getCountry().getId() : null;
        } else if ((stateId != null) && (countryId == null)) {
            LocationState state = locationAccessor.getState(stateId);
            countryId = (state.getCountry() != null) ? state.getCountry().getId() : null;
        }

        UriComponentsBuilder nextUri = UriComponentsBuilder.fromPath("/operator-edit-changelocation/{id}");
        if ((countryId != null) && (stateId == null)) {
            nextUri.queryParam("countryId", countryId);
        } else if ((countryId != null) && (stateId != null) && (locationId == null)) {
            nextUri.queryParam("countryId", countryId).queryParam("stateId", stateId);
        } else if ((countryId != null) && (stateId != null) && (locationId != null)) {
            Location location = locationAccessor.getLocation(locationId);
            operator.setLocation(location);
            operatorAccessor.update(id, operator);
            nextUri = UriComponentsBuilder.fromPath("/operator-edit/{id}");
        }

        return ResponseEntity.status(303).location(nextUri.buildAndExpand(id).encode().toUri()).build();
    }

    @GetMapping("/locations")
    public ModelAndView locations(HttpServletRequest request) {
       List<LocationCountry> countries = locationAccessor.listCountries();
       return new ModelAndView("locations", ViewModels.mapOf("countries", countries));
    }

    @GetMapping("/location-country/{id}")
    public ModelAndView countryDetails(HttpServletRequest request, @PathVariable String id) {
        LocationCountry country = locationAccessor.getCountry(id);
        List<LocationState> states = locationAccessor.listStates(country);
        return new ModelAndView("location-country", ViewModels.mapOf("country", country, "states", states, "statesCount", states.size()));
    }

    @GetMapping("/location-state/{id}")
    public ModelAndView stateDetails(HttpServletRequest request, @PathVariable String id) {
        LocationState state = locationAccessor.getState(id);
        List<Location> locations = locationAccessor.listLocations(state);
        List<LocationCounty> counties = locationAccessor.listCounties(state);
        return new ModelAndView("location-state", ViewModels.mapOf("country", state.getCountry(), "state", state, "counties", counties, "locations", locations, "locationsCount", locations.size(),
                                                            "countiesCount", counties.size()));
    }

    @GetMapping("/location-country-delete/{id}")
    public ModelAndView countryDelete(HttpServletRequest request,  @PathVariable String id) {
        LocationCountry country = locationAccessor.getCountry(id);
        return new ModelAndView("location-country-delete", ViewModels.mapOf("country", country));
    }


    @PostMapping("/location-country-delete-action/{id}")
    ResponseEntity<?> countryDeleteAction(HttpServletRequest request,  @PathVariable String id) {
        LocationCountry country = locationAccessor.getCountry(id);

        List<LocationState> states = locationAccessor.listStates(country);
        for (LocationState state : states) {
            List<Location> locations = locationAccessor.listLocations(state);
            for (Location location : locations) {
                List<Operator> operators = operatorAccessor.listOperators(location);
                for (Operator operator : operators) {
                    operator.setLocation(null);
                    operatorAccessor.update(operator.getId(), operator);
                }
                // delete the location
                locationAccessor.deleteLocation(location.getId());
            }

            // delete the counties
            List<LocationCounty> counties = locationAccessor.listCounties(state);
            for (LocationCounty county : counties) {
                locationAccessor.deleteCounty(county.getId());
            }

            // delete the state
            locationAccessor.deleteState(state.getId());
        }

        // delete the country
        locationAccessor.deleteCountry(country.getId());

        return ResponseEntity.status(303).location(WebUris.path("/locations")).build();
    }

    @GetMapping("/location-country-add")
    public ModelAndView countryAdd(HttpServletRequest request) {
        return new ModelAndView("location-country-add", ViewModels.mapOf("form", ""));
    }


    @PostMapping("/location-country-add-action")
    ResponseEntity<?> countryAddAction(HttpServletRequest request, @Valid @ModelAttribute LocationCountryCreateRequest actionData) {
        LocationCountry country = new LocationCountry();
        country.setName(actionData.name());
        country.setAbbreviation(actionData.abbreviation());

        LocationCountry countryNew = locationAccessor.createCountry(country);
        if (countryNew != null) {
            return ResponseEntity.status(303).location(WebUris.path("/location-country/"+countryNew.getId())).build();
        }

        return ResponseEntity.status(303).location(WebUris.path("/locations")).build();
    }

    @GetMapping("/location-country-edit/{id}")
    public ModelAndView countryEdit(HttpServletRequest request,  @PathVariable String id) {
        LocationCountry country = locationAccessor.getCountry(id);

        return new ModelAndView("location-country-edit", ViewModels.mapOf("country", country));
    }


    @PostMapping("/location-country-edit-action/{id}")
    ResponseEntity<?> countryEditAction(HttpServletRequest request, @Valid @ModelAttribute LocationCountryCreateRequest actionData,  @PathVariable String id) {
        LocationCountry country = locationAccessor.getCountry(id);
        country.setName(actionData.name());
        country.setAbbreviation(actionData.abbreviation());

        locationAccessor.updateCountry(country);


        return ResponseEntity.status(303).location(WebUris.path("/location-country/"+id)).build();
    }

    @GetMapping("/location-state-add/{id}")
    public ModelAndView stateAdd(HttpServletRequest request, @PathVariable String id) {
        LocationCountry country = locationAccessor.getCountry(id);
        return new ModelAndView("location-state-add", ViewModels.mapOf("country", country));
    }


    @PostMapping("/location-state-add-action/{id}")
    ResponseEntity<?> stateAddAction(HttpServletRequest request, @PathVariable String id, @Valid @ModelAttribute LocationStateCreateRequest actionData) {
        LocationCountry country = locationAccessor.getCountry(id);
        LocationState state = new LocationState();
        state.setName(actionData.name());
        state.setAbbreviation(actionData.abbreviation());
        state.setCountry(country);

        LocationState stateNew = locationAccessor.createState(id, state);
        if (stateNew != null) {
            return ResponseEntity.status(303).location(WebUris.path("/location-state/"+stateNew.getId())).build();
        }

        return ResponseEntity.status(303).location(WebUris.path("/location-country/"+id)).build();
    }

    @GetMapping("/location-state-edit/{id}")
    public ModelAndView stateEdit(HttpServletRequest request,  @PathVariable String id) {
        LocationState state = locationAccessor.getState(id);

        return new ModelAndView("location-state-edit", ViewModels.mapOf("state", state));
    }


    @PostMapping("/location-state-edit-action/{id}")
    ResponseEntity<?> stateEditAction(HttpServletRequest request, @Valid @ModelAttribute LocationStateCreateRequest actionData,  @PathVariable String id) {
        LocationState state = locationAccessor.getState(id);
        state.setName(actionData.name());
        state.setAbbreviation(actionData.abbreviation());

        locationAccessor.updateState(state);


        return ResponseEntity.status(303).location(WebUris.path("/location-state/"+id)).build();
    }

    @GetMapping("/location-state-delete/{id}")
    public ModelAndView stateDelete(HttpServletRequest request,  @PathVariable String id) {
        LocationState state = locationAccessor.getState(id);
        return new ModelAndView("location-state-delete", ViewModels.mapOf("state", state));
    }


    @PostMapping("/location-state-delete-action/{id}")
    ResponseEntity<?> stateDeleteAction(HttpServletRequest request, @PathVariable String id) {
        LocationState state = locationAccessor.getState(id);

        List<Location> locations = locationAccessor.listLocations(state);
        for (Location location : locations) {
            List<Operator> operators = operatorAccessor.listOperators(location);
            for (Operator operator : operators) {
                operator.setLocation(null);
                operatorAccessor.update(operator.getId(), operator);
            }
            // delete the location
            locationAccessor.deleteLocation(location.getId());
        }

        // delete the counties
        List<LocationCounty> counties = locationAccessor.listCounties(state);
        for (LocationCounty county : counties) {
            locationAccessor.deleteCounty(county.getId());
        }

        // delete the state
        locationAccessor.deleteState(state.getId());

        return ResponseEntity.status(303).location(WebUris.path("/location-country/"+state.getCountry().getId())).build();
    }

    @GetMapping("/location-state-addbulk/{id}")
    public ModelAndView stateAddBulk(HttpServletRequest request, @PathVariable String id) {
        LocationCountry country = locationAccessor.getCountry(id);
        return new ModelAndView("location-state-addbulk", ViewModels.mapOf("country", country));
    }


    @PostMapping("/location-state-addbulk-action/{id}")
    ResponseEntity<?> stateAddBulkAction(HttpServletRequest request, @PathVariable String id, @Valid @ModelAttribute OperatorBulkCreateRequest actionData) {

        addAllStates(id, actionData.bulkList());

        return ResponseEntity.status(303).location(WebUris.path("/location-country/"+id)).build();
    }

    private void addAllStates(String countryId, String bulkList) {
        String [] lines = bulkList.lines().toArray(String[]::new);
        for (String line : lines) {
            if (line.length() == 0) {
                continue;
            }
            String [] fields = line.split(",");
            LocationState state = new LocationState();
            try {
                if (fields.length == 1) {
                    state.setName(line.replace("\"", ""));
                } else if (fields.length == 2) {
                    String name = fields[0].replace("\"", "");
                    String abbreviation = fields[1].replace("\"", "");
                    state.setName(name);
                    state.setAbbreviation(abbreviation);
                } else if (fields.length > 2) {
                    int index = line.indexOf(",");
                    String name = line.substring(0, index).replace("\"", "");
                    String abbreviation = line.substring(index+1).replace("\"", "");
                    state.setName(name);
                    state.setAbbreviation(abbreviation);
                }
                locationAccessor.createState(countryId, state);
            } catch (Exception e) {
            }
        }
    }

    @GetMapping("/location-county/{id}")
    public ModelAndView countyDetails(HttpServletRequest request, @PathVariable String id) {
        LocationCounty county = locationAccessor.getCounty(id);
        if (county == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        List<Location> locations = locationAccessor.listLocations(county);
        List<Operator> operators = new ArrayList<>();
        for (Location location : locations) {
            operators.addAll(operatorAccessor.listOperators(location));
        }
        operators.sort(Comparator.comparing(Operator::getCallsign));
        return new ModelAndView("location-county", ViewModels.mapOf("county", county,
                "locations", locations, "locationsCount", locations.size(),
                "operators", operators, "operatorCount", operators.size()));
    }

    @GetMapping("/location-county-edit/{id}")
    public ModelAndView countyEdit(HttpServletRequest request, @PathVariable String id) {
        LocationCounty county = locationAccessor.getCounty(id);

        return new ModelAndView("location-county-edit", ViewModels.mapOf("county", county));
    }


    @PostMapping("/location-county-edit-action/{id}")
    ResponseEntity<?> countyEditAction(HttpServletRequest request, @Valid @ModelAttribute LocationCountryCreateRequest actionData,  @PathVariable String id) {
        LocationCounty county = locationAccessor.getCounty(id);
        county.setName(actionData.name());

        locationAccessor.updateCounty(county);

        return ResponseEntity.status(303).location(WebUris.path("/location-county/"+id)).build();
    }

    @GetMapping("/location-county-add/{id}")
    public ModelAndView countyAdd(HttpServletRequest request, @PathVariable String id) {
        LocationState state = locationAccessor.getState(id);
        return new ModelAndView("location-county-add", ViewModels.mapOf("state", state));
    }


    @PostMapping("/location-county-add-action/{id}")
    ResponseEntity<?> countyAddAction(HttpServletRequest request, @PathVariable String id, @Valid @ModelAttribute LocationCountryCreateRequest actionData) {
        LocationState state = locationAccessor.getState(id);
        LocationCounty county = new LocationCounty();
        county.setName(actionData.name());

        LocationCounty countyNew = locationAccessor.createCounty(state, county);
        if (countyNew != null) {
            return ResponseEntity.status(303).location(WebUris.path("/location-county/"+countyNew.getId())).build();
        }

        return ResponseEntity.status(303).location(WebUris.path("/location-state/"+state.getId())).build();
    }

    @GetMapping("/location-county-delete/{id}")
    public ModelAndView countyDelete(HttpServletRequest request, @PathVariable String id) {
        LocationCounty county = locationAccessor.getCounty(id);
        return new ModelAndView("location-county-delete", ViewModels.mapOf("county", county));
    }


    @PostMapping("/location-county-delete-action/{id}")
    ResponseEntity<?> countyDeleteAction(HttpServletRequest request,  @PathVariable String id) {
        LocationCounty county = locationAccessor.getCounty(id);

        List<Location> locations = locationAccessor.listLocations(county);
        for (Location location : locations) {
            location.setCounty(county);
            locationAccessor.updateLocation(location);
        }

        locationAccessor.deleteCounty(county.getId());

        return ResponseEntity.status(303).location(WebUris.path("/location-state/"+county.getState().getId())).build();
    }

    @GetMapping("/location-county-addbulk/{id}")
    public ModelAndView countyAddBulk(HttpServletRequest request, @PathVariable String id) {
        LocationState state = locationAccessor.getState(id);
        return new ModelAndView("location-county-addbulk", ViewModels.mapOf("state", state));
    }


    @PostMapping("/location-county-addbulk-action/{id}")
    ResponseEntity<?> countyAddBulkAction(HttpServletRequest request, @PathVariable String id, @Valid @ModelAttribute OperatorBulkCreateRequest actionData) {
        LocationState state = locationAccessor.getState(id);

        addAllCounties(state, actionData.bulkList());

        return ResponseEntity.status(303).location(WebUris.path("/location-state/"+id)).build();
    }

    private void addAllCounties(LocationState state, String bulkList) {
        String [] lines = bulkList.lines().toArray(String[]::new);
        for (String line : lines) {
            if (line.length() == 0) {
                continue;
            }

            try {
                LocationCounty county = new LocationCounty();
                county.setName(line.replace("\"", ""));
                county.setState(state);
                county.setCountry(state.getCountry());

                locationAccessor.createCounty(state, county);
            } catch (Exception e) {
            }
        }
    }

    @GetMapping("/location-location/{id}")
    public ModelAndView locationDetails(HttpServletRequest request, @PathVariable String id) {
        Location location = locationAccessor.getLocation(id);
        if (location == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        List<Operator> operators = operatorAccessor.listOperators(location);
        return new ModelAndView("location-location", ViewModels.mapOf("location", location,
                "operators", operators, "operatorCount", operators.size()));
    }

    @GetMapping("/location-location-delete/{id}")
    public ModelAndView locationDelete(HttpServletRequest request, @PathVariable String id) {
        Location location = locationAccessor.getLocation(id);
        return new ModelAndView("location-location-delete", ViewModels.mapOf("location", location));
    }


    @PostMapping("/location-location-delete-action/{id}")
    ResponseEntity<?> locationDeleteAction(HttpServletRequest request,  @PathVariable String id) {
        Location location = locationAccessor.getLocation(id);

        locationAccessor.deleteLocation(location.getId());

        List<Operator> operators = operatorAccessor.listOperators(location);
        for (Operator operator : operators) {
            operator.setLocation(null);
            operatorAccessor.update(operator.getId(), operator);
        }

        return ResponseEntity.status(303).location(WebUris.path("/location-state/"+location.getState().getId())).build();
    }

    @GetMapping("/location-location-edit/{id}")
    public ModelAndView locationEdit(HttpServletRequest request, @PathVariable String id) {
        Location location = locationAccessor.getLocation(id);

        return new ModelAndView("location-location-edit", ViewModels.mapOf("location", location));
    }


    @PostMapping("/location-location-edit-action/{id}")
    ResponseEntity<?> locationEditAction(HttpServletRequest request, @Valid @ModelAttribute LocationCountryCreateRequest actionData,  @PathVariable String id) {
        Location location = locationAccessor.getLocation(id);
        location.setName(actionData.name());

        locationAccessor.updateLocation(location);

        return ResponseEntity.status(303).location(WebUris.path("/location-location/"+id)).build();
    }

    @GetMapping("/location-location-add/{id}")
    public ModelAndView locationAdd(HttpServletRequest request, @PathVariable String id) {
        LocationState state = locationAccessor.getState(id);
        return new ModelAndView("location-location-add", ViewModels.mapOf("state", state));
    }


    @PostMapping("/location-location-add-action/{id}")
    ResponseEntity<?> locationAddAction(HttpServletRequest request, @PathVariable String id, @Valid @ModelAttribute LocationCountryCreateRequest actionData) {
        LocationState state = locationAccessor.getState(id);
        Location location = new Location();
        location.setName(actionData.name());
        location.setState(state);
        location.setCountry(state.getCountry());

        Location locationNew = locationAccessor.createLocation(state, location);
        if (locationNew != null) {
            return ResponseEntity.status(303).location(WebUris.path("/location-location/"+locationNew.getId())).build();
        }

        return ResponseEntity.status(303).location(WebUris.path("/location-state/"+state.getId())).build();
    }

    @GetMapping("/location-location-addbulk/{id}")
    public ModelAndView locationAddBulk(HttpServletRequest request, @PathVariable String id) {
        LocationState state = locationAccessor.getState(id);
        return new ModelAndView("location-location-addbulk", ViewModels.mapOf("state", state));
    }


    @PostMapping("/location-location-addbulk-action/{id}")
    ResponseEntity<?> locationAddBulkAction(HttpServletRequest request, @PathVariable String id, @Valid @ModelAttribute OperatorBulkCreateRequest actionData) {
        LocationState state = locationAccessor.getState(id);

        addAllMunicipalities(state, actionData.bulkList());

        return ResponseEntity.status(303).location(WebUris.path("/location-state/"+id)).build();
    }

    private void addAllMunicipalities(LocationState state, String bulkList) {
        String [] lines = bulkList.lines().toArray(String[]::new);
        for (String line : lines) {
            if (line.length() == 0) {
                continue;
            }

            try {
                Location location = new Location();
                location.setName(line.replace("\"", ""));
                location.setState(state);
                location.setCountry(state.getCountry());

                locationAccessor.createLocation(state, location);
            } catch (Exception e) {
            }
        }
    }

    @GetMapping("/location-county-municipality-add/{id}")
    public ModelAndView locationMunicipalityAdd(HttpServletRequest request, @PathVariable String id) {
        LocationCounty county = locationAccessor.getCounty(id);
        List<Location> locationsExisting = locationAccessor.listLocations(county);
        List<Location> locations = locationAccessor.listLocations(county.getState());

        // remove existing from list
        for (Location locationExisting : locationsExisting) {
            for (Location location: locations) {
                if (location.getId().equals(locationExisting.getId())) {
                    locations.remove(location);
                    break;
                }
            }
        }

        return new ModelAndView("location-county-municipality-add", ViewModels.mapOf("county", county, "locations", locations));
    }


    @PostMapping("/location-county-municipality-add-action/{id}")
    ResponseEntity<?> locationMunicipalityAddAction(HttpServletRequest request, @PathVariable String id, @Valid @ModelAttribute LocationCountyChangeRequest actionData) {
        LocationCounty county = locationAccessor.getCounty(id);
        Location locationSelected = locationAccessor.getLocation(actionData.locationId());

        if (locationSelected != null) {
            locationSelected.setCounty(county);
            locationAccessor.updateLocation(locationSelected);
        }

        return ResponseEntity.status(303).location(WebUris.path("/location-county/"+id)).build();
    }

    @GetMapping("/location-county-municipality-remove/{countyId}/{locationId}")
    public ModelAndView locationMunicipalityRemove(HttpServletRequest request, @PathVariable String countyId, @PathVariable String locationId) {
        LocationCounty county = locationAccessor.getCounty(countyId);
        Location location = locationAccessor.getLocation(locationId);

        return new ModelAndView("location-county-municipality-remove", ViewModels.mapOf("county", county, "location", location));
    }


    @PostMapping("/location-county-municipality-remove-action/{countyId}/{locationId}")
    ResponseEntity<?> locationMunicipalityRemoveAction(HttpServletRequest request, @PathVariable String countyId, @PathVariable String locationId) {
        Location location = locationAccessor.getLocation(locationId);

        if (location != null) {
            location.setCounty(null);
            locationAccessor.updateLocation(location);
        }

        return ResponseEntity.status(303).location(WebUris.path("/location-county/"+countyId)).build();
    }

    @GetMapping("/location-county-municipality-addbulk/{id}")
    public ModelAndView locationMunicipalityAddBulk(HttpServletRequest request, @PathVariable String id) {
        LocationCounty county = locationAccessor.getCounty(id);
        return new ModelAndView("location-county-municipality-addbulk", ViewModels.mapOf("county", county));
    }


    @PostMapping("/location-county-municipality-addbulk-action/{id}")
    ResponseEntity<?> locationMunicipalityAddBulkAction(HttpServletRequest request, @PathVariable String id, @Valid @ModelAttribute OperatorBulkCreateRequest actionData) {
        LocationCounty county = locationAccessor.getCounty(id);

        addAllMunicipalitiesToCounty(county, actionData.bulkList());

        return ResponseEntity.status(303).location(WebUris.path("/location-county/"+id)).build();
    }

    private void addAllMunicipalitiesToCounty(LocationCounty county, String bulkList) {
        LocationState state = locationAccessor.getState(county.getState().getId());
        List<Location> locations = locationAccessor.listLocations(state);

        if ((locations == null) || (locations.isEmpty())) {
            return;
        }

        String [] lines = bulkList.lines().toArray(String[]::new);
        for (String line : lines) {
            if (line.length() == 0) {
                continue;
            }

            try {
                String name = line.replace("\"", "");

                for (Location location : locations) {
                    if (location.getName().equalsIgnoreCase(name)) {
                        location.setCounty(county);
                        locationAccessor.updateLocation(location);
                        break;
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    @GetMapping("/district/{id}")
    public ModelAndView districtDetails(HttpServletRequest request, @PathVariable String id) {
        District district = sectionAccessor.getDistrict(id);
        Section section = sectionAccessor.get(district.getSectionId());
        List<Location> locationsDistrict = sectionAccessor.listLocations(district);
        List<Location> locations = locationAccessor.listLocations(locationsDistrict);

        List<SectionOperator> operators = operatorAccessor.listOperators(section, district, locations);
        return new ModelAndView("district", ViewModels.mapOf("section", section, "operators", operators, "operatorCount", operators.size(), "locationCount", locations.size(), "locations", locations,
                                                                "district", district));
    }

    @GetMapping("/district-add/{id}")
    public ModelAndView districtAdd(HttpServletRequest request, @PathVariable String id) {
        Section section = sectionAccessor.get(id);

        return new ModelAndView("district-add", ViewModels.mapOf("section", section));
    }


    @PostMapping("/district-add-action/{id}")
    ResponseEntity<?> districtAddAction(HttpServletRequest request, @PathVariable String id, @Valid @ModelAttribute DistrictCreateRequest actionData) {
        Section section = sectionAccessor.get(id);

        District district = new District();
        district.setName(actionData.name());
        district.setDescription(actionData.description());
        district.setLeaderName(actionData.leaderName());
        district.setLeaderContact(actionData.leaderContact());
        district.setSectionId(section.getId());
        sectionAccessor.addDistrict(section, district);

        return ResponseEntity.status(303).location(WebUris.path("/section/"+id)).build();
    }


    @GetMapping("/district-remove/{id}")
    public ModelAndView districtRemove(HttpServletRequest request, @PathVariable String id) {
        District district = sectionAccessor.getDistrict(id);
        Section section = sectionAccessor.get(district.getSectionId());

        return new ModelAndView("district-remove", ViewModels.mapOf("district", district, "section", section));
    }


    @PostMapping("/district-remove-action/{id}")
    ResponseEntity<?> districtRemoveAction(HttpServletRequest request,  @PathVariable String id) {
        District district = sectionAccessor.getDistrict(id);
        Section section = sectionAccessor.get(district.getSectionId());

        sectionAccessor.removeDistrict(district);

        return ResponseEntity.status(303).location(WebUris.path("/section/"+section.getId())).build();
    }

    @GetMapping("/district-edit/{id}")
    public ModelAndView districtEdit(HttpServletRequest request, @PathVariable String id) {
        District district = sectionAccessor.getDistrict(id);
        Section section = sectionAccessor.get(district.getSectionId());

        return new ModelAndView("district-edit", ViewModels.mapOf("district", district, "section", section));
    }


    @PostMapping("/district-edit-action/{id}")
    ResponseEntity<?> districtEditAction(HttpServletRequest request, @Valid @ModelAttribute DistrictCreateRequest actionData,  @PathVariable String id) {
        District district = sectionAccessor.getDistrict(id);

        district.setName(actionData.name());
        district.setDescription(actionData.description());
        district.setLeaderName(actionData.leaderName());
        district.setLeaderContact(actionData.leaderContact());
        sectionAccessor.updateDistrict(district);

        return ResponseEntity.status(303).location(WebUris.path("/section/"+district.getSectionId())).build();
    }

    @GetMapping("/district-municipality-add/{id}")
    public ModelAndView districtMunicipalityAdd(HttpServletRequest request,  @PathVariable String id, @RequestParam Optional<String> countryId, @RequestParam Optional<String> stateId, @RequestParam Optional<String> locationId) {
        District district = sectionAccessor.getDistrict(id);
        if (district == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Section section = sectionAccessor.get(district.getSectionId());
        List<LocationCountry> countries = null;
        LocationCountry country = null;
        List<LocationState> states = null;
        LocationState state = null;
        List<Location> locations = null;
        Location location = null;
        boolean done = false;

        if (!countryId.isPresent()) {
            countries = locationAccessor.listCountries();
        } else {
            country = locationAccessor.getCountry(countryId.get());
        }
        if (!stateId.isPresent()) {
            if (country != null) {
                states = locationAccessor.listStates(country);
            }
        } else {
            state = locationAccessor.getState(stateId.get());
        }
        if (!locationId.isPresent()) {
            if (state != null) {
                locations = locationAccessor.listLocations(state);
            }
        } else {
            location = locationAccessor.getLocation(locationId.get());
            done = true;
        }
        return new ModelAndView("district-municipality-add", ViewModels.mapOf("district", district,
                                     "countryId", (countryId.isPresent() ? countryId.get() : null),
                                     "country", country,
                                     "countries", countries,
                                     "stateId", (stateId.isPresent() ? stateId.get() : null),
                                     "state", state,
                                     "states", states,
                                     "locationId", (locationId.isPresent() ? locationId.get() : null),
                                     "locations", locations,
                                     "location", location,
                                     "done", done,
                                     "section", section));
    }


    @PostMapping("/district-municipality-add-action/{id}")
    ResponseEntity<?> districtMunicipalityAddAction(HttpServletRequest request, @Valid @ModelAttribute DistrictLocationEditRequest actionData,  @PathVariable String id) {
        District district = sectionAccessor.getDistrict(id);
        if (district == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        String countryId = actionData.countryId();
        String stateId = actionData.stateId();
        String locationId = actionData.locationId();

        if ((locationId != null) && (stateId == null)) {
            Location location = locationAccessor.getLocation(locationId);
            stateId = (location.getState() != null) ? location.getState().getId() : null;
            countryId = (location.getCountry() != null) ? location.getCountry().getId() : null;
        } else if ((stateId != null) && (countryId == null)) {
            LocationState state = locationAccessor.getState(stateId);
            countryId = (state.getCountry() != null) ? state.getCountry().getId() : null;
        }

        UriComponentsBuilder nextUri = UriComponentsBuilder.fromPath("/district-municipality-add/{id}");
        if ((countryId != null) && (stateId == null)) {
            nextUri.queryParam("countryId", countryId);
        } else if ((countryId != null) && (stateId != null) && (locationId == null)) {
            nextUri.queryParam("countryId", countryId).queryParam("stateId", stateId);
        } else if ((countryId != null) && (stateId != null) && (locationId != null)) {
            Location location = locationAccessor.getLocation(locationId);
            sectionAccessor.addLocationToDistrict(district, location);
            nextUri = UriComponentsBuilder.fromPath("/district/{id}");
        }

        return ResponseEntity.status(303).location(nextUri.buildAndExpand(id).encode().toUri()).build();
    }

    @GetMapping("/district-municipality-remove/{id}/municipality/{locationId}")
    public ModelAndView districtMunicipalityRemove(HttpServletRequest request, @PathVariable String id, @PathVariable String locationId) {
        District district = sectionAccessor.getDistrict(id);
        Section section = sectionAccessor.get(district.getSectionId());
        Location location = locationAccessor.getLocation(locationId);

        return new ModelAndView("district-municipality-remove", ViewModels.mapOf("district", district, "section", section, "location", location));
    }


    @PostMapping("/district-municipality-remove-action/{id}/municipality/{locationId}")
    ResponseEntity<?> districtMunicipalityRemoveAction(HttpServletRequest request, @PathVariable String id, @PathVariable String locationId) {
        District district = sectionAccessor.getDistrict(id);
        Location location = locationAccessor.getLocation(locationId);

        sectionAccessor.removeMunicipalityFromDistrict(district, location);

        return ResponseEntity.status(303).location(WebUris.path("/district/"+id)).build();
    }

    @GetMapping("/district-municipality-addbulk/{id}")
    public ModelAndView districtMunicipalityAddBulk(HttpServletRequest request, @PathVariable String id) {
        District district = sectionAccessor.getDistrict(id);
        Section section = sectionAccessor.get(district.getSectionId());
        return new ModelAndView("district-municipality-addbulk", ViewModels.mapOf("district", district, "section", section));
    }


    @PostMapping("/district-municipality-addbulk-action/{id}")
    ResponseEntity<?> districtMunicipalityAddBulkAction(HttpServletRequest request, @PathVariable String id, @Valid @ModelAttribute DistrictMunicipalityBulkCreateRequest actionData) {
        District district = sectionAccessor.getDistrict(id);

        if (addAllMunicipalitiesToDistrict(district, actionData)) {
            return ResponseEntity.status(303).location(WebUris.path("/district/"+id)).build();
        }

        return ResponseEntity.internalServerError().body("Error with provided information. Check all fields for validity.");
    }

    private boolean addAllMunicipalitiesToDistrict(District district, DistrictMunicipalityBulkCreateRequest actionData) {
        LocationCountry country = locationAccessor.getCountryByName(actionData.country());
        if (country == null) {
            // no such country
            return false;
        }
        LocationState state = locationAccessor.getStateByName(country, actionData.state());
        if (state == null) {
            // no such state
            return false;
        }

        boolean found = false;
        String [] lines = actionData.bulkList().lines().toArray(String[]::new);
        for (String line : lines) {
            if (line.length() == 0) {
                continue;
            }

            try {
                String name = line.replace("\"", "");

                Location location = locationAccessor.getLocationByName(country, state, name);
                if (location != null) {
                    sectionAccessor.addLocationToDistrict(district, location);
                    found = true;
                }

            } catch (Exception e) {
            }
        }
        return found;
    }

    private Map<String, String> getLocationDistricts(Section section) {
        Map<String, String> ret = new HashMap<String, String>();

        List<District> districts = sectionAccessor.listDistricts(section);
        for (District district : districts) {
            List<Location> locations = sectionAccessor.listLocations(district);
            for (Location location : locations) {
                ret.put(location.getId(), district.getName());
            }
        }
        return ret;
    }
}
