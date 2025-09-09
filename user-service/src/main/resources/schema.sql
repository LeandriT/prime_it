-- Drop prev tables (útil para H2 en memoria durante el desarrollo)
DROP TABLE IF EXISTS "phones";
DROP TABLE IF EXISTS "users";

-- =========================
-- USERS
-- =========================
CREATE TABLE "users" (
  "uuid" UUID PRIMARY KEY,
  "name" VARCHAR(100) NOT NULL,
  "email" VARCHAR(150) NOT NULL,
  "password" VARCHAR(255) NOT NULL,
  "last_login" TIMESTAMP NOT NULL,
  "token" VARCHAR(512),
  "additional_token" VARCHAR(512),
  "is_active" BOOLEAN NOT NULL,
  "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP

);

ALTER TABLE "users"
  ADD CONSTRAINT "uk_users_email" UNIQUE ("email");

-- =========================
-- PHONES
-- =========================
CREATE TABLE "phones" (
  "uuid" UUID PRIMARY KEY,
  "number" VARCHAR(20) NOT NULL,
  "city_code" VARCHAR(6) NOT NULL,
  "country_code" VARCHAR(6) NOT NULL,
  "user_id" UUID NOT NULL,
  "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" TIMESTAMP,

  CONSTRAINT "fk_phones_user"
    FOREIGN KEY ("user_id") REFERENCES "users" ("uuid")
    ON DELETE CASCADE
);

CREATE INDEX "idx_phones_user_id" ON "phones" ("user_id");
CREATE INDEX "idx_phones_number" ON "phones" ("number");

-- (Opcional) Garantiza que un usuario no repita exactamente el mismo teléfono
-- ALTER TABLE "phones"
--   ADD CONSTRAINT "uk_phones_user_number"
--   UNIQUE ("user_id","number","city_code","country_code");