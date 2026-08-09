-- Novels and writing groups gain an owner.

ALTER TABLE novels ADD COLUMN owner_id BIGINT REFERENCES users(id) ON DELETE CASCADE;

-- Novels created before auth existed have no possible owner, and there is no user to
-- assign them to. They are development leftovers, so they go; novel_beats follow via
-- the ON DELETE CASCADE already on that table's foreign key.
DELETE FROM novels WHERE owner_id IS NULL;

-- With the table clean, the constraint can be enforced by the database rather than
-- trusted to application code. An ownerless novel is now impossible to create, no
-- matter what a future endpoint forgets to set.
ALTER TABLE novels ALTER COLUMN owner_id SET NOT NULL;

-- Groups are different: the seeded ones are community fixtures that belong to nobody,
-- so NULL is meaningful here rather than an accident. ON DELETE SET NULL keeps a group
-- alive when its creator deletes their account -- the group's other members should not
-- lose their listing because one person left.
ALTER TABLE writing_groups ADD COLUMN owner_id BIGINT REFERENCES users(id) ON DELETE SET NULL;

-- Every ownership check filters on owner_id, so it earns an index on both tables.
CREATE INDEX idx_novels_owner ON novels(owner_id);
CREATE INDEX idx_writing_groups_owner ON writing_groups(owner_id);
