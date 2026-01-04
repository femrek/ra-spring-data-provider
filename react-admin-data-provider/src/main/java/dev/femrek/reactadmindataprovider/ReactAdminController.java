package dev.femrek.reactadmindataprovider;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

// T = The Entity Type (e.g., User)
// ID = The ID Type (e.g., Long or UUID)
public interface ReactAdminController<T, ID> {

    // 1. GET_LIST & GET_MANY
    // React Admin sends: GET /resource?sort=["id","DESC"]&range=[0,9]&filter={}
    // We return ResponseEntity to allow setting the "Content-Range" or "X-Total-Count" header
    @GetMapping
    ResponseEntity<List<T>> getList(
            @RequestParam(value = "filter", required = false) String filter,
            @RequestParam(value = "range", required = false) String range,
            @RequestParam(value = "sort", required = false) String sort
    );

    // 2. GET_ONE
    // React Admin sends: GET /resource/123
    @GetMapping("/{id}")
    ResponseEntity<T> getOne(@PathVariable("id") ID id);

    // 3. CREATE
    // React Admin sends: POST /resource
    @PostMapping
    ResponseEntity<T> create(@RequestBody T entity);

    // 4. UPDATE
    // React Admin sends: PUT /resource/123
    @PutMapping("/{id}")
    ResponseEntity<T> update(
            @PathVariable("id") ID id,
            @RequestBody T entity
    );

    // 5. DELETE
    // React Admin sends: DELETE /resource/123
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable("id") ID id);

    // 6. DELETE_MANY (Optional but common in RA)
    // React Admin sends: DELETE /resource?filter={"ids":[1,2,3]}
    // Note: You might need a custom POJO or Map to parse the "filter" JSON string
    @DeleteMapping
    ResponseEntity<Void> deleteMany(@RequestParam("filter") String filter);
}