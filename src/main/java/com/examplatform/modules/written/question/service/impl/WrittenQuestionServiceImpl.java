package com.examplatform.modules.written.question.service.impl;

import com.examplatform.modules.written.exam.entity.WrittenExam;
import com.examplatform.modules.written.exam.repository.WrittenExamRepository;
import com.examplatform.modules.written.question.entity.WrittenQuestion;
import com.examplatform.modules.written.question.mapper.WrittenQuestionMapper;
import com.examplatform.modules.written.question.repository.WrittenQuestionRepository;
import com.examplatform.modules.written.question.request.CreateQuestionRequest;
import com.examplatform.modules.written.question.request.ReorderQuestionsRequest;
import com.examplatform.modules.written.question.request.UpdateQuestionRequest;
import com.examplatform.modules.written.question.response.QuestionAdminResponse;
import com.examplatform.modules.written.question.response.QuestionStudentResponse;
import com.examplatform.modules.written.question.service.WrittenQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class WrittenQuestionServiceImpl implements WrittenQuestionService {

    private final WrittenQuestionRepository questionRepository;
    private final WrittenQuestionMapper questionMapper;
    private final WrittenExamRepository examRepository;

    @Override
    @Transactional
    public QuestionAdminResponse createQuestion(CreateQuestionRequest request) {
        WrittenExam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new NoSuchElementException("Exam not found: " + request.getExamId()));

        WrittenQuestion question = questionMapper.toEntity(request);
        WrittenQuestion saved = questionRepository.save(question);

        recalculateExamTotalMarks(exam.getId());

        return questionMapper.toAdminResponse(saved);
    }

    @Override
    @Transactional
    public QuestionAdminResponse updateQuestion(String questionId, UpdateQuestionRequest request) {
        WrittenQuestion question = getQuestionOrThrow(questionId);
        questionMapper.applyUpdate(question, request);
        WrittenQuestion updated = questionRepository.save(question);

        recalculateExamTotalMarks(question.getExamId());

        return questionMapper.toAdminResponse(updated);
    }

    @Override
    @Transactional
    public void reorderQuestions(String examId, ReorderQuestionsRequest request) {
        List<WrittenQuestion> questions = questionRepository.findByExamIdOrderByQuestionOrderAsc(examId);

        request.getItems().forEach(item -> {
            questions.stream()
                    .filter(q -> q.getId().equals(item.getQuestionId()))
                    .findFirst()
                    .ifPresent(q -> q.setQuestionOrder(item.getNewOrder()));
        });

        questionRepository.saveAll(questions);
    }

    @Override
    @Transactional
    public void deleteQuestion(String questionId) {
        WrittenQuestion question = getQuestionOrThrow(questionId);
        String examId = question.getExamId();
        questionRepository.delete(question);

        recalculateExamTotalMarks(examId);
    }

    @Override
    public List<QuestionAdminResponse> getQuestionsForAdmin(String examId) {
        return questionRepository.findByExamIdOrderByQuestionOrderAsc(examId).stream()
                .map(questionMapper::toAdminResponse)
                .toList();
    }

    @Override
    public List<QuestionStudentResponse> getQuestionsForStudent(String examId) {
        return questionRepository.findByExamIdOrderByQuestionOrderAsc(examId).stream()
                .map(questionMapper::toStudentResponse)
                .toList();
    }

    @Override
    public QuestionAdminResponse getQuestionByIdForAdmin(String questionId) {
        return questionMapper.toAdminResponse(getQuestionOrThrow(questionId));
    }

    private WrittenQuestion getQuestionOrThrow(String questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new NoSuchElementException("Question not found: " + questionId));
    }

    /**
     * Keeps written_exam.total_marks in sync with the sum of all its questions' max marks.
     */
    private void recalculateExamTotalMarks(String examId) {
        List<WrittenQuestion> questions = questionRepository.findByExamIdOrderByQuestionOrderAsc(examId);

        BigDecimal total = questions.stream()
                .map(WrittenQuestion::getTotalMaxMark)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        examRepository.findById(examId).ifPresent(exam -> {
            exam.setTotalMarks(total.intValue());
            examRepository.save(exam);
        });
    }
}