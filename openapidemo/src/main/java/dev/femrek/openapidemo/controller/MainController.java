package dev.femrek.openapidemo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import dev.femrek.openapidemo.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Main Controller", description = "API endpoints for managing resources")
public class MainController {

    @Operation(
            summary = "Get resource by ID",
            description = "Retrieves a single resource by its unique identifier (UUID)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved resource",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid UUID format",
            content = @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Resource not found",
            content = @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)
            )
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO> getOne(
            @Parameter(description = "Resource UUID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        return ResponseEntity.ok(ResponseDTO.builder()
                .id(id)
                .title("Sample Title for ID: " + id)
                .description("This is a sample description for the resource with ID: " + id)
                .build());
    }

    @Operation(
            summary = "Create a new resource",
            description = "Creates a new resource with the provided title and description"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Resource successfully created",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request body",
            content = @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)
            )
        )
    })
    @PostMapping
    public ResponseEntity<ResponseDTO> postOne(@RequestBody PostRequestDTO request) {
        ResponseDTO response = ResponseDTO.builder()
                .id(UUID.randomUUID())
                .title(request.getTitle())
                .description(request.getDescription())
                .build();
        return ResponseEntity.ok(response);
    }
}
