package cl.bancoxyz.bff.controller;

import cl.bancoxyz.bff.dto.AtmBalanceResponse;
import cl.bancoxyz.bff.dto.AtmWithdrawalRequest;
import cl.bancoxyz.bff.dto.AtmWithdrawalResponse;
import cl.bancoxyz.bff.service.AccountLedgerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/atm")
public class AtmBffController {

    private final AccountLedgerService accountLedgerService;

    public AtmBffController(AccountLedgerService accountLedgerService) {
        this.accountLedgerService = accountLedgerService;
    }

    @GetMapping("/cuentas/{cuentaId}/saldo")
    public ResponseEntity<AtmBalanceResponse> balance(@PathVariable long cuentaId) {
        return accountLedgerService.balance(cuentaId)
                .map(balance -> ResponseEntity.ok(new AtmBalanceResponse("atm", cuentaId, balance)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/cuentas/{cuentaId}/retiros")
    public ResponseEntity<AtmWithdrawalResponse> withdraw(@PathVariable long cuentaId, @Valid @RequestBody AtmWithdrawalRequest request) {
        AccountLedgerService.WithdrawalResult result = accountLedgerService.withdraw(cuentaId, request.monto());
        AtmWithdrawalResponse response = new AtmWithdrawalResponse(
                "atm",
                cuentaId,
                result.aprobado(),
                result.mensaje(),
                result.saldoDisponible());
        return result.aprobado() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }
}
