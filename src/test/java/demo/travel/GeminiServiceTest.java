package demo.travel;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class GeminiServiceTest {
    private GeminiService service() {
        return new GeminiService(new ObjectMapper(), Validation.buildDefaultValidatorFactory().getValidator(), "test-key", "gemini-3.8-flash",
                RestClient.builder().baseUrl("https://generativelanguage.googleapis.com/v1beta").build());
    }
    @Test void acceptsValidStructuredModelResponse() throws Exception {
        var response = service().parseResponse("""
          {"reply":"How long would you like to stay?","travelDetails":{"destination":"Ooty","durationDays":3,"startDate":null,"endDate":null,"travellers":{"adults":2,"children":0},"departureCity":null,"budget":25000,"currency":"EUR","interests":["nature"],"travelStyle":null,"accommodationPreferences":[],"transportPreferences":[],"specialRequirements":[]}}
          """);
        assertEquals("Ooty", response.travelDetails().destination());
        assertEquals(2, response.travelDetails().travellers().adults());
    }
    @Test void rejectsMalformedJsonFromModel() {
        assertThrows(Exception.class, () -> service().parseResponse("not-json"));
    }
    @Test void rejectsMissingRequiredModelFields() {
        assertThrows(IllegalArgumentException.class, () -> service().parseResponse("""
          {"reply":"Hi","travelDetails":{"currency":"EUR"}}
          """));
    }

    @Test void sendsGeminiThreeCompatibleRequestAndParsesResponseWithoutLiveApi() throws Exception {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        var gemini = new GeminiService(new ObjectMapper(), Validation.buildDefaultValidatorFactory().getValidator(),
                "unit-test-key", "gemini-3.8-flash",
                builder.baseUrl("https://generativelanguage.googleapis.com/v1beta").build());
        String modelOutput = """
                {"reply":"What dates work for you?","travelDetails":{"destination":"Ooty","durationDays":3,"startDate":null,"endDate":null,"travellers":{"adults":2,"children":0},"departureCity":null,"budget":25000,"currency":"EUR","interests":[],"travelStyle":null,"accommodationPreferences":[],"transportPreferences":[],"specialRequirements":[]}}
                """;
        String providerResponse = new ObjectMapper().writeValueAsString(java.util.Map.of("candidates", java.util.List.of(
                java.util.Map.of("content", java.util.Map.of("parts", java.util.List.of(java.util.Map.of("text", modelOutput)))))));
        server.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(header("x-goog-api-key", "unit-test-key"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("temperature"))))
                .andRespond(withSuccess(providerResponse, MediaType.APPLICATION_JSON));

        TravelDetails details = new TravelDetails(null, null, null, null, null,
                new TravelDetails.Travellers(2, 0), null, null, "EUR", java.util.List.of(), null,
                java.util.List.of(), java.util.List.of(), java.util.List.of());
        var response = gemini.reply(new ChatDtos.ChatRequest("test", "Plan a trip to Ooty", details, java.util.List.of()));
        assertEquals("What dates work for you?", response.reply());
        server.verify();
    }

    @Test void preservesProviderFailureAsCauseAndDoesNotExposeItToApiMessage() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        var gemini = new GeminiService(new ObjectMapper(), Validation.buildDefaultValidatorFactory().getValidator(),
                "unit-test-key", "gemini-3.8-flash",
                builder.baseUrl("https://generativelanguage.googleapis.com/v1beta").build());
        server.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent"))
                .andRespond(withStatus(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":{\"message\":\"private provider diagnostic\"}}"));

        TravelDetails details = new TravelDetails(null, null, null, null, null,
                new TravelDetails.Travellers(1, 0), null, null, "EUR", java.util.List.of(), null,
                java.util.List.of(), java.util.List.of(), java.util.List.of());
        var error = assertThrows(GeminiService.AiUnavailableException.class, () -> gemini.reply(
                new ChatDtos.ChatRequest("test", "Hello", details, java.util.List.of())));
        assertEquals("The travel assistant is temporarily unavailable.", error.getMessage());
        assertNotNull(error.getCause());
        assertTrue(error.getCause() instanceof org.springframework.web.client.RestClientResponseException);
        server.verify();
    }
}
