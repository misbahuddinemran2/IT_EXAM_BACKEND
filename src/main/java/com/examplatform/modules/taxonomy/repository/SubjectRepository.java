package com.examplatform.modules.taxonomy.repository;

import com.examplatform.modules.taxonomy.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository
        extends JpaRepository<Subject, String> {

    List<Subject> findAllByIsActiveOrderByCode(boolean isActive);

    Optional<Subject> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, String id);
}