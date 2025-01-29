package  com.emt.dms1.auth;


import com.emt.dms1.user.UserService;
import com.emt.dms1.utils.EntityResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController
@RequestMapping( value = "/api/v1/auth" , produces = MediaType.APPLICATION_JSON_VALUE)
//@RequiredArgsConstructor
@CrossOrigin
@Slf4j
@ResponseBody
public class AuthenticationController {

    @Autowired
    private  AuthenticationService service;

    @Autowired
    private LogoutService logoutService;

    private UserService userService;

    public AuthenticationController(UserService userService) {
        this.userService = userService;
    }

//    @PostMapping("/createRole")
//    public EntityResponse createRole(
//            @RequestParam String name,
//            @RequestParam List<Permission> privilegesList
//    ) {
//        log.info("no: "+ privilegesList.size() + "\n" + "values: " + privilegesList);
//        return userService.createRole(name, privilegesList);
//    }

    @PostMapping("/CreateUser")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request,
            @RequestParam String role
    ) throws MessagingException, GeneralSecurityException {
        return ResponseEntity.ok(service.register(request, role));
    }
    @PostMapping("/Login")
    @ApiResponse(responseCode = "200", description = "Document saved successfully")
    public EntityResponse authenticate(
            @RequestBody AuthenticationRequest request
    ) {

        return service.authenticate(request);

    }

    @PostMapping("/refresh-token")
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        service.refreshToken(request, response);
    }
    @PutMapping("/changePassword" )
    public EntityResponse<?> updatePassword(@RequestBody AuthenticationRequest request) {
        return service.updatePassword(request);
    }
    @PostMapping("/otp")
    public EntityResponse otpVerification(@RequestBody Integer Otpvalue){

        return service.verifyOtp(Otpvalue);
    }


    @PostMapping("/logout")
    public EntityResponse logout( HttpServletRequest request,
                                  HttpServletResponse response,
                                  Authentication authentication) {
        EntityResponse entityResponse= new EntityResponse();
        try {

            logoutService.logout(request,response, authentication);
            entityResponse.setMessage("Logged out successfully");
            entityResponse.setStatusCode(HttpStatus.OK.value());
        } catch (Exception e) {
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            entityResponse.setMessage("Error occurred during logout");
        }
        return entityResponse;

    }
//    @GetMapping("/loggedinusers")
//    public List<String> getLoggedInUsers() {
//        return userSessionManager.getLoggedInUsers();
//    }


}