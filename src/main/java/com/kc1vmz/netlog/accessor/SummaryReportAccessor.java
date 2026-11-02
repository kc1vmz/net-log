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

import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kc1vmz.netlog.enums.EventType;
import com.kc1vmz.netlog.enums.MembershipType;
import com.kc1vmz.netlog.object.Event;
import com.kc1vmz.netlog.object.MonthlyReportEventStatistics;
import com.kc1vmz.netlog.object.MonthlyReportStatistics;
import com.kc1vmz.netlog.object.Participant;
import com.kc1vmz.netlog.object.Section;
import com.kc1vmz.netlog.object.SectionOperator;
import com.kc1vmz.netlog.utils.DateStrUtils;
import com.kc1vmz.netlog.utils.PdfUtils;
import com.kc1vmz.netlog.utils.SoftwareIdentity;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;

@Singleton
public class SummaryReportAccessor {
    private static final Logger logger = LogManager.getLogger(SummaryReportAccessor.class);
    private
    @Inject OperatorAccessor operatorAccessor;

    public String getUniqueFileName(String directory, String extension) {
        String fileName = MessageFormat.format("{0}.{1}", UUID.randomUUID(), extension.trim());
        return Paths.get(directory, fileName).toString();
    }

    public String generateReport(Section section, Map<Event,List<Participant>> eventParticipants, String dateStr, String title) {
        try {
            List<MonthlyReportEventStatistics> eventStatistics = determineEventStatistics(eventParticipants);
            MonthlyReportStatistics reportStatistics = generateMonthlyStatistics(section, eventParticipants, eventStatistics);

            return createMonthlyReport(section, reportStatistics, dateStr, eventStatistics, title);
        } catch (Exception e) {
            logger.error("Exception caught generating report", e);
        }
        return null;
    }

    public String getTempReportDir() {
        return System.getProperty("java.io.tmpdir");
    }

    private MonthlyReportStatistics generateMonthlyStatistics(Section section, Map<Event,List<Participant>> eventParticipants, List<MonthlyReportEventStatistics> eventStatistics) {
        MonthlyReportStatistics ret = new MonthlyReportStatistics();

        List<SectionOperator> operators = operatorAccessor.listOperators(section);
        int leadership = countLeadership(operators);
        ret.setSectionMembers(operators.size());
        ret.setLeadershipMembersTotal(leadership);
        ret.setParticipations(countAllParticipations(eventParticipants));
        ret.setSessionsHeld(eventParticipants.size());
        ret.setSessionsHeldWithNTS(countNTSParticipations(eventParticipants));
        ret.setUniqueParticipants(countUniqueParticipations(eventParticipants));
        ret.setLeadershipCallsigns(determineLeadershipCallsigns(eventParticipants, operators));
        ret.setLeadershipMembers(ret.getLeadershipCallsigns().size());
        ret.setRacesCallsigns(determineRacesCallsigns(eventParticipants));
        ret.setEventStatistics(determineEventStatistics(eventParticipants));
        ret.setNonParticipants(determineNonParticipantCount(eventParticipants, operators));
        determineCommunicationPlatformStatistics(eventParticipants, ret);
        determineCommunicationBandStatistics(eventParticipants, ret);
        rollupEventStatistics(ret.getEventStatistics(), ret);
        return ret;
    }

    private int determineNonParticipantCount(Map<Event,List<Participant>> eventParticipants, List<SectionOperator> operators) {
        if ((operators == null) || (operators.size() == 0)) {
            return 0;
        }
        List<SectionOperator> operatorCopy = new ArrayList<>();
        operatorCopy.addAll(operators);

        for (Map.Entry<Event,List<Participant>> entry : eventParticipants.entrySet()) {
            List<Participant> participants = entry.getValue();
            for (Participant participant : participants) {
                if (participant.getOperator() != null) {
                    SectionOperator sectionOperator = new SectionOperator(participant.getOperator(), participant.getMembershipType(), null);
                    if (operatorCopy.contains(sectionOperator)) {
                        operatorCopy.remove(sectionOperator);
                    }
                } else {
                    continue;
                }
            }
        }
        return operatorCopy.size();
    }

    private void determineCommunicationPlatformStatistics(Map<Event,List<Participant>> eventParticipants, MonthlyReportStatistics ret) {
        for (Map.Entry<Event,List<Participant>> entry : eventParticipants.entrySet()) {
            List<Participant> participants = entry.getValue();
            for (Participant participant : participants) {
                if (participant.getOperator() != null) {
                    switch (participant.getCommunicationPlatform()) {
                        case ECHOLINK:
                            ret.setCommunicationPlatformEcholinkCount(ret.getCommunicationPlatformEcholinkCount()+1);
                            break;
                        case INTERNET_PACKET:
                            ret.setCommunicationPlatformInternetPacketCount(ret.getCommunicationPlatformInternetPacketCount()+1);
                            break;
                        case RADIO_PACKET:
                            ret.setCommunicationPlatformRadioPacketCount(ret.getCommunicationPlatformRadioPacketCount()+1);
                            break;
                        case RADIO_VOICE:
                            ret.setCommunicationPlatformRadioPhoneCount(ret.getCommunicationPlatformRadioPhoneCount()+1);
                            break;
                        case UNKNOWN:
                        default:
                            ret.setCommunicationPlatformUnknownCount(ret.getCommunicationPlatformUnknownCount()+1);
                            break;
                    }
                }
            }
        }
    }

