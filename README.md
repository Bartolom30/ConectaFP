# ConectaFP

Entorno de desarrollo local del proyecto ConectaFP (Spring Boot + PostgreSQL) con Docker Compose.

## Requisitos
- Git
- Docker y Docker Compose v2 (`docker compose version`)

## Puesta en marcha
```bash
git clone <URL_DEL_REPOSITORIO>
cd ConectaFP
cp .env.example .env        # En Windows (PowerShell): copy .env.example .env
# Edita .env y cambia DB_PASSWORD
docker compose up -d --build
```

## Verificación
```bash
docker compose ps           # database (healthy) y backend en estado Up
docker compose logs -f backend
```
- API: http://localhost:8080
- PostgreSQL: localhost:5432 (usuario/clave/BD definidos en `.env`)

## Comandos útiles
| Acción | Comando |
|---|---|
| Parar servicios | `docker compose down` |
| Parar y borrar datos de la BD | `docker compose down -v` |
| Reconstruir el backend | `docker compose up -d --build backend` |
| Acceder a la BD | `docker compose exec database psql -U $DB_USER -d $DB_NAME` |

## Estructura
```
ConectaFP/
├── backend/            # API Spring Boot (Java 21, Maven)
├── docker-compose.yml
├── .env.example        # Plantilla de variables (sin credenciales reales)
└── .gitignore
```

## Notas
- Nunca subas `.env` al repositorio.
- El backend espera a que PostgreSQL esté `healthy` antes de arrancar.
- Dentro de la red de Docker el backend se conecta a la BD por el host `database`, no `localhost`.
