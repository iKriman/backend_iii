package cl.bancoxyz.bff.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BffControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void webDashboardReturnsCompletePayload() throws Exception {
        mockMvc.perform(get("/api/bff/web/clientes/12/dashboard")
                        .header("X-Channel-Token", "bff-web-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.channel").value("WEB"))
                .andExpect(jsonPath("$.accountsCount", greaterThan(0)))
                .andExpect(jsonPath("$.accounts", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.dataQuality", hasSize(3)));
    }

    @Test
    void mobileSummaryReturnsLightPayload() throws Exception {
        mockMvc.perform(get("/api/bff/mobile/clientes/12/resumen")
                        .header("X-Channel-Token", "bff-mobile-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.channel").value("MOBILE"))
                .andExpect(jsonPath("$.accountsCount", greaterThan(0)))
                .andExpect(jsonPath("$.accounts[0].accountId").exists());
    }

    @Test
    void atmLoginReturnsAtmToken() throws Exception {
        mockMvc.perform(post("/api/bff/atm/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cardNumber": "4000123412341234",
                                  "pin": "1234"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.channel").value("ATM"))
                .andExpect(jsonPath("$.token").value("bff-atm-token"));
    }

    @Test
    void atmCanWithdrawWithAtmToken() throws Exception {
        mockMvc.perform(post("/api/bff/atm/cuentas/101/retiro")
                        .header("X-Channel-Token", "bff-atm-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 100
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(101))
                .andExpect(jsonPath("$.withdrawnAmount").value(100.00));
    }

    @Test
    void rejectsTokenFromAnotherChannel() throws Exception {
        mockMvc.perform(get("/api/bff/atm/cuentas/101/saldo")
                        .header("X-Channel-Token", "bff-web-token"))
                .andExpect(status().isUnauthorized());
    }
}
