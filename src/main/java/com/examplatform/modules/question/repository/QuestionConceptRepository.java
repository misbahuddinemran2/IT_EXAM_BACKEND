package com.examplatform.modules.question.repository;

import com.examplatform.modules.question.entity.QuestionConcept;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionConceptRepository
        extends JpaRepository<QuestionConcept, String> {

    List<QuestionConcept> findAllByQuestionId(String questionId);

    void deleteAllByQuestionId(String questionId);
}