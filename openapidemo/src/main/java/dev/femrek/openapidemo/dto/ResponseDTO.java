package dev.femrek.openapidemo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Schema(description = "Response DTO containing resource data")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ResponseDTO {
    @Schema(description = "Unique identifier of the resource", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Title of the resource", example = "Sample Title")
    private String title;

    @Schema(description = "Description of the resource", example = "This is a sample description")
    private String description;
}
