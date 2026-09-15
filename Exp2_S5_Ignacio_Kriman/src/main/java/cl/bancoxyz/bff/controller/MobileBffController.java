package cl.bancoxyz.bff.controller;

import cl.bancoxyz.bff.dto.BalanceDto;
import cl.bancoxyz.bff.dto.InterestDto;
import cl.bancoxyz.bff.dto.MobileSummaryDto;
import cl.bancoxyz.bff.dto.MovementDto;
import cl.bancoxyz.bff.model.Channel;
import cl.bancoxyz.bff.service.AuthService;
import cl.bancoxyz.bff.service.BankService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bff/mobile")
public class MobileBffController {

    private final AuthService authService;
    private final BankService bankService;

    public MobileBffController(AuthService authService, BankService bankService) {
        this.authService = authService;
        this.bankService = bankService;
    }

    @GetMapping("/clientes/{clienteId}/resumen")
    public MobileSummaryDto summary(
            @PathVariable String clienteId,
            @RequestHeader("X-Channel-Token") String token
    ) {
        authService.requireChannel(Channel.MOBILE, token);
        return bankService.mobileSummary(clienteId);
    }

    @GetMapping("/cuentas/{cuentaId}/saldo")
    public BalanceDto balance(
            @PathVariable Integer cuentaId,
            @RequestHeader("X-Channel-Token") String token
    ) {
        authService.requireChannel(Channel.MOBILE, token);
        return bankService.balance(cuentaId);
    }

    @GetMapping("/cuentas/{cuentaId}/ultimas-transacciones")
    public List<MovementDto> lastTransactions(
            @PathVariable Integer cuentaId,
            @RequestHeader("X-Channel-Token") String token
    ) {
        authService.requireChannel(Channel.MOBILE, token);
        return bankService.lastMovements(cuentaId, 5);
    }

    @GetMapping("/cuentas/{cuentaId}/interes-resumen")
    public InterestDto interestSummary(
            @PathVariable Integer cuentaId,
            @RequestHeader("X-Channel-Token") String token
    ) {
        authService.requireChannel(Channel.MOBILE, token);
        return bankService.interestFor(cuentaId);
    }
}
