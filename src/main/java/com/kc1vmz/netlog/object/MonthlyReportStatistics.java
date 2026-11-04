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

import java.util.List;

public class MonthlyReportStatistics {
    private int sectionMembers;
    private int leadershipMembers;
    private int leadershipMembersTotal;
    private int sessionsHeld;
    private int sessionsHeldWithNTS;
    private int participations;
    private int uniqueParticipants;
    private int nonParticipants;
    private List<String> leadershipCallsigns;
    private List<String> racesCallsigns;
    private List<MonthlyReportEventStatistics> eventStatistics;
    private int totalEvents;
    private double totalEventsHours;
    private int totalTrainingEvents;
    private double totalTrainingEventsHours;
    private int totalPublicServiceEvents;
    private double totalPublicServiceEventsHours;
    private int totalCommunityServiceEvents;
    private double totalCommunityServiceEventsHours;
    private int totalEmergencyEvents;
    private double totalEmergencyEventsHours;
    private int totalExerciseEvents;
    private double totalExerciseEventsHours;
    private int totalNetEvents;
    private double totalNetEventsHours;
    private int totalSkywarnEvents;
    private double totalSkywarnEventsHours;
    private int totalMeetingEvents;
    private double totalMeetingEventsHours;
    private int totalUnclassifiedEvents;
    private double totalUnclassifiedEventsHours;

    private int communicationPlatformUnknownCount;
    private int communicationPlatformRadioPhoneCount;
    private int communicationPlatformEcholinkCount;
    private int communicationPlatformRadioPacketCount;
    private int communicationPlatformInternetPacketCount;
    private int communicationBandUnknownCount;
    private int communicationBandInternetCount;
    private int communicationBand70cmCount;
    private int communicationBand2mCount;
    private int communicationBand1_25mCount;
    private int communicationBand6mCount;
    private int communicationBand10mCount;
    private int communicationBand20mCount;
    private int communicationBand40mCount;
    private int communicationBand80mCount;

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
    public List<String> getRacesCallsigns() {
        return racesCallsigns;
    }
    public void setRacesCallsigns(List<String> racesCallsigns) {
        this.racesCallsigns = racesCallsigns;
    }
    public int getCommunicationPlatformUnknownCount() {
        return communicationPlatformUnknownCount;
    }
    public void setCommunicationPlatformUnknownCount(int communicationPlatformUnknownCount) {
        this.communicationPlatformUnknownCount = communicationPlatformUnknownCount;
    }
    public int getCommunicationPlatformRadioPhoneCount() {
        return communicationPlatformRadioPhoneCount;
    }
    public void setCommunicationPlatformRadioPhoneCount(int communicationPlatformRadioPhoneCount) {
        this.communicationPlatformRadioPhoneCount = communicationPlatformRadioPhoneCount;
    }
    public int getCommunicationPlatformEcholinkCount() {
        return communicationPlatformEcholinkCount;
    }
    public void setCommunicationPlatformEcholinkCount(int communicationPlatformEcholinkCount) {
        this.communicationPlatformEcholinkCount = communicationPlatformEcholinkCount;
    }
    public int getCommunicationPlatformRadioPacketCount() {
        return communicationPlatformRadioPacketCount;
    }
    public void setCommunicationPlatformRadioPacketCount(int communicationPlatformRadioPacketCount) {
        this.communicationPlatformRadioPacketCount = communicationPlatformRadioPacketCount;
    }
    public int getCommunicationPlatformInternetPacketCount() {
        return communicationPlatformInternetPacketCount;
    }
    public void setCommunicationPlatformInternetPacketCount(int communicationPlatformInternetPacketCount) {
        this.communicationPlatformInternetPacketCount = communicationPlatformInternetPacketCount;
    }
    public int getCommunicationBandUnknownCount() {
        return communicationBandUnknownCount;
    }
    public void setCommunicationBandUnknownCount(int communicationBandUnknownCount) {
        this.communicationBandUnknownCount = communicationBandUnknownCount;
    }
    public int getCommunicationBandInternetCount() {
        return communicationBandInternetCount;
    }
    public void setCommunicationBandInternetCount(int communicationBandInternetCount) {
        this.communicationBandInternetCount = communicationBandInternetCount;
    }
    public int getCommunicationBand70cmCount() {
        return communicationBand70cmCount;
    }
    public void setCommunicationBand70cmCount(int communicationBand70cmCount) {
        this.communicationBand70cmCount = communicationBand70cmCount;
    }
    public int getCommunicationBand2mCount() {
        return communicationBand2mCount;
    }
    public void setCommunicationBand2mCount(int communicationBand2mCount) {
        this.communicationBand2mCount = communicationBand2mCount;
    }
    public int getCommunicationBand6mCount() {
        return communicationBand6mCount;
    }
    public void setCommunicationBand6mCount(int communicationBand6mCount) {
        this.communicationBand6mCount = communicationBand6mCount;
    }
    public int getCommunicationBand10mCount() {
        return communicationBand10mCount;
    }
    public void setCommunicationBand10mCount(int communicationBand10mCount) {
        this.communicationBand10mCount = communicationBand10mCount;
    }
    public int getCommunicationBand20mCount() {
        return communicationBand20mCount;
    }
    public void setCommunicationBand20mCount(int communicationBand20mCount) {
        this.communicationBand20mCount = communicationBand20mCount;
    }
    public int getCommunicationBand40mCount() {
        return communicationBand40mCount;
    }
    public void setCommunicationBand40mCount(int communicationBand40mCount) {
        this.communicationBand40mCount = communicationBand40mCount;
    }
    public int getCommunicationBand80mCount() {
        return communicationBand80mCount;
    }
    public void setCommunicationBand80mCount(int communicationBand80mCount) {
        this.communicationBand80mCount = communicationBand80mCount;
    }
    public int getNonParticipants() {
        return nonParticipants;
    }
    public void setNonParticipants(int nonParticipants) {
        this.nonParticipants = nonParticipants;
    }
    public int getCommunicationBand1_25mCount() {
        return communicationBand1_25mCount;
    }
    public void setCommunicationBand1_25mCount(int communicationBand1_25mCount) {
        this.communicationBand1_25mCount = communicationBand1_25mCount;
    }
}
