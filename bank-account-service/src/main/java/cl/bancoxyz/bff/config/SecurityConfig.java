package cl.bancoxyz.bff.config;
import org.springframework.context.annotation.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
@Configuration
public class SecurityConfig {
 @Bean SecurityFilterChain security(HttpSecurity http) throws Exception {
  return http.csrf(c -> c.disable())
   .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .authorizeHttpRequests(a -> a.requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/atm/**").hasAuthority("SCOPE_atm.read")
    .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/atm/**").hasAuthority("SCOPE_atm.write")
    .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/events/**").hasAuthority("SCOPE_events.write")
    .requestMatchers("/api/accounts/**", "/api/migration/**", "/api/web/**", "/api/mobile/**", "/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/actuator/circuitbreakers", "/actuator/circuitbreakerevents").hasAuthority("SCOPE_bank.read")
    .anyRequest().denyAll())
   .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults())).build();
 }
}
