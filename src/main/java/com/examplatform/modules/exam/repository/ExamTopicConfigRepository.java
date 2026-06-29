package com.examplatform.modules.exam.repository;

import com.examplatform.modules.exam.entity.ExamTopicConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamTopicConfigRepository extends JpaRepository<ExamTopicConfig, String> {

    // Exam এর সব topic config
    List<ExamTopicConfig> findByExamId(String examId);

    // Subject অনুযায়ী topic config
    List<ExamTopicConfig> findByExamIdAndSubjectId(String examId, String subjectId);

    // Chapter অনুযায়ী
    List<ExamTopicConfig> findByExamIdAndChapterId(String examId, String chapterId);

    // Specific topic
    List<ExamTopicConfig> findByExamIdAndTopicId(String examId, String topicId);

    // Exam delete হলে সব config delete
    void deleteByExamId(String examId);
}