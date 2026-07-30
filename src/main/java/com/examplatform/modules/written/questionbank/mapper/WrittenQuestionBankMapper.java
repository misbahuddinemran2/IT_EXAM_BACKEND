package com.examplatform.modules.written.questionbank.mapper;

import com.examplatform.modules.taxonomy.entity.Chapter;
import com.examplatform.modules.taxonomy.entity.Subject;
import com.examplatform.modules.taxonomy.entity.Topic;
import com.examplatform.modules.taxonomy.repository.ChapterRepository;
import com.examplatform.modules.taxonomy.repository.SubjectRepository;
import com.examplatform.modules.taxonomy.repository.TopicRepository;
import com.examplatform.modules.written.question.entity.WrittenQuestion;
import com.examplatform.modules.written.questionbank.entity.WrittenQuestionBank;
import com.examplatform.modules.written.questionbank.request.CreateBankQuestionRequest;
import com.examplatform.modules.written.questionbank.response.BankQuestionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Component
@RequiredArgsConstructor
public class WrittenQuestionBankMapper {

    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final TopicRepository topicRepository;

    public WrittenQuestionBank toEntity(CreateBankQuestionRequest req) {
        return WrittenQuestionBank.builder()
                .subject(findSubject(req.getSubjectId()))
                .chapter(findChapter(req.getChapterId()))
                .topic(findTopic(req.getTopicId()))
                .stimulus(req.getStimulus())
                .stimulusBn(req.getStimulusBn())
                .isBoardQuestion(req.isBoardQuestion())
                .board(req.getBoard())
                .examYear(req.getExamYear())
                .partAQuestion(req.getPartAQuestion())
                .partAModelAnswer(req.getPartAModelAnswer())
                .partAMarkingScheme(req.getPartAMarkingScheme())
                .partAMaxMark(req.getPartAMaxMark())
                .partBQuestion(req.getPartBQuestion())
                .partBModelAnswer(req.getPartBModelAnswer())
                .partBMarkingScheme(req.getPartBMarkingScheme())
                .partBMaxMark(req.getPartBMaxMark())
                .partCQuestion(req.getPartCQuestion())
                .partCModelAnswer(req.getPartCModelAnswer())
                .partCMarkingScheme(req.getPartCMarkingScheme())
                .partCMaxMark(req.getPartCMaxMark())
                .partDQuestion(req.getPartDQuestion())
                .partDModelAnswer(req.getPartDModelAnswer())
                .partDMarkingScheme(req.getPartDMarkingScheme())
                .partDMaxMark(req.getPartDMaxMark())
                .build();
    }

    public BankQuestionResponse toResponse(WrittenQuestionBank q) {
        return BankQuestionResponse.builder()
                .id(q.getId())
                .subjectId(q.getSubject().getId())
                .subjectName(q.getSubject().getName())
                .chapterId(q.getChapter().getId())
                .chapterName(q.getChapter().getName())
                .topicId(q.getTopic() != null ? q.getTopic().getId() : null)
                .topicName(q.getTopic() != null ? q.getTopic().getName() : null)
                .stimulus(q.getStimulus())
                .stimulusBn(q.getStimulusBn())
                .isBoardQuestion(q.isBoardQuestion())
                .board(q.getBoard())
                .examYear(q.getExamYear())
                .partAQuestion(q.getPartAQuestion())
                .partAModelAnswer(q.getPartAModelAnswer())
                .partAAiAnswer(q.getPartAAiAnswer())
                .partAMaxMark(q.getPartAMaxMark())
                .partBQuestion(q.getPartBQuestion())
                .partBModelAnswer(q.getPartBModelAnswer())
                .partBAiAnswer(q.getPartBAiAnswer())
                .partBMaxMark(q.getPartBMaxMark())
                .partCQuestion(q.getPartCQuestion())
                .partCModelAnswer(q.getPartCModelAnswer())
                .partCAiAnswer(q.getPartCAiAnswer())
                .partCMaxMark(q.getPartCMaxMark())
                .partDQuestion(q.getPartDQuestion())
                .partDModelAnswer(q.getPartDModelAnswer())
                .partDAiAnswer(q.getPartDAiAnswer())
                .partDMaxMark(q.getPartDMaxMark())
                .totalMaxMark(q.getTotalMaxMark())
                .build();
    }

    /**
     * Bank question কে exam এ attach করার সময় নতুন WrittenQuestion বানায় (copy),
     * bank এর row অপরিবর্তিত থাকে যাতে বারবার reuse করা যায়।
     */
    public WrittenQuestion toWrittenQuestion(WrittenQuestionBank bank, String examId, int questionOrder) {
        return WrittenQuestion.builder()
                .examId(examId)
                .subject(bank.getSubject())
                .chapter(bank.getChapter())
                .topic(bank.getTopic())
                .questionOrder(questionOrder)
                .stimulus(bank.getStimulus())
                .stimulusBn(bank.getStimulusBn())
                .isBoardQuestion(bank.isBoardQuestion())
                .board(bank.getBoard())
                .examYear(bank.getExamYear())
                .partAQuestion(bank.getPartAQuestion())
                .partAModelAnswer(bank.getPartAModelAnswer())
                .partAAiAnswer(bank.getPartAAiAnswer())
                .partAMarkingScheme(bank.getPartAMarkingScheme())
                .partAMaxMark(bank.getPartAMaxMark())
                .partBQuestion(bank.getPartBQuestion())
                .partBModelAnswer(bank.getPartBModelAnswer())
                .partBAiAnswer(bank.getPartBAiAnswer())
                .partBMarkingScheme(bank.getPartBMarkingScheme())
                .partBMaxMark(bank.getPartBMaxMark())
                .partCQuestion(bank.getPartCQuestion())
                .partCModelAnswer(bank.getPartCModelAnswer())
                .partCAiAnswer(bank.getPartCAiAnswer())
                .partCMarkingScheme(bank.getPartCMarkingScheme())
                .partCMaxMark(bank.getPartCMaxMark())
                .partDQuestion(bank.getPartDQuestion())
                .partDModelAnswer(bank.getPartDModelAnswer())
                .partDAiAnswer(bank.getPartDAiAnswer())
                .partDMarkingScheme(bank.getPartDMarkingScheme())
                .partDMaxMark(bank.getPartDMaxMark())
                .build();
    }

    private Subject findSubject(String id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Subject not found: " + id));
    }

    private Chapter findChapter(String id) {
        return chapterRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Chapter not found: " + id));
    }

    private Topic findTopic(String id) {
        if (id == null || id.isBlank()) return null;
        return topicRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Topic not found: " + id));
    }
}
