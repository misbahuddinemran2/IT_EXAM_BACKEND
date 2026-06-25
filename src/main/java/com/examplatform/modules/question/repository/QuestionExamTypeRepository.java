package com.examplatform.modules.question.repository;

import com.examplatform.modules.question.entity.QuestionExamType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionExamTypeRepository
        extends JpaRepository<QuestionExamType,
            QuestionExamType.QuestionExamTypeId> {

    List<QuestionExamType> findAllByQuestionId(String questionId);

    void deleteAllByQuestionId(String questionId);
}