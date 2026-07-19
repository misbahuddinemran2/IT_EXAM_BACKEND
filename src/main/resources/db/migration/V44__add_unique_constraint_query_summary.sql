-- একই প্রশ্নের একাধিক summary row যাতে না জমে, তার জন্য unique constraint
-- (আগে থেকে ডুপ্লিকেট থাকলে এই migration fail করতে পারে, সেক্ষেত্রে আগে ডুপ্লিকেট ক্লিন করতে হবে)

DELETE FROM ict_query_summary a
USING ict_query_summary b
WHERE a.id < b.id
  AND a.question = b.question;

ALTER TABLE ict_query_summary ADD CONSTRAINT uq_query_summary_question UNIQUE (question);
