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

import java.util.List;

import com.kc1vmz.netlog.accessor.EventAccessor;
import com.kc1vmz.netlog.accessor.OperatorAccessor;
import com.kc1vmz.netlog.accessor.RecurringEventAccessor;
import com.kc1vmz.netlog.accessor.SectionAccessor;
import com.kc1vmz.netlog.enums.MembershipType;
import com.kc1vmz.netlog.object.District;
import com.kc1vmz.netlog.object.Event;
import com.kc1vmz.netlog.object.Operator;
import com.kc1vmz.netlog.object.RecurringEvent;
import com.kc1vmz.netlog.object.Section;
import com.kc1vmz.netlog.object.SectionOperator;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import jakarta.inject.Inject;

@Controller("/api/v1/sections")
public class SectionController {

    @Inject
    private SectionAccessor sectionAccessor;
    @Inject
    private OperatorAccessor operatorAccessor;
    @Inject
    private RecurringEventAccessor recurringEventAccessor;
    @Inject
    private EventAccessor eventAccessor;

    @Get
    public List<Section> list() {
        return sectionAccessor.list(false);
    }

    @Get("/{id}")
    public Section get(@PathVariable String id) {
        return sectionAccessor.get(id);
    }

    @Put("/{id}")
    public Section update(@PathVariable String id, @Body Section obj) {
        return sectionAccessor.update(id, obj);
    }

    @Delete("/{id}")
    public void delete(@PathVariable String id) {
        sectionAccessor.delete(id, false);
    }

    @Post
    public HttpResponse<Section> create(@Body Section section) {
        Section sectionNew = sectionAccessor.create(section);
        if (sectionNew == null) {
            return HttpResponse.badRequest();
        }

        return HttpResponse.created(sectionNew);
    }

    @Get("/{id}/operators")
    public List<SectionOperator> listOperators(@PathVariable String id) {
        Section section = sectionAccessor.get(id);
        return operatorAccessor.listOperators(section);
    }

    @Put("/{id}/operators/{operatorId}")
    public List<SectionOperator> addOperator(@PathVariable String id, @PathVariable String operatorId, @QueryValue MembershipType membershipType) {
        Section section = sectionAccessor.get(id);
        Operator operator = operatorAccessor.get(operatorId);
        return operatorAccessor.addOperator(section, operator, membershipType);
    }

    @Delete("/{id}/operators/{operatorId}")
    public List<SectionOperator> removeOperator(@PathVariable String id, @PathVariable String operatorId) {
        Section section = sectionAccessor.get(id);
        Operator operator = operatorAccessor.get(operatorId);
        SectionOperator sectionOperator = new SectionOperator(operator, MembershipType.UNKNOWN, section);
        return operatorAccessor.removeOperator(section, sectionOperator);
    }

    @Get("/{id}/recurringEvents")
    public List<RecurringEvent> listRecurringEvents(@PathVariable String id) {
        Section section = sectionAccessor.get(id);
        return recurringEventAccessor.listBySection(section, false);
    }

    @Post("/{id}/recurringEvents")
    public HttpResponse<RecurringEvent> createRecurringEvent(@PathVariable String id, @Body RecurringEvent recurringEvent) {
        Section section = sectionAccessor.get(id);
        RecurringEvent ret = recurringEventAccessor.createBySection(section, recurringEvent);
        return HttpResponse.created(ret);
    }

    @Get("/{id}/events")
    public List<Event> listEvents(@PathVariable String id) {
        Section section = sectionAccessor.get(id);
        return eventAccessor.listBySection(section, false, true);
    }

    @Delete("/{id}/districts/{districtId}")
    public void removeDistrict(@PathVariable String id, @PathVariable String districtId) {
        District district = sectionAccessor.getDistrict(districtId);
        sectionAccessor.removeDistrict(district);
    }

    @Post("/{id}/districts")
    public HttpResponse<District> createDistrict(@PathVariable String id, @Body District district) {
        Section section = sectionAccessor.get(id);
        District districtNew = sectionAccessor.addDistrict(section, district);
        if (districtNew == null) {
            return HttpResponse.badRequest();
        }

        return HttpResponse.created(districtNew);
    }

}