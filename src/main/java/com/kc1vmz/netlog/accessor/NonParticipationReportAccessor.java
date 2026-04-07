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
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kc1vmz.netlog.object.Event;
import com.kc1vmz.netlog.object.Participant;
import com.kc1vmz.netlog.object.Section;
import com.kc1vmz.netlog.object.SectionOperator;
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
public class NonParticipationReportAccessor {
    private final static int MAX_ROWS = 28;
    private static final Logger logger = LogManager.getLogger(NonParticipationReportAccessor.class);


    public String generateReport(Section section, List<SectionOperator> members, Map<Event,List<Participant>> eventParticipants, String dateStr, String title) {
        try {
            return createReport(section, members, eventParticipants, dateStr, title);
        } catch (Exception e) {
            logger.error("Exception caught generating report", e);
        }
        return null;
    }

    public String getTempReportDir() {
        return System.getProperty("java.io.tmpdir");
    }

    public String createReport(Section section, List<SectionOperator> members, Map<Event,List<Participant>> eventParticipants, String dateStr, String title) throws FileNotFoundException {
        String filename = getUniqueFileName(getTempReportDir(), "pdf");
        try {
            int pageTotal = 1;

            // remove participants from all members
            removeEventParticipants(members, eventParticipants);
            int nonParticipatingMemberCount = members.size();

            pageTotal = (((nonParticipatingMemberCount / 2) + 1) / MAX_ROWS) + 1;

            PdfDocument pdf = new PdfDocument(new PdfWriter(new FileOutputStream(filename)));
            Document document = new Document(pdf);
            document.setMargins(30, 32, 30, 32);

            String headerParticipantsCount = String.format("NON-PARTICIPANTS (%d TOTAL)", nonParticipatingMemberCount);
            int page = 1;
            for (; page <= pageTotal; page++) {
                document.add(addDocumentHeader(title));
                document.add(addInfoHeader(section.getName(), dateStr));
                document.add(addMonthlyReportBanner(headerParticipantsCount));

                Table eventListHeader = addNonParticipantHeader();
                addNonParticipantRows(eventListHeader, members, page);
                document.add(eventListHeader);

                document.add(addFooter(page, pageTotal));
                document.add(addGenerator());
                if (page != pageTotal) {
                    // do not add to last page
                    document.add(new AreaBreak());
                }
            }

            document.close();
            return filename.substring(getTempReportDir().length());
        } catch (Exception e) {
            logger.error("Exception caught creating Monthly Non-Participant Report", e);
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
     * add non participant header
     *
     * @return table
     */
    private Table addNonParticipantHeader() {
        Table table = new Table(4);
        table.useAllAvailableWidth();
        Cell cell1 = new Cell().add(new Paragraph("CALLSIGN")).setBorder(new SolidBorder(1)).setTextAlignment(TextAlignment.CENTER);
        PdfUtils.fixupFontBold(cell1);
        table.addCell(cell1);

        Cell cell2 = new Cell().add(new Paragraph("NAME")).setBorder(new SolidBorder(1)).setTextAlignment(TextAlignment.CENTER);
        PdfUtils.fixupFontBold(cell2);
        table.addCell(cell2);

        Cell cell3 = new Cell().add(new Paragraph("CALLSIGN")).setBorder(new SolidBorder(1)).setTextAlignment(TextAlignment.CENTER);
        PdfUtils.fixupFontBold(cell3);
        table.addCell(cell3);

        Cell cell4 = new Cell().add(new Paragraph("NAME")).setBorder(new SolidBorder(1)).setTextAlignment(TextAlignment.CENTER);
        PdfUtils.fixupFontBold(cell4);
        table.addCell(cell4);

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
    private void addNonParticipantRows(Table table, List<SectionOperator> members, int page) {
        table.useAllAvailableWidth();

        if ((members == null) || (members.isEmpty()) ) {
            return;
        }

        // skip over
        int itemCount = 0;
        int skipIndex = (page - 1) * MAX_ROWS * 2;  // 2 per row
        int maxIndex = skipIndex + (MAX_ROWS * 2);
        if (maxIndex > members.size()) {
            maxIndex = members.size();
        }

        for (int i = skipIndex; i < maxIndex; i+=2) {
            String callsign2 = "";
            String name2 = "";
            SectionOperator left = members.get(i);
            String callsign1 = left.getCallsign();
            String name1 = left.getName();

            if (i+1 < maxIndex) {
                SectionOperator right = members.get(i+1);
                callsign2 = right.getCallsign();
                name2 = right.getName();
            }

            Cell cellCallsign1 = new Cell().add(new Paragraph(callsign1));
            Cell cellName1 = new Cell().add(new Paragraph(name1));
            Cell cellCallsign2 = new Cell().add(new Paragraph(callsign2));
            Cell cellName2 = new Cell().add(new Paragraph(name2));

            table.addCell(cellCallsign1);
            table.addCell(cellName1);
            table.addCell(cellCallsign2);
            table.addCell(cellName2);
            itemCount++;
        }

        // build blank rows
        if (itemCount < MAX_ROWS) {
            for (; itemCount < MAX_ROWS; itemCount++) {
                // add row of 4 cells
                for (int i = 0; i < 4; i++) {
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
        Cell cell = new Cell().add(new Paragraph("Event Non-Participation Log")).setBorder(new SolidBorder(1)).setTextAlignment(TextAlignment.RIGHT);
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
