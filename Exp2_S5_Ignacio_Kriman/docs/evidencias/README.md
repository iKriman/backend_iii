# Evidencias de ejecucion

La API fue ejecutada localmente en `http://localhost:18081`.

## Pruebas automaticas

Comando:

```bash
mvn -Dmaven.repo.local=target/.m2 test
```

Resultado:

```txt
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## BFF Web

Comando:

```bash
curl -H "X-Channel-Token: bff-web-token" \
  http://localhost:18081/api/bff/web/clientes/12/dashboard
```

Salida guardada:

```txt
docs/evidencias/bff-web-dashboard.json
```

Fragmento:

```json
{"channel":"WEB","customerId":"12","totalBalance":280000.00,"accountsCount":50}
```

## BFF Mobile

Comando:

```bash
curl -H "X-Channel-Token: bff-mobile-token" \
  http://localhost:18081/api/bff/mobile/clientes/12/resumen
```

Salida guardada:

```txt
docs/evidencias/bff-mobile-resumen.json
```

Fragmento:

```json
{"channel":"MOBILE","customerId":"12","totalBalance":280000.00,"accountsCount":50}
```

## BFF ATM - Login

Comando:

```bash
curl -H "Content-Type: application/json" \
  -d '{"cardNumber":"4000123412341234","pin":"1234"}' \
  http://localhost:18081/api/bff/atm/auth/login
```

Salida guardada:

```txt
docs/evidencias/bff-atm-login.json
```

## BFF ATM - Saldo

Comando:

```bash
curl -H "X-Channel-Token: bff-atm-token" \
  http://localhost:18081/api/bff/atm/cuentas/101/saldo
```

Salida guardada:

```txt
docs/evidencias/bff-atm-saldo.json
```

Fragmento:

```json
{"accountId":101,"availableBalance":22000.00}
```

## BFF ATM - Retiro

Comando:

```bash
curl -H "X-Channel-Token: bff-atm-token" \
  -H "Content-Type: application/json" \
  -d '{"amount":100}' \
  http://localhost:18081/api/bff/atm/cuentas/101/retiro
```

Salida guardada:

```txt
docs/evidencias/bff-atm-retiro.json
```

Fragmento:

```json
{"accountId":101,"withdrawnAmount":100.00,"availableBalance":21900.00}
```
