IMAGE     := reservas-postgres
CONTAINER := reservas-db
VOLUME    := reservas-pgdata

.DEFAULT_GOAL := help
TEST_DB   := reserva_test_db
FRONT_STAMP := frontend/node_modules/.installed

.PHONY: help check run run-test e2e-run db-test run-api run-web stop db-build db-up db-wait db-down db-logs db-reset test test-all e2e-install e2e clean front-install front-dev front-test front-build

help: ## Muestra los comandos disponibles
	@grep -E '^[a-zA-Z_-]+:.*## ' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*## "}; {printf "  %-12s %s\n", $$1, $$2}'

db-build: ## Construye la imagen de PostgreSQL
	docker build -t $(IMAGE) .

db-up: ## Levanta PostgreSQL en Docker
	@test -f .env || { echo "Falta el archivo .env (cp .env.example .env)"; exit 1; }
	@docker image inspect $(IMAGE) >/dev/null 2>&1 || docker build -t $(IMAGE) .
	@if [ -n "$$(docker ps -aq -f name=^$(CONTAINER)$$)" ]; then \
		docker start $(CONTAINER) >/dev/null; \
	else \
		docker run -d --name $(CONTAINER) --env-file .env -p 5432:5432 \
			-v $(VOLUME):/var/lib/postgresql/data $(IMAGE) >/dev/null; \
	fi
	@$(MAKE) --no-print-directory db-wait

db-wait:
	@echo "Esperando a PostgreSQL..."
	@until docker exec $(CONTAINER) sh -c 'pg_isready -q -U "$$POSTGRES_USER" -d "$$POSTGRES_DB"'; do sleep 1; done
	@echo "PostgreSQL listo en localhost:5432"

db-down: ## Para el contenedor de PostgreSQL (conserva los datos)
	-docker stop $(CONTAINER)

db-logs: ## Muestra los logs de PostgreSQL
	docker logs -f $(CONTAINER)

db-reset: ## Elimina el contenedor y el volumen de datos (BORRA la base de datos)
	-docker rm -f $(CONTAINER)
	-docker volume rm $(VOLUME)

check: ## Comprueba Docker, Java y Node
	@command -v docker >/dev/null 2>&1 || { echo "Falta Docker"; exit 1; }
	@docker info >/dev/null 2>&1 || { echo "Docker no está arrancado"; exit 1; }
	@command -v java >/dev/null 2>&1 || [ -n "$$JAVA_HOME" ] || { echo "Falta Java 21 o superior"; exit 1; }
	@command -v npm >/dev/null 2>&1 || { echo "Falta Node.js"; exit 1; }
	@echo "Docker, Java y Node OK"

run: check db-up $(FRONT_STAMP) ## Arranca BD, API (:8080) y frontend (:5173)
	@./scripts/dev.sh

db-test: db-up ## Crea la BD de test si no existe
	@docker exec $(CONTAINER) sh -c 'psql -U "$$POSTGRES_USER" -lqt | cut -d"|" -f1 | grep -qw $(TEST_DB) || createdb -U "$$POSTGRES_USER" $(TEST_DB)'

run-test: check db-test $(FRONT_STAMP) ## Arranca API (:8081) y frontend (:5174) con la BD de test
	@SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/$(TEST_DB) API_PORT=8081 WEB_PORT=5174 \
		APP_CORS_ALLOWED_ORIGINS=http://localhost:5174 ./scripts/dev.sh

run-api: db-up ## Arranca solo la base de datos y la API Spring Boot
	./mvnw spring-boot:run

run-web: $(FRONT_STAMP) ## Arranca solo el frontend (la API debe estar en :8080)
	cd frontend && npm run dev

stop: db-down ## Para la base de datos (API y frontend se paran con Ctrl+C)

test: ## Ejecuta los tests del backend
	./mvnw test

test-all: test front-test ## Ejecuta los tests del backend y del frontend

e2e-install: ## Descarga Chromium para Playwright
	./mvnw test-compile exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.classpathScope=test -Dexec.args="install chromium"

e2e: ## Tests de UI (necesita make run-test)
	./mvnw test -Dtest=E2ERunner -Dsurefire.failIfNoSpecifiedTests=false -Dheadless=$(or $(HEADLESS),true)

e2e-run: check db-test $(FRONT_STAMP) ## Levanta todo y lanza los tests de UI
	@mkdir -p target
	@./scripts/e2e.sh

clean: ## Limpia la carpeta target
	./mvnw clean

front-install: ## Instala (o reinstala) las dependencias del frontend
	cd frontend && npm install
	@touch $(FRONT_STAMP)

$(FRONT_STAMP): frontend/package.json frontend/package-lock.json
	cd frontend && npm install
	@touch $@

front-dev: run-web

front-test: $(FRONT_STAMP) ## Ejecuta los tests del frontend
	cd frontend && npm test

front-build: $(FRONT_STAMP) ## Genera la build de producción del frontend en frontend/dist
	cd frontend && npm run build
