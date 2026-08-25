# Migracion Batch Legacy Banco XYZ

Proyecto Spring Batch para modernizar tres procesos legacy del Banco XYZ usando los datos del repositorio base [KariVillagran/bank_legacy_data](https://github.com/KariVillagran/bank_legacy_data), especificamente los CSV de `data/semana_1`.

## Objetivo

Implementar Jobs batch que lean archivos CSV, validen y transformen la informacion con `ItemProcessor`, escriban resultados en una base relacional y generen evidencia de salida para auditoria.

## Procesos implementados

1. `reporteTransaccionesDiariasJob`
   - Lee `transacciones.csv`.
   - Valida fecha, monto, tipo y duplicados por fecha/monto/tipo.
   - Detecta anomalias de montos negativos o cero.
   - Escribe en `daily_transaction_report`.
   - Genera `target/reports/reporte_transacciones_diarias.csv`.

2. `calculoInteresesMensualesJob`
   - Lee `intereses.csv`.
   - Valida saldo, edad, tipo de cuenta y duplicados.
   - Calcula intereses mensuales para cuentas de ahorro y prestamo.
   - Escribe saldo final en `monthly_interest_result`.
   - Genera `target/reports/calculo_intereses_mensuales.csv`.

3. `estadosCuentaAnualesJob`
   - Lee `cuentas_anuales.csv`.
   - Valida fechas, tipo de movimiento, descripcion y consistencia del signo del monto.
   - Clasifica movimientos relevantes para auditoria.
   - Escribe en `annual_statement_detail`.
   - Genera `target/reports/estados_cuenta_anuales.csv`.

Los registros rechazados se guardan en la tabla `legacy_errors` con el Job, llave fuente, motivo y payload original.

## Estructura

```text
src/main/java/cl/bancoxyz/batch
  config/BatchConfig.java                 Configuracion de Jobs, Steps, Readers y Writers
  domain/                                 Records de entrada y salida
  processor/                              Reglas de validacion y transformacion
  writer/ReportExportListener.java        Exportacion de reportes CSV
src/main/resources
  data/                                   CSV del repositorio legacy
  schema.sql                              Tablas relacionales
  application.properties                  Ejecucion local con H2 en memoria
  application-postgres.properties         Perfil PostgreSQL
evidencia/                                Salidas y resultados para entrega
```

## Requisitos

- Java 17 o superior.
- Maven 3.9 o superior.
- Opcional: Docker para levantar PostgreSQL.

## Ejecucion local

Compilar:

```bash
mvn test
```

Ejecutar todos los Jobs:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--job=all
```

Ejecutar un Job especifico:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--job=transacciones
mvn spring-boot:run -Dspring-boot.run.arguments=--job=intereses
mvn spring-boot:run -Dspring-boot.run.arguments=--job=anuales
```

## Ejecucion con PostgreSQL

Levantar base:

```bash
docker compose up -d
```

Ejecutar con perfil PostgreSQL:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgres -Dspring-boot.run.arguments=--job=all
```

## Evidencia esperada

Salida final de consola:

```text
daily_transaction_report: 9 registros
monthly_interest_result: 4 registros
annual_statement_detail: 8 registros
legacy_errors: 6 registros
```

Reportes generados:

- `target/reports/reporte_transacciones_diarias.csv`
- `target/reports/calculo_intereses_mensuales.csv`
- `target/reports/estados_cuenta_anuales.csv`

## Propuesta tecnica

La solucion usa Spring Batch con procesamiento por chunks para mejorar rendimiento y control transaccional. Cada archivo CSV tiene un `FlatFileItemReader`, un `ItemProcessor` con reglas de consistencia y un `JdbcBatchItemWriter` para persistir los datos procesados. Los errores de calidad de datos no detienen el proceso: se omiten del flujo principal y se registran en `legacy_errors`, permitiendo auditoria posterior.
