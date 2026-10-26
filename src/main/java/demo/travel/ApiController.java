package demo.travel;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final GeminiService gemini;
    @Value("${travel.guide-name:Your Travel Guide}") private String guideName;
    @Value("${travel.guide-whatsapp:}") private String guideWhatsapp;
    @Value("${travel.guide-email:}") private String guideEmail;
    @Value("${travel.consultation-price:9.99}") private java.math.BigDecimal consultationPrice;
    @Value("${travel.consultation-currency:EUR}") private String consultationCurrency;
    public ApiController(GeminiService gemini) { this.gemini = gemini; }
    @PostMapping("/chat") public ChatDtos.ChatResponse chat(@Valid @RequestBody ChatDtos.ChatRequest request) { return gemini.reply(request); }
    @GetMapping("/health") public ChatDtos.HealthResponse health() { return new ChatDtos.HealthResponse("ok"); }
    @GetMapping("/public-config") public ChatDtos.PublicConfig publicConfig() { return new ChatDtos.PublicConfig(guideName, guideWhatsapp, guideEmail, consultationPrice, consultationCurrency); }
}
