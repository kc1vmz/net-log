package com.kc1vmz.netlog.accessor;

import java.io.BufferedWriter;

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
public class NonParticipationExportAccessor {
    private static final Logger logger = LogManager.getLogger(NonParticipationExportAccessor.class);


    public String generateReport(Section section, List<SectionOperator> members, Map<Event,List<Participant>> eventParticipants, Map<String, String> locationDistricts, String dateStr, String title) {
        try {
            return createReport(section, members, eventParticipants, locationDistricts, dateStr, title);
        } catch (Exception e) {
            logger.error("Exception caught generating report", e);
        }
        return null;
    }

    public String getTempReportDir() {
        return System.getProperty("java.io.tmpdir");
    }

    public String createReport(Section section, List<SectionOperator> members, Map<Event,List<Participant>> eventParticipants,  Map<String, String> locationDistricts, String dateStr, String title) throws FileNotFoundException {
        String filename = getUniqueFileName(getTempReportDir(), "csv");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            removeEventParticipants(members, eventParticipants);
            addNonParticipantRows(writer, members, locationDistricts);
            return filename.substring(getTempReportDir().length());
        } catch (Exception e) {
            logger.error("Exception caught generating report", e);
        }

        return null;

    }

    private void removeEventParticipants(List<SectionOperator> members, Map<Event,List<Participant>> eventParticipants) {
        for (Map.Entry<Event,List<Participant>> mapEntry : eventParticipants.entrySet()) {
            List<Participant> participants = mapEntry.getValue();
            for (Participant participant : participants) {
                if (participant.getOperator() == null) {
                    continue;
                }
                for (SectionOperator member : members) {
                    if (participant.getOperator().getCallsign().equals(member.getCallsign())) {
                        members.remove(member);
                        break;
                    }
                }
            }
        }
    }

    public String getUniqueFileName(String directory, String extension) {
        String fileName = MessageFormat.format("{0}.{1}", UUID.randomUUID(), extension.trim());
        return Paths.get(directory, fileName).toString();
    }

    private void addNonParticipantRows(BufferedWriter writer, List<SectionOperator> members, Map<String, String> locationDistricts) {

        String header = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", 
                                                    "OperatorId", "OperatorCallsign", "OperatorName", "OperatorCountry", "OperatorState", "OperatorCounty", "OperatorMunicipality", "OperatorDistrict",
                                                    "OperatorIsNTS", "OperatorIsRACES", "OperatorIsSkywarn");
        try {
            writer.write(header);
            writer.newLine();
        } catch (Exception e) {
            logger.error("Exception caught writing export header", e);
        }

        if ((members == null) || (members.isEmpty()) ) {
            return;
        }

        for (SectionOperator member : members) {
            String district = "";
            try {
                if (member.getLocation() != null) {
                    district = locationDistricts.get(member.getLocation().getId());
                    if (district == null) {
                        district = "";
                    }
                }
            } catch (Exception e) {
                logger.error("Exception caught looking up district", e);
            }
            String line = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", 
                                            member.getId(), member.getCallsign(), 
                                            (member.getName() == null) ? "" : member.getName(),
                                            ((member.getLocation() != null) && (member.getLocation().getCountry() != null)) ? member.getLocation().getCountry().getName() : "",
                                            ((member.getLocation() != null) && (member.getLocation().getState() != null)) ? member.getLocation().getState().getName() : "",
                                            ((member.getLocation() != null) && (member.getLocation().getCounty() != null)) ? member.getLocation().getCounty().getName() : "",
                                            ((member.getLocation() != null) && (member.getLocation().getName() != null)) ? member.getLocation().getName() : "",
                                            district,
                                            member.isNTS() ? "Yes" : "No",
                                            member.isRACES() ? "Yes" : "No",
                                            member.isSkywarn() ? "Yes" : "No"
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
