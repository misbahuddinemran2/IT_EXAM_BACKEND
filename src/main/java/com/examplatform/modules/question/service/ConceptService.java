package com.examplatform.modules.question.service;

import com.examplatform.common.exception.DuplicateResourceException;
import com.examplatform.common.exception.ResourceNotFoundException;
import com.examplatform.modules.question.dto.request.ConceptCreateRequest;
import com.examplatform.modules.question.dto.response.ConceptResponse;
import com.examplatform.modules.question.entity.Concept;
import com.examplatform.modules.question.repository.ConceptRepository;
import com.examplatform.modules.taxonomy.entity.Topic;
import com.examplatform.modules.taxonomy.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConceptService {

    private final ConceptRepository conceptRepository;
    private final TopicRepository topicRepository;

    public List<ConceptResponse> getConceptsByTopic(String topicId) {
        return conceptRepository
                .findAllByTopicIdAndIsActiveOrderByName(topicId, true)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ConceptResponse> searchConcepts(String search) {
        return conceptRepository
                .searchByName(search)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ConceptResponse createConcept(ConceptCreateRequest request,
                                          String createdBy) {
        Topic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Topic", request.getTopicId()));

        if (conceptRepository.existsByTopicIdAndNameIgnoreCase(
                request.getTopicId(), request.getName())) {
            throw new DuplicateResourceException(
                "Concept already exists: " + request.getName()
            );
        }

        Concept.ConceptType type;
        try {
            type = Concept.ConceptType.valueOf(
                request.getConceptType().toUpperCase());
        } catch (Exception e) {
            type = Concept.ConceptType.DEFINITION;
        }

        Concept parent = null;
        if (request.getParentConceptId() != null) {
            parent = conceptRepository
                    .findById(request.getParentConceptId())
                    .orElse(null);
        }

        Concept concept = Concept.builder()
                .topic(topic)
                .parentConcept(parent)
                .name(request.getName())
                .nameBn(request.getNameBn())
                .description(request.getDescription())
                .conceptType(type)
                .difficultyLevel(request.getDifficultyLevel())
                .importanceScore(request.getImportanceScore())
                .createdBy(createdBy)
                .build();

        return toResponse(conceptRepository.save(concept));
    }

    @Transactional
    public void deleteConcept(String id) {
        Concept concept = conceptRepository.findById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException("Concept", id));
        concept.setActive(false);
        conceptRepository.save(concept);
    }

    public ConceptResponse toResponse(Concept c) {
        return ConceptResponse.builder()
                .id(c.getId())
                .topicId(c.getTopic().getId())
                .topicName(c.getTopic().getName())
                .parentConceptId(c.getParentConcept() != null ?
                    c.getParentConcept().getId() : null)
                .parentConceptName(c.getParentConcept() != null ?
                    c.getParentConcept().getName() : null)
                .name(c.getName())
                .nameBn(c.getNameBn())
                .description(c.getDescription())
                .conceptType(c.getConceptType().name())
                .difficultyLevel(c.getDifficultyLevel())
                .importanceScore(c.getImportanceScore())
                .isActive(c.isActive())
                .build();
    }

    public Concept findById(String id) {
        return conceptRepository.findById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException("Concept", id));
    }
}