package com.kc1vmz.netlog.controller;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kc1vmz.netlog.accessor.EventAccessor;
import com.kc1vmz.netlog.accessor.NonParticipationReportAccessor;
import com.kc1vmz.netlog.accessor.OperatorAccessor;
import com.kc1vmz.netlog.accessor.ParticipantAccessor;
import com.kc1vmz.netlog.accessor.ParticipationReportAccessor;
import com.kc1vmz.netlog.accessor.RecurringEventAccessor;
import com.kc1vmz.netlog.accessor.RecurringEventReportAccessor;
import com.kc1vmz.netlog.accessor.ReportAccessor;
import com.kc1vmz.netlog.accessor.SectionAccessor;
import com.kc1vmz.netlog.accessor.SummaryReportAccessor;
import com.kc1vmz.netlog.enums.EventType;
import com.kc1vmz.netlog.enums.MembershipType;
import com.kc1vmz.netlog.object.Event;
import com.kc1vmz.netlog.object.Operator;
import com.kc1vmz.netlog.object.Participant;
import com.kc1vmz.netlog.object.RecurringEvent;
import com.kc1vmz.netlog.object.Section;
import com.kc1vmz.netlog.object.SectionOperator;
import com.kc1vmz.netlog.request.ActiveEventCheckInOutRequest;
import com.kc1vmz.netlog.request.ActiveEventEditRequest;
import com.kc1vmz.netlog.request.ActiveEventStartEndRequest;
import com.kc1vmz.netlog.request.BlankRequest;
import com.kc1vmz.netlog.request.MonthlyReportRequest;
import com.kc1vmz.netlog.request.OperatorBulkCreateRequest;
import com.kc1vmz.netlog.request.OperatorCreateRequest;
import com.kc1vmz.netlog.request.QuarterlyReportRequest;
import com.kc1vmz.netlog.request.RecurringEventCreateRequest;
import com.kc1vmz.netlog.request.RecurringEventReportRequest;
import com.kc1vmz.netlog.request.SectionCreateRequest;

import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.views.View;

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

import io.micronaut.views.fields.FormGenerator;
import jakarta.inject.Inject;
import jakarta.validation.Valid;

@Controller
public class UIController {

    @Inject
    private SectionAccessor sectionAccessor;
    @Inject
    private RecurringEventAccessor recurringEventAccessor;
    @Inject
    private EventAccessor eventAccessor;
    @Inject
    private OperatorAccessor operatorAccessor;
    @Inject
    private ParticipantAccessor participantAccessor;
    @Inject
    private ReportAccessor reportAccessor;
    @Inject
    private SummaryReportAccessor summaryReportAccessor;
    @Inject
    private ParticipationReportAccessor participationReportAccessor;
    @Inject
    private RecurringEventReportAccessor recurringEventReportAccessor;
    @Inject
    private NonParticipationReportAccessor nonParticipationReportAccessor;

    private static final Logger logger = LogManager.getLogger(UIController.class);

    private FormGenerator formGenerator;

    public UIController(FormGenerator formGenerator) {
        this.formGenerator = formGenerator;
    }

    @View("archives")
    @Get("/archives")
    public HttpResponse<?> archiveEvents(HttpRequest<?> request) {
        List<Event> events = eventAccessor.listSecured(null);
        hydrateActiveEvents(events);
        return HttpResponse.ok(CollectionUtils.mapOf("events", events));
    }


    @View("archivedEvent")
    @Get("/archivedEvent/{id}")
    public HttpResponse<?> archiveEventsDetails(HttpRequest<?> request, @PathVariable String id) {
        Event event = eventAccessor.get(id);
        hydrateActiveEvent(event);

        List<Participant> participants = participantAccessor.listParticipants(event, false);
        hydrateParticipants(event, participants);

        Collections.sort(participants, new Comparator<Participant>() {
            @Override
            public int compare(Participant obj1, Participant obj2) {
                return obj1.getOperator().getCallsign().compareTo(obj2.getOperator().getCallsign());
            }
        });

        return HttpResponse.ok(CollectionUtils.mapOf("event", event, "participants", participants, "participantCount", participants.size()));
    }


    @SuppressWarnings("unchecked")
    @View("archivedEvent-report")
    @Get("/archivedEvent-report/{id}")
    public Map<String, Object> archivedEventReport(HttpRequest<?> request,  @PathVariable String id) {
        Event event = eventAccessor.get(id);
        return CollectionUtils.mapOf("event", event, 
                                     "form", formGenerator.generate("/archivedEvent-report-action/"+id, BlankRequest.class));
    }

    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Post("/archivedEvent-report-action/{id}")
    HttpResponse<?> archivedEventReportAction(HttpRequest<?> request, @PathVariable String id) {
        Event event = eventAccessor.get(id);
        List<Participant> participants = participantAccessor.listParticipants(event, false);

        String filename = reportAccessor.generateReport(event, participants);
        if (filename == null) {
            return HttpResponse.serverError("Could not create report");
        }

        return HttpResponse.seeOther(UriBuilder.of("/").path("/archivedEvent-report-pdf/"+id+"/"+filename).build());
    } 

    @Get(uri = "/archivedEvent-report-pdf/{id}/{filename}", produces = MediaType.APPLICATION_PDF)
    public HttpResponse<byte[]> downloadArchivedEventReport(HttpRequest<?> request, @PathVariable String id, @PathVariable String filename) {
        Event event = eventAccessor.get(id);

        try {
            filename = reportAccessor.getTempReportDir()+filename;
            byte[] fileBytes = Files.readAllBytes(Paths.get(filename));
            String newFilename = String.format("NetLog-%s-%s.pdf", event.getName(), event.getStartTime());
            return HttpResponse.ok(fileBytes)
                    .header("Content-Disposition", "attachment; filename=\""+newFilename+"\"");
        } catch (Exception e) {
            return HttpResponse.serverError();
        }
    }

    @SuppressWarnings("unchecked")
    @View("archivedEvents-monthlyreport")
    @Get("/archivedEvents-monthlyreport")
    public Map<String, Object> archivedEventsMonthlyReport(HttpRequest<?> request) {
        List<Section> sections = sectionAccessor.list();
        return CollectionUtils.mapOf("sections", sections, "form", formGenerator.generate("/archivedEvent-monthlyreport-action", MonthlyReportRequest.class));
    }