    private void determineCommunicationBandStatistics(Map<Event,List<Participant>> eventParticipants, MonthlyReportStatistics ret) {
        for (Map.Entry<Event,List<Participant>> entry : eventParticipants.entrySet()) {
            List<Participant> participants = entry.getValue();
            for (Participant participant : participants) {
                if (participant.getOperator() != null) {
                    switch (participant.getCommunicationBand()) {
                        case INTERNET:
                            ret.setCommunicationBandInternetCount(ret.getCommunicationBandInternetCount()+1);
                            break;
                        case CM_70:
                            ret.setCommunicationBand70cmCount(ret.getCommunicationBand70cmCount()+1);
                            break;
                        case M_1_25:
                            ret.setCommunicationBand1_25mCount(ret.getCommunicationBand1_25mCount()+1);
                            break;
                        case M_2:
                            ret.setCommunicationBand2mCount(ret.getCommunicationBand2mCount()+1);
                            break;
                        case M_6:
                            ret.setCommunicationBand6mCount(ret.getCommunicationBand6mCount()+1);
                            break;
                        case M_10:
                            ret.setCommunicationBand10mCount(ret.getCommunicationBand10mCount()+1);
                            break;
                        case M_20:
                            ret.setCommunicationBand20mCount(ret.getCommunicationBand20mCount()+1);
                            break;
                        case M_40:
                            ret.setCommunicationBand40mCount(ret.getCommunicationBand40mCount()+1);
                            break;
                        case M_80:
                            ret.setCommunicationBand80mCount(ret.getCommunicationBand80mCount()+1);
                            break;
                        case UNKNOWN:
                        default:
                            ret.setCommunicationBandUnknownCount(ret.getCommunicationBandUnknownCount()+1);
                            break;
                    }
                }
            }
        }
    }

    private void rollupEventStatistics(List<MonthlyReportEventStatistics>  eventStatistics, MonthlyReportStatistics ret) {
        ret.setTotalEvents(eventStatistics.size());

        for (MonthlyReportEventStatistics eventStatistic : eventStatistics) {
            switch (eventStatistic.getEvent().getType()) {
                case EventType.COMMUNITY:
                    ret.setTotalCommunityServiceEvents(ret.getTotalCommunityServiceEvents()+1);
                    ret.setTotalCommunityServiceEventsHours(ret.getTotalCommunityServiceEventsHours()+eventStatistic.getHours());
                    break;
                case EventType.EMERGENCY:
                    ret.setTotalEmergencyEvents(ret.getTotalEmergencyEvents()+1);
                    ret.setTotalEmergencyEventsHours(ret.getTotalEmergencyEventsHours()+eventStatistic.getHours());
                    break;
                case EventType.EXERCISE:
                    ret.setTotalExerciseEvents(ret.getTotalExerciseEvents()+1);
                    ret.setTotalExerciseEventsHours(ret.getTotalExerciseEventsHours()+eventStatistic.getHours());
                    break;
                case EventType.MEETING:
                    ret.setTotalMeetingEvents(ret.getTotalMeetingEvents()+1);
                    ret.setTotalMeetingEventsHours(ret.getTotalMeetingEventsHours()+eventStatistic.getHours());
                    break;
                case EventType.NET:
                    ret.setTotalNetEvents(ret.getTotalNetEvents()+1);
                    ret.setTotalNetEventsHours(ret.getTotalNetEventsHours()+eventStatistic.getHours());
                    break;
                case EventType.PUBLIC:
                    ret.setTotalPublicServiceEvents(ret.getTotalPublicServiceEvents()+1);
                    ret.setTotalPublicServiceEventsHours(ret.getTotalPublicServiceEventsHours()+eventStatistic.getHours());
                    break;
                case EventType.SKYWARN:
                    ret.setTotalSkywarnEvents(ret.getTotalSkywarnEvents()+1);
                    ret.setTotalSkywarnEventsHours(ret.getTotalSkywarnEventsHours()+eventStatistic.getHours());
                    break;
                case EventType.TRAINING:
                    ret.setTotalTrainingEvents(ret.getTotalTrainingEvents()+1);
                    ret.setTotalTrainingEventsHours(ret.getTotalTrainingEventsHours()+eventStatistic.getHours());
                    break;
                case EventType.UNCLASSIFIED:
                    ret.setTotalUnclassifiedEvents(ret.getTotalUnclassifiedEvents()+1);
                    ret.setTotalUnclassifiedEventsHours(ret.getTotalUnclassifiedEventsHours()+eventStatistic.getHours());
                    break;
                default:
                    break;
            }
        }
        ret.setTotalEventsHours(ret.getTotalCommunityServiceEventsHours() +
                                ret.getTotalEmergencyEventsHours() +
                                ret.getTotalExerciseEventsHours() +
                                ret.getTotalMeetingEventsHours() +
                                ret.getTotalNetEventsHours() +
                                ret.getTotalPublicServiceEventsHours() +
                                ret.getTotalSkywarnEventsHours() +
                                ret.getTotalTrainingEventsHours() +
                                ret.getTotalUnclassifiedEventsHours());
    }

