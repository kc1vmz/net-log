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
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kc1vmz.netlog.object.RecurringEvent;
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
public class RecurringEventReportAccessor {
    private final static int MAX_ROWS = 8;
    private static final Logger logger = LogManager.getLogger(RecurringEventReportAccessor.class);


    public String generateReport(Section section, List<RecurringEvent> recurringEvents, String title) {
        try {
            return createReport(section, recurringEvents, title);
        } catch (Exception e) {
            logger.error("Exception caught generating report", e);
        }
        return null;
    }

    public String getTempReportDir() {
        return System.getProperty("java.io.tmpdir");
    }

    public String createReport(Section section, List<RecurringEvent> recurringEvents, String title) throws FileNotFoundException {
        String filename = getUniqueFileName(getTempReportDir(), "pdf");
        try {
            PdfDocument pdf = new PdfDocument(new PdfWriter(new FileOutputStream(filename)));
            Document document = new Document(pdf);
            document.setMargins(30, 32, 30, 32);

            int pageCount = 1;
            if ((recurringEvents != null) && (!recurringEvents.isEmpty())) {
                // calculate number of pages
                pageCount = (recurringEvents.size() / MAX_ROWS) + 1;
            }

            for (int page = 1; page <= pageCount; page++) {
                document.add(addDocumentHeader(title));
                document.add(addInfoHeader(section.getName()));
                document.add(addBanner("Events"));

                Table communicationsLogHeader = addEventHeader();
                addEventRows(communicationsLogHeader, recurringEvents, page);
                document.add(communicationsLogHeader);

                document.add(addFooter(page, pageCount, title));
                document.add(addGenerator());
                if (page != pageCount) {
                    // do not add to last page
                    document.add(new AreaBreak());
                }
            }
            document.close();
            return filename.substring(getTempReportDir().length());
        } catch (Exception e) {
            logger.error("Exception caught creating 309 report", e);
        }
        return null;
    }

    private void addEventRows(Table table, List<RecurringEvent> recurringEvents, int page) {
        table.useAllAvailableWidth();

        if ((recurringEvents == null) || (recurringEvents.isEmpty()) ) {
            return;
        }

        // skip over
        int skipCount = (page - 1) * MAX_ROWS;
        int itemCount = 0;

        for (RecurringEvent entry : recurringEvents) {
            // skip to corret page of messages
            if (skipCount != 0) {
                skipCount--;
                continue;
            }

            String value = String.format("Name: %s\nDescription: %s\nLocation: %s\nSchedule: %s",
                            entry.getName(), entry.getDescription(), entry.getLocation(), entry.getSchedule());
            Cell cellValue = new Cell().add(new Paragraph(value));
            table.addCell(cellValue);

            itemCount++;
            if (itemCount == MAX_ROWS) {
                // limit page to MAX_ROWS rows
                break;
            }
        }

        // build blank rows
        if (itemCount < MAX_ROWS) {
            for (; itemCount < MAX_ROWS; itemCount++) {
                Cell cell = new Cell().add(new Paragraph("\n\n\n"));
                table.addCell(cell);
            }
        }

    }

    /**
     * add event header
     *
     * @return table
     */
    private Table addEventHeader() {
        Table table = new Table(1);
        table.useAllAvailableWidth();
        Cell cell1 = new Cell().add(new Paragraph("Name / Description / Location / Schedule")).setBorder(new SolidBorder(1)).setTextAlignment(TextAlignment.CENTER);
        PdfUtils.fixupFontBold(cell1);
        table.addCell(cell1);

        return table;
    }

    /**
     * add banner
     * 
     * @return table
     */
    private Table addBanner(String title) {
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
     * add info header
     *
     * @param ctx application context
     * @return table
     */
    private Table addInfoHeader(String sectionName) {
        Table table = new Table(1);
        table.useAllAvailableWidth();

        String sectionNameValue = String.format("SECTION: %s", sectionName);
        Cell cell2 = new Cell().add(new Paragraph(sectionNameValue)).setBorder(new SolidBorder(1));
        PdfUtils.fixupFont(cell2);
        table.addCell(cell2);

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
    private Table addFooter(int pageCurrent, int pageCount, String title) {
        Table table = new Table(2);
        table.useAllAvailableWidth();
        table.addCell(new Cell().add(new Paragraph("Page " + pageCurrent + " of " + pageCount)).setBorder(new SolidBorder(1)));
        Cell cell = new Cell().add(new Paragraph(title)).setBorder(new SolidBorder(1)).setTextAlignment(TextAlignment.RIGHT);
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
