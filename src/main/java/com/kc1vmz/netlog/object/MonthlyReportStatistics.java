package com.kc1vmz.netlog.object;

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

import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.serde.annotation.Serdeable.Deserializable;

@Serdeable
@Deserializable
public class MonthlyReportStatistics {
    private int sectionMembers;
    private int leadershipMembers;
    private int leadershipMembersTotal;
    private int sessionsHeld;
    private int sessionsHeldWithNTS;
    private int participations;
    private int uniqueParticipants;
    List<String> leadershipCallsigns;
    List<MonthlyReportEventStatistics> eventStatistics;
    int totalEvents;
    double totalEventsHours;
    int totalTrainingEvents;
    double totalTrainingEventsHours;
    int totalPublicServiceEvents;
    double totalPublicServiceEventsHours;
    int totalCommunityServiceEvents;
    double totalCommunityServiceEventsHours;
    int totalEmergencyEvents;
    double totalEmergencyEventsHours;
    int totalExerciseEvents;
    double totalExerciseEventsHours;
    int totalNetEvents;
    double totalNetEventsHours;
    int totalSkywarnEvents;
    double totalSkywarnEventsHours;
    int totalMeetingEvents;
    double totalMeetingEventsHours;
    int totalUnclassifiedEvents;
    double totalUnclassifiedEventsHours;

