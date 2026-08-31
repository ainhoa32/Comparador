# AGENTS.md

Spring Boot 3.4.4 / Java 21 / Maven web app: a personal price-comparison backend that scrapes Spanish supermarket sites (Mercadona, Carrefour, Dia, Ahorramas) via jsoup and serves REST endpoints. Intentionally has no users, no auth, and no database. A separate Vite React frontend (NOT in this repo) talks to it on port 5173.

## Commands
- Run: `./mvnw spring-boot:run`
- Build: `./mvnw package`
- Tests: `./mvnw test` — the single test (`contextLoads`) is a `@SpringBootTest`. No DB/service is required; any MySQL instance must be stopped, or leaving stale compiled classes without a `clean` will fail the context.

## Architecture & structure
Under `com.proyecto.comparadorProyecto`:
- `controllers/` — `ProductosControllerAPIRest.java` (REST: `/productos/precioGranel/{producto}` and `/productos/precioGranel/{producto}/{supermercados}`), `ComparadorProyectoPruebaController.java` (Thymeleaf MVC smoke page `/prueba2`).
- `services/` — `ProductosService` (thin REST wrapper) and `ComparadorService` (parallel query + sort by priority/price).
- `buscador/` — supermarket scraping: `Supermercado` interface implemented per shop in `buscador/supermercados/` (each returns `CompletableFuture<List<ProductoDto>>` for parallel queries); parsed JSON API models in `buscador/models/<shop>/`. `ConfiguracionSupermercados` wires them into a `List<Supermercado>` bean. `ClienteHttp` (java.net.http) / `ClienteJsoup` (jsoup) do the requests.
- `dto/` — only `ProductoDto` remains.

## Gotchas
- No DB dependency is present. `application.properties` sets only the app name — do not reintroduce datasource/JPA config, and never resurrect the `models/`/`repository/`/`security/` packages (user/DB features were intentionally removed).
- Editing scrapers: they hit live sites; rate/HTML/JSON changes will break parsing. Tests don't cover them.