    @SuppressWarnings("unchecked")
    @View("archivedEvents-monthlyreport")
    @Get("/section-monthlyreport/{id}")
    public Map<String, Object> sectionMonthlyReport(HttpRequest<?> request, @PathVariable String id) {
        Section section = sectionAccessor.get(id);
        List<Section> sections = new ArrayList<>();
        sections.add(section);
        return CollectionUtils.mapOf("sections", sections, "form", formGenerator.generate("/archivedEvent-monthlyreport-action", MonthlyReportRequest.class));
    }

    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Post("/archivedEvents-monthlyreport-action")
    HttpResponse<?> archivedEventsMonthlyReportAction(HttpRequest<?> request, @Valid @Body MonthlyReportRequest actionData) {

        Section section = sectionAccessor.get(actionData.sectionId());
        Map<Event, List<Participant>> eventParticipants = getEvents(section, actionData.month(), actionData.year());
        String dateStr = actionData.month()+"-"+actionData.year();

        String filename = summaryReportAccessor.generateReport(section, eventParticipants, dateStr, "MONTHLY SUMMARY");
        if (filename == null) {
            return HttpResponse.serverError("Could not create report");
        }

        return HttpResponse.seeOther(UriBuilder.of("/").path("/archivedEvents-monthlyreport-pdf/"+section.getId()+"/"+dateStr+"/"+filename).build());
    } 

    @Get(uri = "/archivedEvents-monthlyreport-pdf/{sectionId}/{dateStr}/{filename}", produces = MediaType.APPLICATION_PDF)
    public HttpResponse<byte[]> downloadArchivedEventMonthlyReport(HttpRequest<?> request, @PathVariable String sectionId, @PathVariable String dateStr, @PathVariable String filename) {

        try {
            Section section = sectionAccessor.get(sectionId);
            filename = summaryReportAccessor.getTempReportDir()+filename;
            byte[] fileBytes = Files.readAllBytes(Paths.get(filename));
            String newFilename = String.format("NetLog-MonthlySummaryReport-%s-%s.pdf", section.getName(), dateStr);
            return HttpResponse.ok(fileBytes)
                    .header("Content-Disposition", "attachment; filename=\""+newFilename+"\"");
        } catch (Exception e) {
            return HttpResponse.serverError();
        }
    }

    @SuppressWarnings("unchecked")
    @View("archivedEvents-monthlyparticipationreport")
    @Get("/archivedEvents-monthlyparticipationreport")
    public Map<String, Object> archivedEventsMonthlyParticipationReport(HttpRequest<?> request) {
        List<Section> sections = sectionAccessor.list();
        return CollectionUtils.mapOf("sections", sections, "form", formGenerator.generate("/archivedEvent-monthlyparticipationreport-action", MonthlyReportRequest.class));
    }

    @SuppressWarnings("unchecked")
    @View("archivedEvents-monthlyparticipationreport")
    @Get("/section-monthlyparticipationreport/{id}")
    public Map<String, Object> sectionMonthlyParticipationReport(HttpRequest<?> request,  @PathVariable String id) {
        List<Section> sections = new ArrayList<>();
        Section section = sectionAccessor.get(id);
        sections.add(section);
        return CollectionUtils.mapOf("sections", sections, "form", formGenerator.generate("/archivedEvent-monthlyparticipationreport-action", MonthlyReportRequest.class));
    }

    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Post("/archivedEvents-monthlyparticipationreport-action")
    HttpResponse<?> archivedEventsMonthlyParticipationReportAction(HttpRequest<?> request, @Valid @Body MonthlyReportRequest actionData) {

        Section section = sectionAccessor.get(actionData.sectionId());
        Map<Event, List<Participant>> eventParticipants = getEvents(section, actionData.month(), actionData.year());
        String dateStr = actionData.month()+"-"+actionData.year();

        String filename = participationReportAccessor.generateReport(section, eventParticipants, dateStr, "MONTHLY PARTICIPATION");
        if (filename == null) {
            return HttpResponse.serverError("Could not create report");
        }

        return HttpResponse.seeOther(UriBuilder.of("/").path("/archivedEvents-monthlyparticipationreport-pdf/"+section.getId()+"/"+dateStr+"/"+filename).build());
    } 

    @Get(uri = "/archivedEvents-monthlyparticipationreport-pdf/{sectionId}/{dateStr}/{filename}", produces = MediaType.APPLICATION_PDF)
    public HttpResponse<byte[]> downloadArchivedEventMonthlyParticipationReport(HttpRequest<?> request, @PathVariable String sectionId, @PathVariable String dateStr, @PathVariable String filename) {

        try {
            Section section = sectionAccessor.get(sectionId);
            filename = participationReportAccessor.getTempReportDir()+filename;
            byte[] fileBytes = Files.readAllBytes(Paths.get(filename));
            String newFilename = String.format("NetLog-MonthlyParticipationReport-%s-%s.pdf", section.getName(), dateStr);
            return HttpResponse.ok(fileBytes)
                    .header("Content-Disposition", "attachment; filename=\""+newFilename+"\"");
        } catch (Exception e) {
            return HttpResponse.serverError();
        }
    }

    @SuppressWarnings("unchecked")
    @View("archivedEvents-quarterlyreport")
    @Get("/archivedEvents-quarterlyreport")
    public Map<String, Object> archivedEventsQuarterlyReport(HttpRequest<?> request) {
        List<Section> sections = sectionAccessor.list();
        return CollectionUtils.mapOf("sections", sections, "form", formGenerator.generate("/archivedEvent-quarterlyreport-action", QuarterlyReportRequest.class));
    }

