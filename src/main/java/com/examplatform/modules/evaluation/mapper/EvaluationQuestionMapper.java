package com.examplatform.modules.evaluation.mapper;

import com.examplatform.modules.evaluation.dto.QuestionCreateRequest;
import com.examplatform.modules.evaluation.dto.QuestionResponse;
import com.examplatform.modules.evaluation.dto.QuestionUpdateRequest;
import com.examplatform.modules.evaluation.entity.EvaluationDataset;
import com.examplatform.modules.evaluation.entity.EvaluationQuestion;
import com.examplatform.modules.evaluation.enums.EvaluationQuestionType;
import com.examplatform.modules.evaluation.enums.QuestionDifficulty;
import com.examplatform.modules.ictchatbot.entity.IctBookChunk;
import com.examplatform.modules.ictchatbot.repository.IctBookChunkRepository;
import com.examplatform.modules.taxonomy.entity.Chapter;
import com.examplatform.modules.taxonomy.entity.Subject;
import com.examplatform.modules.taxonomy.entity.Topic;
import com.examplatform.modules.taxonomy.repository.ChapterRepository;
import com.examplatform.modules.taxonomy.repository.SubjectRepository;
import com.examplatform.modules.taxonomy.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Component
@RequiredArgsConstructor
public class EvaluationQuestionMapper {

    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final TopicRepository topicRepository;
    private final IctBookChunkRepository ictBookChunkRepository;

    public EvaluationQuestion toEntity(QuestionCreateRequest req, EvaluationDataset dataset) {
        return EvaluationQuestion.builder()
                .dataset(dataset)
                .questionText(req.getQuestionText())
                .expectedAnswer(req.getExpectedAnswer())
                .expectedWriterNames(req.getExpectedWriterNames())
                .difficulty(req.getDifficulty() != null ? QuestionDifficulty.valueOf(req.getDifficulty()) : null)
                .questionType(req.getQuestionType() != null ? EvaluationQuestionType.valueOf(req.getQuestionType()) : null)
                .referenceBook(req.getReferenceBook())
                .referencePage(req.getReferencePage())
                .referenceChunk(findChunk(req.getReferenceChunkId()))
                .subject(findSubject(req.getSubjectId()))
                .chapter(findChapter(req.getChapterId()))
                .topic(findTopic(req.getTopicId()))
                .isActive(true)
                .build();
    }

    public void applyUpdate(EvaluationQuestion question, QuestionUpdateRequest req) {
        if (req.getQuestionText() != null) question.setQuestionText(req.getQuestionText());
        if (req.getExpectedAnswer() != null) question.setExpectedAnswer(req.getExpectedAnswer());
        if (req.getExpectedWriterNames() != null) question.setExpectedWriterNames(req.getExpectedWriterNames());
        if (req.getDifficulty() != null) question.setDifficulty(QuestionDifficulty.valueOf(req.getDifficulty()));
        if (req.getQuestionType() != null) question.setQuestionType(EvaluationQuestionType.valueOf(req.getQuestionType()));
        if (req.getReferenceBook() != null) question.setReferenceBook(req.getReferenceBook());
        if (req.getReferencePage() != null) question.setReferencePage(req.getReferencePage());
        if (req.getReferenceChunkId() != null) question.setReferenceChunk(findChunk(req.getReferenceChunkId()));
        if (req.getSubjectId() != null) question.setSubject(findSubject(req.getSubjectId()));
        if (req.getChapterId() != null) question.setChapter(findChapter(req.getChapterId()));
        if (req.getTopicId() != null) {
            question.setTopic(req.getTopicId().isBlank() ? null : findTopic(req.getTopicId()));
        }
        if (req.getIsActive() != null) question.setActive(req.getIsActive());
    }

    public QuestionResponse toResponse(EvaluationQuestion q) {
        return QuestionResponse.builder()
                .id(q.getId())
                .datasetId(q.getDataset().getId())
                .questionText(q.getQuestionText())
                .expectedAnswer(q.getExpectedAnswer())
                .expectedWriterNames(q.getExpectedWriterNames())
                .difficulty(q.getDifficulty() != null ? q.getDifficulty().name() : null)
                .questionType(q.getQuestionType() != null ? q.getQuestionType().name() : null)
                .referenceBook(q.getReferenceBook())
                .referencePage(q.getReferencePage())
                .referenceChunkId(q.getReferenceChunk() != null ? q.getReferenceChunk().getId() : null)
                .subjectId(q.getSubject() != null ? q.getSubject().getId() : null)
                .subjectName(q.getSubject() != null ? q.getSubject().getName() : null)
                .chapterId(q.getChapter() != null ? q.getChapter().getId() : null)
                .chapterName(q.getChapter() != null ? q.getChapter().getName() : null)
                .topicId(q.getTopic() != null ? q.getTopic().getId() : null)
                .topicName(q.getTopic() != null ? q.getTopic().getName() : null)
                .isActive(q.isActive())
                .createdAt(q.getCreatedAt())
                .updatedAt(q.getUpdatedAt())
                .build();
    }

    private Subject findSubject(String id) {
        if (id == null || id.isBlank()) return null;
        return subjectRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Subject not found: " + id));
    }

    private Chapter findChapter(String id) {
        if (id == null || id.isBlank()) return null;
        return chapterRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Chapter not found: " + id));
    }

    private Topic findTopic(String id) {
        if (id == null || id.isBlank()) return null;
        return topicRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Topic not found: " + id));
    }

    private IctBookChunk findChunk(String id) {
        if (id == null || id.isBlank()) return null;
        return ictBookChunkRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Book chunk not found: " + id));
    }
}
