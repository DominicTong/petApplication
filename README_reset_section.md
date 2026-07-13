## Resetting your progress

If you want to wipe your tasks, coins, and collected animals and start over — without touching the animal pool itself — run:

```sql
USE petapp;

TRUNCATE TABLE user_collection;
TRUNCATE TABLE tasks;
UPDATE user_data SET coins = 0, total_tasks_completed = 0 WHERE id = 1;
```

`user_collection` must be truncated before `tasks` isn't actually required (they're unrelated tables), but it's good practice to clear child/dependent data first out of habit — `user_collection` has a foreign key on `animals`, not on `tasks`.

**Full wipe (including re-seeding the animal pool):**

```sql
DROP DATABASE petapp;
```

then re-run:

```
mysql -u root -p < schema.sql
```

This recreates every table from scratch and re-seeds all 18 animals.

**Optional: save this as `reset.sql`** in the project root so it's a one-line reset during development or demoing:

```
mysql -u root -p petapp < reset.sql
```