    @SuppressWarnings("unchecked")
    @View("archivedEvents-quarterlyreport")
    @Get("/section-quarterlyreport/{id}")
    public Map<String, Object> sectionQuarterlyReport(HttpRequest<?> request,  @PathVariable String id) {
        List<Section> sections = new ArrayList<>();
        Section section = sectionAccessor.get(id);
        sections.add(section);
        return CollectionUtils.mapOf("sections", sections, "form", formGenerator.generate("/archivedEvent-quarterlyreport-action", QuarterlyReportRequest.class));
    }

    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Post("/archivedEvents-quarterlyreport-action")
    HttpResponse<?> archivedEventsMonthlyReportAction(HttpRequest<?> request, @Valid @Body QuarterlyReportRequest actionData) {

        Section section = sectionAccessor.get(actionData.sectionId());
        Map<Event, List<Participant>> eventParticipants = getEvents(section, actionData.quarter(), actionData.year());
        String dateStr = actionData.quarter()+"-"+actionData.year();

        String filename = summaryReportAccessor.generateReport(section, eventParticipants, dateStr, "QUARTERLY SUMMARY");
        if (filename == null) {
            return HttpResponse.serverError("Could not create report");
        }

        return HttpResponse.seeOther(UriBuilder.of("/").path("/archivedEvents-quarterlyreport-pdf/"+section.getId()+"/"+dateStr+"/"+filename).build());
    } 

    @Get(uri = "/archivedEvents-quarterlyreport-pdf/{sectionId}/{dateStr}/{filename}", produces = MediaType.APPLICATION_PDF)
    public HttpResponse<byte[]> downloadArchivedEventQuarterlyReport(HttpRequest<?> request, @PathVariable String sectionId, @PathVariable String dateStr, @PathVariable String filename) {

        try {
            Section section = sectionAccessor.get(sectionId);
            filename = summaryReportAccessor.getTempReportDir()+filename;
            byte[] fileBytes = Files.readAllBytes(Paths.get(filename));
            String newFilename = String.format("NetLog-QuarterlySummaryReport-%s-%s.pdf", section.getName(), dateStr);
            return HttpResponse.ok(fileBytes)
                    .header("Content-Disposition", "attachment; filename=\""+newFilename+"\"");
        } catch (Exception e) {
            return HttpResponse.serverError();
        }
    }

    @SuppressWarnings("unchecked")
    @View("archivedEvents-quarterlyparticipationreport")
    @Get("/archivedEvents-quarterlyparticipationreport")
    public Map<String, Object> archivedEventsQuarterlyParticipationReport(HttpRequest<?> request) {
        List<Section> sections = sectionAccessor.list();
        return CollectionUtils.mapOf("sections", sections, "form", formGenerator.generate("/archivedEvent-quarterlyparticipationreport-action", QuarterlyReportRequest.class));
    }

    @SuppressWarnings("unchecked")
    @View("archivedEvents-quarterlyparticipationreport")
    @Get("/section-quarterlyparticipationreport/{id}")
    public Map<String, Object> sectionQuarterlyParticipationReport(HttpRequest<?> request, @PathVariable String id) {
        List<Section> sections = new ArrayList<>();
        Section section = sectionAccessor.get(id);
        sections.add(section);
        return CollectionUtils.mapOf("sections", sections, "form", formGenerator.generate("/archivedEvent-quarterlyparticipationreport-action", QuarterlyReportRequest.class));
    }

    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Post("/archivedEvents-quarterlyparticipationreport-action")
    HttpResponse<?> archivedEventsQuarterlyParticipationReportAction(HttpRequest<?> request, @Valid @Body QuarterlyReportRequest actionData) {

        Section section = sectionAccessor.get(actionData.sectionId());
        Map<Event, List<Participant>> eventParticipants = getEvents(section, actionData.quarter(), actionData.year());
        String dateStr = actionData.quarter()+"-"+actionData.year();

        String filename = participationReportAccessor.generateReport(section, eventParticipants, dateStr, "QUARTERLY PARTICIPATION");
        if (filename == null) {
            return HttpResponse.serverError("Could not create report");
        }

        return HttpResponse.seeOther(UriBuilder.of("/").path("/archivedEvents-quarterlyparticipationreport-pdf/"+section.getId()+"/"+dateStr+"/"+filename).build());
    } 

    @Get(uri = "/archivedEvents-quarterlyparticipationreport-pdf/{sectionId}/{dateStr}/{filename}", produces = MediaType.APPLICATION_PDF)
    public HttpResponse<byte[]> downloadArchivedEventQuarterlyParticipationReport(HttpRequest<?> request, @PathVariable String sectionId, @PathVariable String dateStr, @PathVariable String filename) {

        try {
            Section section = sectionAccessor.get(sectionId);
            filename = participationReportAccessor.getTempReportDir()+filename;
            byte[] fileBytes = Files.readAllBytes(Paths.get(filename));
            String newFilename = String.format("NetLog-QuarterlyParticipationReport-%s-%s.pdf", section.getName(), dateStr);
            return HttpResponse.ok(fileBytes)
                    .header("Content-Disposition", "attachment; filename=\""+newFilename+"\"");
        } catch (Exception e) {
            return HttpResponse.serverError();
        }
    }

    @SuppressWarnings("unchecked")
    @View("archivedEvent-delete")
    @Get("/archivedEvent-delete/{id}")
    public Map<String, Object> archivedEventDelete(HttpRequest<?> request,  @PathVariable String id) {
        Event event = eventAccessor.get(id);
        return CollectionUtils.mapOf("event", event, 
                                     "form", formGenerator.generate("/archivedEvent-delete-action/"+id, BlankRequest.class));
    }

    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Post("/archivedEvent-delete-action/{id}")
    HttpResponse<?> archivedEventDeleteAction(HttpRequest<?> request, @PathVariable String id) {
        Event event = eventAccessor.get(id);

        // delete participants
        List<Participant> participants = participantAccessor.listParticipants(event, false);
        for (Participant participant : participants) {
            participantAccessor.delete(participant.getId());
        }

        eventAccessor.delete(id);

        return HttpResponse.seeOther(UriBuilder.of("/").path("/archives").build());
    } 

    @View("home")
    @Get("/")
    public HttpResponse<?> home(HttpRequest<?> request) {
        List<Section> sections = sectionAccessor.list();
        List<Operator> operators = operatorAccessor.list();
        List<Event> activeEvents = eventAccessor.list(true, true);
        List<RecurringEvent> recurringEvents = recurringEventAccessor.list(true);

        int sectionCount = 0;
        int operatorCount = 0;
        int activeEventsCount = 0;
        int recurringEventsCount = 0;
        if (sections != null) {
            sectionCount = sections.size();
        }
        if (operators != null) {
            operatorCount = operators.size();
        }
        if (activeEvents != null) {
            activeEventsCount = activeEvents.size();
        }
        if (recurringEvents != null) {
            recurringEventsCount = recurringEvents.size();
        }
        return HttpResponse.ok(CollectionUtils.mapOf("sectionCount", sectionCount, "operatorCount", operatorCount, "activeEventsCount", activeEventsCount, "recurringEventsCount", recurringEventsCount));
    }

