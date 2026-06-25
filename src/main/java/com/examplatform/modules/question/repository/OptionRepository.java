package com.examplatform.modules.question.repository;

import com.examplatform.modules.question.entity.Option;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OptionRepository
        extends JpaRepository<Option, String> {

    List<Option> findAllByQuestionIdOrderByOrderIndex(
            String questionId);

    void deleteAllByQuestionId(String questionId);
}