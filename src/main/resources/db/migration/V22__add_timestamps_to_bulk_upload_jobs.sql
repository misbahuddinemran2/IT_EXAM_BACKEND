ALTER TABLE bulk_upload_jobs
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT now();

ALTER TABLE bulk_upload_jobs
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT now();
