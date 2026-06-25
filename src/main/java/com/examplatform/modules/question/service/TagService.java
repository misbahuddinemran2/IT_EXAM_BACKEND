package com.examplatform.modules.question.service;

import com.examplatform.common.exception.DuplicateResourceException;
import com.examplatform.common.exception.ResourceNotFoundException;
import com.examplatform.modules.question.dto.request.TagCreateRequest;
import com.examplatform.modules.question.dto.response.TagResponse;
import com.examplatform.modules.question.entity.Tag;
import com.examplatform.modules.question.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public List<TagResponse> getAllTags() {
        return tagRepository
                .findAllByOrderByUsageCountDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TagResponse createTag(TagCreateRequest request) {
        if (tagRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException(
                "Tag already exists: " + request.getName()
            );
        }

        Tag.TagType type;
        try {
            type = Tag.TagType.valueOf(
                request.getTagType().toUpperCase());
        } catch (Exception e) {
            type = Tag.TagType.CUSTOM;
        }

        Tag tag = Tag.builder()
                .name(request.getName().toLowerCase())
                .tagType(type)
                .colorCode(request.getColorCode())
                .build();

        return toResponse(tagRepository.save(tag));
    }

    @Transactional
    public void deleteTag(String id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException("Tag", id));
        tagRepository.delete(tag);
    }

    public TagResponse toResponse(Tag t) {
        return TagResponse.builder()
                .id(t.getId())
                .name(t.getName())
                .tagType(t.getTagType().name())
                .colorCode(t.getColorCode())
                .usageCount(t.getUsageCount())
                .build();
    }

    public Tag findById(String id) {
        return tagRepository.findById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException("Tag", id));
    }
}