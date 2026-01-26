package dev.femrek.reactadmindataprovider.integration;

import dev.femrek.reactadmindataprovider.controller.RAController;
import dev.femrek.reactadmindataprovider.service.IRAService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
class UserController extends RAController<UserResponseDTO, UserCreateDTO, Long> {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected IRAService<UserResponseDTO, UserCreateDTO, Long> getService() {
        return userService;
    }
}