    @View("license")
    @Get("/license")
    public HttpResponse<?> license(HttpRequest<?> request) {
        return HttpResponse.ok();
    }

    @View("sections")
    @Get("/sections")
    public HttpResponse<?> sections(HttpRequest<?> request) {
       List<Section> sections = sectionAccessor.list();
       return HttpResponse.ok(CollectionUtils.mapOf("sections", sections));
    }

    @View("section")
    @Get("/section/{id}")
    public HttpResponse<?> sectionDetails(HttpRequest<?> request, @PathVariable String id) {
        Section section = sectionAccessor.get(id);
        List<SectionOperator> operators = operatorAccessor.listOperators(section);
        return HttpResponse.ok(CollectionUtils.mapOf("section", section, "operators", operators, "operatorCount", operators.size()));
    }

    @SuppressWarnings("unchecked")
    @View("section-edit")
    @Get("/section-edit/{id}")
    public Map<String, Object> sectionEdit(HttpRequest<?> request,  @PathVariable String id) {
        Section section = sectionAccessor.get(id);
        List<SectionOperator> sectionOperators = operatorAccessor.listOperators(section);
        List<Operator> operators = operatorAccessor.list();
        removeMemberOperators(operators, sectionOperators);
        String sectionOperatorsString = buildSectionOperatorsString(sectionOperators);

        return CollectionUtils.mapOf("section", section, "sectionOperators", sectionOperators, "sectionOperatorsString", sectionOperatorsString, "operators", operators, 
                                     "form", formGenerator.generate("/section-edit-action/"+id, SectionCreateRequest.class));
    }

    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Post("/section-edit-action/{id}")
    HttpResponse<?> sectionEditAction(HttpRequest<?> request, @Valid @Body SectionCreateRequest actionData,  @PathVariable String id) {
        Section section = sectionAccessor.get(id);
        section.setName(actionData.name());
        section.setDescription(actionData.description());

        sectionAccessor.update(id, section);

        addMembers(section, actionData.callsignMemberIds());

        return HttpResponse.seeOther(UriBuilder.of("/").path("/section/"+id).build());
    } 

    @SuppressWarnings("unchecked")
    @View("section-delete")
    @Get("/section-delete/{id}")
    public Map<String, Object> sectionDelete(HttpRequest<?> request,  @PathVariable String id) {
        Section section = sectionAccessor.get(id);
        return CollectionUtils.mapOf("section", section, 
                                     "form", formGenerator.generate("/section-delete-action/"+id, BlankRequest.class));
    }

    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Post("/section-delete-action/{id}")
    HttpResponse<?> sectionDeleteAction(HttpRequest<?> request,  @PathVariable String id) {
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

        return HttpResponse.seeOther(UriBuilder.of("/").path("/sections").build());
    } 

    @SuppressWarnings("unchecked")
    @View("section-add")
    @Get("/section-add")
    public Map<String, Object> sectionAdd(HttpRequest<?> request) {
        return CollectionUtils.mapOf("form", formGenerator.generate("/section-add-action", SectionCreateRequest.class));
    }

    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Post("/section-add-action")
    HttpResponse<?> sectionAddAction(HttpRequest<?> request, @Valid @Body SectionCreateRequest actionData) {
        Section section = new Section();
        section.setName(actionData.name());
        section.setDescription(actionData.description());

        Section sectionNew = sectionAccessor.create(section);
        if (sectionNew != null) {
            return HttpResponse.seeOther(UriBuilder.of("/").path("/section/"+sectionNew.getId()).build());
        }

        return HttpResponse.seeOther(UriBuilder.of("/").path("/sections").build());
    } 


    @SuppressWarnings("unchecked")
    @View("section-monthlynonparticipationreport")
    @Get("/section-monthlynonparticipationreport/{id}")
    public Map<String, Object> sectionMonthlyNonParticipationReport(HttpRequest<?> request, @PathVariable String id) {
        Section section = sectionAccessor.get(id);
        return CollectionUtils.mapOf("section", section, "form", formGenerator.generate("/section-monthlynonparticipationreport-action", MonthlyReportRequest.class));
    }

    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Post("/section-monthlynonparticipationreport-action")
    HttpResponse<?> sectionMonthlyNonParticipationReportAction(HttpRequest<?> request, @Valid @Body MonthlyReportRequest actionData) {

        Section section = sectionAccessor.get(actionData.sectionId());
        Map<Event, List<Participant>> eventParticipants = getEvents(section, actionData.month(), actionData.year());
        String dateStr = actionData.month()+"-"+actionData.year();
        List<SectionOperator> members = operatorAccessor.listOperators(section);

        String filename = nonParticipationReportAccessor.generateReport(section, members, eventParticipants, dateStr, "MONTHLY NON-PARTICIPATION");
        if (filename == null) {
            return HttpResponse.serverError("Could not create report");
        }

        return HttpResponse.seeOther(UriBuilder.of("/").path("/section-monthlynonparticipationreport-pdf/"+section.getId()+"/"+dateStr+"/"+filename).build());
    } 

    @Get(uri = "/section-monthlynonparticipationreport-pdf/{sectionId}/{dateStr}/{filename}", produces = MediaType.APPLICATION_PDF)
    public HttpResponse<byte[]> downloadSectionMonthlyNonParticipationReport(HttpRequest<?> request, @PathVariable String sectionId, @PathVariable String dateStr, @PathVariable String filename) {

        try {
            Section section = sectionAccessor.get(sectionId);
            filename = nonParticipationReportAccessor.getTempReportDir()+filename;
            byte[] fileBytes = Files.readAllBytes(Paths.get(filename));
            String newFilename = String.format("NetLog-MonthlyNonParticipationReport-%s-%s.pdf", section.getName(), dateStr);
            return HttpResponse.ok(fileBytes)
                    .header("Content-Disposition", "attachment; filename=\""+newFilename+"\"");
        } catch (Exception e) {
            return HttpResponse.serverError();
        }
    }

