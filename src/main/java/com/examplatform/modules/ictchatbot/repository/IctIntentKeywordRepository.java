package com.examplatform.modules.ictchatbot.repository;

import com.examplatform.modules.ictchatbot.entity.IctIntentKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IctIntentKeywordRepository extends JpaRepository<IctIntentKeyword, String> {

    List<IctIntentKeyword> findByIsActiveTrue();

    List<IctIntentKeyword> findAllByOrderByIntentAscCreatedAtDesc();
}
