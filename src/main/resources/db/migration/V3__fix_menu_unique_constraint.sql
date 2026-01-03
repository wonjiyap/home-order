-- Drop existing unique constraint (created in V1)
ALTER TABLE menus DROP CONSTRAINT IF EXISTS menus_category_id_name_key;

-- Create partial unique index that only applies to non-deleted records
CREATE UNIQUE INDEX IF NOT EXISTS unique_category_menu_active ON menus (category_id, name) WHERE deleted_at IS NULL;
