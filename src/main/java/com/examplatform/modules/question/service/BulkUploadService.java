package com.examplatform.modules.question.service;

import com.examplatform.modules.question.dto.request.*;
import com.examplatform.modules.question.dto.response.BulkUploadResultResponse;
import com.examplatform.modules.question.entity.BulkUploadJob;
import com.examplatform.modules.question.repository.BulkUploadJobRepository;
import com.examplatform.modules.taxonomy.repository.ChapterRepository;
import com.examplatform.modules.taxonomy.repository.SubjectRepository;
import com.examplatform.modules.taxonomy.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkUploadService {

    private final BulkUploadJobRepository bulkUploadJobRepository;
    private final QuestionService questionService;
    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final TopicRepository topicRepository;

    public BulkUploadResultResponse uploadAndImport(
            MultipartFile file,
            String subjectId,
            String chapterId,
            String topicId)throws IOException {

        // 1. Create job
        BulkUploadJob job = BulkUploadJob.builder()
                .uploadedBy("system")
                .fileName(file.getOriginalFilename())
                .fileSizeKb((int) (file.getSize() / 1024))
                .startedAt(LocalDateTime.now())
                .status(BulkUploadJob.JobStatus.VALIDATING)
                .build();
        job = bulkUploadJobRepository.save(job);

        // 2. Parse Excel
        List<BulkUploadRow> rows = parseExcel(file);
        log.info("Total parsed rows: {}", rows.size());
        job.setTotalRows(rows.size());

        // 3. Validate
        List<BulkUploadRow> validRows = new ArrayList<>();
        List<BulkUploadRow> failedRows = new ArrayList<>();

        for (BulkUploadRow row : rows) {
            validateRow(row);
            if (row.isValid()) {
                validRows.add(row);
            } else {
                failedRows.add(row);
                log.warn("Row {} validation failed: {}",
                        row.getRowNumber(), row.getErrorMessage());
            }
        }

        job.setValidRows(validRows.size());
        job.setFailedRows(failedRows.size());
        job.setStatus(BulkUploadJob.JobStatus.IMPORTING);
        bulkUploadJobRepository.save(job);

        // 4. Import valid rows
        int imported = 0;
        for (BulkUploadRow row : validRows) {
            try {
                importRow(
                        row,
                        subjectId,
                        chapterId,
                        topicId
                );
                imported++;
                log.info("Row {} imported successfully",
                        row.getRowNumber());
            } catch (Exception e) {
                log.warn("Row {} import failed: {}",
                        row.getRowNumber(), e.getMessage());
                row.setValid(false);
                row.setErrorMessage(e.getMessage() != null ?
                        e.getMessage() :
                        "Unknown error: " +
                        e.getClass().getSimpleName());
                failedRows.add(row);
            }
        }

        // 5. Complete
        job.setImportedRows(imported);
        job.setStatus(BulkUploadJob.JobStatus.COMPLETED);
        job.setCompletedAt(LocalDateTime.now());
        bulkUploadJobRepository.save(job);

        // 6. Build result
        List<BulkUploadResultResponse.RowError> errors =
                failedRows.stream()
                        .map(r -> BulkUploadResultResponse
                                .RowError.builder()
                                .rowNumber(r.getRowNumber())
                                .errorMessage(r.getErrorMessage())
                                .build())
                        .toList();

        return BulkUploadResultResponse.builder()
                .jobId(job.getId())
                .fileName(job.getFileName())
                .totalRows(job.getTotalRows())
                .validRows(job.getValidRows())
                .failedRows(job.getFailedRows())
                .importedRows(imported)
                .status(job.getStatus().name())
                .errors(errors)
                .build();
    }

    private List<BulkUploadRow> parseExcel(
            MultipartFile file) throws IOException {

        List<BulkUploadRow> rows = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(
                file.getInputStream())) {

            Sheet sheet = workbook.getSheetAt(0);
            int rowNum = 0;

            for (Row row : sheet) {
                if (rowNum == 0) {
                    rowNum++;
                    continue;
                }

                if (isRowEmpty(row)) {
                    rowNum++;
                    continue;
                }

                BulkUploadRow uploadRow = BulkUploadRow.builder()
                        .rowNumber(rowNum + 1)
                        .questionText(getCellValue(row, 0))
                        .questionType(getCellValue(row, 1))
                        .difficultyLevel(getCellValue(row, 2))
                        .cognitiveLevel(getCellValue(row, 3))
                        .optionA(getCellValue(row, 4))
                        .optionB(getCellValue(row, 5))
                        .optionC(getCellValue(row, 6))
                        .optionD(getCellValue(row, 7))
                        .correctOption(getCellValue(row, 8))
                        .explanationA(getCellValue(row, 9))
                        .explanationB(getCellValue(row, 10))
                        .explanationC(getCellValue(row, 11))
                        .explanationD(getCellValue(row, 12))
                        .sourceReference(getCellValue(row, 13))
                        .yearAppeared(getCellValue(row, 14))
                        .build();

                log.info("Parsed Row {}: q='{}' optA='{}' correct='{}'",
                        rowNum + 1,
                        uploadRow.getQuestionText(),
                        uploadRow.getOptionA(),
                        uploadRow.getCorrectOption());

                rows.add(uploadRow);
                rowNum++;
            }
        }

        log.info("Total rows parsed: {}", rows.size());
        return rows;
    }

    private void validateRow(BulkUploadRow row) {
        List<String> errors = new ArrayList<>();

        if (isEmpty(row.getQuestionText())) {
            errors.add("Question text is required");
        }
        if (isEmpty(row.getOptionA())) {
            errors.add("Option A is required");
        }
        if (isEmpty(row.getOptionB())) {
            errors.add("Option B is required");
        }
        if (isEmpty(row.getCorrectOption())) {
            errors.add("Correct option is required");
        }
        if (!isEmpty(row.getCorrectOption()) &&
                !row.getCorrectOption().trim()
                     .matches("^[A-D]$")) {
            errors.add("Correct option must be A, B, C or D");
        }

        if (!errors.isEmpty()) {
            row.setValid(false);
            row.setErrorMessage(String.join("; ", errors));
        }
    }

    private void importRow(
            BulkUploadRow row,
            String subjectId,
            String chapterId,
            String topicId
    ) {
        // Subject
        var subject = subjectRepository
                .findById(subjectId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Subject not found"));

        var chapter = chapterRepository
                .findById(chapterId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Chapter not found"));

        var topic = topicRepository
                .findById(topicId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Topic not found"));

        // Options
        List<OptionRequest> options = new ArrayList<>();
        String correct = row.getCorrectOption()
                .toUpperCase().trim();

        if (!isEmpty(row.getOptionA())) {
            options.add(buildOption("A", row.getOptionA(),
                correct.equals("A"), row.getExplanationA()));
        }
        if (!isEmpty(row.getOptionB())) {
            options.add(buildOption("B", row.getOptionB(),
                correct.equals("B"), row.getExplanationB()));
        }
        if (!isEmpty(row.getOptionC())) {
            options.add(buildOption("C", row.getOptionC(),
                correct.equals("C"), row.getExplanationC()));
        }
        if (!isEmpty(row.getOptionD())) {
            options.add(buildOption("D", row.getOptionD(),
                correct.equals("D"), row.getExplanationD()));
        }

        // Difficulty
        int difficulty = 3;
        try {
            difficulty = Integer.parseInt(
                row.getDifficultyLevel().trim());
        } catch (Exception ignored) {}

        // Year
        Integer year = null;
        try {
            if (!isEmpty(row.getYearAppeared())) {
                year = Integer.parseInt(
                    row.getYearAppeared().trim());
            }
        } catch (Exception ignored) {}

        // Build request
        QuestionCreateRequest request = new QuestionCreateRequest();
        request.setQuestionText(row.getQuestionText().trim());
        request.setSubjectId(subject.getId());
        request.setChapterId(chapter.getId());
        request.setTopicId(topic.getId());
        request.setDifficultyLevel(difficulty);
        request.setCognitiveLevel(
            isEmpty(row.getCognitiveLevel()) ?
                "REMEMBER" : row.getCognitiveLevel().trim());
        request.setQuestionType(
            isEmpty(row.getQuestionType()) ?
                "MCQ_SINGLE" : row.getQuestionType().trim());
        request.setSourceReference(row.getSourceReference());
        request.setYearAppeared(year);
        request.setOptions(options);

        questionService.createQuestion(request);
    }

    private OptionRequest buildOption(String key, String text,
            boolean correct, String explanation) {
        OptionRequest opt = new OptionRequest();
        opt.setOptionKey(key);
        opt.setOptionText(text);
        opt.setCorrect(correct);
        opt.setExplanation(explanation);
        return opt;
    }

    private String getCellValue(Row row, int index) {
        try {
            Cell cell = row.getCell(index);
            if (cell == null) return "";
            return switch (cell.getCellType()) {
                case STRING ->
                    cell.getStringCellValue().trim();
                case NUMERIC ->
                    String.valueOf(
                        (long) cell.getNumericCellValue());
                case BOOLEAN ->
                    String.valueOf(cell.getBooleanCellValue());
                case FORMULA -> {
                    try {
                        yield cell.getStringCellValue().trim();
                    } catch (Exception e) {
                        yield String.valueOf(
                            (long) cell.getNumericCellValue());
                    }
                }
                default -> "";
            };
        } catch (Exception e) {
            log.warn("Cell read error col {}: {}",
                    index, e.getMessage());
            return "";
        }
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        Cell cell = row.getCell(0);
        if (cell == null) return true;
        if (cell.getCellType() == CellType.BLANK) return true;
        return getCellValue(row, 0).isEmpty();
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}