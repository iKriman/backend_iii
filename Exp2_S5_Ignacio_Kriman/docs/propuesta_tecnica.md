# Propuesta tecnica - Patron Backend for Frontend

## Contexto

Banco XYZ requiere exponer datos legacy a tres tipos de clientes: navegadores web, aplicaciones moviles y cajeros automaticos. Cada canal tiene necesidades distintas de payload, seguridad y experiencia de usuario.

## Estrategia seleccionada

Se selecciona una estrategia de tres BFF logicos dentro de un mismo proyecto Spring Boot:

- BFF Web para entregar datos completos y soportar pantallas con mayor detalle.
- BFF Mobile para entregar datos esenciales y disminuir consumo de ancho de banda.
- BFF ATM para limitar operaciones a flujos criticos: login, saldo, retiro y ultimos movimientos.

Esta estrategia mantiene el patron BFF porque cada frontend consume un backend adaptado a su necesidad. Al mismo tiempo, se usa una capa comun de servicios para evitar duplicacion de logica bancaria.

## Componentes

```txt
Frontend Web    -> WebBffController    -> BankService -> BankDataRepository -> CSV legacy
Frontend Mobile -> MobileBffController -> BankService -> BankDataRepository -> CSV legacy
Cajero ATM      -> AtmBffController    -> BankService -> BankDataRepository -> CSV legacy
```

## Seguridad por canal

La autenticacion y autorizacion se simula mediante tokens por canal:

- `bff-web-token` permite acceder solo a endpoints Web.
- `bff-mobile-token` permite acceder solo a endpoints Mobile.
- `bff-atm-token` permite acceder solo a endpoints ATM.

Si un token de Web intenta llamar a ATM, la API responde `401 Unauthorized`. Esto representa una separacion de permisos por canal.

## Datos legacy

La aplicacion usa:

- `intereses.csv`: cuentas, cliente, saldo, edad y tipo de producto.
- `cuentas_anuales.csv`: movimientos historicos por cuenta.
- `transacciones.csv`: transacciones diarias generales.

Durante la carga se validan fechas, montos, tipos permitidos y duplicados. Las filas invalidas se omiten para proteger la consistencia de los datos expuestos por los BFF.

## Decision sobre saldos

Los datos legacy contienen montos negativos, registros faltantes y operaciones que pueden dejar saldos inconsistentes. Para exponer datos seguros al cliente, el servicio calcula `availableBalance` y no permite que el saldo disponible mostrado sea menor que cero.

## Beneficios

- Menor acoplamiento entre clientes y datos legacy.
- Payload completo para Web y ligero para Mobile.
- Superficie reducida y controlada para ATM.
- Reutilizacion de reglas comunes sin perder separacion BFF.
- Mejor trazabilidad mediante reporte de calidad de datos.
