package com.kc1vmz.netlog.controller;

import java.util.List;

import com.kc1vmz.netlog.accessor.EventAccessor;
import com.kc1vmz.netlog.accessor.ParticipantAccessor;

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

import com.kc1vmz.netlog.accessor.RecurringEventAccessor;
import com.kc1vmz.netlog.accessor.SectionAccessor;
import com.kc1vmz.netlog.object.Event;
import com.kc1vmz.netlog.object.Participant;
import com.kc1vmz.netlog.object.RecurringEvent;
import com.kc1vmz.netlog.object.Section;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import jakarta.inject.Inject;

@Controller("/api/v1/recurringEvents")
public class RecurringEventController {

    @Inject
    private RecurringEventAccessor recurringEventAccessor;
    @Inject
    private EventAccessor eventAccessor;
    @Inject
    private SectionAccessor sectionAccessor;
    @Inject
    private ParticipantAccessor participantAccessor;

    @Get("/{id}")
    public RecurringEvent get(@PathVariable String id) {
        return get(id, true);
    }

    private RecurringEvent get(String id, boolean hydrate) {
        RecurringEvent recurringEvent = recurringEventAccessor.get(id);
        if ((recurringEvent != null) && (recurringEvent.getSection() != null) && (recurringEvent.getSection().getId() != null)) {
            recurringEvent.setSection(sectionAccessor.get(recurringEvent.getSection().getId()));
        }
        return recurringEvent;
    }

    @Put("/{id}")
    public RecurringEvent update(@PathVariable String id, @Body RecurringEvent obj) {
        return recurringEventAccessor.update(id, obj);
    }

    @Delete("/{id}")
    public void delete(@PathVariable String id) {
        RecurringEvent recurringEvent = recurringEventAccessor.get(id);

        List<Event> events = eventAccessor.listByRecurringEvent(recurringEvent, false, false);
        for (Event event : events) {
            List<Participant> participants = participantAccessor.listParticipants(event, false);
            for (Participant participant : participants) {
                participantAccessor.delete(participant.getId());
            }
            eventAccessor.delete(event.getId());
        }
        recurringEventAccessor.delete(id);
    }

    @Get("/{id}/events")
    public List<Event> listEvents(@PathVariable String id) {
        RecurringEvent recurringEvent = recurringEventAccessor.get(id);
        return eventAccessor.listByRecurringEvent(recurringEvent, false, true);
    }

    @Post("/{id}/events")
    public HttpResponse<Event> create(@PathVariable String id, @Body Event event) {
        RecurringEvent recurringEvent = recurringEventAccessor.get(id);
        Section section = null;
        if ((recurringEvent != null) && (recurringEvent.getSection() != null) && (recurringEvent.getSection().getId() != null)) {
            section = sectionAccessor.get(recurringEvent.getSection().getId());
        }
        Event eventNew = eventAccessor.create(section, recurringEvent, event);
        if (eventNew == null) {
            return HttpResponse.badRequest();
        }

        return HttpResponse.created(eventNew);
    }


}