    private int countNTSParticipations(Map<Event,List<Participant>> eventParticipants) {
        int ret = 0;

        for (Map.Entry<Event,List<Participant>> entry : eventParticipants.entrySet()) {
            List<Participant> participants = entry.getValue();
            for (Participant participant : participants) {
                if (participant.getOperator() == null) {
                    continue;
                }
                if (participant.getOperator().isNTS()) {
                    ret++;
                    break; // one per event
                }
            }
        }
        return ret;
    }

    private List<MonthlyReportEventStatistics> determineEventStatistics(Map<Event,List<Participant>> eventParticipants) {
        List<MonthlyReportEventStatistics> ret = new ArrayList<>();
        for (Map.Entry<Event,List<Participant>> entry : eventParticipants.entrySet()) {
            MonthlyReportEventStatistics monthlyReportEventStatistic = new MonthlyReportEventStatistics();
            monthlyReportEventStatistic.setEvent(entry.getKey());
            monthlyReportEventStatistic.setParticipantCount(entry.getValue().size());
            monthlyReportEventStatistic.setHours(determineHours(entry.getValue()));
            ret.add(monthlyReportEventStatistic);
        }
        return ret;
    }

    private double determineHours(List<Participant> participants) {
        double minutes = 0;

        for (Participant participant : participants) {
            double diff = participant.getEndTime().toEpochSecond(ZoneOffset.ofTotalSeconds(0)) -
                                participant.getStartTime().toEpochSecond(ZoneOffset.ofTotalSeconds(0));
            minutes += diff;
        }

        return (minutes / (60 * 60)); // convert to hours
    }

    private List<String> determineLeadershipCallsigns(Map<Event, List<Participant>> eventParticipants, List<SectionOperator> operators) {
        List<String> ret = new ArrayList<>();
        Set<String> uniqueCallsigns = new HashSet<>();
        for (Map.Entry<Event,List<Participant>> entry : eventParticipants.entrySet()) {
            List<Participant> participants = entry.getValue();
            for (Participant participant : participants) {
                if (participant.getOperator() != null) {
                    if (participant.getMembershipType().equals(MembershipType.LEADERSHIP)) {
                        uniqueCallsigns.add(participant.getOperator().getCallsign());
                    }
                } else {
                    continue;
                }
            }
        }
        for (String uniqueCallsign : uniqueCallsigns) {
            for (SectionOperator operator : operators) {
                if (operator.getCallsign().equals(uniqueCallsign)) {
                    ret.add(uniqueCallsign);
                    break;
                }
            }
        }
        return ret;
    }

    private List<String> determineRacesCallsigns(Map<Event, List<Participant>> eventParticipants) {
        List<String> ret = new ArrayList<>();
        Set<String> uniqueCallsigns = new HashSet<>();
        for (Map.Entry<Event,List<Participant>> entry : eventParticipants.entrySet()) {
            List<Participant> participants = entry.getValue();
            for (Participant participant : participants) {
                if (participant.getOperator() != null) {
                    if (participant.getOperator().isRACES()) {
                        uniqueCallsigns.add(participant.getOperator().getCallsign());
                    }
                } else {
                    continue;
                }
            }
        }
        // do not need to be members
        ret.addAll(uniqueCallsigns);
        return ret;
    }

    private int countUniqueParticipations(Map<Event,List<Participant>> eventParticipants) {
        Set<String> uniqueCallsigns = new HashSet<>();
        for (Map.Entry<Event,List<Participant>> entry : eventParticipants.entrySet()) {
            List<Participant> participants = entry.getValue();
            for (Participant participant : participants) {
                if (participant.getOperator() != null) {
                    uniqueCallsigns.add(participant.getOperator().getCallsign());
                } else {
                    continue;
                }
            }
        }
        return uniqueCallsigns.size();
    }

    private int countAllParticipations(Map<Event,List<Participant>> eventParticipants) {
        int ret = 0;
        for (Map.Entry<Event,List<Participant>> entry : eventParticipants.entrySet()) {
            ret += entry.getValue().size();
        }
        return ret;
    }

