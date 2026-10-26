# T051: Sesión 5 - Riesgos Técnicos

**Equipo:** T051  
**Integrantes:** Leo Moreno, Antonio Brenes, Sergio Ropero  
**Proyecto:** ConectaFP  
**Repositorio:** https://github.com/Bartolom30/ConectaFP

## 2. Identificación y evaluación de riesgos

### 2.1 Escala de evaluación

**Probabilidad (P)**

| Valor | Nivel | Descripción |
|---|---|---|
| 1 | Muy baja | Poco probable |
| 2 | Baja | Podría ocurrir |
| 3 | Media | Es posible que ocurra |
| 4 | Alta | Es probable que ocurra |
| 5 | Muy alta | Muy probable o recurrente |

**Impacto (I)**

| Valor | Nivel | Descripción |
|---|---|---|
| 1 | Muy bajo | Solución inmediata |
| 2 | Bajo | Incidencia menor |
| 3 | Medio | Requiere trabajo adicional |
| 4 | Alto | Retraso importante |
| 5 | Muy alto | Compromete seguridad |

**Cálculo de R = P × I**

| Resultado (R) | Severidad | Actuación |
|---|---|---|
| 1-4 | Baja | Controles básicos y supervisión |
| 5-11 | Moderada | Prevención y revisión periódica |
| 12-25 | Crítica | Mitigación y plan de contingencia |

### 2.2 Registro de riesgos

| Código | Área | Riesgo | P | I | R | Severidad |
|---|---|---|---|---|---|---|
| RT-01 | Backend | Errores al registrar usuarios, consultar ofertas o presentar candidaturas | 3 | 4 | 12 | Crítica |
| RT-02 | Base de datos | Errores en el almacenamiento como la pérdida o corrupción de datos | 3 | 5 | 15 | Crítica |
| RT-03 | Seguridad | Exposición de datos personales de alumnos o entidades | 2 | 5 | 10 | Moderada |
| RT-04 | Frontend | Errores de interfaz, validación o incompatibilidad entre navegadores | 3 | 3 | 9 | Moderada |
| RT-05 | Infraestructura | Fallos de configuración o despliegue de la aplicación | 3 | 4 | 12 | Crítica |
| RT-06 | Integraciones | Errores en servicios externos como los correos de confirmación | 3 | 3 | 9 | Moderada |
| RT-07 | Equipo | Retrasos por falta de coordinación o problemas con Git | 4 | 3 | 12 | Crítica |
| RT-08 | Rendimiento | Consultas lentas o tiempos de respuesta elevados | 2 | 4 | 8 | Moderada |

### 2.3 Prioridad de actuación

1. **RT-02: Pérdida o corrupción de datos.** Es prioritario porque puede provocar la pérdida de información y obligar a reconstruir el entorno.
2. **RT-01: Errores críticos del backend.** Puede impedir el funcionamiento de las principales funcionalidades de la aplicación.
3. **RT-05: Fallos de despliegue o infraestructura.** Puede impedir el acceso a la aplicación incluso cuando el código funciona correctamente.

## 3. Matriz gráfica de severidad 5 × 5

Filas: probabilidad (P). Columnas: impacto (I). Entre paréntesis, R = P × I.

| P \ I | 1 | 2 | 3 | 4 | 5 |
|---|---|---|---|---|---|
| 5 - Muy alta | Moderada (5) | Moderada (10) | Crítica (15) | Crítica (20) | Crítica (25) |
| 4 - Alta | Baja (4) | Moderada (8) | Crítica (12) **RT-07** | Crítica (16) | Crítica (20) |
| 3 - Media | Baja (3) | Moderada (6) | Moderada (9) **RT-04, RT-06** | Crítica (12) **RT-01, RT-05** | Crítica (15) **RT-02** |
| 2 - Baja | Baja (2) | Baja (4) | Moderada (6) | Moderada (8) **RT-08** | Moderada (10) **RT-03** |
| 1 - Muy baja | Baja (1) | Baja (2) | Baja (3) | Baja (4) | Moderada (5) |

## 4. Fichas del Plan de Contingencia

### Ficha de Contingencia: RT-02 (Pérdida o corrupción de datos)

