# Banco XYZ — Desarrollo Backend III

**Experiencia 3, semana 8 · Ignacio Kriman · Duoc UC**

Continuación del proyecto de la semana 6. La solución expone los datos bancarios migrados desde CSV y agrega OAuth2, comunicación asíncrona mediante JMS y un despliegue conjunto con Docker Compose. Conserva los BFF web, móvil y ATM, Config Server, Eureka y Resilience4j.

## Propuesta técnica

| Componente | Responsabilidad | Puerto local |
|---|---|---|
| `config-server` | Configuración centralizada, perfil nativo | 8888 |
| `discovery-server` | Registro y descubrimiento con Eureka | 8761 |
| `auth-server` | Emite JWT firmados mediante OAuth2 | 9000 |
| `bank-account-service` | Cuentas, migración y BFF; publica eventos JMS | 8081 |
| `notification-service` | Consume los eventos y permite consultar los últimos 100 | 8082 |
| `artemis` | Broker JMS con cola persistente | Interno, 61616 |

El cliente obtiene un token del servidor de autorización y lo presenta al servicio bancario. Los recursos validan firma RSA, emisor, vencimiento y audiencia `bank-services`. Los permisos se comprueban mediante scopes. El flujo `client_credentials` representa clientes de servidor, como un backend o un cajero; no es un inicio de sesión de personas y los secretos no deben incorporarse a un frontend público.

Una solicitud `POST /api/events/migration/106` envía `MIGRATION_REVIEW_REQUESTED` a la cola `bank.migration.events`. El productor responde 202 después de que JMS confirma el envío, sin esperar al consumidor. `notification-service` recibe el JSON, registra su identificador y lo expone en su API protegida. Si el consumidor se detiene, el broker retiene el evento. Si el envío falla, la API devuelve 503, sin simular una entrega exitosa.

El reporte de migración usa Circuit Breaker, Retry y Bulkhead. El auditor legacy continúa siendo una simulación del proyecto base: se puede forzar su fallo para observar el reporte alternativo y la apertura del circuito. Retry queda dentro del Circuit Breaker, con dos intentos y 100 ms entre ellos. Bulkhead limita la concurrencia a cinco llamadas. Tras fallos suficientes el circuito abre durante 10 segundos y luego admite dos llamadas de prueba.

## Estructura

- `pom.xml`: proyecto Maven multimódulo, Java 17, Spring Boot 3.3.5 y Spring Cloud 2023.0.4.
- `config-repo/`: propiedades centralizadas de resiliencia.
- `config-server/` y `discovery-server/`: infraestructura Spring Cloud original.
- `auth-server/`: clientes OAuth2, emisión de JWT y publicación de claves públicas.
- `bank-account-service/`: datos CSV, lógica bancaria, BFF, seguridad y productor JMS.
- `notification-service/`: consumidor JMS y consulta de notificaciones.
- `Dockerfile`: construcción multietapa reutilizada para las cinco imágenes Java.
- `docker-compose.yaml`: seis contenedores, dependencias, healthchecks, red y volumen del broker.
- `scripts/`: pruebas de ejecución para Windows PowerShell.
- `docs/evidencias/`: resultados de verificación y guía para completar las evidencias.

## Ejecución en Windows

Se necesita Docker Desktop iniciado, con contenedores Linux y Docker Compose v2. Para ejecutar por esta vía no es necesario instalar Maven ni Java. La primera construcción descarga las dependencias y puede tardar varios minutos. Conviene asignar al menos 6 GB de memoria a Docker.

Descomprimir y abrir PowerShell en la carpeta que contiene `docker-compose.yaml`:

```powershell
Copy-Item .env.example .env
docker compose up -d --build --wait --wait-timeout 300
docker compose ps
```

Copiar `.env.example` solo la primera vez. Los seis servicios deben aparecer ejecutándose y con estado saludable. Revisar errores con `docker compose logs --tail 100 NOMBRE_DEL_SERVICIO`.

Eureka se puede abrir en <http://localhost:8761>. Los servicios de negocio `BANK-ACCOUNT-SERVICE` y `NOTIFICATION-SERVICE` deben registrarse tras algunos segundos. Config Server expone <http://localhost:8888/bank-account-service/default>. Sus puertos se publican exclusivamente en `127.0.0.1` para la demostración local.

Para detener sin borrar la cola:

```powershell
docker compose down
```

## OAuth2 y permisos

Las contraseñas de demostración están en `.env.example`. `.env` está excluido de Git y del contexto Docker. No se guardan secretos reales en el código Java ni en Config Server.

| Cliente | Scopes permitidos |
|---|---|
| `bank-api-client` | `bank.read`, `events.write`, `events.read` |
| `bank-atm-client` | `atm.read`, `atm.write` |

Ejemplo PowerShell, utilizando el secreto de demostración (adaptarlo si se cambió `.env`):

```powershell
$basic = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes('bank-api-client:api-secret-demo-2026'))
$token = Invoke-RestMethod -Method Post -Uri 'http://localhost:9000/oauth2/token' -Headers @{Authorization="Basic $basic"} -Body @{grant_type='client_credentials';scope='bank.read events.write events.read'}
$headers = @{Authorization="Bearer $($token.access_token)"}
Invoke-RestMethod 'http://localhost:8081/api/accounts/106' -Headers $headers
Invoke-RestMethod -Method Post 'http://localhost:8081/api/events/migration/106' -Headers $headers
Invoke-RestMethod 'http://localhost:8082/api/notifications' -Headers $headers
```