    public int getSectionMembers() {
        return sectionMembers;
    }
    public void setSectionMembers(int sectionMembers) {
        this.sectionMembers = sectionMembers;
    }
    public int getLeadershipMembers() {
        return leadershipMembers;
    }
    public void setLeadershipMembers(int leadershipMembers) {
        this.leadershipMembers = leadershipMembers;
    }
    public int getLeadershipMembersTotal() {
        return leadershipMembersTotal;
    }
    public void setLeadershipMembersTotal(int leadershipMembersTotal) {
        this.leadershipMembersTotal = leadershipMembersTotal;
    }
    public int getSessionsHeld() {
        return sessionsHeld;
    }
    public void setSessionsHeld(int sessionsHeld) {
        this.sessionsHeld = sessionsHeld;
    }
    public int getParticipations() {
        return participations;
    }
    public void setParticipations(int participations) {
        this.participations = participations;
    }
    public int getUniqueParticipants() {
        return uniqueParticipants;
    }
    public void setUniqueParticipants(int uniqueParticipants) {
        this.uniqueParticipants = uniqueParticipants;
    }
    public List<String> getLeadershipCallsigns() {
        return leadershipCallsigns;
    }
    public void setLeadershipCallsigns(List<String> leadershipCallsigns) {
        this.leadershipCallsigns = leadershipCallsigns;
    }
    public List<MonthlyReportEventStatistics> getEventStatistics() {
        return eventStatistics;
    }
    public void setEventStatistics(List<MonthlyReportEventStatistics> eventStatistics) {
        this.eventStatistics = eventStatistics;
    }
    public int getSessionsHeldWithNTS() {
        return sessionsHeldWithNTS;
    }
    public void setSessionsHeldWithNTS(int sessionsHeldWithNTS) {
        this.sessionsHeldWithNTS = sessionsHeldWithNTS;
    }
    public int getTotalEvents() {
        return totalEvents;
    }
    public void setTotalEvents(int totalEvents) {
        this.totalEvents = totalEvents;
    }
    public double getTotalEventsHours() {
        return totalEventsHours;
    }
    public void setTotalEventsHours(double totalEventsHours) {
        this.totalEventsHours = totalEventsHours;
    }
    public int getTotalTrainingEvents() {
        return totalTrainingEvents;
    }
    public void setTotalTrainingEvents(int totalTrainingEvents) {
        this.totalTrainingEvents = totalTrainingEvents;
    }
    public double getTotalTrainingEventsHours() {
        return totalTrainingEventsHours;
    }
    public void setTotalTrainingEventsHours(double totalTrainingEventsHours) {
        this.totalTrainingEventsHours = totalTrainingEventsHours;
    }
    public int getTotalPublicServiceEvents() {
        return totalPublicServiceEvents;
    }
    public void setTotalPublicServiceEvents(int totalPublicServiceEvents) {
        this.totalPublicServiceEvents = totalPublicServiceEvents;
    }
    public double getTotalPublicServiceEventsHours() {
        return totalPublicServiceEventsHours;
    }
    public void setTotalPublicServiceEventsHours(double totalPublicServiceEventsHours) {
        this.totalPublicServiceEventsHours = totalPublicServiceEventsHours;
    }
    public int getTotalCommunityServiceEvents() {
        return totalCommunityServiceEvents;
    }
    public void setTotalCommunityServiceEvents(int totalCommunityServiceEvents) {
        this.totalCommunityServiceEvents = totalCommunityServiceEvents;
    }
    public double getTotalCommunityServiceEventsHours() {
        return totalCommunityServiceEventsHours;
    }
    public void setTotalCommunityServiceEventsHours(double totalCommunityServiceEventsHours) {
        this.totalCommunityServiceEventsHours = totalCommunityServiceEventsHours;
    }
    public int getTotalEmergencyEvents() {
        return totalEmergencyEvents;
    }
    public void setTotalEmergencyEvents(int totalEmergencyEvents) {
        this.totalEmergencyEvents = totalEmergencyEvents;
    }
    public double getTotalEmergencyEventsHours() {
        return totalEmergencyEventsHours;
    }
    public void setTotalEmergencyEventsHours(double totalEmergencyEventsHours) {
        this.totalEmergencyEventsHours = totalEmergencyEventsHours;
    }
    public int getTotalSkywarnEvents() {
        return totalSkywarnEvents;
    }
    public void setTotalSkywarnEvents(int totalSkywarnEvents) {
        this.totalSkywarnEvents = totalSkywarnEvents;
    }
    public double getTotalSkywarnEventsHours() {
        return totalSkywarnEventsHours;
    }
    public void setTotalSkywarnEventsHours(double totalSkywarnEventsHours) {
        this.totalSkywarnEventsHours = totalSkywarnEventsHours;
    }
    public int getTotalMeetingEvents() {
        return totalMeetingEvents;
    }
    public void setTotalMeetingEvents(int totalMeetingEvents) {
        this.totalMeetingEvents = totalMeetingEvents;
    }
    public double getTotalMeetingEventsHours() {
        return totalMeetingEventsHours;
    }
    public void setTotalMeetingEventsHours(double totalMeetingEventsHours) {
        this.totalMeetingEventsHours = totalMeetingEventsHours;
    }
    public int getTotalUnclassifiedEvents() {
        return totalUnclassifiedEvents;
    }
    public void setTotalUnclassifiedEvents(int totalUnclassifiedEvents) {
        this.totalUnclassifiedEvents = totalUnclassifiedEvents;
    }
    public double getTotalUnclassifiedEventsHours() {
        return totalUnclassifiedEventsHours;
    }
    public void setTotalUnclassifiedEventsHours(double totalUnclassifiedEventsHours) {
        this.totalUnclassifiedEventsHours = totalUnclassifiedEventsHours;
    }
    public int getTotalExerciseEvents() {
        return totalExerciseEvents;
    }
    public void setTotalExerciseEvents(int totalExerciseEvents) {
        this.totalExerciseEvents = totalExerciseEvents;
    }
    public double getTotalExerciseEventsHours() {
        return totalExerciseEventsHours;
    }
    public void setTotalExerciseEventsHours(double totalExerciseEventsHours) {
        this.totalExerciseEventsHours = totalExerciseEventsHours;
    }
    public int getTotalNetEvents() {
        return totalNetEvents;
    }
    public void setTotalNetEvents(int totalNetEvents) {
        this.totalNetEvents = totalNetEvents;
    }
    public double getTotalNetEventsHours() {
        return totalNetEventsHours;
    }
    public void setTotalNetEventsHours(double totalNetEventsHours) {
        this.totalNetEventsHours = totalNetEventsHours;
    }
}
