package com.provoly.event;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import com.provoly.event.dto.ExportEventDto;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jboss.logging.Logger;

@ApplicationScoped
public class XslxService {

    private final Logger logger;

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
            generateHeaderRow(header, "Nom", headerIndex++);
            generateHeaderRow(header, "Identifiant", headerIndex++);
            generateHeaderRow(header, "Statut", headerIndex++);
            generateHeaderRow(header, "Catégorie", headerIndex++);
            generateHeaderRow(header, "Criticité", headerIndex++);
            generateHeaderRow(header, "Description", headerIndex++);
            generateHeaderRow(header, "Adresse", headerIndex++);
            generateHeaderRow(header, "Source", headerIndex++);
            generateHeaderRow(header, "Référence de l'équipement", headerIndex++);
            generateHeaderRow(header, "Métier", headerIndex++);
            generateHeaderRow(header, "Date de début", headerIndex++);
            generateHeaderRow(header, "Date de fin", headerIndex++);
            generateHeaderRow(header, "Date de création", headerIndex++);
            generateHeaderRow(header, "Date de dernière modification", headerIndex++);
            generateHeaderRow(header, "Date de clotûre", headerIndex++);
            generateHeaderRow(header, "Progression de la procédure", headerIndex++);
            generateHeaderRow(header, "Évènements liés", headerIndex++);
            generateHeaderRow(header, "Demandes d'interventions liées", headerIndex);
            generateHeaderRow(header, "Évènement parent", headerIndex);

            var columnIndex = 1;

            logger.debugf("Insert %s events in file", events.size());
            for (var event : events) {
                Row row = sheet.createRow(columnIndex);

                int rowIndex = 0;
                setRow(row, rowIndex++, event.name());
                setRow(row, rowIndex++, event.id().toString());
                setRow(row, rowIndex++, event.status());
                setRow(row, rowIndex++, event.category());
                setRow(row, rowIndex++, event.criticality());
                setRow(row, rowIndex++, event.description());
                setRow(row, rowIndex++, event.address());
                setRow(row, rowIndex++, event.externalSourceRef());
                setRow(row, rowIndex++, event.equipment());
                setRow(row, rowIndex++, event.domain());
                setRow(row, rowIndex++, event.startDate());
                setRow(row, rowIndex++, event.endDate());
                setRow(row, rowIndex++, event.creationDate());
                setRow(row, rowIndex++, event.lastModificationDate());
                setRow(row, rowIndex++, event.endDate());
                setRow(row, rowIndex++, event.procedureProgress());
                setRow(row, rowIndex++, event.linkedEvents().toString());
                setRow(row, rowIndex, event.services().toString());
                setRow(row, rowIndex, event.parent());
                columnIndex++;
            }

            outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
        }

        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    private void setRow(Row row, int index, Object value) {
        Cell cell = row.createCell(index);
        if (value == null) {
            value = "";
        }
        switch (value) {
            case String s -> cell.setCellValue(s);
            case Instant instant -> cell.setCellValue(instant.toString());
            case Float f -> cell.setCellValue(f);
            case Integer i -> cell.setCellValue(i);
            default -> throw new IllegalStateException("Unexpected value: " + value);
        }
    }

    private void generateHeaderRow(Row header, String name, int index) {
        Cell headerCell = header.createCell(index);
        headerCell.setCellValue(name);
    }
}
