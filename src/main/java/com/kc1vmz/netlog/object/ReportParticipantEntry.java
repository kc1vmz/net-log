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

public class ReportParticipantEntry {

    private String callsign;
    private int eventCount; 
    private boolean [] eventMask;
    private int eventParticipationCount;
    private boolean isSectionMember;
    
    public ReportParticipantEntry(String callsign, int eventCount, boolean isSectionMember) {
        this.callsign = callsign;
        this.eventCount = eventCount;
        this.eventMask = new boolean[eventCount];
        this.eventParticipationCount = 0;
        this.isSectionMember = isSectionMember;
    }

    public String getCallsign() {
        return callsign;
    }
    public void setCallsign(String callsign) {
        this.callsign = callsign;
    }
    public int getEventCount() {
        return eventCount;
    }
    public void setEventCount(int eventCount) {
        this.eventCount = eventCount;
    }
    public boolean[] getEventMask() {
        return eventMask;
    }
    public void setEventMask(boolean[] eventMask) {
        this.eventMask = eventMask;
    }
    public int getEventParticipationCount() {
        return eventParticipationCount;
    }
    public void setEventParticipationCount(int eventParticipationCount) {
        this.eventParticipationCount = eventParticipationCount;
    }
    public boolean isSectionMember() {
        return isSectionMember;
    }
    public void setSectionMember(boolean isSectionMember) {
        this.isSectionMember = isSectionMember;
    }
}