package  com.emt.dms1.user;
import com.emt.dms1.auth.RegisterRequest;
import com.emt.dms1.config.JwtService;
import com.emt.dms1.userAudit.UserLogService;
import com.emt.dms1.userAudit.UserLogType;
import com.emt.dms1.utils.EntityResponse;
import com.emt.dms1.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class  UserService {
    @Autowired
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final  UserLogService userLogService;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    private User userDetails;

    public UserService(UserRepository userRepository, JwtService jwtService, UserLogService userLogService, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.userLogService = userLogService;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    public EntityResponse updateUser2(RegisterRequest request, Role role) {
        EntityResponse entityResponse = new EntityResponse<>();
        try {
            log.info("-----> Updating User <-------");

            // Check if the user exists
            Optional<User> optionalUser = userRepository.getUserByEmployeeNumber(request.getEmployeeNumber());
            if (optionalUser.isPresent()) {
                User existingUser = optionalUser.get();
                // Update user attributes
                existingUser.setFirstName(request.getFirstname());
                existingUser.setLastName(request.getLastname());
                existingUser.setEmailAddress(request.getEmailAddress());
                existingUser.setDepartment(existingUser.getDepartment());
                existingUser.setPhoneNumber(existingUser.getPhoneNumber());
                existingUser.setRole(role);
                existingUser.setPassword(passwordEncoder.encode(existingUser.getPassword()));

                // Save the updated user
                User updatedUser = userRepository.save(existingUser);

                entityResponse.setMessage("User updated successfully");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(updatedUser);
            } else {
                log.warn("'''''''''''''''warning''''''''''''''");
                log.warn("--> USER WITH THIS ID DOES NOT EXIST <-- ");
                log.warn(",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");

                entityResponse.setMessage("User with the Employee Number does not exist");
                entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                entityResponse.setEntity(null);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            log.error("--> ERROR WHILE UPDATING USER BY Employee Number <--: " + exception.getLocalizedMessage());
            entityResponse.setMessage(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase() + exception.getLocalizedMessage());
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            entityResponse.setEntity(null);
        }
        return entityResponse;
    }


    public EntityResponse getUserByEmployeeNo(String employeeNumber) {
        EntityResponse entityResponse = new EntityResponse<>();
        try {
            log.info("----->FETCHING USERS<-------");
            Optional<User> exists = userRepository.getUserByEmployeeNumber(employeeNumber);
            if (exists.isPresent()) {
                UserDTO userDTO= mapEntityToDTO(exists.get());
                entityResponse.setMessage("User with Employee Number: " + employeeNumber + "found");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(userDTO);
            } else {
                log.warn("'''''''''''''''warning''''''''''''''");
                log.warn("-->USER WITH THIS  Employee Number DOES NOT EXIST <-- ");
                log.warn(",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");

                entityResponse.setMessage("User with the Employee Number does not exist");
                entityResponse.setStatusCode(HttpStatus.CONFLICT.value());
                entityResponse.setEntity(null);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            log.error("--> ERROR WHILE FETCHING USER BY Employee Number <--: " + exception.getLocalizedMessage());
            entityResponse.setMessage(HttpStatus.EXPECTATION_FAILED.getReasonPhrase() + exception.getLocalizedMessage());
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            entityResponse.setEntity(null);
        }
        return entityResponse;
    }

    public GetUserRequest findUserById(int id) {
        User user = userRepository.findById(id)

                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        return GetUserRequest.builder()
                .id(user.getId())
                .firstname(user.getFirstName())
                .lastname(user.getLastName())
                .emailAddress(user.getEmailAddress())
                .role(user.getRole())
                .build();
    }

    public EntityResponse updateUserRole(int userId, String newRole) {
        EntityResponse entityResponse = new EntityResponse<>();
        // Retrieve the user by ID
        Optional<User> users = userRepository.findById(userId);
        // Check if the list is empty
        if (users.isEmpty()) {
            entityResponse.setMessage(String.format("User with id %d not found",userId));
            entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
        }else {
            User user = users.get();
            Optional<Role> optionalRole = roleRepository.findByName(newRole);
            System.out.println("OPT role: "+ optionalRole);

            if (optionalRole.isEmpty()) {
                entityResponse.setMessage(String.format("Role with name %s not found",newRole));
                entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
            }else {
                Role updatedRole = optionalRole.get();
                // Update the user's role
                user.setRole(updatedRole);
                // Save the updated user
                User updatedUser = userRepository.save(user);
                entityResponse.setMessage("Role changed successfully to: "+ newRole);
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(updatedUser);
                userLogService.logUserAction(user.getEmailAddress(), UserLogType.Rolechanged, user);
            }
        }
        return entityResponse;
    }

    public EntityResponse fetchUsers() {
        EntityResponse entityResponse = new EntityResponse<>();

        try {
            log.info("----->LISTING USERS<-------");
            List<User> employeeList = userRepository.findByDeletedFlagOrderByIdAsc('N');
            if (employeeList.isEmpty()) {
                log.warn("'''''''''''''''warning''''''''''''''");
                log.warn("--------> DATABASE IS EMPTY <------ ");
                log.warn(",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");


                entityResponse.setMessage("User list is empty");
                entityResponse.setStatusCode(HttpStatus.CONFLICT.value());
                entityResponse.setEntity(null);
            } else {
                //
                List<UserDTO> dtoList= mapEntitiesToDTOs(employeeList);
                entityResponse.setMessage("Users found");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(dtoList);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            log.error("--> ERROR WHILE FETCHING ALL EMPLOYEES<--: " + exception.getLocalizedMessage());
            entityResponse.setMessage(HttpStatus.EXPECTATION_FAILED.getReasonPhrase() + exception.getLocalizedMessage());
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            entityResponse.setEntity(null);
        }
        return entityResponse;
    }

    public EntityResponse updateUser(User user) {
        EntityResponse entityResponse = new EntityResponse<>();
        try {
            log.info("----->Updating User<-------");
            Optional<User> exists = userRepository.getUserByEmployeeNumber(user.getEmployeeNumber());
            if (exists.isPresent()) {
                entityResponse.setMessage("User with ID " + user.getEmployeeNumber() + "found");

                user.setEmployeeNumber(user.getEmployeeNumber());
                user.setFirstName(user.getFirstName());
                user.setLastName(user.getLastName());
                user.setEmailAddress(user.getEmailAddress());
                user.setPhoneNumber(user.getPhoneNumber());
                user.setDepartment(user.getDepartment());
                user.setRole(user.getRole());
                user.setPassword(user.getPassword());
                LocalDate currentDate = LocalDate.now();
                user.setCreatedOn(currentDate);
                //
                userRepository.save(user);
                entityResponse.setMessage("User Updated successfully");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(user);
            } else {
                log.warn("'''''''''''''''warning''''''''''''''");
                log.warn("-->USER WITH THIS ID DOES NOT EXIST <-- ");
                log.warn(",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");

                entityResponse.setMessage("User with the Employee Number does not exist");
                entityResponse.setStatusCode(HttpStatus.CONFLICT.value());
                entityResponse.setEntity(null);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            log.error("--> ERROR WHILE FETCHING USER BY Employee Number <--: " + exception.getLocalizedMessage());
            entityResponse.setMessage(HttpStatus.EXPECTATION_FAILED.getReasonPhrase() + exception.getLocalizedMessage());
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            entityResponse.setEntity(null);
        }
        return entityResponse;
    }



    @Transactional
    public EntityResponse deleteUser(int userId) {
        EntityResponse entityResponse = new EntityResponse<>();

        try {
            log.info("----->LISTING USERS<-------");
            Optional<User> exists = userRepository.findById(userId);
            if (exists.isPresent()) {
                User existingUser = exists.get();

                existingUser.setDeletedFlag('Y');
                existingUser.setDeletedBy(SecurityUtils.getCurrentUserLogin());
                existingUser.setDeleteOn(LocalDate.now());

                userRepository.save(existingUser);

                entityResponse.setMessage("User deleted successfully");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(existingUser);
            } else {
                log.warn("'''''''''''''''warning''''''''''''''");
                log.warn("--------> USER WITH THE Employee Number DOES NOT EXIST <------ ");
                log.warn(",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");

                entityResponse.setMessage("User not deleted");
                entityResponse.setStatusCode(HttpStatus.CONFLICT.value());
                entityResponse.setEntity(null);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            log.error("--> ERROR WHILE DELETING USER<--: " + exception.getLocalizedMessage());
            entityResponse.setMessage(HttpStatus.EXPECTATION_FAILED.getReasonPhrase() + exception.getLocalizedMessage());
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            entityResponse.setEntity(null);
        }
        return entityResponse;
    }

    // UserService
    public EntityResponse checkToken(String token) {
        EntityResponse entityResponse = new EntityResponse<>();
        try {
            // Validate token

            boolean isValidToken = jwtService.isTokenValid(token, userDetails);
            if (isValidToken) {
                entityResponse.setMessage("Token is valid");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(null);
            } else {
                entityResponse.setMessage("Token is invalid");
                entityResponse.setStatusCode(HttpStatus.UNAUTHORIZED.value());
                entityResponse.setEntity(null);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            log.error("--> ERROR WHILE VALIDATING TOKEN <--: " + exception.getLocalizedMessage());
            entityResponse.setMessage(HttpStatus.EXPECTATION_FAILED.getReasonPhrase() + exception.getLocalizedMessage());
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            entityResponse.setEntity(null);
        }
        return entityResponse;
    }


    public EntityResponse getUserDetails() {
        EntityResponse entityResponse=new EntityResponse<>();
        Optional<User> user= userRepository.findByEmailAddress(SecurityUtils.getCurrentUserLogin());
        if(user.isPresent()){
            UserDTO dto= mapEntityToDTO(user.get());


            entityResponse.setMessage("user found ");
            entityResponse.setStatusCode(HttpStatus.OK.value());
            entityResponse.setEntity(dto);
        }else {
            entityResponse.setMessage("user not logged in");
            entityResponse.setStatusCode(HttpStatus.NO_CONTENT.value());
        }
        return  entityResponse;
    }

    public List<UserDTO> mapEntitiesToDTOs(List<User> users) {
        return users.stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO mapEntityToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmailAddress(user.getEmailAddress());
        dto.setLastName(user.getLastName());
        dto.setEmployeeNumber(user.getEmployeeNumber());
        dto.setFirstName(user.getFirstName());
        dto.setDepartment(user.getDepartment());
        dto.setCreatedOn(String.valueOf(user.getCreatedOn()));
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRole(user.getRole().getName());
        dto.setPrivileges(user.getAuthorities().toString());
        return dto;
    }
    public EntityResponse createRole(String name, List<Permission> privilegesList) {
        EntityResponse entityResponse = new EntityResponse();
        Optional<Role> existingRole = roleRepository.findByName(name);

        try {
            if (name == null){
                entityResponse.setStatusCode(HttpStatus.NO_CONTENT.value());
                entityResponse.setMessage("Role name cannot be blank");
            }
            else if (existingRole.isPresent()) {
                // Role already exists
                entityResponse.setMessage("Role with the name \"" + name + "\" already exists.");
                entityResponse.setStatusCode(HttpStatus.FOUND.value());
                entityResponse.setEntity(existingRole);
            }
            else if(privilegesList.isEmpty()){
                entityResponse.setStatusCode(HttpStatus.NO_CONTENT.value());
                entityResponse.setMessage("Add at least one privilege to the Role");
            }
            else {
                // Create and save new role
                Role role = new Role();
                role.setName(name);
                role.setPermissions(privilegesList);
                roleRepository.save(role);

                // Set success message and status code
                entityResponse.setMessage("Role with the name \"" + name + "\" created successfully.");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(role);
            }
        } catch (Exception e) {
            // Handle unexpected errors
            entityResponse.setMessage("Error while creating role: "+ e.getMessage());
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return entityResponse;
    }
    public EntityResponse updateRole(String name, List<Permission> privilegesList) {
        EntityResponse entityResponse = new EntityResponse();
        Optional<Role> existingRole = roleRepository.findByName(name);

        try {

            if (existingRole.isPresent()) {
                  Role brandRole= existingRole.get();
                 brandRole.setPermissions(privilegesList);
                roleRepository.save(brandRole);

                // Set success message and status code
                entityResponse.setMessage("Role with the name \"" + name + "\" has been updated successfully.");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(brandRole);

            } else {
                // Create and save new role
                entityResponse.setMessage("Role with the name \"" + name + "\" already exists.");
                entityResponse.setStatusCode(HttpStatus.FOUND.value());


            }
        } catch (Exception e) {
            // Handle unexpected errors
            entityResponse.setMessage(e.getMessage());
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());

        }

        return entityResponse;
    }


//    public EntityResponse createRole(String name, List<Permission> privilegesList) {
//        EntityResponse entityResponse = new EntityResponse();
//
//        Role role = new Role();
//        role.setName(name);
//        role.setPermissions(privilegesList);
//
//        roleRepository.save(role);
//
//        return entityResponse;
//    }

    public EntityResponse getAllRoles() {
        EntityResponse entityResponse = new EntityResponse();
        List<Role> roleList = roleRepository.findAll();

        if (roleList.isEmpty()) {
            entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
            entityResponse.setMessage("Roles not found");
        } else {
            // Initialize a list to hold all role names
            List<String> roleNameList = new ArrayList<>();

            // Loop through the roles and add each name to the list
            for(Role role : roleList) {
                roleNameList.add(role.getName());
            }

            // After accumulating all role names, set them in the EntityResponse
            entityResponse.setStatusCode(HttpStatus.OK.value());
            entityResponse.setMessage("Roles found");
            entityResponse.setEntity(roleNameList); // Set the entire list of role names as the entity
        }

        return entityResponse;
    }


    private List<RoleDTO> mapRolesToDTOs(List<Role> roles) {
        return roles.stream()
                .map(this::mapRoleToDTO)
                .collect(Collectors.toList());
    }

    public RoleDTO mapRoleToDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setName(role.getName());
        return dto;
    }

    public EntityResponse getAllPrivileges() {
        EntityResponse entityResponse = new EntityResponse<>();


        var privileges = Permission.values();
        entityResponse.setEntity(privileges);
        entityResponse.setMessage(" pri success");
        entityResponse.setStatusCode( HttpStatus.OK.value());

    return entityResponse;
    }
    public EntityResponse getPrivilegesByRole( String roleName) {
        EntityResponse entityResponse = new EntityResponse<>();
        Optional<Role> userRole= roleRepository.findByName(roleName);
        if(userRole.isEmpty()){

            entityResponse.setMessage("No Role with Such Name");
            entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
        }
        else{
           List<Permission> privileges= userRole.get().getPermissions();

            entityResponse.setEntity(privileges);
            entityResponse.setMessage(" Privileges found succesfully");
            entityResponse.setStatusCode( HttpStatus.OK.value());

        }



        return entityResponse;
    }
}


 /*   public ResponseEntity<String> changePassword(Map<String, String> requestMap) {
        try {
            User user  = userRepository.findById(userDetails.getUsername());
            if (userObj != null) {
                if (userObj.getPassword().equals(requestMap.get("oldPassword"))) {
                    userObj.setPassword(requestMap.get("newPassword"));
                    userRepository.save(userObj);
                    return AuthenticationResponse.getResponseEntity("Password Updated Successfully", HttpStatus.OK);
                }
                return AuthenticationResponse.getResponseEntity("Incorrect Old Password", HttpStatus.BAD_REQUEST);
            }
            return AuthenticationResponse.getResponseEntity("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception ex) {
            ex.printStackTrace(); // Log the exception or handle it appropriately
            return AuthenticationResponse.getResponseEntity("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    public ResponseEntity<String> forgotPassword( User requestMap) {
        try{
            Optional<User> user = userRepository.findByEmailAddress(requestMap.getEmailAddress());
            if(!Objects.isNull(user) && !Strings.isNullOrEmpty(userModel.getEmailAddress()))
                emailUtils.forgotMail(userModel.getEmailAddress(), "Credentials by Cafe Management", user.getPassword());
            return ResponseUtil.getResponseEntity("Check your mail for Credentials.",HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return ResponseUtil.getResponseEntity(ResponseConstants.SOMETHING_WENT_WRONG,HttpStatus.INTERNAL_SERVER_ERROR);
    } */