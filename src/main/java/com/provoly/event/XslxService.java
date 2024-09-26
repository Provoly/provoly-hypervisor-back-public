package com.provoly.event;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import com.provoly.event.dto.ExportEventDto;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jboss.logging.Logger;

@ApplicationScoped
public class XslxService {

    public static final int ID_SIZE = 6;
    private final Logger logger;
    private CellStyle style;

    public XslxService(Logger logger) {
        this.logger = logger;
    }

    public ByteArrayInputStream generateExcelWithEvents(List<ExportEventDto> events) throws IOException {
        ByteArrayOutputStream outputStream;
        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Evènements");
            Row header = sheet.createRow(0);

            logger.debugf("Generate header");
            int headerIndex = 0;
            generateHeaderRow(header, "Identifiant", headerIndex++);
            generateHeaderRow(header, "Statut", headerIndex++);
            generateHeaderRow(header, "Nom", headerIndex++);
            generateHeaderRow(header, "Catégorie", headerIndex++);
            generateHeaderRow(header, "Criticité", headerIndex++);
            generateHeaderRow(header, "Source", headerIndex++);
            generateHeaderRow(header, "Métier", headerIndex++);
            generateHeaderRow(header, "Description", headerIndex++);
            generateHeaderRow(header, "Adresse", headerIndex++);
            generateHeaderRow(header, "Équipement lié", headerIndex++);
            generateHeaderRow(header, "Demande(s) d'intervention liée(s)", headerIndex++);
            generateHeaderRow(header, "Date de début", headerIndex++);
            generateHeaderRow(header, "Date de fin", headerIndex++);
            generateHeaderRow(header, "Date de création", headerIndex++);
            generateHeaderRow(header, "Date de dernière modification", headerIndex++);
            generateHeaderRow(header, "Date de clôture", headerIndex++);
            generateHeaderRow(header, "Avancement de la procédure", headerIndex++);
            generateHeaderRow(header, "Événement(s) lié(s)", headerIndex++);
            generateHeaderRow(header, "Événement parent", headerIndex);

            var columnIndex = 1;

            logger.debugf("Insert %s events in file", events.size());
            for (var event : events) {
                Row row = sheet.createRow(columnIndex);

                int rowIndex = 0;
                setRow(row, rowIndex++, formatIdWithZeros(event.id().toString()));
                setRow(row, rowIndex++, event.status());
                setRow(row, rowIndex++, event.name());
                setRow(row, rowIndex++, event.category());
                setRow(row, rowIndex++, event.criticality());
                setRow(row, rowIndex++, event.externalSourceRef());
                setRow(row, rowIndex++, event.domain());
                setRow(row, rowIndex++, event.description());
                setRow(row, rowIndex++, event.address());
                setRow(row, rowIndex++, event.equipment());
                setRow(row, rowIndex++, event.services().toString());
                setRow(row, rowIndex++, event.startDate());
                setRow(row, rowIndex++, event.endDate());
                setRow(row, rowIndex++, event.creationDate());
                setRow(row, rowIndex++, event.lastModificationDate());
                setRow(row, rowIndex++, event.closeDate());
                setRow(row, rowIndex++, event.procedureProgress() / 100.0f);
                setRow(row, rowIndex++, removeCurrentIdFromLinkedEvent(event.linkedEvents(), event.id()));
                setRow(row, rowIndex, event.parent() == null ? null : formatIdWithZeros(event.parent().toString()));
                columnIndex++;
            }

            outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
        }

        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    private static String removeCurrentIdFromLinkedEvent(List<Integer> eventIds, Integer currentId) {
        return eventIds
                .stream()
                .filter(id -> !id.equals(currentId))
                .map(e -> formatIdWithZeros(e.toString())).toList().toString();
    }

    private static String formatIdWithZeros(String formattedId) {
        var currentIdSize = formattedId.length();
        while (currentIdSize != ID_SIZE) {
            formattedId = "0%s".formatted(formattedId);
            currentIdSize = formattedId.length();
        }
        return formattedId;
    }

    private void setRow(Row row, int index, Object value) {
        Cell cell = row.createCell(index);
        if (value == null) {
            value = "";
        }
        switch (value) {
            case String s -> cell.setCellValue(s);
            case Instant instant -> cell.setCellValue(instant.toString());
            case Float f -> {
                DecimalFormat df = new DecimalFormat("#,###.##%");
                cell.setCellValue((df.format(f)));
            }
            case Integer i -> cell.setCellValue(i);
            default -> throw new IllegalStateException("Unexpected value: " + value);
        }
    }

    private void generateHeaderRow(Row header, String name, int index) {
        Cell headerCell = header.createCell(index);
        headerCell.setCellValue(name);
    }
}
