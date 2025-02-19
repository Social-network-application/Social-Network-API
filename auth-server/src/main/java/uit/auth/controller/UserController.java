package uit.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uit.auth.entity.User;
import uit.auth.service.UserService;

import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    public List<User> getAll(@RequestParam(defaultValue = "", required = false) String id,
                             @RequestParam(defaultValue = "", required = false) String username,
                             @RequestParam(defaultValue = "", required = false) String email) {
        return userService.getAll(id, username, email);
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @GetMapping("/username/{username}")
    public User getByUserName(@PathVariable String username) {
        return userService.getByUsername(username);
    }

    @PostMapping("/register")
    public User create(@RequestBody User user) throws Exception {
        return userService.create(user);
    }

    @PostMapping("/admin/create")
    public User createAdmin(@RequestBody User user) throws Exception {
        return userService.createAdmin(user);
    }

    @PutMapping("/{id}")
    public User update(@PathVariable Long id,
                       @RequestParam(defaultValue = "", required = false) String username,
                       @RequestParam(defaultValue = "", required = false) String email,
                       @RequestParam(defaultValue = "", required = false) String password,
                       @RequestParam(defaultValue = "", required = false) String gender,
                       @RequestParam(defaultValue = "", required = false) String birthday,
                       @RequestParam(defaultValue = "", required = false) String status,
                       @RequestParam(defaultValue = "", required = false) String role,
                       @RequestParam(defaultValue = "", required = false) String hometown,
                       @RequestParam(defaultValue = "", required = false) String address,
                       @RequestPart(value = "file", required = false) MultipartFile file
    ) throws ParseException {
        return userService.update(id, username, email, password, gender, birthday, status, role, hometown, address, file);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        userService.deleteById(id);
    }

    @PostMapping("/disable/{userId}")
    public User disableUser(@PathVariable Long userId) throws Exception {
        return userService.disableUser(userId);
    }

    @PostMapping("/enable/{userId}")
    public User enableUser(@PathVariable Long userId) throws Exception {
        return userService.enableUser(userId);
    }

    @GetMapping("/admin/test")
    public String helloAdmin() {
        return "Hello Admin";
    }
}
