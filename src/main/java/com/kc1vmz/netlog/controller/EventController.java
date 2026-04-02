package com.kc1vmz.netlog.controller;

import java.time.LocalDateTime;
import java.util.List;

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

import com.kc1vmz.netlog.accessor.EventAccessor;
import com.kc1vmz.netlog.accessor.OperatorAccessor;
import com.kc1vmz.netlog.accessor.ParticipantAccessor;
import com.kc1vmz.netlog.accessor.RecurringEventAccessor;
import com.kc1vmz.netlog.accessor.SectionAccessor;
import com.kc1vmz.netlog.object.Event;
import com.kc1vmz.netlog.object.Operator;
import com.kc1vmz.netlog.object.Participant;
import com.kc1vmz.netlog.object.RecurringEvent;
import com.kc1vmz.netlog.object.Section;

import io.micronaut.http.annotation.*;
import jakarta.inject.Inject;

@Controller("/api/v1/events")
public class EventController {

    @Inject
    private EventAccessor eventAccessor;
    @Inject
    private OperatorAccessor operatorAccessor;
    @Inject
    private SectionAccessor sectionAccessor;    
    @Inject
    private RecurringEventAccessor recurringEventAccessor;    
    @Inject
    private ParticipantAccessor participantAccessor;

    @Get("/{id}")
    public Event get(@PathVariable String id) {
        return get(id, true);
    }

    private Event get(@PathVariable String id, boolean hydrate) {
        Event event = eventAccessor.get(id);
        if (hydrate) {
            if ((event != null) && (event.getSection() != null) && (event.getSection().getId() != null) ) {
                event.setSection(sectionAccessor.get(event.getSection().getId()));
            }
            if ((event != null) && (event.getRecurringEvent() != null) && (event.getRecurringEvent().getId() != null) ) {
                event.setRecurringEvent(recurringEventAccessor.get(event.getRecurringEvent().getId()));
            }
        }
        return event;
    }

    @Put("/{id}")
    public Event update(@PathVariable String id, @Body Event obj) {
        return eventAccessor.update(id, obj);
    }

    @Delete("/{id}")
    public void delete(@PathVariable String id) {
        eventAccessor.delete(id);
    }

    @Get("/{id}/participants")
    public List<Participant> listParticipants(@PathVariable String id) {
        // given the event, get the operators
        // then fill in the section and recurringEvent
        Event event = get(id);

        List<Participant> participants =  participantAccessor.listParticipants(event, false);
        if ((participants != null) && (!participants.isEmpty())) {
            for (Participant participant : participants) {
                // fill in the event
                participant.setEvent(event);
                if ((participant.getOperator() != null) && (participant.getOperator().getId() != null)) {
                    participant.setOperator(operatorAccessor.get(participant.getOperator().getId()));
                }
            }
        }

        return participants;
    }

    @Put("/{id}/participants/{operatorId}")
    public List<Participant> checkInParticipant(@PathVariable String id, @PathVariable String operatorId) {
        Event event = eventAccessor.get(id);
        Operator operator = operatorAccessor.get(operatorId);
        List<Participant> participants = participantAccessor.checkInParticipant(event, operator, LocalDateTime.now());

        hydrateParticipants(participants);

        return participants;
    }

    @Delete("/{id}/participants/{operatorId}")
    public List<Participant> checkOutParticipant(@PathVariable String id, @PathVariable String operatorId) {
        Event event = eventAccessor.get(id);
        Operator operator = operatorAccessor.get(operatorId);
        List<Participant> participants = participantAccessor.checkOutParticipant(event, operator, LocalDateTime.now());

        hydrateParticipants(participants);

        return participants;
    }

    private void hydrateParticipants(List<Participant> participants) {
        if (participants != null) {
            RecurringEvent recurringEvent = null;
            Section section = null;
            for (Participant participant : participants) {
                if ((participant.getOperator() != null) && (participant.getOperator().getId() != null)) {
                    participant.setOperator(operatorAccessor.get(participant.getOperator().getId()));
                }
                if (recurringEvent == null) {
                    if ((participant.getEvent() != null) && (participant.getEvent().getRecurringEvent() != null) && (participant.getEvent().getRecurringEvent().getId() != null)) {
                        recurringEvent = recurringEventAccessor.get(participant.getEvent().getRecurringEvent().getId());
                    }
                }
                if (section == null) {
                    if ((participant.getEvent() != null) && (participant.getEvent().getSection() != null) && (participant.getEvent().getSection().getId() != null)) {
                        section = sectionAccessor.get(participant.getEvent().getSection().getId());
                    }
                }
                participant.getEvent().setRecurringEvent(recurringEvent);
                participant.getEvent().setSection(section);
            }
        }
    }
}