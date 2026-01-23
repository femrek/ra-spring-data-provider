package dev.femrek.reactadmindataprovider.unit.jsonserver;

import dev.femrek.reactadmindataprovider.jsonserver.controller.RAControllerJS;
import dev.femrek.reactadmindataprovider.jsonserver.service.IRAServiceJS;
import dev.femrek.reactadmindataprovider.unit.User;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserControllerJS extends RAControllerJS<User, Long> {
    private final UserServiceJS userServiceJS;

    public UserControllerJS(UserServiceJS userServiceJS) {
        this.userServiceJS = userServiceJS;
    }

    @Override
    protected IRAServiceJS<User, Long> getService() {
        return userServiceJS;
    }
}

