package cl.bancoxyz.bff;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.jms.core.JmsTemplate;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
@SpringBootTest(properties={"spring.config.import=","spring.cloud.config.enabled=false","eureka.client.enabled=false","ARTEMIS_PASSWORD=test","bank.resilience.force-legacy-audit-failure=true"})
@AutoConfigureMockMvc
class BffSecurityTests {
 @Autowired MockMvc mvc;
 @MockBean JwtDecoder decoder;
 @MockBean JmsTemplate jms;
 private org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token(String scope){
  return jwt().authorities(new SimpleGrantedAuthority("SCOPE_"+scope));
 }
 @Test void missingTokenIsUnauthorized() throws Exception {mvc.perform(get("/api/accounts/106")).andExpect(status().isUnauthorized());}
 @Test void basicNoLongerAuthenticates() throws Exception {mvc.perform(get("/api/accounts/106").with(httpBasic("api-user","api123"))).andExpect(status().isUnauthorized());}
 @Test void apiCanReadAccounts() throws Exception {mvc.perform(get("/api/accounts/106").with(token("bank.read"))).andExpect(status().isOk());}
 @Test void apiCannotUseAtm() throws Exception {mvc.perform(get("/api/atm/cuentas/106/saldo").with(token("bank.read"))).andExpect(status().isForbidden());}
 @Test void atmCanReadBalance() throws Exception {mvc.perform(get("/api/atm/cuentas/106/saldo").with(token("atm.read"))).andExpect(status().isOk());}
 @Test void atmCannotReadAccounts() throws Exception {mvc.perform(get("/api/accounts/106").with(token("atm.read"))).andExpect(status().isForbidden());}
 @Test void reportFallsBack() throws Exception {
  mvc.perform(get("/api/accounts/106/migration-report").with(token("bank.read"))).andExpect(status().isOk())
   .andExpect(jsonPath("$.origen").value("fallback-local-cache")).andExpect(jsonPath("$.estado").value("VALIDATED_WITH_FALLBACK"));
 }
 @Test void eventIsPublished() throws Exception {
  mvc.perform(post("/api/events/migration/106").with(token("events.write"))).andExpect(status().isAccepted()).andExpect(jsonPath("$.eventId").exists());
  verify(jms).convertAndSend(eq("bank.migration.events"),any(String.class));
 }
 @Test void brokerFailureIsServiceUnavailable() throws Exception {
  doThrow(new org.springframework.jms.UncategorizedJmsException("broker down")).when(jms).convertAndSend(anyString(),any(String.class));
  mvc.perform(post("/api/events/migration/106").with(token("events.write"))).andExpect(status().isServiceUnavailable());
 }
 @Test void unknownAccountDoesNotPublish() throws Exception {
  mvc.perform(post("/api/events/migration/999999999").with(token("events.write"))).andExpect(status().isNotFound());
  verify(jms,never()).convertAndSend(anyString(),any(String.class));
 }
 @Test void readScopeCannotPublish() throws Exception {mvc.perform(post("/api/events/migration/106").with(token("bank.read"))).andExpect(status().isForbidden());}
 @Test void invalidBearerRejected() throws Exception {
  when(decoder.decode("invalid")).thenThrow(new org.springframework.security.oauth2.jwt.BadJwtException("invalid"));
  mvc.perform(get("/api/accounts/106").header("Authorization","Bearer invalid")).andExpect(status().isUnauthorized());
 }
}
