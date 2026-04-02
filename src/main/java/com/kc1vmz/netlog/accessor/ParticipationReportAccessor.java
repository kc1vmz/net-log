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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kc1vmz.netlog.object.Event;
import com.kc1vmz.netlog.object.Participant;
import com.kc1vmz.netlog.object.ReportParticipantEntry;
import com.kc1vmz.netlog.object.Section;
import com.kc1vmz.netlog.utils.DateStrUtils;
import com.kc1vmz.netlog.utils.PdfUtils;
import com.kc1vmz.netlog.utils.SoftwareIdentity;

import jakarta.inject.Singleton;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;

@Singleton
public class ParticipationReportAccessor {
    private final static int MAX_ROWS_EVENTS = 28;
    private final static int MAX_ROWS_PARTICIPANTS = 28;
    private static final Logger logger = LogManager.getLogger(ParticipationReportAccessor.class);


    public String generateReport(Section section, Map<Event,List<Participant>> eventParticipants, String dateStr, String title) {
        try {
            return createReport(section, eventParticipants, dateStr, title);
        } catch (Exception e) {
            logger.error("Exception caught generating report", e);
        }
        return null;
    }

    public String getTempReportDir() {
        return System.getProperty("java.io.tmpdir");
    }

    public String createReport(Section section, Map<Event,List<Participant>> eventParticipants, String dateStr, String title) throws FileNotFoundException {
        String filename = getUniqueFileName(getTempReportDir(), "pdf");
        try {
            int eventPages = 1;
            int participantPages = 1;
            int pageTotal = 1;
            int totalEvents = eventParticipants.size();

            Map<String, ReportParticipantEntry> participations = buildReportEntries(eventParticipants);
            int participants = participations.size();

            if (!eventParticipants.isEmpty()) {
                // calculate number of event pages
                eventPages = (totalEvents / MAX_ROWS_EVENTS) + 1;
            }

            if (!participations.isEmpty()) {
                // calculate number of participation pages
                participantPages = ((((totalEvents/10)+1) * participations.size()) / MAX_ROWS_PARTICIPANTS) + 1;
            }
            // participantPages needs to handle X callsigns by (Y/10)+1 events

            pageTotal = eventPages + participantPages;
 
            PdfDocument pdf = new PdfDocument(new PdfWriter(new FileOutputStream(filename)));
            Document document = new Document(pdf);
            document.setMargins(30, 32, 30, 32);

            int page = 1;
            for (; page <= eventPages; page++) {
                document.add(addDocumentHeader(title));
                document.add(addInfoHeader(section.getName(), dateStr));
                document.add(addMonthlyReportBanner("EVENTS"));

                Table eventListHeader = addEventListHeader();
                addEventRows(eventListHeader, eventParticipants, page);
                document.add(eventListHeader);

                document.add(addFooter(page, pageTotal));
                document.add(addGenerator());
                if (page != pageTotal) {
                    // do not add to last page
                    document.add(new AreaBreak());
                }
            }

            int eventSets = (eventParticipants.size() / 10) + 1;
            int participantSets = (participants / MAX_ROWS_PARTICIPANTS) + 1;
            for (int eventSetIndex = 0; eventSetIndex < eventSets; eventSetIndex++) {
                for (int participantSetIndex = 0; participantSetIndex < participantSets; participantSetIndex++) {
                    document.add(addDocumentHeader(title));
                    document.add(addInfoHeader(section.getName(), dateStr));
                    document.add(addMonthlyReportBanner("PARTICIPATION GRID"));

                    Table participationGridHeader = addParticipationGridHeader(eventSetIndex, eventParticipants.size());
                    addCallsignRows(participationGridHeader, participations, eventSetIndex, participantSetIndex, totalEvents);
                    document.add(participationGridHeader);

                    document.add(addFooter(page, pageTotal));
                    document.add(addGenerator());
                    if (page != pageTotal) {
                        // do not add to last page
                        document.add(new AreaBreak());
                    }
                    page++;
                }
            }

            document.close();
            return filename.substring(getTempReportDir().length());
        } catch (Exception e) {
            logger.error("Exception caught creating Monthly Participant Report", e);
        }
        return null;

    }

