package demo.travel;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import java.util.List;

public final class ChatDtos {
    private ChatDtos() {}
    public record ChatRequest(@Size(max=80) String conversationId, @NotBlank @Size(max=2000) String message, @Valid @NotNull TravelDetails travelDetails, @Size(max=12) List<@Valid HistoryMessage> history) {}
    public record HistoryMessage(@NotBlank @Pattern(regexp="assistant|user") String role, @NotBlank @Size(max=2000) String text) {}
    public record ChatResponse(@NotBlank @Size(max=1200) String reply, @NotNull @Valid TravelDetails travelDetails) {}
    public record ErrorResponse(String message) {}
    public record HealthResponse(String status) {}
    public record PublicConfig(String guideName, String guideWhatsapp, String guideEmail, java.math.BigDecimal consultationPrice, String consultationCurrency) {}
}