- **Descripción:** La base de datos PostgreSQL se corrompe o se borra el volumen de Docker (por ejemplo con `docker compose down -v`) y se pierden usuarios, ofertas o candidaturas.
- **Probabilidad:** 3 | **Impacto:** 5 | **Nivel de riesgo (R):** 15 (Crítica)
- **Disparador (Trigger):**
  - El contenedor de la base de datos está `unhealthy` o se reinicia más de 3 veces en 5 minutos (`docker compose ps`).
  - La ruta `/api/health` indica `database: DOWN` durante más de 1 minuto.
  - Las tablas principales tienen 0 filas o menos del 90 % de las que había en el último backup.
- **Medidas preventivas:**
  - Hacer un backup diario con `pg_dump` y guardar las últimas 7 copias en la carpeta `backups/` (añadida al `.gitignore`).
  - Tener un fichero `db/seeds.sql` en el repositorio con datos de prueba.
  - No ejecutar `docker compose down -v` sin haber hecho antes un backup.
  - Probar una vez por sprint que el backup se puede restaurar.
- **Plan de contingencia (Plan B):**
  1. Parar el backend para que no escriba en una base de datos dañada: `docker compose stop backend`.
  2. Mirar los logs de la base de datos: `docker compose logs --tail=100 database`.
  3. Borrar el volumen dañado y crear la base de datos otra vez: `docker compose down -v` y `docker compose up -d database`.
  4. Restaurar el último backup: `docker compose exec -T database sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"' < backups/ultimo_backup.sql`.
  5. Si no hay backup, cargar los datos de prueba con el mismo comando usando `db/seeds.sql`.
  6. Comprobar que `http://localhost:8080/api/health` dice `database: UP` y volver a arrancar el backend con `docker compose up -d backend`.
- **Responsable:** Database Admin (suplente: Backend Lead)

### Ficha de Contingencia: RT-01 (Errores críticos del backend)

- **Descripción:** Fallan los endpoints principales (registro de usuarios, consulta de ofertas y candidaturas) por errores en el código o por un merge que rompe algo que antes funcionaba.
- **Probabilidad:** 3 | **Impacto:** 4 | **Nivel de riesgo (R):** 12 (Crítica)
- **Disparador (Trigger):**
  - `mvn test` falla en la rama `main` o en una Pull Request.
  - `/api/health` no responde 200 durante más de 2 minutos.
  - Más del 5 % de las peticiones dan error 5xx en 5 minutos (se ve en `docker compose logs backend`).
- **Medidas preventivas:**
  - Toda Pull Request tiene que ser revisada por otro miembro del equipo antes del merge a `main`.
  - Hacer tests de los servicios más importantes (registro, ofertas y candidaturas).
  - Validar los datos de entrada con `@Valid` y controlar los errores en un único sitio con `@RestControllerAdvice`.
  - Crear un tag de Git (`v0.1.0`, `v0.2.0`...) en cada versión estable para poder volver atrás.
- **Plan de contingencia (Plan B):**
  1. Ver qué falla con `docker compose ps` y `docker compose logs --tail=200 backend`, y buscar el commit que lo ha causado con `git log --oneline -10`.
  2. Avisar al equipo y parar los merges a `main` hasta arreglarlo.
  3. Si el fallo viene del último merge, revertirlo con `git revert <hash>` y hacer `git push`.
  4. Reconstruir el backend: `docker compose up -d --build backend`.
  5. Probar `/api/health` y los tres flujos principales (registro, ofertas y candidaturas).
  6. Si en 30 minutos no encontramos la causa, volver al último tag estable con `git checkout <tag>`.
- **Responsable:** Backend Lead (suplente: otro desarrollador backend)

### Ficha de Contingencia: RT-05 (Fallos de configuración o despliegue)

- **Descripción:** Faltan variables de entorno, la imagen de Docker no se construye o hay diferencias entre desarrollo y producción, y la aplicación no se puede usar aunque el código esté bien.
- **Probabilidad:** 3 | **Impacto:** 4 | **Nivel de riesgo (R):** 12 (Crítica)
- **Disparador (Trigger):**
  - `docker compose up -d --build` termina con error o algún contenedor queda en estado `Exited`.
  - Salen avisos del tipo `The "X" variable is not set` o `port is already allocated`.
  - El despliegue falla 2 veces seguidas o la aplicación no responde 200 a los 3 minutos de desplegar.
