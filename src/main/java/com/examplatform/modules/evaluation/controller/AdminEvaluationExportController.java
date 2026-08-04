package com.examplatform.modules.evaluation.controller;

import com.examplatform.modules.evaluation.report.CsvExportService;
import com.examplatform.modules.evaluation.report.JsonExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/research/runs")
@RequiredArgsConstructor
public class AdminEvaluationExportController {

    private final CsvExportService csvExportService;
    private final JsonExportService jsonExportService;

    @GetMapping("/{id}/export.csv")
    public ResponseEntity<byte[]> exportCsv(@PathVariable String id) {
        byte[] data = csvExportService.generateCsv(id);

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("evaluation-run-" + id + ".csv")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(disposition);
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));

        return ResponseEntity.ok().headers(headers).body(data);
    }

    @GetMapping("/{id}/export.json")
    public ResponseEntity<byte[]> exportJson(@PathVariable String id) {
        byte[] data = jsonExportService.generateJson(id);

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("evaluation-run-" + id + ".json")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(disposition);
        headers.setContentType(MediaType.APPLICATION_JSON);

        return ResponseEntity.ok().headers(headers).body(data);
    }
}
