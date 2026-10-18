# reservas-qa-automation

Aplicación de reservas para negocios (peluquerías, restaurantes...) que uso como proyecto para practicar automatización de pruebas a varios niveles: unitarias, de API y de interfaz.

- **Backend**: Java 21, Spring Boot, Spring Security (JWT), JPA, PostgreSQL
- **Frontend**: React + Vite
- **Tests**: JUnit 5, Mockito, Cucumber (Gherkin en español), REST Assured, Playwright para Java, Vitest + Testing Library

## Estrategia de pruebas

| Nivel | Herramientas | Qué cubre | Dónde |
|---|---|---|---|
| Unitarias | JUnit 5, Mockito | Servicio de auth, JWT, transiciones de estado de las reservas | `src/test/java/.../{service,security,model,controller}` |
| API | Cucumber + REST Assured | Login y registro, usuarios, búsqueda y CRUD de negocios: casos positivos, negativos, validaciones, permisos por rol | `src/test/resources/features/api` |
| UI (E2E) | Cucumber + Playwright | Login, registro y catálogo de negocios desde el navegador, con Page Object | `src/test/resources/features/ui` |
| Frontend | Vitest + Testing Library | Cliente HTTP, validaciones, páginas | `frontend/src/test` |

Los escenarios están etiquetados por tipo (`@Positivo`, `@Negativo`, `@Validacion`, `@Seguridad`) y por funcionalidad (`@Login`, `@Business`...), así que se puede lanzar un subconjunto:

```bash
./mvnw test -Dtest=CucumberTestRunner -Dcucumber.filter.tags="@Business and @Negativo"
```

Los tests de API levantan la aplicación en un puerto aleatorio contra H2 en memoria, así que no necesitan Docker.

## Cómo ejecutarlo

Requisitos: Docker, Java 21 o superior y Node.js.

```bash
cp .env.example .env
make run          # PostgreSQL + API (http://localhost:8080) + frontend (http://localhost:5173)
```

Tests:

```bash
make test         # unitarias + API
make front-test   # frontend
make e2e-install  # una vez: descarga Chromium
make e2e-run      # UI: levanta un entorno de test aparte, ejecuta y lo para
```

`make help` lista el resto de comandos.

## Informes

- API: `target/cucumber-reports/report.html`
- UI: `target/cucumber-reports/e2e/report.html` (con captura si falla) y traces en `target/e2e-traces/`

## API

| Recurso | Endpoints |
|---|---|
| Auth | `POST /auth/register`, `POST /auth/login` |
| Usuario | `GET/PUT /user/me`, `PATCH /user/me/password`, `GET /user/{uuid}` |
| Negocios | `GET /business?q=`, `GET /business/{uuid}`, `GET /business/mine`, `POST /business`, `PUT/DELETE /business/{uuid}`, `GET /business/{uuid}/bookings` |
| Reservas | `GET /booking/{uuid}`, `GET /booking/mine`, `POST /booking`, `PUT /booking/{uuid}`, `PATCH /booking/{uuid}/{confirm,complete,cancel}` |

Roles: `USER`, `BUSINESS_OWNER` (al publicar el primer negocio) y `ADMIN`.

## Autor

Mario Castro Romero
