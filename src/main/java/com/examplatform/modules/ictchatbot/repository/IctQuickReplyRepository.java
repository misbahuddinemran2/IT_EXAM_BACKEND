package com.examplatform.modules.ictchatbot.repository;

import com.examplatform.modules.ictchatbot.entity.IctQuickReply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IctQuickReplyRepository extends JpaRepository<IctQuickReply, String> {

    List<IctQuickReply> findByIsActiveTrue();

    List<IctQuickReply> findAllByOrderByCreatedAtDesc();
}
