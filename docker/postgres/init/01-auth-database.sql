-- Bounded context "identity": database used ONLY by the authentication-api.
-- Runs once, when the postgres data volume is empty (docker-entrypoint-initdb.d).
-- Tables are created by the authentication-api Liquibase changelog, not here.

CREATE ROLE authenticator WITH LOGIN PASSWORD 'admin';

-- Being the owner lets authenticator create objects in the public schema (PostgreSQL 15+).
CREATE DATABASE auth OWNER authenticator;

-- Nobody else (e.g. the car-reselling user) can even connect to it.
REVOKE ALL ON DATABASE auth FROM PUBLIC;
GRANT ALL ON DATABASE auth TO authenticator;
