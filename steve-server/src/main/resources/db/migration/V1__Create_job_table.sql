create table "job" (
  "id" BIGSERIAL PRIMARY KEY,
  "app_name" TEXT NOT NULL,
  "status" TEXT NOT NULL,
  "attributes" JSONB NOT NULL,
  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITHOUT TIME ZONE NULL DEFAULT NULL
);
