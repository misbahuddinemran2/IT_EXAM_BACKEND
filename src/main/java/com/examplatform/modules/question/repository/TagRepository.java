package com.examplatform.modules.question.repository;

import com.examplatform.modules.question.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository
        extends JpaRepository<Tag, String> {

    List<Tag> findAllByOrderByUsageCountDesc();

    List<Tag> findAllByTagTypeOrderByName(Tag.TagType tagType);

    Optional<Tag> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}