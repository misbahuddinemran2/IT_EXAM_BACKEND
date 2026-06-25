package com.examplatform.modules.taxonomy.service;

import com.examplatform.common.exception.DuplicateResourceException;
import com.examplatform.common.exception.ResourceNotFoundException;
import com.examplatform.modules.taxonomy.dto.request.*;
import com.examplatform.modules.taxonomy.dto.response.*;
import com.examplatform.modules.taxonomy.entity.*;
import com.examplatform.modules.taxonomy.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaxonomyService {

    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final TopicRepository topicRepository;

    // ─── SUBJECT ───────────────────────────────────────

    public List<SubjectResponse> getAllSubjects() {
        return subjectRepository
                .findAllByIsActiveOrderByCode(true)
                .stream()
                .map(this::toSubjectResponse)
                .toList();
    }

    public SubjectResponse getSubject(String id) {
        return toSubjectResponse(findSubject(id));
    }

    @Transactional
    public SubjectResponse createSubject(SubjectRequest request) {
        if (subjectRepository.existsByCode(
                request.getCode().toUpperCase())) {
            throw new DuplicateResourceException(
                "Subject code already exists: " + request.getCode()
            );
        }

        Subject subject = Subject.builder()
                .name(request.getName())
                .nameBn(request.getNameBn())
                .code(request.getCode().toUpperCase())
                .isActive(true)
                .build();

        return toSubjectResponse(subjectRepository.save(subject));
    }

    @Transactional
    public SubjectResponse updateSubject(String id,
                                         SubjectRequest request) {
        Subject subject = findSubject(id);

        if (subjectRepository.existsByCodeAndIdNot(
                request.getCode().toUpperCase(), id)) {
            throw new DuplicateResourceException(
                "Subject code already exists: " + request.getCode()
            );
        }

        subject.setName(request.getName());
        subject.setNameBn(request.getNameBn());
        subject.setCode(request.getCode().toUpperCase());

        return toSubjectResponse(subjectRepository.save(subject));
    }

    @Transactional
    public void deleteSubject(String id) {
        Subject subject = findSubject(id);
        subject.setActive(false);
        subjectRepository.save(subject);
    }

    // ─── CHAPTER ───────────────────────────────────────

    public List<ChapterResponse> getChaptersBySubject(String subjectId) {
        findSubject(subjectId); // existence check
        return chapterRepository
                .findAllBySubjectIdAndIsActiveOrderByOrderIndex(
                    subjectId, true)
                .stream()
                .map(this::toChapterResponse)
                .toList();
    }

    @Transactional
    public ChapterResponse createChapter(ChapterRequest request) {
        Subject subject = findSubject(request.getSubjectId());

        if (chapterRepository.existsBySubjectIdAndNameIgnoreCase(
                request.getSubjectId(), request.getName())) {
            throw new DuplicateResourceException(
                "Chapter already exists: " + request.getName()
            );
        }

        Chapter chapter = Chapter.builder()
                .subject(subject)
                .name(request.getName())
                .nameBn(request.getNameBn())
                .orderIndex(request.getOrderIndex())
                .isActive(true)
                .build();

        return toChapterResponse(chapterRepository.save(chapter));
    }

    @Transactional
    public ChapterResponse updateChapter(String id,
                                          ChapterRequest request) {
        Chapter chapter = findChapter(id);

        if (chapterRepository
                .existsBySubjectIdAndNameIgnoreCaseAndIdNot(
                    chapter.getSubject().getId(),
                    request.getName(), id)) {
            throw new DuplicateResourceException(
                "Chapter already exists: " + request.getName()
            );
        }

        chapter.setName(request.getName());
        chapter.setNameBn(request.getNameBn());
        chapter.setOrderIndex(request.getOrderIndex());

        return toChapterResponse(chapterRepository.save(chapter));
    }

    @Transactional
    public void deleteChapter(String id) {
        Chapter chapter = findChapter(id);
        chapter.setActive(false);
        chapterRepository.save(chapter);
    }

    // ─── TOPIC ─────────────────────────────────────────

    public List<TopicResponse> getTopicsByChapter(String chapterId) {
        findChapter(chapterId); // existence check
        return topicRepository
                .findAllByChapterIdAndIsActiveOrderByOrderIndex(
                    chapterId, true)
                .stream()
                .map(this::toTopicResponse)
                .toList();
    }

    @Transactional
    public TopicResponse createTopic(TopicRequest request) {
        Chapter chapter = findChapter(request.getChapterId());

        if (topicRepository.existsByChapterIdAndNameIgnoreCase(
                request.getChapterId(), request.getName())) {
            throw new DuplicateResourceException(
                "Topic already exists: " + request.getName()
            );
        }

        Topic topic = Topic.builder()
                .chapter(chapter)
                .name(request.getName())
                .nameBn(request.getNameBn())
                .description(request.getDescription())
                .orderIndex(request.getOrderIndex())
                .isActive(true)
                .build();

        return toTopicResponse(topicRepository.save(topic));
    }

    @Transactional
    public TopicResponse updateTopic(String id, TopicRequest request) {
        Topic topic = findTopic(id);

        if (topicRepository
                .existsByChapterIdAndNameIgnoreCaseAndIdNot(
                    topic.getChapter().getId(),
                    request.getName(), id)) {
            throw new DuplicateResourceException(
                "Topic already exists: " + request.getName()
            );
        }

        topic.setName(request.getName());
        topic.setNameBn(request.getNameBn());
        topic.setDescription(request.getDescription());
        topic.setOrderIndex(request.getOrderIndex());

        return toTopicResponse(topicRepository.save(topic));
    }

    @Transactional
    public void deleteTopic(String id) {
        Topic topic = findTopic(id);
        topic.setActive(false);
        topicRepository.save(topic);
    }

    // ─── HIERARCHY ─────────────────────────────────────

    public List<SubjectHierarchyResponse> getFullHierarchy() {
        return subjectRepository
                .findAllByIsActiveOrderByCode(true)
                .stream()
                .map(subject -> {
                    List<SubjectHierarchyResponse.ChapterHierarchy>
                        chapters = chapterRepository
                            .findAllBySubjectIdAndIsActiveOrderByOrderIndex(
                                subject.getId(), true)
                            .stream()
                            .map(chapter -> {
                                List<SubjectHierarchyResponse.TopicItem>
                                    topics = topicRepository
                                        .findAllByChapterIdAndIsActiveOrderByOrderIndex(
                                            chapter.getId(), true)
                                        .stream()
                                        .map(t ->
                                            SubjectHierarchyResponse
                                                .TopicItem.builder()
                                                .id(t.getId())
                                                .name(t.getName())
                                                .nameBn(t.getNameBn())
                                                .orderIndex(t.getOrderIndex())
                                                .build())
                                        .toList();

                                return SubjectHierarchyResponse
                                        .ChapterHierarchy.builder()
                                        .id(chapter.getId())
                                        .name(chapter.getName())
                                        .nameBn(chapter.getNameBn())
                                        .orderIndex(chapter.getOrderIndex())
                                        .topics(topics)
                                        .build();
                            })
                            .toList();

                    return SubjectHierarchyResponse.builder()
                            .id(subject.getId())
                            .name(subject.getName())
                            .nameBn(subject.getNameBn())
                            .code(subject.getCode())
                            .chapters(chapters)
                            .build();
                })
                .toList();
    }

    // ─── PRIVATE HELPERS ───────────────────────────────

    private Subject findSubject(String id) {
        return subjectRepository.findById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException("Subject", id));
    }

    private Chapter findChapter(String id) {
        return chapterRepository.findById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException("Chapter", id));
    }

    private Topic findTopic(String id) {
        return topicRepository.findById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException("Topic", id));
    }

    private SubjectResponse toSubjectResponse(Subject s) {
        return SubjectResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .nameBn(s.getNameBn())
                .code(s.getCode())
                .isActive(s.isActive())
                .createdAt(s.getCreatedAt().toString())
                .build();
    }

    private ChapterResponse toChapterResponse(Chapter c) {
        return ChapterResponse.builder()
                .id(c.getId())
                .subjectId(c.getSubject().getId())
                .subjectName(c.getSubject().getName())
                .name(c.getName())
                .nameBn(c.getNameBn())
                .orderIndex(c.getOrderIndex())
                .isActive(c.isActive())
                .build();
    }

    private TopicResponse toTopicResponse(Topic t) {
        return TopicResponse.builder()
                .id(t.getId())
                .chapterId(t.getChapter().getId())
                .chapterName(t.getChapter().getName())
                .subjectId(t.getChapter().getSubject().getId())
                .subjectName(t.getChapter().getSubject().getName())
                .name(t.getName())
                .nameBn(t.getNameBn())
                .description(t.getDescription())
                .orderIndex(t.getOrderIndex())
                .isActive(t.isActive())
                .build();
    }
}