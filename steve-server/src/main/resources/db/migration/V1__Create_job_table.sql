create table "job" (
  "id" BIGSERIAL PRIMARY KEY,
  "app_name" TEXT NOT NULL,
  "status" TEXT NOT NULL,
  "attributes" JSONB NOT NULL,
  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITHOUT TIME ZONE NULL
);