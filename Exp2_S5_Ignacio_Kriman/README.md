# Banco XYZ - Backend for Frontend con Spring Boot

Proyecto academico para implementar el patron Backend for Frontend (BFF) en el Banco XYZ. La solucion expone tres backends logicos para clientes Web, Mobile y Cajeros Automaticos, reutilizando una capa comun de servicios bancarios y lectura de datos legacy.

## Objetivo

Optimizar la comunicacion entre distintos clientes del Banco XYZ mediante APIs adaptadas por canal:

- BFF Web: entrega informacion completa para interfaces de escritorio.
- BFF Mobile: entrega respuestas ligeras para reducir ancho de banda.
- BFF ATM: entrega operaciones criticas y acotadas para cajeros automaticos.

Los datos se basan en el repositorio indicado por la actividad:

https://github.com/KariVillagran/bank_legacy_data

Se copiaron los CSV de `data/semana_3` a `src/main/resources/data` para que la aplicacion pueda ejecutarse sin depender de internet.

## Estrategia BFF

La estrategia usada es BFF por canal dentro de un mismo proyecto Spring Boot. Cada cliente tiene su propio controlador y sus propios DTO de respuesta:

- `WebBffController`: respuestas completas, datos de cuentas, movimientos, intereses y calidad de datos.
- `MobileBffController`: respuestas resumidas, saldos y ultimos movimientos.
- `AtmBffController`: autenticacion simple de cajero, consulta de saldo, retiro y cierre de sesion.

La logica comun se centraliza en `BankService` y la lectura de datos legacy en `BankDataRepository`. Esto evita duplicar reglas de negocio y mantiene la separacion de experiencia por frontend.

## Estructura

```txt
src/main/java/cl/bancoxyz/bff
├── controller   # BFF Web, Mobile y ATM
├── dto          # Respuestas diferentes para cada canal
├── model        # Modelos internos
├── repository   # Carga y limpieza de CSV legacy
└── service      # Autenticacion por canal y logica bancaria comun

src/main/resources/data
├── transacciones.csv
├── intereses.csv
└── cuentas_anuales.csv

docs
├── propuesta_tecnica.md
└── evidencias
    ├── bff-web-dashboard.json
    ├── bff-mobile-resumen.json
    ├── bff-atm-login.json
    ├── bff-atm-saldo.json
    └── bff-atm-retiro.json
```

## Requisitos

- Java 17 o superior.
- Maven 3.9 o superior.

## Ejecutar

```bash
mvn spring-boot:run
```

Por defecto la API usa el puerto `8080`. Si ese puerto esta ocupado:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=18081
```

## Tokens por canal

La autenticacion se simula mediante el header `X-Channel-Token`.

```txt
WEB:    bff-web-token
MOBILE: bff-mobile-token
ATM:    bff-atm-token
```

Cada BFF rechaza tokens de otro canal con HTTP `401 Unauthorized`.

## Endpoints

### BFF Web

```txt
GET /api/bff/web/clientes/{clienteId}/dashboard
GET /api/bff/web/cuentas/{cuentaId}
GET /api/bff/web/cuentas/{cuentaId}/transacciones
GET /api/bff/web/cuentas/{cuentaId}/intereses
GET /api/bff/web/calidad-datos
```

Ejemplo:

```bash
curl -H "X-Channel-Token: bff-web-token" \
  http://localhost:8080/api/bff/web/clientes/12/dashboard
```

### BFF Mobile

```txt
GET /api/bff/mobile/clientes/{clienteId}/resumen
GET /api/bff/mobile/cuentas/{cuentaId}/saldo
GET /api/bff/mobile/cuentas/{cuentaId}/ultimas-transacciones
GET /api/bff/mobile/cuentas/{cuentaId}/interes-resumen
```

Ejemplo:

```bash
curl -H "X-Channel-Token: bff-mobile-token" \
  http://localhost:8080/api/bff/mobile/clientes/12/resumen
```

### BFF ATM

```txt
POST /api/bff/atm/auth/login
GET  /api/bff/atm/cuentas/{cuentaId}/saldo
POST /api/bff/atm/cuentas/{cuentaId}/retiro
GET  /api/bff/atm/cuentas/{cuentaId}/ultimos-movimientos
POST /api/bff/atm/auth/logout
```

Login ATM:

```bash
curl -H "Content-Type: application/json" \
  -d '{"cardNumber":"4000123412341234","pin":"1234"}' \
  http://localhost:8080/api/bff/atm/auth/login
```

Retiro ATM:

```bash
curl -H "X-Channel-Token: bff-atm-token" \
  -H "Content-Type: application/json" \
  -d '{"amount":100}' \
  http://localhost:8080/api/bff/atm/cuentas/101/retiro
```

## Calidad e integridad de datos

Al cargar los CSV se aplican validaciones:

- Fechas en formatos `yyyy-MM-dd`, `dd-MM-yyyy`, `dd/MM/yyyy` y `yyyy/MM/dd`.
- Montos mayores a cero.
- Tipos de producto validos: `ahorro`, `prestamo`, `hipoteca`.
- Tipos de transaccion validos: `deposito`, `retiro`, `compra`, `pago`.
- Omision de filas duplicadas.
- Valores faltantes o invalidos se rechazan y se reportan en `/api/bff/web/calidad-datos`.

## Pruebas

```bash
mvn test
```

Resultado verificado:

```txt
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Evidencia de ejecucion

Las salidas reales de consola/API estan en:

```txt
docs/evidencias/
```

Se generaron ejecutando la API localmente en el puerto `18081` y llamando a cada BFF con `curl`.