En Postman: enviar `POST http://localhost:9000/oauth2/token`, usar Basic Auth con el identificador y secreto del cliente y un cuerpo `x-www-form-urlencoded` con `grant_type=client_credentials` y los scopes separados por espacios. Copiar `access_token` y usar Bearer Token en las solicitudes a las API. Los tokens duran 15 minutos. Reiniciar `auth-server` cambia su clave de demostración: solicitar nuevos tokens.

| Método y endpoint | Scope |
|---|---|
| `GET /api/accounts` y `/api/accounts/106` | `bank.read` |
| `GET /api/accounts/106/migration-report` | `bank.read` |
| `GET /api/migration/quality` | `bank.read` |
| `GET /api/web/dashboard?cuentaId=106` | `bank.read` |
| `GET /api/mobile/resumen/106` | `bank.read` |
| `GET /api/atm/cuentas/106/saldo` | `atm.read` |
| `POST /api/atm/cuentas/106/retiros` con `{"monto":1000}` | `atm.write` |
| `POST /api/events/migration/106` | `events.write` |
| `GET http://localhost:8082/api/notifications` | `events.read` |
| `GET /actuator/circuitbreakers` y `/actuator/circuitbreakerevents` | `bank.read` |

Una solicitud sin token o con token inválido devuelve 401. Un token válido sin el scope necesario devuelve 403. HTTP Basic se usa únicamente para autenticar al cliente en la emisión del token; ya no autentica las API bancarias.

## Pruebas y evidencia

Con los contenedores saludables, ejecutar:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\probar.ps1 -ProbarResiliencia -ProbarCola
powershell -ExecutionPolicy Bypass -File .\scripts\pruebas-unitarias.ps1
```

El primer script guarda una transcripción real en `docs/evidencias/prueba-FECHA-HORA.txt`. Comprueba salud, emisión de tokens, respuestas 401/403/200, publicación 202 y recepción del mismo `eventId`. `-ProbarCola` detiene el consumidor y verifica la entrega pendiente al reiniciarlo. `-ProbarResiliencia` recrea temporalmente el servicio bancario con fallos forzados, comprueba el fallback e imprime el circuito. Finalmente restaura el entorno anterior. La recreación reinicia los saldos en memoria; ejecutar estas pruebas sobre los datos de demostración.

Las pruebas Maven se ejecutan dentro de un contenedor y guardan `docs/evidencias/pruebas-unitarias.txt`. Si se dispone de Java 17 y Maven también puede ejecutarse `mvn test` desde la raíz. Los tests de seguridad usan JWT simulados y un productor JMS simulado; las tres pruebas de `auth-server` ejercitan HTTP y emisión real de tokens. El script de ejecución comprueba el flujo completo con los servicios levantados.

Para visualizar manualmente los fallos, cambiar `FORCE_LEGACY_AUDIT_FAILURE=true` en `.env`, ejecutar `docker compose up -d --force-recreate bank-account-service` y consultar varias veces el reporte. Se obtiene `VALIDATED_WITH_FALLBACK`. Restaurar `false` y recrear el servicio para recuperar `VALIDATED`.

## Entrega

1. Ejecutar las pruebas y revisar que terminen correctamente.
2. Adjuntar capturas de `docker compose ps`, Eureka, autorización OAuth2, fallback y recepción JMS. La transcripción sirve como salida de consola verificable.
3. Subir el código a un repositorio GitHub propio y agregar su URL en `docs/ENTREGA.md`. No subir `.env` ni carpetas `target/`.
4. Comprimir código, README y evidencias juntos como `Exp3_S8_Ignacio_Kriman.zip`. Completar el nombre si el docente exige todos los apellidos.

## Alcance y despliegue cloud

Esta es una demostración académica ejecutable, no un sistema bancario productivo. Los CSV y saldos permanecen en memoria como en la base de semana 6; los retiros se reinician al arrancar y no se deben escalar esas instancias sin persistencia compartida. El consumidor conserva los últimos 100 eventos en memoria y deduplica únicamente dentro de esa ventana y proceso. JMS entrega al menos una vez; para efectos de negocio durables se requiere almacenamiento e idempotencia persistentes. El volumen `artemis-data` conserva mensajes aún no consumidos. La cola no reemplaza una base de datos ni una outbox transaccional.

Compose prepara un despliegue en un host con Docker, pero no crea recursos en un proveedor cloud. Para exponerlo fuera del PC se deben configurar HTTPS, DNS y el emisor OAuth2 público; mantener Config Server, Eureka y Artemis en una red privada; utilizar gestión de secretos, claves de firma persistentes, datos persistentes, respaldos y versiones soportadas tras verificar compatibilidad. Las claves RSA se generan al arrancar exclusivamente para simplificar esta demostración. Las versiones de Spring del proyecto original se conservaron por compatibilidad académica.

## Referencias técnicas

- [Spring Authorization Server](https://docs.spring.io/spring-authorization-server/reference/getting-started.html).
- [Spring Boot 3.3 y Resource Server](https://docs.spring.io/spring-boot/3.3/reference/web/spring-security.html).
- [Apache Artemis y Docker](https://artemis.apache.org/components/artemis/documentation/latest/docker.html).
- Datos legacy originales: <https://github.com/KariVillagran/bank_legacy_data> (CSV incluidos en el proyecto recibido).
