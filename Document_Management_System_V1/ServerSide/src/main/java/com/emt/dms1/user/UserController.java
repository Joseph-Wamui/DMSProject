package com.emt.dms1.user;

import com.emt.dms1.auth.RegisterRequest;
import com.emt.dms1.utils.EntityResponse;
import org.springframework.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@CrossOrigin
@RestController
@Slf4j
@EnableMethodSecurity
@RequestMapping(value = "/api/v1/users" , produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    @Autowired
    private UserService userService;


    @GetMapping("/FetchAllUsers")
    public EntityResponse getAllUsers() {
        return userService.fetchUsers();
    }

    @GetMapping("/getUserByEmployeeNumber")
    public EntityResponse getUserByEmployeeNumber(@PathVariable String employeeNumber) {
        return userService.getUserByEmployeeNo(employeeNumber);
    }

    @GetMapping("/getUserById/{id}")
    public GetUserRequest getUserByID(@PathVariable int id) {
        return userService.findUserById(id);
    }

    @PutMapping("/updateUserRole")
    public EntityResponse updateUserRole(@RequestParam String userId, @RequestParam String newRole) {
        int parsedLong = Integer.parseInt(userId);
        return userService.updateUserRole(parsedLong, newRole);
    }


    @PutMapping("/UpdateUser")
    public ResponseEntity<EntityResponse> updateUser(@RequestBody RegisterRequest registerRequest,Role role) {

        EntityResponse response = userService.updateUser2(registerRequest,role);

        // Check the status code from the response and return appropriate ResponseEntity
        if (response.getStatusCode() == HttpStatus.OK.value()) {
            return ResponseEntity.ok().body(response);
        } else if (response.getStatusCode() == HttpStatus.NOT_FOUND.value()) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/deleteUser")
    public EntityResponse deleteUser(@RequestParam String userId) {
        int parsedInt = Integer.parseInt(userId);
        return userService.deleteUser(parsedInt);
    }
    // Update password

    // Check token validity
    @GetMapping("/checkToken")
    public EntityResponse<?> checkToken(@RequestParam("token") String token) {
        return userService.checkToken(token);
    }
    @GetMapping("/getUserDetails")
    public  EntityResponse getUserDetails(){return userService.getUserDetails();};

//    @PreAuthorize("hasAuthority('Create_role')")
    @PostMapping("/createRole")
    public EntityResponse createRole(
            @RequestParam String name,
            @RequestParam List<Permission> privilegesList
    ) {
//        log.info("no: "+ privilegesList.size() + "\n" + "values: " + privilegesList);
        return userService.createRole(name, privilegesList);
    }
    @PutMapping("/UpdateRole")
    public EntityResponse updateRole(
            @RequestParam String name,
            @RequestParam List<Permission> privilegesList
    ) {
        log.info("no: "+ privilegesList.size() + "\n" + "values: " + privilegesList);
        return userService.updateRole(name, privilegesList);
    }
    @GetMapping("/GetRoles")
    public EntityResponse getAllRoles() {
        return userService.getAllRoles();
    }

    @GetMapping("/privileges")
    public EntityResponse getAllPrivileges() {

        return userService.getAllPrivileges();
    }

    @GetMapping("/rolePrivileges")
    public EntityResponse getPrivileges( @RequestParam String role) {

        return userService.getPrivilegesByRole(role);
    }
}
    // Forgot password
    /*@PostMapping("/forgotPassword")
    public ResponseEntity<?> forgotPassword(@RequestBody UserModel userModel) {
        return userService.forgotPassword(userModel);
    }
}*/
