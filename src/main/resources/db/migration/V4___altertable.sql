ALTER TABLE categories
    ADD COLUMN created_by VARCHAR(255) NOT NULL DEFAULT 'system',
    ADD COLUMN updated_by VARCHAR(255) NOT NULL DEFAULT 'system';

ALTER TABLE borrows
    ADD COLUMN created_by VARCHAR(255) NOT NULL DEFAULT 'system',
    ADD COLUMN updated_by VARCHAR(255) NOT NULL DEFAULT 'system';

ALTER TABLE books
    ADD COLUMN created_by VARCHAR(255) NOT NULL DEFAULT 'system',
    ADD COLUMN updated_by VARCHAR(255) NOT NULL DEFAULT 'system';

ALTER TABLE book_requests
    ADD COLUMN created_by VARCHAR(255) NOT NULL DEFAULT 'system',
    ADD COLUMN updated_by VARCHAR(255) NOT NULL DEFAULT 'system';