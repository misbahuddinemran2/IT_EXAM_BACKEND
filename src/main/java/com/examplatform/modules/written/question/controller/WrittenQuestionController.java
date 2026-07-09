package com.examplatform.modules.written.question.controller;

import com.examplatform.modules.written.question.response.QuestionStudentResponse;
import com.examplatform.modules.written.question.service.WrittenQuestionService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/written/questions")
@RequiredArgsConstructor
public class WrittenQuestionController {

    private final WrittenQuestionService questionService;

    @GetMapping("/exam/{examId}")
    public List<QuestionStudentResponse> getQuestionsForExam(@PathVariable String examId) {
        return questionService.getQuestionsForStudent(examId);
    }
}