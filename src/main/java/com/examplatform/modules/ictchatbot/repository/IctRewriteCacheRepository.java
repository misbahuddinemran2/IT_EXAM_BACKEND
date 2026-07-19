package com.examplatform.modules.ictchatbot.repository;

import com.examplatform.modules.ictchatbot.entity.IctRewriteCache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IctRewriteCacheRepository extends JpaRepository<IctRewriteCache, UUID> {

    Optional<IctRewriteCache> findByOriginalAnswerHashAndCategory(
            String originalAnswerHash,
            String category
    );
}
