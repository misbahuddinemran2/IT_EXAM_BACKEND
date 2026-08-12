package com.examplatform.modules.guide.service;

import com.examplatform.common.exception.ResourceNotFoundException;
import com.examplatform.modules.guide.entity.GuideContent;
import com.examplatform.modules.guide.repository.GuideContentRepository;
import com.examplatform.modules.question.dto.response.OptionResponse;
import com.examplatform.modules.question.dto.response.QuestionResponse;
import com.examplatform.modules.question.entity.Option;
import com.examplatform.modules.question.entity.Question;
import com.examplatform.modules.question.repository.OptionRepository;
import com.examplatform.modules.question.repository.QuestionRepository;
import com.examplatform.modules.written.question.entity.WrittenQuestion;
import com.examplatform.modules.written.question.repository.WrittenQuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuideContentStudentService {

    private final GuideContentRepository guideContentRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final WrittenQuestionRepository writtenQuestionRepository;

    /**
     * Fetch published guide content for a topic. Read screen entry point.
     */
    @Transactional
    public GuideContent getPublishedContent(String topicId) {
        GuideContent content = guideContentRepository
                .findByTopicIdAndStatus(topicId, GuideContent.GuideStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No published guide content for topic: " + topicId));
        return content;
    }

    /**
     * Practice Options screen — MCQ list, delegates to existing Question module.
     * Includes options (with isCorrect) since this is untimed practice, not
     * a graded exam session.
     */
    @Transactional
    public List<QuestionResponse> getMcqForTopic(String topicId) {
        List<Question> questions = questionRepository.findWithFilters(
                Question.QuestionStatus.APPROVED,
                null,
                null,
                topicId,
                null,
                PageRequest.of(0, 50)
        ).getContent();
        return questions.stream().map(this::toQuestionResponse).toList();
    }

    /**
     * Practice Options screen — Previous Year MCQ (board only) for the topic.
     */
    @Transactional
    public List<QuestionResponse> getBoardMcqForTopic(String topicId) {
        List<Question> questions = questionRepository.findByTopicIdAndStatusAndIsBoardQuestionTrue(
                topicId, Question.QuestionStatus.APPROVED);
        return questions.stream().map(this::toQuestionResponse).toList();
    }

    /**
     * Practice Options screen — Board question list, delegates to Written module.
     */
    public List<WrittenQuestion> getBoardQuestionsForTopic(String topicId) {
        return writtenQuestionRepository.findByTopicIdAndIsBoardQuestionTrue(topicId);
    }

    /**
     * Practice Options screen — all CQ (board + non-board) for the topic.
     */
    public List<WrittenQuestion> getCqForTopic(String topicId) {
        return writtenQuestionRepository.findByTopicId(topicId);
    }

    // ---- lightweight mapper: Question + Options -> QuestionResponse ----
    // (mirrors QuestionService#toResponse but skips concepts/tags/examTypes,
    // which the practice card UI doesn't need)
    private QuestionResponse toQuestionResponse(Question q) {
        List<Option> options = optionRepository.findAllByQuestionIdOrderByOrderIndex(q.getId());
        List<OptionResponse> optionResponses = options.stream()
                .map(o -> OptionResponse.builder()
                        .id(o.getId())
                        .optionKey(o.getOptionKey())
                        .optionText(o.getOptionText())
                        .optionTextBn(o.getOptionTextBn())
                        .isCorrect(o.isCorrect())
                        .explanation(o.getExplanation())
                        .orderIndex(o.getOrderIndex())
                        .build())
                .toList();

        return QuestionResponse.builder()
                .id(q.getId())
                .questionText(q.getQuestionText())
                .questionTextBn(q.getQuestionTextBn())
                .questionType(q.getQuestionType().name())
                .language(q.getLanguage().name())
                .subjectId(q.getSubject().getId())
                .subjectName(q.getSubject().getName())
                .chapterId(q.getChapter().getId())
                .chapterName(q.getChapter().getName())
                .topicId(q.getTopic().getId())
                .topicName(q.getTopic().getName())
                .difficultyLevel(q.getDifficultyLevel())
                .cognitiveLevel(q.getCognitiveLevel().name())
                .estimatedTimeSec(q.getEstimatedTimeSec())
                .sourceReference(q.getSourceReference())
                .yearAppeared(q.getYearAppeared())
                .isBoardQuestion(q.isBoardQuestion())
                .board(q.getBoard())
                .status(q.getStatus().name())
                .options(optionResponses)
                .createdAt(q.getCreatedAt().toString())
                .build();
    }
}
