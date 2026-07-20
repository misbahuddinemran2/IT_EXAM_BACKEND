package com.examplatform.modules.ictchatbot.repository;

import com.examplatform.modules.ictchatbot.entity.IctSynonym;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IctSynonymRepository extends JpaRepository<IctSynonym, UUID> {
    List<IctSynonym> findByIsActiveTrue();
}
