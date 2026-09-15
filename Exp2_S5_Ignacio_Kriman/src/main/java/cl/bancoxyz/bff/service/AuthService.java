package cl.bancoxyz.bff.service;

import cl.bancoxyz.bff.model.Channel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    @Value("${bff.security.token.web}")
    private String webToken;

    @Value("${bff.security.token.mobile}")
    private String mobileToken;

    @Value("${bff.security.token.atm}")
    private String atmToken;

 
    public String tokenFor(Channel channel) {
        return switch (channel) {
            case WEB -> webToken;
            case MOBILE -> mobileToken;
            case ATM -> atmToken;
        };
    }


    public void requireChannel(Channel channel, String providedToken) {
        if (providedToken == null || providedToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token no proporcionado");
        }

        String expectedToken = tokenFor(channel);
        
        if (!expectedToken.equals(providedToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Acceso denegado: Token inválido para el canal " + channel);
        }
    }
}