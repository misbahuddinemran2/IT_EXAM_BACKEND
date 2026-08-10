-- Student এর academic session (batch) রাখার জন্য, ICT Practical module এর session-wise
-- visibility filter এ ব্যবহার হয়। NULL/খালি থাকলে 'ALL' টার্গেটেড experiment সবাই দেখতে পাবে।
ALTER TABLE users ADD COLUMN session VARCHAR(20);

CREATE INDEX idx_users_session ON users (session);
