package com.examplatform.modules.written.question.service;

import com.examplatform.modules.written.question.request.CreateQuestionRequest;
import com.examplatform.modules.written.question.request.ReorderQuestionsRequest;
import com.examplatform.modules.written.question.request.UpdateQuestionRequest;
import com.examplatform.modules.written.question.response.QuestionAdminResponse;
import com.examplatform.modules.written.question.response.QuestionStudentResponse;

import java.util.List;

public interface WrittenQuestionService {

    QuestionAdminResponse createQuestion(CreateQuestionRequest request);

    QuestionAdminResponse updateQuestion(String questionId, UpdateQuestionRequest request);

    void reorderQuestions(String examId, ReorderQuestionsRequest request);

    void deleteQuestion(String questionId);

    List<QuestionAdminResponse> getQuestionsForAdmin(String examId);

    List<QuestionStudentResponse> getQuestionsForStudent(String examId);

    QuestionAdminResponse getQuestionByIdForAdmin(String questionId);
}