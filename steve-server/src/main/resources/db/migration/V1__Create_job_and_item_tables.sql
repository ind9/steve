create table "job" (
  "id" UUID PRIMARY KEY,
  "app_name" TEXT NOT NULL,
  "state" TEXT NOT NULL,
  "attributes" HSTORE NULL DEFAULT NULL,
  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITHOUT TIME ZONE NULL DEFAULT NULL
);

create table "item" (
  "id" UUID PRIMARY KEY,
  "job_id" UUID NOT NULL REFERENCES job(id),
  "status" TEXT NOT NULL,
  "attributes" HSTORE NULL DEFAULT NULL,
  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITHOUT TIME ZONE NULL DEFAULT NULL
);
