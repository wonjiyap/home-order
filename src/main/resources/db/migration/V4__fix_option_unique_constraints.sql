-- Drop existing unique constraints (created in V1)
ALTER TABLE option_groups DROP CONSTRAINT IF EXISTS option_groups_menu_id_name_key;
DROP INDEX IF EXISTS unique_menu_option_group;

ALTER TABLE options DROP CONSTRAINT IF EXISTS options_option_group_id_name_key;
DROP INDEX IF EXISTS unique_option_group_option;

-- Create partial unique indexes that only apply to non-deleted records
CREATE UNIQUE INDEX IF NOT EXISTS unique_menu_option_group_active ON option_groups (menu_id, name) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS unique_option_group_option_active ON options (option_group_id, name) WHERE deleted_at IS NULL;