    private int countLeadership(List<SectionOperator> operators) {
        int ret = 0;

        for (SectionOperator operator : operators) {
            if (operator.getMembershipType().equals(MembershipType.LEADERSHIP)) {
                ret++;
            }
        }
        return ret;
    }

    private String createMonthlyReport(Section section, MonthlyReportStatistics monthlyStatistics, String dateStr, List<MonthlyReportEventStatistics> eventStatistics, String title) {
        String filename = getUniqueFileName(getTempReportDir(), "pdf");
        try {
            PdfDocument pdf = new PdfDocument(new PdfWriter(new FileOutputStream(filename)));
            Document document = new Document(pdf);
            document.setMargins(30, 32, 30, 32);

            document.add(addMonthlyReportDocumentHeader(title));
            document.add(addMonthlyReportInfoHeader(dateStr, section.getName()));

            document.add(addMonthlyReportBanner("PARTICIPANT STATISTICS"));
            document.add(addMonthlyReportParticipantStatistics(monthlyStatistics));

            document.add(addMonthlyReportBanner("LEADERSHIP PARTICIPATION"));
            document.add(addMonthlyReportLeadershipInformation(monthlyStatistics));

            document.add(addMonthlyReportBanner("RACES PARTICIPATION"));
            document.add(addMonthlyReportRACESInformation(monthlyStatistics));

            document.add(addMonthlyReportBanner("PLATFORM PARTICIPATION"));
            document.add(addMonthlyReportPlatformInformation(monthlyStatistics));

            document.add(addMonthlyReportBanner("BAND PARTICIPATION"));
            document.add(addMonthlyReportBandInformation(monthlyStatistics));

            document.add(addMonthlyReportBanner("EVENT STATISTICS"));
            Table eventStatisticsHeader = addMonthlyReportEventStatisticsHeader();
            document.add(addMonthlyReportEventStatistics(eventStatisticsHeader, monthlyStatistics));

            document.add(addMonthlyReportFooter());
            document.close();
            return filename.substring(getTempReportDir().length());
        } catch (Exception e) {
            logger.error("Exception caught creating Monthly Summary Report", e);
        }
        return null;
    }

