package com.examplatform.modules.ictchatbot.repository;

import com.examplatform.modules.ictchatbot.entity.IctRewriteKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IctRewriteKeywordRepository extends JpaRepository<IctRewriteKeyword, UUID> {

    List<IctRewriteKeyword> findByIsActiveTrue();

    List<IctRewriteKeyword> findAllByOrderByCreatedAtDesc();
}
