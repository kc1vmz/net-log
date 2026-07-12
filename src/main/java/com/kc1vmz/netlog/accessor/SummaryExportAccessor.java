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

    public String generateReport(Section section, Map<Event,List<Participant>> eventParticipants, List<SectionOperator> members, String dateStr, String title) {
        try {
            return createReport(section, eventParticipants, members, dateStr, title);
        } catch (Exception e) {
            logger.error("Exception caught generating report", e);
        }
        return null;
    }

    public String getTempReportDir() {
        return System.getProperty("java.io.tmpdir");
    }

    public String createReport(Section section, Map<Event,List<Participant>> eventParticipants, List<SectionOperator> members, String dateStr, String title) throws FileNotFoundException {
        String filename = getUniqueFileName(getTempReportDir(), "csv");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            addEventRows(writer, eventParticipants);
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
    private void addEventRows(BufferedWriter writer, Map<Event,List<Participant>> eventParticipants) {

        String header = String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"", 
                                            "EventId", "EventType", "EventName", "EventDescription", "EventNCS", "EventState", "EventStartTime", "EventEndTime", "EventParticipantsTotal",
                                                    "OperatorId", "OperatorCallsign", "OperatorName");
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
                String line = String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%d\", \"%s\", \"%s\", \"%s\"", 
                                                entry.getId(), entry.getType().name(), entry.getName(), entry.getDescription(), entry.getNetControlCallsign(), entry.getState().name(), 
                                                entry.getPrettyStartTime(), entry.getPrettyEndTime(), 0, "", "", "");
                try {
                    writer.write(line);
                    writer.newLine();
                } catch (Exception e) {
                    logger.error("Exception caught writing export entry", e);
                }
            } else {

                for (Participant participant : participants) {
                    String line = String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%d\", \"%s\", \"%s\", \"%s\"", 
                                                    entry.getId(), entry.getType().name(), entry.getName(), entry.getDescription(), entry.getNetControlCallsign(), entry.getState().name(), 
                                                    entry.getPrettyStartTime(), entry.getPrettyEndTime(), participants.size(), participant.getOperator().getId(), participant.getOperator().getCallsign(), 
                                                    (participant.getOperator().getName() == null) ? "" : participant.getOperator().getName());
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
