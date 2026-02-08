package dev.femrek.reactadmindataprovider.unit;

import dev.femrek.reactadmindataprovider.controller.RAController;
import dev.femrek.reactadmindataprovider.service.IRAService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Extended REST controller for User entity with bulk operations support.
 * This controller extends RAControllerJSExtended to provide all standard
 * React Admin operations plus updateMany and deleteMany endpoints.
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
class UserController extends RAController<UserResponseDTO, UserCreateDTO, Long> {
    private static final Log log = LogFactory.getLog(UserController.class);
    private final UserService userServiceJSExtended;

    public UserController(UserService userServiceJSExtended) {
        this.userServiceJSExtended = userServiceJSExtended;
    }

    @Override
    protected IRAService<UserResponseDTO, UserCreateDTO, Long> getService() {
        return userServiceJSExtended;
    }

    @Override
    public ResponseEntity<List<UserResponseDTO>> getList(int _start, int _end, String _sort, String _order, String _embed, Map<String, String> allParams) {
        log.info("Received getList request with params: " + allParams);
        return super.getList(_start, _end, _sort, _order, _embed, allParams);
    }

    @Override
    public ResponseEntity<List<UserResponseDTO>> getMany(List<Long> id) {
        log.info("Received getMany request with ids: " + id);
        return super.getMany(id);
    }

    @Override
    public ResponseEntity<List<UserResponseDTO>> getManyReference(String target, String targetId, int _start, int _end, String _sort, String _order, String _embed, Map<String, String> allParams) {
        log.info("Received getManyReference request with target: " + target + ", targetId: " + targetId + ", params: " + allParams);
        return super.getManyReference(target, targetId, _start, _end, _sort, _order, _embed, allParams);
    }

    @Override
    public ResponseEntity<UserResponseDTO> getOne(Long id) {
        log.info("Received getOne request with id: " + id);
        return super.getOne(id);
    }

    @Override
    public ResponseEntity<UserResponseDTO> create(UserCreateDTO data) {
        log.info("Received create request with data: " + data);
        return super.create(data);
    }

    @Override
    public ResponseEntity<UserResponseDTO> update(Long id, Map<String, Object> fields) {
        log.info("Received update request with id: " + id + ", fields: " + fields);
        return super.update(id, fields);
    }

    @Override
    public ResponseEntity<List<Long>> updateMany(List<Long> id, Map<String, Object> fields) {
        log.info("Received updateMany request with ids: " + id + ", fields: " + fields);
        return super.updateMany(id, fields);
    }

    @Override
    public ResponseEntity<Void> delete(Long id) {
        log.info("Received delete request with id: " + id);
        return super.delete(id);
    }

    @Override
    public ResponseEntity<List<Long>> deleteMany(List<Long> id) {
        log.info("Received deleteMany request with ids: " + id);
        return super.deleteMany(id);
    }
}
