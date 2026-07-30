package com.examplatform.modules.written.questionbank.controller;

import com.examplatform.modules.written.questionbank.request.AttachToExamRequest;
import com.examplatform.modules.written.questionbank.request.CreateBankQuestionRequest;
import com.examplatform.modules.written.questionbank.response.BankQuestionResponse;
import com.examplatform.modules.written.questionbank.service.WrittenQuestionBankService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/written/question-bank")
@RequiredArgsConstructor
public class AdminWrittenQuestionBankController {

    private final WrittenQuestionBankService bankService;

    @PostMapping
    public BankQuestionResponse create(@RequestBody CreateBankQuestionRequest request) {
        return bankService.createBankQuestion(request);
    }

    @GetMapping
    public List<BankQuestionResponse> getAll() {
        return bankService.getAll();
    }

    @GetMapping("/subject/{subjectId}")
    public List<BankQuestionResponse> getBySubject(@PathVariable String subjectId) {
        return bankService.getBySubject(subjectId);
    }

    @GetMapping("/subject/{subjectId}/chapter/{chapterId}")
    public List<BankQuestionResponse> getBySubjectAndChapter(
            @PathVariable String subjectId, @PathVariable String chapterId) {
        return bankService.getBySubjectAndChapter(subjectId, chapterId);
    }

    @GetMapping("/{id}")
    public BankQuestionResponse getById(@PathVariable String id) {
        return bankService.getById(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        bankService.deleteBankQuestion(id);
    }

    /**
     * Bank থেকে একাধিক প্রশ্ন select করে exam এ attach করে
     */
    @PostMapping("/attach-to-exam")
    public List<String> attachToExam(@RequestBody AttachToExamRequest request) {
        return bankService.attachToExam(request);
    }
}
