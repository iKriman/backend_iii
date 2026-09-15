package cl.bancoxyz.bff.controller;

import cl.bancoxyz.bff.dto.AtmLoginRequest;
import cl.bancoxyz.bff.dto.AtmLoginResponse;
import cl.bancoxyz.bff.dto.AtmWithdrawalRequest;
import cl.bancoxyz.bff.dto.AtmWithdrawalResponse;
import cl.bancoxyz.bff.dto.BalanceDto;
import cl.bancoxyz.bff.dto.MovementDto;
import cl.bancoxyz.bff.model.Channel;
import cl.bancoxyz.bff.service.AuthService;
import cl.bancoxyz.bff.service.BankService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bff/atm")
public class AtmBffController {

    private final AuthService authService;
    private final BankService bankService;

    public AtmBffController(AuthService authService, BankService bankService) {
        this.authService = authService;
        this.bankService = bankService;
    }

    @PostMapping("/auth/login")
    public AtmLoginResponse login(@Valid @RequestBody AtmLoginRequest request) {
        if (!"1234".equals(request.pin())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "PIN no valido");
        }
        return new AtmLoginResponse(
                "ATM",
                authService.tokenFor(Channel.ATM),
                LocalDateTime.now().plusMinutes(10)
        );
    }

    @GetMapping("/cuentas/{cuentaId}/saldo")
    public BalanceDto balance(
            @PathVariable Integer cuentaId,
            @RequestHeader("X-Channel-Token") String token
    ) {
        authService.requireChannel(Channel.ATM, token);
        return bankService.balance(cuentaId);
    }

    @PostMapping("/cuentas/{cuentaId}/retiro")
    public AtmWithdrawalResponse withdraw(
            @PathVariable Integer cuentaId,
            @Valid @RequestBody AtmWithdrawalRequest request,
            @RequestHeader("X-Channel-Token") String token
    ) {
        authService.requireChannel(Channel.ATM, token);
        return bankService.withdraw(cuentaId, request.amount());
    }

    @GetMapping("/cuentas/{cuentaId}/ultimos-movimientos")
    public List<MovementDto> lastMovements(
            @PathVariable Integer cuentaId,
            @RequestHeader("X-Channel-Token") String token
    ) {
        authService.requireChannel(Channel.ATM, token);
        return bankService.lastMovements(cuentaId, 3);
    }

    @PostMapping("/auth/logout")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> logout(@RequestHeader("X-Channel-Token") String token) {
        authService.requireChannel(Channel.ATM, token);
        return Map.of("status", "sesion ATM finalizada");
    }
}
