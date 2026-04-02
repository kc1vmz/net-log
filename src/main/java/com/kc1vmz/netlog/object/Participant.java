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

import com.kc1vmz.netlog.enums.ElectricalPowerType;
import com.kc1vmz.netlog.enums.MembershipType;
import com.kc1vmz.netlog.utils.PrettyZonedDateTimeFormatter;

import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.serde.annotation.Serdeable.Deserializable;

@Serdeable
@Deserializable
public class Participant {
    private String id;
    private Operator operator;
    private Event event;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private MembershipType membershipType;
    private ElectricalPowerType primaryPower;
    private ElectricalPowerType backupPower;
    private int transmitPower;
    private String prettyStartTime;
    private String prettyEndTime;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
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
    public MembershipType getMembershipType() {
        return membershipType;
    }
    public void setMembershipType(MembershipType membershipType) {
        this.membershipType = membershipType;
    }
    public ElectricalPowerType getPrimaryPower() {
        return primaryPower;
    }
    public void setPrimaryPower(ElectricalPowerType primaryPower) {
        this.primaryPower = primaryPower;
    }
    public ElectricalPowerType getBackupPower() {
        return backupPower;
    }
    public void setBackupPower(ElectricalPowerType backupPower) {
        this.backupPower = backupPower;
    }
    public int getTransmitPower() {
        return transmitPower;
    }
    public void setTransmitPower(int transmitPower) {
        this.transmitPower = transmitPower;
    }
    public Operator getOperator() {
        return operator;
    }
    public void setOperator(Operator operator) {
        this.operator = operator;
    }
    public Event getEvent() {
        return event;
    }
    public void setEvent(Event event) {
        this.event = event;
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
}
