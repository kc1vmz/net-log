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

import com.kc1vmz.netlog.accessor.EventAccessor;
import com.kc1vmz.netlog.accessor.OperatorAccessor;
import com.kc1vmz.netlog.accessor.ParticipantAccessor;
import com.kc1vmz.netlog.accessor.RecurringEventAccessor;
import com.kc1vmz.netlog.accessor.SectionAccessor;
import com.kc1vmz.netlog.object.Participant;

import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/v1/participants")
public class ParticipantController {

    @Autowired
    private EventAccessor eventAccessor;
    @Autowired
    private OperatorAccessor operatorAccessor;
    @Autowired
    private SectionAccessor sectionAccessor;    
    @Autowired
    private RecurringEventAccessor recurringEventAccessor;    
    @Autowired
    private ParticipantAccessor participantAccessor;

    @GetMapping("/{id}")
    public Participant get(@PathVariable String id) {
        return get(id, true);
    }

    private Participant get(@PathVariable String id, boolean hydrate) {
        Participant participant = participantAccessor.get(id);
        if (hydrate) {
            if ((participant != null) && (participant.getEvent() != null) && (participant.getEvent().getId() != null) ) {
                participant.setEvent(eventAccessor.get(participant.getEvent().getId()));
            }
            if ((participant != null) && (participant.getOperator() != null) && (participant.getOperator().getId() != null) ) {
                participant.setOperator(operatorAccessor.get(participant.getOperator().getId()));
            }
            if ((participant != null) && (participant.getEvent() != null) && (participant.getEvent().getRecurringEvent() != null) && 
                            (participant.getEvent().getRecurringEvent().getId() != null)) {
                participant.getEvent().setRecurringEvent(recurringEventAccessor.get(participant.getEvent().getRecurringEvent().getId()));
            }
            if ((participant != null) && (participant.getEvent() != null) && (participant.getEvent().getRecurringEvent() != null) && 
                            (participant.getEvent().getRecurringEvent().getSection() != null) &&
                            (participant.getEvent().getRecurringEvent().getSection().getId() != null)) {
                participant.getEvent().getRecurringEvent().setSection(sectionAccessor.get(participant.getEvent().getRecurringEvent().getSection().getId()));
            }
        }
        return participant;
    }

    @PutMapping("/{id}")
    public Participant update(@PathVariable String id, @RequestBody Participant obj) {
        return participantAccessor.update(id, obj);
    }
}