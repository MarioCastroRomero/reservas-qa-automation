ARG POSTGRES_VERSION=16-alpine
FROM postgres:${POSTGRES_VERSION}

ENV POSTGRES_DB=reserva_db \
    POSTGRES_USER=postgres \
    PGDATA=/var/lib/postgresql/data/pgdata

EXPOSE 5432

HEALTHCHECK --interval=10s --timeout=5s --retries=5 --start-period=20s \
  CMD pg_isready -U "$POSTGRES_USER" -d "$POSTGRES_DB" || exit 1