- **Medidas preventivas:**
  - Mantener el `.env.example` actualizado: si alguien añade una variable nueva, la pone también ahí.
  - Comprobar la configuración con `docker compose config` antes de desplegar.
  - Desplegar siempre primero en Staging y después en Producción.
  - Que otro compañero pruebe el README desde cero una vez por sprint.
- **Plan de contingencia (Plan B):**
  1. Ver el error con `docker compose config` y `docker compose logs --tail=100`.
  2. Comparar las variables del `.env` con las del `.env.example` y añadir las que falten.
  3. Si el puerto está ocupado, buscar qué lo usa con `ss -tulpn | grep 8080` o cambiar `BACKEND_PORT` en el `.env`.
  4. Reconstruir sin caché: `docker compose down`, `docker compose build --no-cache backend` y `docker compose up -d`.
  5. Si falla en Staging o Producción, volver a desplegar la última versión estable (rollback) desde el proveedor o desde el tag de Git.
  6. Comprobar `/api/health` y anotar la causa para que no se repita.
- **Responsable:** DevOps Lead (suplente: Backend Lead)

## 5. Protocolo de Ergonomía y PRL del Desarrollador

Este protocolo lo aplicamos los tres miembros del equipo durante todo el proyecto, tanto en clase como cuando trabajamos por nuestra cuenta.

Normativa en la que nos basamos: Ley 31/1995 de Prevención de Riesgos Laborales, Real Decreto 488/1997 (pantallas de visualización) y artículo 88 de la LOPDGDD (derecho a la desconexión digital).

### 5.1 Ergonomía física

| Elemento | Pauta |
|---|---|
| **Pantalla** | El borde superior a la altura de los ojos y a unos 50-70 cm de distancia. Sin reflejos y con el brillo ajustado. |
| **Portátil** | Usar un soporte y teclado y ratón externos para no mirar hacia abajo todo el rato. |
| **Silla y postura** | Espalda apoyada, codos y rodillas a unos 90° y pies apoyados en el suelo. |
| **Teclado y ratón** | Muñecas rectas, sin doblarlas, y usar atajos del IDE para tocar menos el ratón. |
| **Iluminación** | Unos 500 lux en el puesto, con la luz natural a un lado de la pantalla y no delante ni detrás. |
| **Entorno** | Temperatura entre 17 y 27 °C, ventilar la sala y evitar el ruido. |

### 5.2 Fatiga visual y carga mental

- Regla 20-20-20: cada 20 minutos mirar 20 segundos a un punto a unos 6 metros.
- Técnica Pomodoro: 25 minutos de trabajo y 5 de descanso. Después de 4 bloques, una pausa larga de 15 a 30 minutos.
- En los descansos levantarse, beber agua y estirar cuello, hombros y muñecas.
- No pasar más de 2 horas seguidas delante de la pantalla sin una pausa larga.
- Máximo 2 tareas abiertas por persona para no saturarnos, y dividir las tareas grandes en otras más pequeñas.
- Si alguien lleva 45 minutos atascado con el mismo error, pide ayuda a un compañero.
- Si alguien nota dolor de cabeza, ojos secos u hormigueo en las manos, lo dice al equipo para revisar su puesto y su carga de trabajo.

### 5.3 Desconexión digital

- Todos los miembros del equipo tienen derecho a la desconexión digital.
- No se espera respuesta a mensajes entre las 21:00 y las 08:00, ni en fines de semana y festivos.
- La única excepción es una incidencia crítica acordada por el equipo (aplicación caída en producción o una entrega en menos de 24 horas).
- Durante la programación autónoma cada uno puede silenciar las notificaciones y mirar los mensajes en sus descansos.
- La comunicación es asíncrona por defecto (issues y Pull Requests) y nadie tiene que responder al momento.
- No hay guardias obligatorias ni consecuencias por no contestar fuera de horario.
- Se revisa si se cumple en la retrospectiva de cada sprint.
