package com.examplatform.modules.guide.service;

import com.examplatform.common.exception.ResourceNotFoundException;
import com.examplatform.modules.guide.entity.GuideContent;
import com.examplatform.modules.guide.repository.GuideContentRepository;
import com.examplatform.modules.question.entity.Question;
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
     * No new table; simple topic filter on existing repository.
     */
    public List<Question> getMcqForTopic(String topicId) {
        return questionRepository.findWithFilters(
                Question.QuestionStatus.APPROVED,
                null,
                null,
                topicId,
                null,
                PageRequest.of(0, 50)
        ).getContent();
    }

    /**
     * Practice Options screen — Previous Year MCQ (board only) for the topic.
     */
    public List<Question> getBoardMcqForTopic(String topicId) {
        return questionRepository.findByTopicIdAndStatusAndIsBoardQuestionTrue(
                topicId, Question.QuestionStatus.APPROVED);
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
}
