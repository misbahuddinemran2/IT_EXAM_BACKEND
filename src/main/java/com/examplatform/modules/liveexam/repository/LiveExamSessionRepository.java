package com.examplatform.modules.liveexam.repository;

import com.examplatform.modules.liveexam.entity.LiveExamSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LiveExamSessionRepository extends JpaRepository<LiveExamSession, String> {

    Optional<LiveExamSession> findByExamIdAndUserId(String examId, String userId);

    // Grace period পার হয়ে যাওয়া disconnected sessions (scheduler এর জন্য)
    @Query("SELECT s FROM LiveExamSession s WHERE s.status = 'DISCONNECTED' " +
            "AND s.disconnectedAt <= :cutoff")
    List<LiveExamSession> findExpiredDisconnectedSessions(@Param("cutoff") LocalDateTime cutoff);

    Optional<LiveExamSession> findByExamIdAndUserIdAndCycleNumber(
            String examId, String userId, int cycleNumber);

    boolean existsByExamIdAndUserId(String examId, String userId); // যেকোনো cycle-এ attempt আছে কিনা

    // exam republish হয়ে cycleNumber বদলে গেলেও, ইউজারের যেকোনো cycle-এর মধ্যে
    // সবচেয়ে সাম্প্রতিক session খুঁজে বের করার জন্য (attemptStatus/marks দেখাতে ব্যবহার হবে)
    Optional<LiveExamSession> findTopByExamIdAndUserIdOrderByCreatedAtDesc(String examId, String userId);

    // Duration শেষ হয়ে যাওয়া কিন্তু এখনো IN_PROGRESS/DISCONNECTED (safety net)
    @Query("SELECT s FROM LiveExamSession s WHERE s.status IN ('IN_PROGRESS','DISCONNECTED') " +
            "AND s.expiresAt <= :now")
    List<LiveExamSession> findTimeExpiredActiveSessions(@Param("now") LocalDateTime now);

    // Per-exam leaderboard (marks descending)
    @Query("SELECT s FROM LiveExamSession s WHERE s.examId = :examId " +
            "AND s.status IN ('SUBMITTED','AUTO_SUBMITTED') " +
            "ORDER BY s.obtainedMarks DESC, s.submittedAt ASC")
    List<LiveExamSession> findLeaderboardByExamId(@Param("examId") String examId);
}
