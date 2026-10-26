package demo.travel;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import demo.travel.GeminiService.AiUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ApiController.class)
@Import(ApiExceptionHandler.class)
class ApiControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean GeminiService gemini;
    private static final String valid = """
      {"conversationId":"demo","message":"Plan a trip to Ooty","travelDetails":{"destination":null,"durationDays":null,"startDate":null,"endDate":null,"travellers":{"adults":0,"children":0},"departureCity":null,"budget":null,"currency":"EUR","interests":[],"travelStyle":null,"accommodationPreferences":[],"transportPreferences":[],"specialRequirements":[]}}
      """;
    @Test void healthReturnsOk() throws Exception { mvc.perform(get("/api/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ok")); }
    @Test void rejectsBlankMessage() throws Exception { mvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON).content(valid.replace("Plan a trip to Ooty", ""))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists()); }
    @Test void rejectsInvalidCurrency() throws Exception { mvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON).content(valid.replace("EUR", "Euro"))).andExpect(status().isBadRequest()); }
    @Test void returnsStructuredReply() throws Exception {
      TravelDetails details = new TravelDetails("Ooty", 3, null, null, null, new TravelDetails.Travellers(2,0), null, null, "EUR", java.util.List.of(), null, java.util.List.of(), java.util.List.of(), java.util.List.of());
      when(gemini.reply(any())).thenReturn(new ChatDtos.ChatResponse("Lovely! What month?", details));
      mvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON).content(valid)).andExpect(status().isOk()).andExpect(jsonPath("$.reply").value("Lovely! What month?" )).andExpect(jsonPath("$.travelDetails.destination").value("Ooty"));
    }
    @Test void hidesAiErrorsBehindFriendly503() throws Exception {
      when(gemini.reply(any())).thenThrow(new AiUnavailableException("The travel assistant is temporarily unavailable."));
      mvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON).content(valid)).andExpect(status().isServiceUnavailable()).andExpect(jsonPath("$.message").value("The travel assistant is temporarily unavailable."));
    }
    @Test void publicConfigContainsGuideSettingsWithoutSecrets() throws Exception { mvc.perform(get("/api/public-config")).andExpect(status().isOk()).andExpect(jsonPath("$.guideName").exists()).andExpect(jsonPath("$.consultationPrice").value(9.99)).andExpect(jsonPath("$.consultationCurrency").value("EUR")).andExpect(jsonPath("$.geminiApiKey").doesNotExist()); }
}
