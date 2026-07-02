package com.examplatform.modules.liveexam.scheduler;

import com.examplatform.modules.liveexam.service.LiveExamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LiveExamScheduler {

    private final LiveExamService liveExamService;

    // প্রতি ১ মিনিটে চেক করবে expired/disconnected sessions
    @Scheduled(fixedDelay = 60_000)
    public void checkExpiredSessions() {
        try {
            liveExamService.processExpiredSessions();
        } catch (Exception e) {
            log.error("Error in live exam expiry scheduler", e);
        }
    }
}
