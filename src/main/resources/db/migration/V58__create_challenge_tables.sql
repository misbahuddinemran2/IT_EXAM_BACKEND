-- 1vs1 Challenge feature tables

CREATE TABLE challenge (
    id BIGSERIAL PRIMARY KEY,
    mode VARCHAR(20) NOT NULL,              -- FRIEND / RANDOM
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING/ACTIVE/COMPLETED/EXPIRED/DECLINED
    creator_id BIGINT NOT NULL,
    opponent_id BIGINT,                      -- null until matched (RANDOM mode) or until accepted
    chapter_id BIGINT,
    topic_id BIGINT,                         -- null = mixed/random topic within chapter
    question_count INT NOT NULL DEFAULT 10,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    expires_at TIMESTAMP,
    completed_at TIMESTAMP,
    CONSTRAINT fk_challenge_creator FOREIGN KEY (creator_id) REFERENCES users(id),
    CONSTRAINT fk_challenge_opponent FOREIGN KEY (opponent_id) REFERENCES users(id)
);

CREATE INDEX idx_challenge_creator ON challenge(creator_id);
CREATE INDEX idx_challenge_opponent ON challenge(opponent_id);
CREATE INDEX idx_challenge_status ON challenge(status);

CREATE TABLE challenge_question (
    id BIGSERIAL PRIMARY KEY,
    challenge_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,             -- references guide_practice_mcq.id
    order_index INT NOT NULL,
    CONSTRAINT fk_cq_challenge FOREIGN KEY (challenge_id) REFERENCES challenge(id) ON DELETE CASCADE,
    CONSTRAINT fk_cq_question FOREIGN KEY (question_id) REFERENCES guide_practice_mcq(id)
);

CREATE INDEX idx_challenge_question_challenge ON challenge_question(challenge_id);

CREATE TABLE challenge_attempt (
    id BIGSERIAL PRIMARY KEY,
    challenge_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    selected_option_id BIGINT,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    time_taken_ms INT,
