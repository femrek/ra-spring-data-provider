package dev.femrek.openapidemo.controller;

import dev.femrek.openapidemo.dto.PostRequestDTO;
import dev.femrek.openapidemo.dto.ResponseDTO;
import dev.femrek.reactadmindataprovider.controller.RAController;
import dev.femrek.reactadmindataprovider.service.IRAService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/resoruce")
@Tag(name = "RA Controller<Post>", description = "Controller for handling RA operations with ResponseDTO and PostRequestDTO")
public class RaController extends RAController<ResponseDTO, PostRequestDTO, UUID> {
    @Override
    protected IRAService<ResponseDTO, PostRequestDTO, UUID> getService() {
        return new IRAService<>() {
            @Override
            public Page<ResponseDTO> findWithFilters(Map<String, String> filters, Pageable pageable) {
                return Page.empty();
            }

            @Override
            public Page<ResponseDTO> findWithTargetAndFilters(
                    String target,
                    String targetId,
                    Map<String, String> filters,
                    Pageable pageable
            ) {
                return Page.empty();
            }

            @Override
            public List<ResponseDTO> findAllById(Iterable<UUID> uuids) {
                return List.of(ResponseDTO.builder()
                        .id(UUID.randomUUID())
                        .title("Sample Title")
                        .description("This is a sample description")
                        .build());
            }

            @Override
            public ResponseDTO findById(UUID uuid) {
                return ResponseDTO.builder()
                        .id(uuid)
                        .title("Sample Title for ID: " + uuid)
                        .description("This is a sample description for the resource with ID: " + uuid)
                        .build();
            }

            @Override
            public ResponseDTO create(PostRequestDTO data) {
                return ResponseDTO.builder()
                        .id(UUID.randomUUID())
                        .title(data.getTitle())
                        .description(data.getDescription())
                        .build();
            }

            @Override
            public ResponseDTO update(UUID uuid, Map<String, Object> fields) {
                return ResponseDTO.builder()
                        .id(uuid)
                        .title("Updated Title for ID: " + uuid)
                        .description("This is an updated description for the resource with ID: " + uuid)
                        .build();
            }

            @Override
            public List<UUID> updateMany(Iterable<UUID> uuids, Map<String, Object> fields) {
                List<UUID> updatedIds = new ArrayList<>();
                uuids.forEach(updatedIds::add);
                return updatedIds;
            }

            @Override
            public void deleteById(UUID uuid) {
            }

            @Override
            public List<UUID> deleteMany(Iterable<UUID> uuids) {
                List<UUID> deletedIds = new ArrayList<>();
                uuids.forEach(deletedIds::add);
                return deletedIds;
            }
        };
    }
}
