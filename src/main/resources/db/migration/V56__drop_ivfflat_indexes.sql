-- =========================================
-- V56: ivfflat index সরিয়ে exact (seq scan) search চালু করা
-- Reason: dataset ছোট (chunk ~119 rows, cache আরও কম) থাকায়
-- ivfflat lists=100 configuration ভুলভাবে অনেক list-কে খালি
-- রেখে দিচ্ছিল, ফলে nearest-neighbor query মাঝে মাঝে ভুলভাবে
-- 0 row রিটার্ন করছিল exception ছাড়াই। এত ছোট dataset-এ
-- sequential scan সবসময় exact এবং যথেষ্ট দ্রুত (মিলিসেকেন্ডে),
-- তাই approximate ivfflat index-এর দরকার নেই।
-- =========================================

DROP INDEX IF EXISTS ict_chunk_embedding_idx;
DROP INDEX IF EXISTS ict_cache_embedding_idx;
