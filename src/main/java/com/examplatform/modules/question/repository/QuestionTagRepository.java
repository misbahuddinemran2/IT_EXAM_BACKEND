package com.examplatform.modules.question.repository;

import com.examplatform.modules.question.entity.QuestionTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionTagRepository
        extends JpaRepository<QuestionTag,
            QuestionTag.QuestionTagId> {

    List<QuestionTag> findAllByQuestionId(String questionId);

    void deleteAllByQuestionId(String questionId);
}