-- Drop existing unique constraint
ALTER TABLE categories DROP CONSTRAINT categories_party_id_name_key;

-- Create partial unique index that only applies to non-deleted records
CREATE UNIQUE INDEX unique_party_category_active ON categories (party_id, name) WHERE deleted_at IS NULL;