    @SuppressWarnings("unchecked")
    @View("section-quarterlynonparticipationreport")
    @Get("/section-quarterlynonparticipationreport/{id}")
    public Map<String, Object> sectionQuarterlyNonParticipationReport(HttpRequest<?> request, @PathVariable String id) {
        Section section = sectionAccessor.get(id);
        return CollectionUtils.mapOf("section", section, "form", formGenerator.generate("/section-quarterlynonparticipationreport-action", QuarterlyReportRequest.class));
    }

    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Post("/section-quarterlynonparticipationreport-action")
    HttpResponse<?> sectionQuarterlyNonParticipationReportAction(HttpRequest<?> request, @Valid @Body QuarterlyReportRequest actionData) {
        Section section = sectionAccessor.get(actionData.sectionId());
        Map<Event, List<Participant>> eventParticipants = getEvents(section, actionData.quarter(), actionData.year());
        String dateStr = actionData.quarter()+"-"+actionData.year();
        List<SectionOperator> members = operatorAccessor.listOperators(section);

        String filename = nonParticipationReportAccessor.generateReport(section, members, eventParticipants, dateStr, "MONTHLY NON-PARTICIPATION");
        if (filename == null) {
            return HttpResponse.serverError("Could not create report");
        }

        return HttpResponse.seeOther(UriBuilder.of("/").path("/section-quarterlynonparticipationreport-pdf/"+section.getId()+"/"+dateStr+"/"+filename).build());
    } 

    @Get(uri = "/section-quarterlynonparticipationreport-pdf/{sectionId}/{dateStr}/{filename}", produces = MediaType.APPLICATION_PDF)
    public HttpResponse<byte[]> downloadSectionQuarterlyNonParticipationReport(HttpRequest<?> request, @PathVariable String sectionId, @PathVariable String dateStr, @PathVariable String filename) {
        try {
            Section section = sectionAccessor.get(sectionId);
            filename = nonParticipationReportAccessor.getTempReportDir()+filename;
            byte[] fileBytes = Files.readAllBytes(Paths.get(filename));
            String newFilename = String.format("NetLog-QuarterlyNonParticipationReport-%s-%s.pdf", section.getName(), dateStr);
            return HttpResponse.ok(fileBytes)
                    .header("Content-Disposition", "attachment; filename=\""+newFilename+"\"");
        } catch (Exception e) {
            return HttpResponse.serverError();
        }
    }

    @View("operators")
    @Get("/operators")
    public HttpResponse<?> operators(HttpRequest<?> request) {
       List<Operator> operators = operatorAccessor.list();
       return HttpResponse.ok(CollectionUtils.mapOf("operators", operators));
    }

    @View("operator")
    @Get("/operator/{id}")
    public HttpResponse<?> operatorDetails(HttpRequest<?> request, @PathVariable String id) {
        Operator operator = operatorAccessor.get(id);
        List<SectionOperator> sectionOperators = operatorAccessor.listSections(operator);
        hydrateSectionOperators(sectionOperators);
        return HttpResponse.ok(CollectionUtils.mapOf("operator", operator, "sectionOperators", sectionOperators));
    }

    @SuppressWarnings("unchecked")
    @View("operator-edit")
    @Get("/operator-edit/{id}")
    public Map<String, Object> operatorEdit(HttpRequest<?> request,  @PathVariable String id) {
        Operator operator = operatorAccessor.get(id);
        return CollectionUtils.mapOf("operator", operator, "isSkywarnChecked", operator.isSkywarn() ? "yes" : null,  "isNTSChecked", operator.isNTS() ? "yes" : null, 
                                     "form", formGenerator.generate("/operator-edit-action/"+id, OperatorCreateRequest.class));
    }

    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Post("/operator-edit-action/{id}")
    HttpResponse<?> operatorEditAction(HttpRequest<?> request, @Valid @Body OperatorCreateRequest actionData,  @PathVariable String id) {
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
        operator.setNTS(isNTS);
        operator.setSkywarn(isSkywarn);

        operatorAccessor.update(id, operator);

        return HttpResponse.seeOther(UriBuilder.of("/").path("/operator/"+id).build());
    } 

    @SuppressWarnings("unchecked")
    @View("operator-add")
    @Get("/operator-add")
    public Map<String, Object> operatorAdd(HttpRequest<?> request) {
        return CollectionUtils.mapOf("form", formGenerator.generate("/operator-add-action", OperatorCreateRequest.class));
    }

    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Post("/operator-add-action")
    HttpResponse<?> operatorAddAction(HttpRequest<?> request, @Valid @Body OperatorCreateRequest actionData) {
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
        operator.setNTS(isNTS);
        operator.setSkywarn(isSkywarn);

        Operator operatorNew = operatorAccessor.create(operator);
        if (operatorNew != null) {
            return HttpResponse.seeOther(UriBuilder.of("/").path("/operator/"+operatorNew.getId()).build());
        }

        return HttpResponse.seeOther(UriBuilder.of("/").path("/operators").build());
    } 

    @SuppressWarnings("unchecked")
    @View("operator-addbulk")
    @Get("/operator-addbulk")
    public Map<String, Object> operatorAddBulk(HttpRequest<?> request) {
        return CollectionUtils.mapOf("form", formGenerator.generate("/operator-addbulk-action", OperatorBulkCreateRequest.class));
    }

    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Post("/operator-addbulk-action")
    HttpResponse<?> operatorAddBulkAction(HttpRequest<?> request, @Valid @Body OperatorBulkCreateRequest actionData) {

        addAllOperators(actionData.bulkList());

        return HttpResponse.seeOther(UriBuilder.of("/").path("/operators").build());
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

    @SuppressWarnings("unchecked")
    @View("operator-delete")
    @Get("/operator-delete/{id}")
    public Map<String, Object> operatorDelete(HttpRequest<?> request,  @PathVariable String id) {
        Operator operator = operatorAccessor.get(id);
        return CollectionUtils.mapOf("operator", operator, 
                                     "form", formGenerator.generate("/operator-delete-action/"+id, BlankRequest.class));
    }

    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Post("/operator-delete-action/{id}")
    HttpResponse<?> operatorDeleteAction(HttpRequest<?> request, @PathVariable String id) {
        operatorAccessor.get(id);
        operatorAccessor.delete(id);

        return HttpResponse.seeOther(UriBuilder.of("/").path("/operators").build());
    } 

    @View("recurringEvents")
    @Get("/recurringEvents")
    public HttpResponse<?> recurringEvents(HttpRequest<?> request) {
       List<RecurringEvent> recurringEvents = recurringEventAccessor.list(true);
       hydrateRecurringEvents(recurringEvents);

       return HttpResponse.ok(CollectionUtils.mapOf("recurringEvents", recurringEvents));
    }

    @SuppressWarnings("unchecked")
    @View("recurringEvents-report")
    @Get("/recurringEvents-report")
    public Map<String, Object> recurringEventsReport(HttpRequest<?> request) {
        List<Section> sections = sectionAccessor.list();
        return CollectionUtils.mapOf("sections", sections, 
                                        "form", formGenerator.generate("/recurringEvents-report-action", RecurringEventReportRequest.class));
    }

    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Post("/recurringEvents-report-action")
    HttpResponse<?> recurringEventsReportAction(HttpRequest<?> request, @Valid @Body RecurringEventReportRequest actionData) {

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
            return HttpResponse.serverError("Could not create report");
        }

        return HttpResponse.seeOther(UriBuilder.of("/").path("/recurringEvents-report-pdf/"+section.getId()+"/"+filename).build());
    } 

