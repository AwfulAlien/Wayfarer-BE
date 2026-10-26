package demo.travel;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record TravelDetails(
    @Size(max=160) String destination,
    @Min(1) @Max(365) Integer durationDays,
    @Pattern(regexp="^$|^\\d{4}-\\d{2}-\\d{2}$") String startDate,
    @Pattern(regexp="^$|^\\d{4}-\\d{2}-\\d{2}$") String endDate,
    @Size(max=120) String dateNotes,
    @Valid @NotNull Travellers travellers,
    @Size(max=100) String departureCity,
    @DecimalMin("0") @DecimalMax("1000000000") Double budget,
    @NotBlank @Pattern(regexp="^[A-Z]{3}$") String currency,
    @NotNull @Size(max=20) List<@Size(max=60) String> interests,
    @Size(max=100) String travelStyle,
    @NotNull @Size(max=20) List<@Size(max=60) String> accommodationPreferences,
    @NotNull @Size(max=20) List<@Size(max=60) String> transportPreferences,
    @NotNull @Size(max=30) List<@Size(max=120) String> specialRequirements
) {
    public record Travellers(@Min(0) @Max(50) int adults, @Min(0) @Max(50) int children) {}
}
