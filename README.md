# Banco XYZ

**Desarrollo Backend III · Experiencia 3, semana 8**  
**Ignacio Kriman · Duoc UC**

## Objetivo

Continuar el proyecto de la semana 6 incorporando seguridad con OAuth2, mensajería asíncrona y ejecución mediante Docker Compose. El sistema carga y depura datos bancarios desde archivos CSV y los expone mediante una API y los BFF web, móvil y ATM.

Repositorio: [iKriman/backend_iii](https://github.com/iKriman/backend_iii)

## Propuesta técnica

El proyecto utiliza Java 17, Spring Boot 3.3.5 y Spring Cloud 2023.0.4. Las responsabilidades se distribuyen entre los siguientes componentes:

| Componente | Función | Puerto local |
|---|---|---|
| `config-server` | Centraliza la configuración desde `config-repo/` | 8888 |
| `discovery-server` | Registra los servicios mediante Eureka | 8761 |
| `auth-server` | Emite tokens JWT mediante OAuth2 | 9000 |
| `bank-account-service` | Expone cuentas y BFF; publica eventos JMS | 8081 |
| `notification-service` | Recibe eventos y permite consultar los últimos 100 | 8082 |
| `artemis` | Administra la cola de mensajes JMS | 61616, dentro de la red Docker |

La autenticación utiliza el flujo OAuth2 `client_credentials`. Cada cliente obtiene un token y lo envía como Bearer Token en las solicitudes. Los servicios validan la firma RSA, el emisor, la audiencia `bank-services`, el vencimiento y los permisos asignados mediante scopes. Este flujo autentica aplicaciones de servidor, como el backend o el cajero; no representa un inicio de sesión de personas.

Para la comunicación asíncrona, el servicio bancario publica eventos `MIGRATION_REVIEW_REQUESTED` en la cola `bank.migration.events`. El servicio de notificaciones consume los mensajes y registra su `eventId`. El productor responde con HTTP 202 cuando confirma el envío a JMS, sin esperar al consumidor. Si el consumidor está detenido, el broker conserva el mensaje pendiente; si el envío no se confirma por un error de JMS, la API devuelve HTTP 503.

El reporte de migración utiliza Resilience4j con Circuit Breaker, Retry y Bulkhead. El auditor legacy simula una dependencia que puede fallar. Se realizan hasta dos intentos, con 100 ms entre ellos, y se permiten cinco llamadas concurrentes. El circuito abre durante 10 segundos cuando alcanza el umbral de fallos y luego permite dos llamadas de prueba. Ante un fallo, el servicio devuelve un reporte alternativo con los datos locales.

## Estructura

| Ruta | Contenido |
|---|---|
| `pom.xml` | Configuración del proyecto Maven multimódulo |
| `config-repo/` | Propiedades centralizadas de resiliencia |
| `config-server/` | Servidor Spring Cloud Config |
| `discovery-server/` | Servidor Eureka |
| `auth-server/` | Configuración OAuth2 y generación de JWT |
| `bank-account-service/` | CSV, lógica bancaria, BFF, seguridad y productor JMS |
| `notification-service/` | Consumidor JMS y API de notificaciones |
| `Dockerfile` | Construcción multietapa de las cinco imágenes Java |
| `docker-compose.yaml` | Servicios, red, puertos, volumen y comprobaciones de salud |
| `scripts/` | Scripts de pruebas para PowerShell |
| `docs/evidencias/` | Salidas de ejecución y capturas |

## Ejecución

Se requiere Docker Desktop iniciado con contenedores Linux y Docker Compose v2. Java y Maven se ejecutan dentro de los contenedores. La primera construcción descarga las imágenes y dependencias.

Desde PowerShell, en la raíz del proyecto, crear `.env` a partir del ejemplo la primera vez:

```powershell
Copy-Item .env.example .env
```

Construir e iniciar los servicios:

```powershell
docker compose up -d --build --wait --wait-timeout 300
docker compose ps
```

Los seis contenedores deben aparecer con estado `healthy`. Las imágenes pueden consultarse con:

```powershell
docker compose images
```

Eureka está disponible en [http://localhost:8761](http://localhost:8761). Los servicios `BANK-ACCOUNT-SERVICE` y `NOTIFICATION-SERVICE` se registran después de iniciar. La configuración bancaria se consulta en [http://localhost:8888/bank-account-service/default](http://localhost:8888/bank-account-service/default).

Para revisar los registros de un servicio:

```powershell
docker compose logs --tail 100 bank-account-service
```

Para detener los contenedores conservando el volumen de Artemis:

```powershell
docker compose down
```

## Seguridad y endpoints

Las credenciales locales de ejemplo se encuentran en `.env.example`. El archivo `.env` está excluido de Git y del contexto de construcción Docker.

| Cliente OAuth2 | Scopes disponibles |
|---|---|
| `bank-api-client` | `bank.read`, `events.write`, `events.read` |
| `bank-atm-client` | `atm.read`, `atm.write` |

Para obtener un token en Postman:

1. Enviar `POST http://localhost:9000/oauth2/token`.
2. Seleccionar Basic Auth e ingresar el identificador y el secreto del cliente configurado en `.env`.
3. En el cuerpo, seleccionar `x-www-form-urlencoded` y agregar `grant_type=client_credentials` y `scope` con los permisos separados por espacios.
4. Copiar `access_token` y utilizarlo como Bearer Token en las solicitudes a la API.

Los tokens tienen una duración de 15 minutos. Las rutas bancarias utilizan el puerto 8081; la consulta de notificaciones utiliza el puerto 8082.

| Método y ruta | Scope requerido |
|---|---|
| `GET /api/accounts` | `bank.read` |
| `GET /api/accounts/106` | `bank.read` |
| `GET /api/accounts/106/migration-report` | `bank.read` |
| `GET /api/migration/quality` | `bank.read` |
| `GET /api/web/dashboard?cuentaId=106` | `bank.read` |
| `GET /api/mobile/resumen/106` | `bank.read` |
| `GET /api/atm/cuentas/106/saldo` | `atm.read` |
| `POST /api/atm/cuentas/106/retiros` | `atm.write` |
| `POST /api/events/migration/106` | `events.write` |
| `GET /api/notifications` en el puerto 8082 | `events.read` |
| `GET /actuator/circuitbreakers` | `bank.read` |
| `GET /actuator/circuitbreakerevents` | `bank.read` |

El retiro recibe un cuerpo JSON como `{"monto":1000}`. Una solicitud sin token o con un token inválido devuelve 401; un token válido sin el permiso requerido devuelve 403. HTTP Basic se utiliza para solicitar el token, mientras que las API requieren Bearer Token.

## Pruebas y evidencias

Con los contenedores saludables, ejecutar las pruebas funcionales:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\probar.ps1 -ProbarResiliencia -ProbarCola
```

El script verifica la salud de los servicios, la emisión de tokens, los permisos de acceso y la publicación y recepción de eventos con el mismo `eventId`. También comprueba la entrega de mensajes pendientes al reiniciar el consumidor, el reporte de fallback y la apertura del circuito. Guarda la salida en `docs/evidencias/prueba-FECHA-HORA.txt`.

Para ejecutar las pruebas Maven:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\pruebas-unitarias.ps1
```

La salida se guarda en `docs/evidencias/pruebas-unitarias.txt`. Con Java 17 y Maven instalados también se pueden ejecutar con `mvn test` desde la raíz. Las pruebas de seguridad del servicio bancario utilizan JWT y un productor JMS simulados; las pruebas de `auth-server` verifican la emisión de tokens mediante HTTP. El script funcional verifica la comunicación con los servicios en ejecución.

Para comprobar manualmente la resiliencia, establecer `FORCE_LEGACY_AUDIT_FAILURE=true` en `.env` y recrear el servicio:

```powershell
docker compose up -d --force-recreate bank-account-service
```

Al consultar el reporte de migración se obtiene `VALIDATED_WITH_FALLBACK`. Al restaurar la variable a `false` y recrear el servicio, el reporte vuelve a `VALIDATED`. La recreación reinicia los saldos en memoria.

## Alcance

Los datos cargados desde CSV, los saldos y las últimas 100 notificaciones se mantienen en memoria. Los retiros se pierden al reiniciar el servicio. La deduplicación de eventos se limita a la ventana de notificaciones conservadas y al proceso actual; no garantiza un procesamiento único después de un reinicio. El volumen `artemis-data` conserva los mensajes pendientes del broker.

Las claves RSA se generan al iniciar `auth-server`. Después de reiniciarlo deben solicitarse nuevos tokens. Los clientes OAuth2 también se registran en memoria.

Docker Compose permite ejecutar el conjunto en un host con Docker. Para una exposición pública se necesita configurar HTTPS, un emisor OAuth2 accesible, secretos propios y persistencia de datos y claves. En la configuración local, los puertos publicados están vinculados a `127.0.0.1` y Artemis permanece dentro de la red Docker.

## Referencias

- [Spring Authorization Server](https://docs.spring.io/spring-authorization-server/reference/getting-started.html)
- [Spring Boot 3.3 y Spring Security](https://docs.spring.io/spring-boot/3.3/reference/web/spring-security.html)
- [Apache Artemis y Docker](https://artemis.apache.org/components/artemis/documentation/latest/docker.html)
- [Datos bancarios legacy](https://github.com/KariVillagran/bank_legacy_data)