    @Get(uri = "/recurringEvents-report-pdf/{sectionId}/{filename}", produces = MediaType.APPLICATION_PDF)
    public HttpResponse<byte[]> downloadRecurringEventsReport(HttpRequest<?> request, @PathVariable String sectionId, @PathVariable String filename) {

        try {
            Section section = sectionAccessor.get(sectionId);
            filename = participationReportAccessor.getTempReportDir()+filename;
            byte[] fileBytes = Files.readAllBytes(Paths.get(filename));
            String newFilename = String.format("NetLog-EventSchedule-%s.pdf", section.getName());
            return HttpResponse.ok(fileBytes)
                    .header("Content-Disposition", "attachment; filename=\""+newFilename+"\"");
        } catch (Exception e) {
            return HttpResponse.serverError();
        }
    }

    @View("recurringEvent")
    @Get("/recurringEvent/{id}")
    public HttpResponse<?> recurringEventsDetails(HttpRequest<?> request, @PathVariable String id) {
       RecurringEvent recurringEvent = recurringEventAccessor.get(id);
       hydrateRecurringEvent(recurringEvent);
       return HttpResponse.ok(CollectionUtils.mapOf("recurringEvent", recurringEvent));
    }

    @SuppressWarnings("unchecked")
    @View("recurringEvent-edit")
    @Get("/recurringEvent-edit/{id}")
    public Map<String, Object> recurringEventEdit(HttpRequest<?> request,  @PathVariable String id) {
        RecurringEvent recurringEvent = recurringEventAccessor.get(id);
        hydrateRecurringEvent(recurringEvent);
        return CollectionUtils.mapOf("recurringEvent", recurringEvent, "recurringEventType"+recurringEvent.getType().ordinal(), "yes",
                                     "form", formGenerator.generate("/recurringEvent-edit-action/"+id, RecurringEventCreateRequest.class));
    }

    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Post("/recurringEvent-edit-action/{id}")
    HttpResponse<?> recurringEventEditAction(HttpRequest<?> request, @Valid @Body RecurringEventCreateRequest actionData,  @PathVariable String id) {
        RecurringEvent recurringEvent = recurringEventAccessor.get(id);
        recurringEvent.setName(actionData.name());
        recurringEvent.setDescription(actionData.description());
        recurringEvent.setLocation(actionData.location());
        recurringEvent.setSchedule(actionData.schedule());
        recurringEvent.setType(EventType.values()[actionData.type()]);
        recurringEvent.setNetControlCallsign(actionData.netControlCallsign());

        recurringEventAccessor.update(id, recurringEvent);

        return HttpResponse.seeOther(UriBuilder.of("/").path("/recurringEvent/"+id).build());
    } 

    @SuppressWarnings("unchecked")
    @View("recurringEvent-add")
    @Get("/recurringEvent-add")
    public Map<String, Object> recurringEventAdd(HttpRequest<?> request) {
        List<Section> sections = sectionAccessor.list();

        return CollectionUtils.mapOf("sections", sections, "form", formGenerator.generate("/recurringEvent-add-action", RecurringEventCreateRequest.class));
    }

    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Post("/recurringEvent-add-action")
    HttpResponse<?> recurringEventAddAction(HttpRequest<?> request, @Valid @Body RecurringEventCreateRequest actionData) {
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
            return HttpResponse.seeOther(UriBuilder.of("/").path("/recurringEvent/"+recurringEventNew.getId()).build());
        }

