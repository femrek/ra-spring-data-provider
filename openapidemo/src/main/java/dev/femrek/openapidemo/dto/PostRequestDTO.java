package dev.femrek.openapidemo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Request DTO for creating a new resource")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class PostRequestDTO {
    @Schema(description = "Title of the resource",
            example = "Sample Title",
            requiredMode = REQUIRED)
    private String title;

    @Schema(description = "Description of the resource",
            example = "This is a sample description",
            requiredMode = REQUIRED)
    private String description;
}