    private Map<String, ReportParticipantEntry> buildReportEntries(Map<Event,List<Participant>> eventParticipants) {
        int eventCount = eventParticipants.size();
        int eventIndex = 0;
        Map<String, ReportParticipantEntry> ret = new TreeMap<String, ReportParticipantEntry>();

        for (Map.Entry<Event,List<Participant>> mapEntry : eventParticipants.entrySet()) {
            List<Participant> participants = mapEntry.getValue();
            for (Participant participant : participants) {
                if (participant.getOperator() == null) {
                    continue;
                }
                ReportParticipantEntry entry = ret.get(participant.getOperator().getCallsign());
                if (entry == null) {
                    entry = new ReportParticipantEntry(participant.getOperator().getCallsign(), eventCount);
                    ret.put(participant.getOperator().getCallsign(), entry);
                }
                boolean [] eventMask = entry.getEventMask();
                eventMask[eventIndex] = true;
                entry.setEventMask(eventMask);
                entry.setEventParticipationCount(entry.getEventParticipationCount()+1);
            }

            eventIndex++;
        }

        return ret;
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

    public String getUniqueFileName(String directory, String extension) {
        String fileName = MessageFormat.format("{0}.{1}", UUID.randomUUID(), extension.trim());
        return Paths.get(directory, fileName).toString();
    }

    /**
     * add the document header
     *
     * @param table table to create
     * @param ctx application context
     */
    private Table addDocumentHeader(String title) {
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
     * add event list header
     *
     * @return table
     */
    private Table addEventListHeader() {
        Table table = new Table(6);
        table.useAllAvailableWidth();
        Cell cell1 = new Cell().add(new Paragraph("#")).setBorder(new SolidBorder(1)).setTextAlignment(TextAlignment.CENTER);
        PdfUtils.fixupFontBold(cell1);
        table.addCell(cell1);

        Cell cell2 = new Cell().add(new Paragraph("START")).setBorder(new SolidBorder(1)).setTextAlignment(TextAlignment.CENTER);
        PdfUtils.fixupFontBold(cell2);
        table.addCell(cell2);

        Cell cell3 = new Cell().add(new Paragraph("NAME")).setBorder(new SolidBorder(1)).setTextAlignment(TextAlignment.CENTER);
        PdfUtils.fixupFontBold(cell3);
        table.addCell(cell3);

        Cell cell7 = new Cell().add(new Paragraph("TYPE")).setBorder(new SolidBorder(1)).setTextAlignment(TextAlignment.CENTER);
        PdfUtils.fixupFontBold(cell7);
        table.addCell(cell7);

        Cell cell4 = new Cell().add(new Paragraph("OPS")).setBorder(new SolidBorder(1)).setTextAlignment(TextAlignment.CENTER);
        PdfUtils.fixupFontBold(cell4);
        table.addCell(cell4);

        Cell cell6 = new Cell().add(new Paragraph("NET CONTROL")).setBorder(new SolidBorder(1)).setTextAlignment(TextAlignment.CENTER);
        PdfUtils.fixupFontBold(cell6);
        table.addCell(cell6);

        return table;
    }

    /**
     * add participation grid header
     *
     * @return table
     */
    private Table addParticipationGridHeader(int startEventIndex, int totalEvents) {
        int columns = 10;

        Table table = new Table(columns+1); // add callsign
        table.useAllAvailableWidth();
        Cell cell1 = new Cell().add(new Paragraph("CALLSIGN/COUNT")).setBorder(new SolidBorder(1)).setTextAlignment(TextAlignment.CENTER);
        PdfUtils.fixupFontBold(cell1);
        table.addCell(cell1);

        for (int i = 0; i < columns; i++) {
            String text = ""+((startEventIndex*10)+1+i);
            if ((startEventIndex*10)+i+1 > totalEvents) {
                text = "";
            }
            Cell cell = new Cell().add(new Paragraph(text)).setBorder(new SolidBorder(1)).setTextAlignment(TextAlignment.CENTER);
            PdfUtils.fixupFontBold(cell);
            table.addCell(cell);
        }

        return table;
    }

    /**
     * add info header
     *
     * @param ctx application context
     * @return table
     */
    private Table addInfoHeader(String sectionName, String dateStr) {
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
     * add event data rows
     *
     */
    private void addEventRows(Table table, Map<Event,List<Participant>> eventParticipants, int page) {
        table.useAllAvailableWidth();

        if ((eventParticipants == null) || (eventParticipants.isEmpty()) ) {
            return;
        }

        // skip over
        int skipCount = (page - 1) * MAX_ROWS_EVENTS;
        int itemCount = 0;
        int itemIndex = 0;

        for (Map.Entry<Event,List<Participant>> mapEntry : eventParticipants.entrySet()) {
            itemIndex++;
            // skip to correct page of messages
            if (skipCount != 0) {
                skipCount--;
                continue;
            }
            Event entry = mapEntry.getKey();
            List<Participant> participants = mapEntry.getValue();

            Cell cellIndex = new Cell().add(new Paragraph(""+itemIndex));
            Cell cellStartTime = new Cell().add(new Paragraph(entry.getPrettyStartTime()));
            Cell cellName = new Cell().add(new Paragraph(entry.getName()));
            Cell cellType = new Cell().add(new Paragraph(entry.getType().toString()));
            Cell cellParticipants = new Cell().add(new Paragraph(""+participants.size()));
            Cell cellNetControl = new Cell().add(new Paragraph(entry.getNetControlCallsign()));
            table.addCell(cellIndex);
            table.addCell(cellStartTime);
            table.addCell(cellName);
            table.addCell(cellType);
            table.addCell(cellParticipants);
            table.addCell(cellNetControl);

            itemCount++;
            if (itemCount == MAX_ROWS_EVENTS) {
                // limit page to MAX_ROWS rows
                break;
            }
        }

        // build blank rows
        if (itemCount < MAX_ROWS_EVENTS) {
            for (; itemCount < MAX_ROWS_EVENTS; itemCount++) {
                // add row of 6 cells
                for (int i = 0; i < 6; i++) {
                    Cell cell = new Cell();
                    cell.setHeight(16);
                    table.addCell(cell);
                }
            }
        }
    }

    /**
     * add callsign rows
     * 
     * eventSetIndex - starting index of events as a multiple of 10
     * participantSetIndex - starting index as a multiple of MAX_ROW
     *
     */
    private void addCallsignRows(Table table, Map<String, ReportParticipantEntry> callsignEntries, int eventSetIndex, int participantSetIndex, int totalEvents) {
        table.useAllAvailableWidth();

        if ((callsignEntries == null) || (callsignEntries.isEmpty()) ) {
            return;
        }

        // skip over
        int skipCount = (participantSetIndex) * MAX_ROWS_PARTICIPANTS;
        int itemCount = 0;

        for (Map.Entry<String,ReportParticipantEntry> mapEntry : callsignEntries.entrySet()) {
            // skip to correct page of messages
            if (skipCount != 0) {
                skipCount--;
                continue;
            }
            String callsign = mapEntry.getKey();
            ReportParticipantEntry values = mapEntry.getValue();

            String callsignCount = String.format("%s (%d)", callsign, values.getEventParticipationCount());
            Cell cellName = new Cell().add(new Paragraph(callsignCount));
            table.addCell(cellName);

            for (int i = 0; i < 10; i++) {
                String text = "";
                if (eventSetIndex*10+i < totalEvents) {
                    if (values.getEventMask()[eventSetIndex*10+i]) {
                        text = "Yes";
                    }
                }
                Cell cell = new Cell().add(new Paragraph(text)).setBorder(new SolidBorder(1)).setTextAlignment(TextAlignment.CENTER);
                PdfUtils.fixupFontBold(cell);
                table.addCell(cell);
            }

            itemCount++;
            if (itemCount == MAX_ROWS_PARTICIPANTS) {
                // limit page to MAX_ROWS rows
                break;
            }
        }

        // build blank rows
        if (itemCount < MAX_ROWS_PARTICIPANTS) {
            for (; itemCount < MAX_ROWS_PARTICIPANTS; itemCount++) {
                // add row of 11 cells
                for (int i = 0; i < 11; i++) {
                    Cell cell = new Cell();
                    cell.setHeight(16);
                    table.addCell(cell);
                }
            }
        }
    }

    /**
     * add footer
     *
     * @param ctx application context
     * @param pageCurrent current page number
     * @param pageCount total number of pages
     * @return table
     */
    private Table addFooter(int pageCurrent, int pageCount) {
        Table table = new Table(2);
        table.useAllAvailableWidth();
        table.addCell(new Cell().add(new Paragraph("Page " + pageCurrent + " of " + pageCount)).setBorder(new SolidBorder(1)));
        Cell cell = new Cell().add(new Paragraph("Event Participation Log")).setBorder(new SolidBorder(1)).setTextAlignment(TextAlignment.RIGHT);
        PdfUtils.makeBold(cell);
        table.addCell(cell);
        return table;
    }

    /**
     * add generator row
     *
     * @return table
     */
    private Table addGenerator() {
        Table table = new Table(1);
        table.useAllAvailableWidth();
        String text = String.format("Report generated by %s %s", SoftwareIdentity.NAME, SoftwareIdentity.VERSION);
        table.addCell(new Cell().add(new Paragraph(text)).setBorder(new SolidBorder(1)).setTextAlignment(TextAlignment.RIGHT));
        return table;
    }
}
