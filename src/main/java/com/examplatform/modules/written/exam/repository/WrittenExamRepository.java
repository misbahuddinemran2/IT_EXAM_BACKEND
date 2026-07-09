package com.examplatform.modules.written.exam.repository;

import com.examplatform.modules.written.exam.entity.WrittenExam;
import com.examplatform.modules.written.exam.enums.ExamStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WrittenExamRepository extends JpaRepository<WrittenExam, String> {

    List<WrittenExam> findByStatus(ExamStatus status);

    List<WrittenExam> findByEducationLevelAndStatus(String educationLevel, ExamStatus status);

    List<WrittenExam> findByStatusAndEndTimeBefore(ExamStatus status, LocalDateTime time);

    Optional<WrittenExam> findByIdAndStatus(String id, ExamStatus status);
}