        return HttpResponse.seeOther(UriBuilder.of("/").path("/recurringEvents").build());
    } 

    @SuppressWarnings("unchecked")
    @View("recurringEvent-delete")
    @Get("/recurringEvent-delete/{id}")
    public Map<String, Object> recurringEventDelete(HttpRequest<?> request,  @PathVariable String id) {
        RecurringEvent recurringEvent = recurringEventAccessor.get(id);
        return CollectionUtils.mapOf("recurringEvent", recurringEvent, 
                                     "form", formGenerator.generate("/recurringEvent-delete-action/"+id, BlankRequest.class));
    }

    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Post("/recurringEvent-delete-action/{id}")
    HttpResponse<?> recurringEventDeleteAction(HttpRequest<?> request, @PathVariable String id) {

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

        return HttpResponse.seeOther(UriBuilder.of("/").path("/recurringEvents").build());
    } 

    @SuppressWarnings("unchecked")
    @View("recurringEvent-start")
    @Get("/recurringEvent-start/{id}")
    public Map<String, Object> recurringEventStart(HttpRequest<?> request,  @PathVariable String id) {
        RecurringEvent recurringEvent = recurringEventAccessor.get(id);
        return CollectionUtils.mapOf("recurringEvent", recurringEvent, "startTimeStr", LocalDateTime.now(),
                                     "form", formGenerator.generate("/recurringEvent-start-action/"+id, ActiveEventStartEndRequest.class));
    }

    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Post("/recurringEvent-start-action/{id}")
    HttpResponse<?> recurringEventStartAction(HttpRequest<?> request, @Valid @Body ActiveEventStartEndRequest actionData, @PathVariable String id) {
        RecurringEvent recurringEvent = recurringEventAccessor.get(id);

        Event event = new Event();
        event.setDescription(recurringEvent.getDescription());
        event.setLocation(recurringEvent.getLocation());
        event.setName(recurringEvent.getName());
        event.setRecurringEvent(recurringEvent);
        event.setSection(recurringEvent.getSection());
        event.setSecure(false);
        event.setStartTime(LocalDateTime.parse(actionData.startTimeStr()));
        event.setType(recurringEvent.getType());
        event.setNetControlCallsign(recurringEvent.getNetControlCallsign());
        event.setSectionActive(true);

        Event eventNew = eventAccessor.create(recurringEvent.getSection(), recurringEvent, event);
        if (eventNew != null) {
            return HttpResponse.seeOther(UriBuilder.of("/").path("/activeEvent/"+eventNew.getId()).build());
        }

        return HttpResponse.seeOther(UriBuilder.of("/").path("/recurringEvents").build());
    } 

    @View("activeEvents")
    @Get("/activeEvents")
    public HttpResponse<?> activeEvents(HttpRequest<?> request) {
        List<Event> activeEvents = eventAccessor.list(true, true);

        hydrateActiveEvents(activeEvents);
        return HttpResponse.ok(CollectionUtils.mapOf("activeEvents", activeEvents));
    }

    @View("activeEvent")
    @Get("/activeEvent/{id}")
    public HttpResponse<?> activeEventsDetails(HttpRequest<?> request, @PathVariable String id) {
        Event event = eventAccessor.get(id);
        hydrateActiveEvent(event);

        List<Participant> participants = participantAccessor.listParticipants(event, false);
        hydrateParticipants(event, participants);

        Collections.sort(participants, new Comparator<Participant>() {
            @Override
            public int compare(Participant obj1, Participant obj2) {
                return obj1.getOperator().getCallsign().compareTo(obj2.getOperator().getCallsign());
            }
        });

        return HttpResponse.ok(CollectionUtils.mapOf("event", event, "participants", participants, "participantCount", participants.size()));
    }

    @SuppressWarnings("unchecked")
    @View("activeEvent-edit")
    @Get("/activeEvent-edit/{id}")
    public Map<String, Object> activeEventEdit(HttpRequest<?> request,  @PathVariable String id) {
        Event event = eventAccessor.get(id);
        hydrateActiveEvent(event);

        List<Participant> participants = participantAccessor.listParticipants(event, true);

        Collections.sort(participants, new Comparator<Participant>() {
            @Override
            public int compare(Participant obj1, Participant obj2) {
                return obj1.getOperator().getCallsign().compareTo(obj2.getOperator().getCallsign());
            }
        });

        return CollectionUtils.mapOf("event", event, "eventType"+event.getType().ordinal(), "yes",  "participants", participants, 
                                        "checkInTimeStr", event.getStartTime().toString(),
                                        "form", formGenerator.generate("/activeEvent-edit-action/"+id, ActiveEventEditRequest.class));
    }

    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Post("/activeEvent-edit-action/{id}")
    HttpResponse<?> activeEventEditAction(HttpRequest<?> request, @Valid @Body ActiveEventEditRequest actionData,  @PathVariable String id) {
        Event event = eventAccessor.get(id);
        event.setName(actionData.name());
        event.setDescription(actionData.name());
        event.setLocation(actionData.location());
        event.setStartTime(LocalDateTime.parse(actionData.startTimeStr()));
        // no setting secure here
        event.setType(EventType.values()[actionData.type()]);
        event.setNetControlCallsign(actionData.netControlCallsign());

        event = eventAccessor.update(id, event);

        return HttpResponse.seeOther(UriBuilder.of("/").path("/activeEvent/"+id).build());
    } 

    @SuppressWarnings("unchecked")
    @View("activeEvent-checkin")
    @Get("/activeEvent-checkin/{id}")
    public Map<String, Object> activeEventCheckin(HttpRequest<?> request,  @PathVariable String id) {
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

        return CollectionUtils.mapOf("event", event, "checkInTimeStr", event.getStartTime().toString(), "operators", operators, "previousOperators", previousOperators,
                                        "form", formGenerator.generate("/activeEvent-checkin-action/"+id, ActiveEventCheckInOutRequest.class));
    }

    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Post("/activeEvent-checkin-action/{id}")
    HttpResponse<?> activeEventCheckinAction(HttpRequest<?> request, @Valid @Body ActiveEventCheckInOutRequest actionData,  @PathVariable String id) {
        Event event = eventAccessor.get(id);

        checkInParticipants(event, actionData.participantIds());

        return HttpResponse.seeOther(UriBuilder.of("/").path("/activeEvent/"+id).build());
    } 

    @SuppressWarnings("unchecked")
    @View("activeEvent-checkout")
    @Get("/activeEvent-checkout/{id}")
    public Map<String, Object> activeEventCheckout(HttpRequest<?> request,  @PathVariable String id) {
        Event event = eventAccessor.get(id);
        hydrateActiveEvent(event);

        List<Participant> participants = participantAccessor.listParticipants(event, true);
        Collections.sort(participants, new Comparator<Participant>() {
            @Override
            public int compare(Participant obj1, Participant obj2) {
                return obj1.getOperator().getCallsign().compareTo(obj2.getOperator().getCallsign());
            }
        });

        return CollectionUtils.mapOf("event", event, "participants", participants, "checkOutTimeStr", LocalDateTime.now(),
                                        "form", formGenerator.generate("/activeEvent-checkout-action/"+id, ActiveEventCheckInOutRequest.class));
    }

    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Post("/activeEvent-checkout-action/{id}")
    HttpResponse<?> activeEventCheckoutAction(HttpRequest<?> request, @Valid @Body ActiveEventCheckInOutRequest actionData,  @PathVariable String id) {
        Event event = eventAccessor.get(id);

        checkOutParticipants(event, actionData.participantIds());

        return HttpResponse.seeOther(UriBuilder.of("/").path("/activeEvent/"+id).build());
    } 

    @SuppressWarnings("unchecked")
    @View("participant-edittimes")
    @Get("/participant-edittimes/{id}/")
    public Map<String, Object> participantEdit(HttpRequest<?> request,  @PathVariable String id) {
        Participant participant = participantAccessor.get(id);

        return CollectionUtils.mapOf("participant", participant, "showEndTime" , (participant.getEndTime() == null) ? null : "true",
                                        "form", formGenerator.generate("/participant-edittimes-action/"+id, ActiveEventStartEndRequest.class));
    }

    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Post("/participant-edittimes-action/{id}")
    HttpResponse<?> participantEditAction(HttpRequest<?> request, @Valid @Body ActiveEventStartEndRequest actionData,  @PathVariable String id) {
        Participant participant = participantAccessor.get(id);
        participant.setStartTime(LocalDateTime.parse(actionData.startTimeStr()));
        if (participant.getEndTime() != null) {
            participant.setEndTime(LocalDateTime.parse(actionData.endTimeStr()));
        }

        participantAccessor.update(id, participant);

        return HttpResponse.seeOther(UriBuilder.of("/").path("/activeEvent/"+participant.getEvent().getId()).build());
    } 

    @SuppressWarnings("unchecked")
    @View("participant-remove")
    @Get("/participant-remove/{id}")
    public Map<String, Object> participantRemove(HttpRequest<?> request,  @PathVariable String id) {
        Participant participant = participantAccessor.get(id);
        Event event = eventAccessor.get(participant.getEvent().getId());
        participant.setEvent(event);
        return CollectionUtils.mapOf("participant", participant, 
                                     "form", formGenerator.generate("/participant-remove-action/"+id, BlankRequest.class));
    }

    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Post("/participant-remove-action/{id}")
    HttpResponse<?> participantRemoveAction(HttpRequest<?> request, @PathVariable String id) {
        Participant participant = participantAccessor.get(id);
        String eventId = participant.getEvent().getId();
        participantAccessor.delete(id);

        return HttpResponse.seeOther(UriBuilder.of("/").path("/activeEvent/"+eventId).build());
    } 

    @SuppressWarnings("unchecked")
    @View("activeEvent-secure")
    @Get("/activeEvent-secure/{id}")
    public Map<String, Object> activeEventSecure(HttpRequest<?> request,  @PathVariable String id) {
        Event event = eventAccessor.get(id);
        return CollectionUtils.mapOf("event", event, "endTimeStr", LocalDateTime.now().toString(),
                                     "form", formGenerator.generate("/activeEvent-secure-action/"+id, ActiveEventStartEndRequest.class));
    }

    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Post("/activeEvent-secure-action/{id}")
    HttpResponse<?> activeEventSecureAction(HttpRequest<?> request, @Valid @Body ActiveEventStartEndRequest actionData, @PathVariable String id) {
        Event event = eventAccessor.get(id);

        // check out participants
        List<Participant> participants = participantAccessor.listParticipants(event, true);
        for (Participant participant : participants) {
            participantAccessor.checkOutParticipant(event, participant.getOperator(), LocalDateTime.parse(actionData.endTimeStr()));
        }

        event.setSecure(true);
        event.setEndTime(LocalDateTime.parse(actionData.endTimeStr()));
        eventAccessor.update(id, event);

        return HttpResponse.seeOther(UriBuilder.of("/").path("/activeEvents").build());
    } 

    @SuppressWarnings("unchecked")
    @View("activeEvent-delete")
    @Get("/activeEvent-delete/{id}")
    public Map<String, Object> activeEventDelete(HttpRequest<?> request,  @PathVariable String id) {
        Event event = eventAccessor.get(id);
        return CollectionUtils.mapOf("event", event, 
                                     "form", formGenerator.generate("/activeEvent-delete-action/"+id, BlankRequest.class));
    }

    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Post("/activeEvent-delete-action/{id}")
    HttpResponse<?> activeEventDeleteAction(HttpRequest<?> request, @PathVariable String id) {
        Event event = eventAccessor.get(id);

        // delete participants
        List<Participant> participants = participantAccessor.listParticipants(event, false);
        for (Participant participant : participants) {
            participantAccessor.delete(participant.getId());
        }

        eventAccessor.delete(id);

        return HttpResponse.seeOther(UriBuilder.of("/").path("/activeEvents").build());
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

    private void addMembers(Section section, String callsignMemberIds) {
        List<SectionOperator> newIds = convertToSectionOperatorList(callsignMemberIds);
        List<SectionOperator> existingOperators = operatorAccessor.listOperators(section);
        List<SectionOperator> addIds = new ArrayList<>();
        List<SectionOperator> removeOperators = new ArrayList<>();

        // remove those not in new list from old list
        for (SectionOperator existingOperator : existingOperators) {
            boolean found = false;
            for (SectionOperator newId : newIds) {
                if (newId.getId().equals(existingOperator.getId())) {
                    // found one in the list already
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
                    // found one in the list already
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
        List<SectionOperator> ret = new ArrayList<>();
        if ((callsignMemberIds == null) || (callsignMemberIds.isEmpty())) {
            return ret;
        }
        for (int i = 0; i < callsignMemberIds.length(); i += 37) {
            SectionOperator sectionOperator = new SectionOperator();
            sectionOperator.setId(callsignMemberIds.substring(i, i+36));
            String membershipType = callsignMemberIds.substring(i+36, i+37);
            sectionOperator.setMembershipType(MembershipType.values()[Integer.parseInt(membershipType)]);
            ret.add(sectionOperator);
        }

        return ret;
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

    private Map<Event, List<Participant>> getEvents(Section section, String monthStr, String yearStr) {
        if (monthStr.startsWith("Q")) {
            return getEventsQuarterly(section, monthStr, yearStr);
        }
        Map<Event, List<Participant>> eventParticipants = new TreeMap<>();
        List<Event> events = eventAccessor.listSecured(section);

        int year = Integer.parseInt(yearStr);
        int month = Integer.parseInt(monthStr);

        for (Event event : events) {
            if ((event.getStartTime().getMonthValue() == month) && (event.getStartTime().getYear() == year)) {
                List<Participant> participants = participantAccessor.listParticipants(event, false);
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

    private Map<Event, List<Participant>> getEventsQuarterly(Section section, String monthStr, String yearStr) {
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
        }

        for (Event event : events) {
            if ((event.getStartTime().getMonthValue() >= startMonth) && (event.getStartTime().getMonthValue() <= endMonth) && (event.getStartTime().getYear() == year)) {
                List<Participant> participants = participantAccessor.listParticipants(event, false);
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
}
