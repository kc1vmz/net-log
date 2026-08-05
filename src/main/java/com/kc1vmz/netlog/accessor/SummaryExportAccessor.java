package com.kc1vmz.netlog.accessor;

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

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kc1vmz.netlog.object.Event;
import com.kc1vmz.netlog.object.Participant;
import com.kc1vmz.netlog.object.Section;
import com.kc1vmz.netlog.object.SectionOperator;
import jakarta.inject.Singleton;

@Singleton
public class SummaryExportAccessor {
    private static final Logger logger = LogManager.getLogger(SummaryExportAccessor.class);

    public String getUniqueFileName(String directory, String extension) {
        String fileName = MessageFormat.format("{0}.{1}", UUID.randomUUID(), extension.trim());
        return Paths.get(directory, fileName).toString();
    }

    public String generateReport(Section section, Map<Event,List<Participant>> eventParticipants, List<SectionOperator> members, Map<String, String> locationDistricts, String dateStr, String title) {
        try {
            return createReport(section, eventParticipants, members, locationDistricts, dateStr, title);
        } catch (Exception e) {
            logger.error("Exception caught generating report", e);
        }
        return null;
    }

    public String getTempReportDir() {
        return System.getProperty("java.io.tmpdir");
    }

    public String createReport(Section section, Map<Event,List<Participant>> eventParticipants, List<SectionOperator> members, Map<String, String> locationDistricts, String dateStr, String title) throws FileNotFoundException {
        String filename = getUniqueFileName(getTempReportDir(), "csv");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            addEventRows(writer, eventParticipants, locationDistricts);
            return filename.substring(getTempReportDir().length());
        } catch (Exception e) {
            logger.error("Exception caught generating report", e);
        }

        return null;
    }

    /**
     * add event data rows
     *
     */
    private void addEventRows(BufferedWriter writer, Map<Event, List<Participant>> eventParticipants, Map<String, String> locationDistricts) {

        String header = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", 
                                            "EventId", "EventType", "EventName", "EventDescription", "EventNCS", "EventState", "EventStartTime", "EventEndTime", "EventParticipantsTotal",
                                                    "OperatorId", "OperatorCallsign", "OperatorName", "OperatorCountry", "OperatorState", "OperatorCounty", "OperatorMunicipality", "OperatorDistrict", 
                                                    "OperatorIsNTS", "OperatorIsRACES", "OperatorIsSkywarn", "OperatorMembershipType", "OperatorCheckInTime", "OperatorCheckOutTime",
                                                    "OperatorPrimaryPower", "OperatorBackupPower", "OperatorTransmit Power");
        try {
            writer.write(header);
            writer.newLine();
        } catch (Exception e) {
            logger.error("Exception caught writing export header", e);
        }

        if ((eventParticipants == null) || (eventParticipants.isEmpty()) ) {
            return;
        }

        for (Map.Entry<Event,List<Participant>> mapEntry : eventParticipants.entrySet()) {
            Event entry = mapEntry.getKey();
            List<Participant> participants = mapEntry.getValue();
            if ((participants == null) || participants.isEmpty()) {
                String line = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", 
                                                entry.getId(), entry.getType().name(), entry.getName(), entry.getDescription(), entry.getNetControlCallsign(), entry.getState().name(), 
                                                entry.getPrettyStartTime(), entry.getPrettyEndTime(), 0, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
                try {
                    writer.write(line);
                    writer.newLine();
                } catch (Exception e) {
                    logger.error("Exception caught writing export entry", e);
                }
            } else {

                for (Participant participant : participants) {
                    String district = "";
                    try {
                        if (participant.getOperator().getLocation() != null) {
                            district = locationDistricts.get(participant.getOperator().getLocation().getId());
                            if (district == null) {
                                district = "";
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Exception caught looking up district", e);
                    }
                    String line = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%d", 
                                                    entry.getId(), entry.getType().name(), entry.getName(), entry.getDescription(), entry.getNetControlCallsign(), entry.getState().name(), 
                                                    entry.getPrettyStartTime(), entry.getPrettyEndTime(), participants.size(), participant.getOperator().getId(), participant.getOperator().getCallsign(), 
                                                    (participant.getOperator().getName() == null) ? "" : participant.getOperator().getName(),
                                                    ((participant.getOperator().getLocation() != null) && (participant.getOperator().getLocation().getCountry() != null)) ? participant.getOperator().getLocation().getCountry().getName() : "",
                                                    ((participant.getOperator().getLocation() != null) && (participant.getOperator().getLocation().getState() != null)) ? participant.getOperator().getLocation().getState().getName() : "",
                                                    ((participant.getOperator().getLocation() != null) && (participant.getOperator().getLocation().getCounty() != null)) ? participant.getOperator().getLocation().getCounty().getName() : "",
                                                    ((participant.getOperator().getLocation() != null) && (participant.getOperator().getLocation().getName() != null)) ? participant.getOperator().getLocation().getName() : "",
                                                    district,
                                                    participant.getOperator().isNTS() ? "Yes" : "No",
                                                    participant.getOperator().isRACES() ? "Yes" : "No",
                                                    participant.getOperator().isSkywarn() ? "Yes" : "No",
                                                    (participant.getMembershipType() != null) ?  participant.getMembershipType().name() : "UNKNOWN",
                                                    participant.getPrettyStartTime(), participant.getPrettyEndTime(),
                                                    participant.getPrimaryPower(), participant.getBackupPower(), participant.getTransmitPower()
                                                );
                    try {
                        writer.write(line);
                        writer.newLine();
                    } catch (Exception e) {
                        logger.error("Exception caught writing export entry", e);
                    }
                }
            }
        }
    }
}
