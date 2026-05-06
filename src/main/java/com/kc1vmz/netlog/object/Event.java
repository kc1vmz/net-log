    package com.kc1vmz.netlog.object;

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

import java.time.LocalDateTime;

import com.kc1vmz.netlog.enums.EventState;
import com.kc1vmz.netlog.enums.EventType;
import com.kc1vmz.netlog.utils.PrettyZonedDateTimeFormatter;

import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.serde.annotation.Serdeable.Deserializable;

@Serdeable
@Deserializable
public class Event implements Comparable<Event> {
    private String id;
    private String name;
    private String description;
    private EventType type;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private RecurringEvent recurringEvent;
    private Section section;
    private String location;
    private String netControlCallsign;
    private String prettyStartTime;
    private String prettyEndTime;
    private boolean sectionActive;
    private EventState state;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public EventType getType() {
        return type;
    }
    public void setType(EventType type) {
        this.type = type;
    }
    public LocalDateTime getStartTime() {
        return startTime;
    }
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
        setPrettyStartTime(PrettyZonedDateTimeFormatter.format(startTime));
    }
    public LocalDateTime getEndTime() {
        return endTime;
    }
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
        setPrettyEndTime(PrettyZonedDateTimeFormatter.format(endTime));
    }
    public RecurringEvent getRecurringEvent() {
        return recurringEvent;
    }
    public void setRecurringEvent(RecurringEvent recurringEvent) {
        this.recurringEvent = recurringEvent;
    }
    public Section getSection() {
        return section;
    }
    public void setSection(Section section) {
        this.section = section;
    }
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public String getNetControlCallsign() {
        return netControlCallsign;
    }
    public void setNetControlCallsign(String netControlCallsign) {
        this.netControlCallsign = netControlCallsign;
    }
    public String getPrettyStartTime() {
        return prettyStartTime;
    }
    public void setPrettyStartTime(String prettyStartTime) {
        this.prettyStartTime = prettyStartTime;
    }
    public String getPrettyEndTime() {
        return prettyEndTime;
    }
    public void setPrettyEndTime(String prettyEndTime) {
        this.prettyEndTime = prettyEndTime;
    }
    @Override
    public int compareTo(Event other) {
        String thisValue = this.prettyStartTime+this.name+this.id;
        String othervalue = other.prettyStartTime+other.name+other.id;
        return thisValue.compareTo(othervalue);
    }
    public boolean isSectionActive() {
        return sectionActive;
    }
    public void setSectionActive(boolean sectionActive) {
        this.sectionActive = sectionActive;
    }
    public EventState getState() {
        return state;
    }
    public void setState(EventState state) {
        this.state = state;
    }
}
