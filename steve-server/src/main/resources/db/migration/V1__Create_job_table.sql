create table "job" (
  "id" UUID PRIMARY KEY,
  "app_name" TEXT NOT NULL,
  "status" TEXT NOT NULL,
  "attributes" HSTORE NULL DEFAULT NULL,
  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITHOUT TIME ZONE NULL DEFAULT NULL
);