    private Table addMonthlyReportBandInformation(MonthlyReportStatistics monthlyStatistics) {
        Table table = new Table(10);
        table.useAllAvailableWidth();

        Cell cellUnknown = new Cell().add(new Paragraph("Unknown:")).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cellUnknown);
        table.addCell(cellUnknown);
        Cell cellUnknownValue = new Cell().add(new Paragraph(""+monthlyStatistics.getCommunicationBandUnknownCount())).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cellUnknownValue);
        table.addCell(cellUnknownValue);

        Cell cellInternet = new Cell().add(new Paragraph("Internet:")).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cellInternet);
        table.addCell(cellInternet);
        Cell cellInternetValue = new Cell().add(new Paragraph(""+monthlyStatistics.getCommunicationBandInternetCount())).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cellInternetValue);
        table.addCell(cellInternetValue);

        Cell cell70cm = new Cell().add(new Paragraph("70cm:")).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cell70cm);
        table.addCell(cell70cm);
        Cell cell70cmValue = new Cell().add(new Paragraph(""+monthlyStatistics.getCommunicationBand70cmCount())).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cell70cmValue);
        table.addCell(cell70cmValue);

        Cell cell1_25m = new Cell().add(new Paragraph("1.25m:")).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cell1_25m);
        table.addCell(cell1_25m);
        Cell cell1_25mValue = new Cell().add(new Paragraph(""+monthlyStatistics.getCommunicationBand1_25mCount())).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cell1_25mValue);
        table.addCell(cell1_25mValue);

        Cell cell2m = new Cell().add(new Paragraph("2m:")).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cell2m);
        table.addCell(cell2m);
        Cell cell2mValue = new Cell().add(new Paragraph(""+monthlyStatistics.getCommunicationBand2mCount())).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cell2mValue);
        table.addCell(cell2mValue);

        Cell cell6m = new Cell().add(new Paragraph("6m:")).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cell6m);
        table.addCell(cell6m);
        Cell cell6mValue = new Cell().add(new Paragraph(""+monthlyStatistics.getCommunicationBand6mCount())).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cell6mValue);
        table.addCell(cell6mValue);

        Cell cell10m = new Cell().add(new Paragraph("10m:")).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cell10m);
        table.addCell(cell10m);
        Cell cell10mValue = new Cell().add(new Paragraph(""+monthlyStatistics.getCommunicationBand10mCount())).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cell10mValue);
        table.addCell(cell10mValue);

        Cell cell20m = new Cell().add(new Paragraph("20m:")).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cell20m);
        table.addCell(cell20m);
        Cell cell20mValue = new Cell().add(new Paragraph(""+monthlyStatistics.getCommunicationBand20mCount())).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cell20mValue);
        table.addCell(cell20mValue);

        Cell cell40m = new Cell().add(new Paragraph("40m:")).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cell40m);
        table.addCell(cell40m);
        Cell cell40mValue = new Cell().add(new Paragraph(""+monthlyStatistics.getCommunicationBand40mCount())).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cell40mValue);
        table.addCell(cell40mValue);

        Cell cell80m = new Cell().add(new Paragraph("80m:")).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cell80m);
        table.addCell(cell80m);
        Cell cell80mValue = new Cell().add(new Paragraph(""+monthlyStatistics.getCommunicationBand80mCount())).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cell80mValue);
        table.addCell(cell80mValue);

        return table;
    }

    private Table addMonthlyReportPlatformInformation(MonthlyReportStatistics monthlyStatistics) {
        Table table = new Table(10);
        table.useAllAvailableWidth();

        Cell cellUnknown = new Cell().add(new Paragraph("Unknown:")).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cellUnknown);
        table.addCell(cellUnknown);
        Cell cellUnknownValue = new Cell().add(new Paragraph(""+monthlyStatistics.getCommunicationPlatformUnknownCount())).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cellUnknownValue);
        table.addCell(cellUnknownValue);

        Cell cellRadioPhone = new Cell().add(new Paragraph("Radio/Phone:")).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cellRadioPhone);
        table.addCell(cellRadioPhone);
        Cell cellRadioPhoneValue = new Cell().add(new Paragraph(""+monthlyStatistics.getCommunicationPlatformRadioPhoneCount())).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cellRadioPhoneValue);
        table.addCell(cellRadioPhoneValue);

        Cell cellRadioPacket = new Cell().add(new Paragraph("Radio/Packet:")).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cellRadioPacket);
        table.addCell(cellRadioPacket);
        Cell cellRadioPacketValue = new Cell().add(new Paragraph(""+monthlyStatistics.getCommunicationPlatformRadioPacketCount())).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cellRadioPacketValue);
        table.addCell(cellRadioPacketValue);

        Cell cellEcholink = new Cell().add(new Paragraph("Echolink:")).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cellEcholink);
        table.addCell(cellEcholink);
        Cell cellEcholinkValue = new Cell().add(new Paragraph(""+monthlyStatistics.getCommunicationPlatformEcholinkCount())).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cellEcholinkValue);
        table.addCell(cellEcholinkValue);

        Cell cellInternetPacket = new Cell().add(new Paragraph("Internet/Packet:")).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cellInternetPacket);
        table.addCell(cellInternetPacket);
        Cell cellInternetPacketValue = new Cell().add(new Paragraph(""+monthlyStatistics.getCommunicationPlatformInternetPacketCount())).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cellInternetPacketValue);
        table.addCell(cellInternetPacketValue);

        return table;
    }

    private Table addMonthlyReportEventStatistics(Table table, MonthlyReportStatistics monthlyStatistics) {
        table.useAllAvailableWidth();
        // triplets

        Cell cellNetName = new Cell().add(new Paragraph("Nets")).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cellNetName);
        table.addCell(cellNetName);

        Cell cellNetValue = new Cell().add(new Paragraph(""+monthlyStatistics.getTotalNetEvents())).setBorder(new SolidBorder(0));
        PdfUtils.fixupFont(cellNetValue);
        table.addCell(cellNetValue);

        Cell cellNetHours = new Cell().add(new Paragraph(printableDouble(monthlyStatistics.getTotalNetEventsHours()))).setBorder(new SolidBorder(0));
        PdfUtils.fixupFont(cellNetHours);
        table.addCell(cellNetHours);

        Cell cellExerciseName = new Cell().add(new Paragraph("Exercises")).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cellExerciseName);
        table.addCell(cellExerciseName);

        Cell cellExerciseValue = new Cell().add(new Paragraph(""+monthlyStatistics.getTotalExerciseEvents())).setBorder(new SolidBorder(0));
        PdfUtils.fixupFont(cellExerciseValue);
        table.addCell(cellExerciseValue);

        Cell cellExerciseHours = new Cell().add(new Paragraph(printableDouble(monthlyStatistics.getTotalExerciseEventsHours()))).setBorder(new SolidBorder(0));
        PdfUtils.fixupFont(cellExerciseHours);
        table.addCell(cellExerciseHours);

        Cell cellTrainingName = new Cell().add(new Paragraph("Training")).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cellTrainingName);
        table.addCell(cellTrainingName);

        Cell cellTrainingValue = new Cell().add(new Paragraph(""+monthlyStatistics.getTotalTrainingEvents())).setBorder(new SolidBorder(0));
        PdfUtils.fixupFont(cellTrainingValue);
        table.addCell(cellTrainingValue);

        Cell cellTrainingHours = new Cell().add(new Paragraph(printableDouble(monthlyStatistics.getTotalTrainingEventsHours()))).setBorder(new SolidBorder(0));
        PdfUtils.fixupFont(cellTrainingHours);
        table.addCell(cellTrainingHours);

        Cell cellPublicServiceName = new Cell().add(new Paragraph("Public service")).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cellPublicServiceName);
        table.addCell(cellPublicServiceName);

        Cell cellPublicServiceValue = new Cell().add(new Paragraph(""+monthlyStatistics.getTotalPublicServiceEvents())).setBorder(new SolidBorder(0));
        PdfUtils.fixupFont(cellPublicServiceValue);
        table.addCell(cellPublicServiceValue);

        Cell cellPublicServiceHours = new Cell().add(new Paragraph(printableDouble(monthlyStatistics.getTotalPublicServiceEventsHours()))).setBorder(new SolidBorder(0));
        PdfUtils.fixupFont(cellPublicServiceHours);
        table.addCell(cellPublicServiceHours);

        Cell cellCommunityServiceName = new Cell().add(new Paragraph("Community service")).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cellCommunityServiceName);
        table.addCell(cellCommunityServiceName);

        Cell cellCommunityServiceValue = new Cell().add(new Paragraph(""+monthlyStatistics.getTotalCommunityServiceEvents())).setBorder(new SolidBorder(0));
        PdfUtils.fixupFont(cellCommunityServiceValue);
        table.addCell(cellCommunityServiceValue);

        Cell cellCommunityServiceHours = new Cell().add(new Paragraph(printableDouble(monthlyStatistics.getTotalCommunityServiceEventsHours()))).setBorder(new SolidBorder(0));
        PdfUtils.fixupFont(cellCommunityServiceHours);
        table.addCell(cellCommunityServiceHours);

        Cell cellEmergencyName = new Cell().add(new Paragraph("Emergency")).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cellEmergencyName);
        table.addCell(cellEmergencyName);

        Cell cellEmergencyValue = new Cell().add(new Paragraph(""+monthlyStatistics.getTotalEmergencyEvents())).setBorder(new SolidBorder(0));
        PdfUtils.fixupFont(cellEmergencyValue);
        table.addCell(cellEmergencyValue);

        Cell cellEmergencyHours = new Cell().add(new Paragraph(printableDouble(monthlyStatistics.getTotalEmergencyEventsHours()))).setBorder(new SolidBorder(0));
        PdfUtils.fixupFont(cellEmergencyHours);
        table.addCell(cellEmergencyHours);

        Cell cellSkywarnName = new Cell().add(new Paragraph("SKYWARN")).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cellSkywarnName);
        table.addCell(cellSkywarnName);

        Cell cellSkywarnValue = new Cell().add(new Paragraph(""+monthlyStatistics.getTotalSkywarnEvents())).setBorder(new SolidBorder(0));
        PdfUtils.fixupFont(cellSkywarnValue);
        table.addCell(cellSkywarnValue);

        Cell cellSkywarnHours = new Cell().add(new Paragraph(printableDouble(monthlyStatistics.getTotalSkywarnEventsHours()))).setBorder(new SolidBorder(0));
        PdfUtils.fixupFont(cellSkywarnHours);
        table.addCell(cellSkywarnHours);

        Cell cellMeetingsName = new Cell().add(new Paragraph("Meetings")).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cellMeetingsName);
        table.addCell(cellMeetingsName);

        Cell cellMeetingsValue = new Cell().add(new Paragraph(""+monthlyStatistics.getTotalMeetingEvents())).setBorder(new SolidBorder(0));
        PdfUtils.fixupFont(cellMeetingsValue);
        table.addCell(cellMeetingsValue);

        Cell cellMeetingsHours = new Cell().add(new Paragraph(printableDouble(monthlyStatistics.getTotalMeetingEventsHours()))).setBorder(new SolidBorder(0));
        PdfUtils.fixupFont(cellMeetingsHours);
        table.addCell(cellMeetingsHours);

        Cell cellUnclassifiedName = new Cell().add(new Paragraph("Unclassified")).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cellUnclassifiedName);
        table.addCell(cellUnclassifiedName);

        Cell cellUnclassifiedValue = new Cell().add(new Paragraph(""+monthlyStatistics.getTotalUnclassifiedEvents())).setBorder(new SolidBorder(0));
        PdfUtils.fixupFont(cellUnclassifiedValue);
        table.addCell(cellUnclassifiedValue);

        Cell cellUnclassifiedHours = new Cell().add(new Paragraph(printableDouble(monthlyStatistics.getTotalUnclassifiedEventsHours()))).setBorder(new SolidBorder(0));
        PdfUtils.fixupFont(cellUnclassifiedHours);
        table.addCell(cellUnclassifiedHours);

        Cell cellTotalName = new Cell().add(new Paragraph("Total")).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cellTotalName);
        table.addCell(cellTotalName);

        Cell cellTotalNameValue = new Cell().add(new Paragraph(""+monthlyStatistics.getTotalEvents())).setBorder(new SolidBorder(0));
        PdfUtils.fixupFont(cellTotalNameValue);
        table.addCell(cellTotalNameValue);

        Cell cellTotalNameHours = new Cell().add(new Paragraph(printableDouble(monthlyStatistics.getTotalEventsHours()))).setBorder(new SolidBorder(0));
        PdfUtils.fixupFont(cellTotalNameHours);
        table.addCell(cellTotalNameHours);

        return table;
    }

    private String printableDouble(double value) {
        // "0.00" pattern ensures exactly two digits are always displayed
        DecimalFormat dfDefault = new DecimalFormat("0.00");
        return dfDefault.format(value); 
    }

    private Table addMonthlyReportLeadershipInformation(MonthlyReportStatistics monthlyStatistics) {
        String callsigns = "";
        Table table = new Table(2);
        table.useAllAvailableWidth();

        Cell cellLeadershipCallsigns = new Cell().add(new Paragraph("Leadership callsigns:")).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cellLeadershipCallsigns);
        table.addCell(cellLeadershipCallsigns);

        for (String callsign : monthlyStatistics.getLeadershipCallsigns()) {
            if (callsigns.length() != 0) {
                callsigns += ", ";
            }
            callsigns += callsign;
        }
        
        Cell cellLeadershipCallsignsValue = new Cell().add(new Paragraph(callsigns)).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cellLeadershipCallsignsValue);
        table.addCell(cellLeadershipCallsignsValue);

        return table;
    }

    private Table addMonthlyReportRACESInformation(MonthlyReportStatistics monthlyStatistics) {
        String callsigns = "";
        Table table = new Table(2);
        table.useAllAvailableWidth();

        Cell cellLeadershipCallsigns = new Cell().add(new Paragraph("RACES callsigns:")).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cellLeadershipCallsigns);
        table.addCell(cellLeadershipCallsigns);

        for (String callsign : monthlyStatistics.getRacesCallsigns()) {
            if (callsigns.length() != 0) {
                callsigns += ", ";
            }
            callsigns += callsign;
        }
        
        Cell cellLeadershipCallsignsValue = new Cell().add(new Paragraph(callsigns)).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cellLeadershipCallsignsValue);
        table.addCell(cellLeadershipCallsignsValue);

        return table;
    }

    private Table addMonthlyReportParticipantStatistics(MonthlyReportStatistics monthlyStatistics) {
        Table table = new Table(4);
        table.useAllAvailableWidth();

        Cell cellSectionMembers = new Cell().add(new Paragraph("Section members:")).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cellSectionMembers);
        table.addCell(cellSectionMembers);
        Cell cellSectionMembersValue = new Cell().add(new Paragraph(""+monthlyStatistics.getSectionMembers())).setBorder(new SolidBorder(0));
        PdfUtils.fixupFont(cellSectionMembersValue);
        table.addCell(cellSectionMembersValue);

        Cell cellSectionLeadershipTotal = new Cell().add(new Paragraph("Section leaders:")).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cellSectionLeadershipTotal);
        table.addCell(cellSectionLeadershipTotal);
        Cell cellSectionLeadershipTotalValue = new Cell().add(new Paragraph(""+monthlyStatistics.getLeadershipMembersTotal())).setBorder(new SolidBorder(0));
        PdfUtils.fixupFont(cellSectionLeadershipTotalValue);
        table.addCell(cellSectionLeadershipTotalValue);

        Cell cellParticipations = new Cell().add(new Paragraph("Total operator participations:")).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cellParticipations);
        table.addCell(cellParticipations);
        Cell cellParticipationsValue = new Cell().add(new Paragraph(""+monthlyStatistics.getParticipations())).setBorder(new SolidBorder(0));
        PdfUtils.fixupFont(cellParticipationsValue);
        table.addCell(cellParticipationsValue);

        Cell cellSectionLeadership = new Cell().add(new Paragraph("Total leader participations:")).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cellSectionLeadership);
        table.addCell(cellSectionLeadership);
        Cell cellSectionLeadershipValue = new Cell().add(new Paragraph(""+monthlyStatistics.getLeadershipMembers())).setBorder(new SolidBorder(0));
        PdfUtils.fixupFont(cellSectionLeadershipValue);
        table.addCell(cellSectionLeadershipValue);

        Cell cellUniqueParticipants = new Cell().add(new Paragraph("Unique participants:")).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cellUniqueParticipants);
        table.addCell(cellUniqueParticipants);
        Cell cellUniqueParticipantsValue = new Cell().add(new Paragraph(""+monthlyStatistics.getUniqueParticipants())).setBorder(new SolidBorder(0));
        PdfUtils.fixupFont(cellUniqueParticipantsValue);
        table.addCell(cellUniqueParticipantsValue);

        Cell cellEventsHeld = new Cell().add(new Paragraph("Events held:")).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cellEventsHeld);
        table.addCell(cellEventsHeld);
        Cell cellEventsHeldValue = new Cell().add(new Paragraph(""+monthlyStatistics.getSessionsHeld())).setBorder(new SolidBorder(0));
        PdfUtils.fixupFont(cellEventsHeldValue);
        table.addCell(cellEventsHeldValue);

        Cell cellBlank1 = new Cell().add(new Paragraph("Section non-participants:")).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cellBlank1);
        table.addCell(cellBlank1);
        Cell cellBlank2 = new Cell().add(new Paragraph(""+monthlyStatistics.getNonParticipants())).setBorder(new SolidBorder(0));
        PdfUtils.fixupFont(cellBlank2);
        table.addCell(cellBlank2);

        Cell cellEventsHeldWithNTS = new Cell().add(new Paragraph("Events held with NTS liaison:")).setBorder(new SolidBorder(0)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cellEventsHeldWithNTS);
        table.addCell(cellEventsHeldWithNTS);
        Cell cellEventsHeldWithNTSValue = new Cell().add(new Paragraph(""+monthlyStatistics.getSessionsHeldWithNTS())).setBorder(new SolidBorder(0));
        PdfUtils.fixupFont(cellEventsHeldWithNTSValue);
        table.addCell(cellEventsHeldWithNTSValue);

        return table;
    }

    /**
     * add the document header
     *
     * @param table table to create
     * @param ctx application context
     */
    private Table addMonthlyReportDocumentHeader(String title) {
        Table table = new Table(2);
        table.useAllAvailableWidth();

        Cell cell1 = new Cell().add(new Paragraph(title)).setBorder(new SolidBorder(1)).setVerticalAlignment(VerticalAlignment.MIDDLE);
        PdfUtils.makeBold(cell1);
        table.addCell(cell1);

        LocalDateTime now = LocalDateTime.now();
        String dateStr = DateStrUtils.getDateStr(now);
        String timeStr = DateStrUtils.getTimeStr(now);
        Cell cell3 = new Cell().add(new Paragraph("DATE PREPARED:" + dateStr + " \n" +
                                    "TIME PREPARED: " + timeStr + "\n")).setBorder(new SolidBorder(1));
        PdfUtils.fixupFont(cell3);
        table.addCell(cell3);
        return table;
    }

    /**
     * add banner
     * 
     * @return table
     */
    private Table addMonthlyReportBanner(String title) {
        Table table = new Table(1);

        table.useAllAvailableWidth();
        Cell cell = new Cell().add(new Paragraph(title)).setBorder(new SolidBorder(1)).setTextAlignment(TextAlignment.CENTER);
        PdfUtils.makeDarkBackground(cell);
        table.addCell(cell);
        return table;
    }

    /**
     * add event statistics log header
     *
     * @return table
     */
    private Table addMonthlyReportEventStatisticsHeader() {
        Table table = new Table(3);
        table.useAllAvailableWidth();
        Cell cell1 = new Cell().add(new Paragraph("Event Type")).setBorder(new SolidBorder(1)).setTextAlignment(TextAlignment.CENTER);
        PdfUtils.fixupFontBold(cell1);
        table.addCell(cell1);

        Cell cell2 = new Cell().add(new Paragraph("Events")).setBorder(new SolidBorder(1)).setTextAlignment(TextAlignment.CENTER);
        PdfUtils.fixupFontBold(cell2);
        table.addCell(cell2);

        Cell cell3 = new Cell().add(new Paragraph("Total hours")).setBorder(new SolidBorder(1)).setTextAlignment(TextAlignment.CENTER);
        PdfUtils.fixupFontBold(cell3);
        table.addCell(cell3);

        return table;
    }

    /**
     * add info header
     *
     * @param ctx application context
     * @return table
     */
    private Table addMonthlyReportInfoHeader(String dateStr, String sectionName) {
        Table table = new Table(2);
        table.useAllAvailableWidth();

        String sectionNameValue = String.format("SECTION: %s", sectionName);
        Cell cell2 = new Cell().add(new Paragraph(sectionNameValue)).setBorder(new SolidBorder(1));
        PdfUtils.fixupFont(cell2);
        table.addCell(cell2);

        String reportingPeriod = String.format("REPORTING PERIOD %s", dateStr);
        Cell cell1 = new Cell().add(new Paragraph(reportingPeriod)).setBorder(new SolidBorder(1));
        PdfUtils.fixupFont(cell1);
        table.addCell(cell1);

        return table;
    }

    /**
     * add footer
     *
     * @param ctx application context
     * @param pageCurrent current page number
     * @param pageCount total number of pages
     * @return table
     */
    private Table addMonthlyReportFooter() {
        String text = String.format("Report generated by %s %s", SoftwareIdentity.NAME, SoftwareIdentity.VERSION);

        Table table = new Table(1);
        table.useAllAvailableWidth();
        Cell cell = new Cell().add(new Paragraph(text)).setBorder(new SolidBorder(1)).setTextAlignment(TextAlignment.CENTER);
        PdfUtils.makeBold(cell);
        table.addCell(cell);
        return table;
    }
}
