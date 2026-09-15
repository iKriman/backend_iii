package cl.bancoxyz.bff.controller;

import cl.bancoxyz.bff.dto.AccountDetailDto;
import cl.bancoxyz.bff.dto.DataQualityDto;
import cl.bancoxyz.bff.dto.InterestDto;
import cl.bancoxyz.bff.dto.MovementDto;
import cl.bancoxyz.bff.dto.WebDashboardDto;
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
@RequestMapping("/api/bff/web")
public class WebBffController {

    private final AuthService authService;
    private final BankService bankService;

    public WebBffController(AuthService authService, BankService bankService) {
        this.authService = authService;
        this.bankService = bankService;
    }

    @GetMapping("/clientes/{clienteId}/dashboard")
    public WebDashboardDto dashboard(
            @PathVariable String clienteId,
            @RequestHeader("X-Channel-Token") String token
    ) {
        authService.requireChannel(Channel.WEB, token);
        return bankService.webDashboard(clienteId);
    }

    @GetMapping("/cuentas/{cuentaId}")
    public AccountDetailDto account(
            @PathVariable Integer cuentaId,
            @RequestHeader("X-Channel-Token") String token
    ) {
        authService.requireChannel(Channel.WEB, token);
        return bankService.accountDetail(cuentaId);
    }

    @GetMapping("/cuentas/{cuentaId}/transacciones")
    public List<MovementDto> transactions(
            @PathVariable Integer cuentaId,
            @RequestHeader("X-Channel-Token") String token
    ) {
        authService.requireChannel(Channel.WEB, token);
        return bankService.movements(cuentaId);
    }

    @GetMapping("/cuentas/{cuentaId}/intereses")
    public InterestDto interests(
            @PathVariable Integer cuentaId,
            @RequestHeader("X-Channel-Token") String token
    ) {
        authService.requireChannel(Channel.WEB, token);
        return bankService.interestFor(cuentaId);
    }

    @GetMapping("/calidad-datos")
    public List<DataQualityDto> dataQuality(@RequestHeader("X-Channel-Token") String token) {
        authService.requireChannel(Channel.WEB, token);
        return bankService.dataQuality();
    }
}
