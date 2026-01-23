package dev.femrek.reactadmindataprovider.unit.product;

import dev.femrek.reactadmindataprovider.controller.RAController;
import dev.femrek.reactadmindataprovider.service.IRAService;
import dev.femrek.reactadmindataprovider.unit.User;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Extended REST controller for User entity with bulk operations support.
 * This controller extends RAControllerJSExtended to provide all standard
 * React Admin operations plus updateMany and deleteMany endpoints.
 */
@RestController
@RequestMapping("/api/users-extended")
@CrossOrigin(origins = "*")
public class UserController extends RAController<User, Long> {
    private final UserService userServiceJSExtended;

    public UserController(UserService userServiceJSExtended) {
        this.userServiceJSExtended = userServiceJSExtended;
    }

    @Override
    protected IRAService<User, Long> getService() {
        return userServiceJSExtended;
    }
}

