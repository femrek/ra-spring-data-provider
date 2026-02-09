package dev.femrek.openapidemo.controller;

import dev.femrek.openapidemo.dto.UserCreateDTO;
import dev.femrek.openapidemo.dto.UserResponseDTO;
import dev.femrek.openapidemo.service.UserService;
import dev.femrek.reactadmindataprovider.controller.RAController;
import dev.femrek.reactadmindataprovider.service.IRAService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/openapi")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Resource: User", description = "Controller for handling RA operations with ResponseDTO and PostRequestDTO")
public class UserController extends RAController<UserResponseDTO, UserCreateDTO, Long> {
    private final UserService userService;

    @Override
    protected IRAService<UserResponseDTO, UserCreateDTO, Long> getService() {
        return userService;
    }
}
