package com.examplatform.modules.exam.repository;

import com.examplatform.modules.exam.entity.ExamSubjectConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamSubjectConfigRepository extends JpaRepository<ExamSubjectConfig, String> {

    // Exam এর সব subject config
    List<ExamSubjectConfig> findByExamId(String examId);

    // Specific subject config
    List<ExamSubjectConfig> findByExamIdAndSubjectId(String examId, String subjectId);

    // Exam delete হলে সব config delete
    void deleteByExamId(String examId);

    // Exam এ কতগুলো subject আছে
    long countByExamId(String examId);
}