-- Move resume/document file storage off the local disk (which the target platform
-- doesn't persist) and into the row itself. Existing files are not backfillable since
-- they lived on the old host's filesystem, so this drops storage_path outright.
ALTER TABLE resumes DROP COLUMN storage_path;
ALTER TABLE resumes ADD COLUMN file_data BYTEA;

ALTER TABLE documents DROP COLUMN storage_path;
ALTER TABLE documents ADD COLUMN file_data BYTEA;
