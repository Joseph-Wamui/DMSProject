
package com.emt.dms1.auth;

import com.emt.dms1.user.Role;
import com.emt.dms1.user.RoleRepository;
import com.emt.dms1.user.UserRepository;
import com.emt.dms1.user.UserService;
import com.emt.dms1.config.JwtService;
import com.emt.dms1.emails.EmailNotificationService;
import com.emt.dms1.otp.OtpGenerator;
import com.emt.dms1.token.Token;
import com.emt.dms1.token.TokenRepository;
import com.emt.dms1.token.TokenType;
import com.emt.dms1.user.User;
import com.emt.dms1.userAudit.UserLogService;
import com.emt.dms1.userAudit.UserLogType;
import com.emt.dms1.utils.EntityResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {


    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private  final EmailNotificationService emailNotificationService;
    private final OtpGenerator otpGenerator;
    private final UserLogService userLogService;
    private final UserRepository userRepository;
    private final UserService userService;
    private  final RoleRepository roleRepository;





    public AuthenticationResponse register(RegisterRequest request, String role) throws MessagingException, GeneralSecurityException {
        Optional<Role> optionalRole = roleRepository.getByName(role);
        var foundRole = optionalRole.get();
        var user = User.builder()
                .firstName(request.getFirstname())
                .lastName(request.getLastname())
                .emailAddress(request.getEmailAddress())
                .password(passwordEncoder.encode(request.getPassword()))
                .employeeNumber(request.getEmployeeNumber())
                .department(request.getDepartment())
                .createdOn(LocalDate.now())
                .phoneNumber(request.getPhoneNumber())
                .role(foundRole)
                .deletedFlag('N')
                .build();
        var savedUser = userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(savedUser, jwtToken);

//        emailNotificationService.sendCreatedNotification(request.getEmailAddress(), request.getPassword());

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    public EntityResponse authenticate(AuthenticationRequest request) {
        EntityResponse entityResponse = new EntityResponse<>();
        String emailAddress = null;
        try {
            // Attempt to authenticate the user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmailAddress(),
                            request.getPassword()
                    )
            );

            // If authentication succeeds, generate OTP and send it to the user
            emailAddress = request.getEmailAddress();
            Integer otp = otpGenerator.generateOTP(emailAddress);
            // emailNotificationService.sendNotification(otp, emailAddress);
            Optional<User> user = userRepository.findByEmailAddress(emailAddress);

            if (user.isPresent()) {
                User user1 = user.get();
                userLogService.logUserAction(emailAddress, UserLogType.Logged_in_successfully, user1);
            }

            // Log success message
            log.info("Authentication successful for email: {}", emailAddress);

            entityResponse.setMessage("OTP sent successfully");
            entityResponse.setStatusCode(HttpStatus.OK.value());
            // Return a response indicating that OTP is sent, but authentication is pending
            return entityResponse;

        } catch (AuthenticationException e) {
            // Log authentication failure
            log.error("Authentication failed for email: {}", request.getEmailAddress(), e);

            // Attempt to find the user to log the failed login attempt
            Optional<User> user = userRepository.findByEmailAddress(request.getEmailAddress());

            if (user.isPresent()) {
                User user1 = user.get();
                userLogService.logUserAction(request.getEmailAddress(), UserLogType.Failed_login_attempt, user1);
            } else {
                // If the user does not exist, log as Access Denied
                userLogService.logUserAction(request.getEmailAddress(), UserLogType.Access_denied, null);
                entityResponse.setMessage("Access Denied");
                entityResponse.setStatusCode(HttpStatus.FORBIDDEN.value());
                // If authentication fails, return unauthorized status code
                return entityResponse;
            }

            entityResponse.setMessage("Authentication failed");
            entityResponse.setStatusCode(HttpStatus.FORBIDDEN.value());
            // If authentication fails, return unauthorized status code
            return entityResponse;
        }
    }


    public EntityResponse verifyOtp(Integer otpValue) {
        EntityResponse entityResponse= new EntityResponse<>();
        if (otpValue == null || otpValue.equals(0)) {
            log.error("OTP value is null or empty.");
            entityResponse.setStatusCode(HttpStatus.FORBIDDEN.value());
            entityResponse.setMessage("OTP value is null or empty.");
            return entityResponse;
        }
        String emailAddress = otpGenerator.getEmailByOtp(otpValue);

        Integer cachedOtp = otpGenerator.getOPTByKey(emailAddress);
        if (cachedOtp == null || !cachedOtp.equals(otpValue)) {
            log.error("Cached OTP not found or does not match the provided OTP: {}", cachedOtp);
            entityResponse.setStatusCode(HttpStatus.FORBIDDEN.value());
            entityResponse.setMessage("Cached OTP not found or does not match the provided OTP");
            return entityResponse;
        }

//    String emailAddress = otpGenerator.getEmailByOtp(otpValue);
        if (emailAddress == null) {
            log.error("Email address not found in the OTP cache for OTP: {}", otpValue);
            entityResponse.setStatusCode(HttpStatus.FORBIDDEN.value());
            entityResponse.setMessage("Email address not found");
            return entityResponse;
        }

        log.info("Email address extracted from OTP cache: {}", emailAddress);

        otpGenerator.clearOTPFromCache(emailAddress);

        var user = userRepository.findByEmailAddress(emailAddress)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);

        AuthenticationResponse authenticationResponse = AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .user(userService.mapEntityToDTO(user))
//            .refreshToken(refreshToken)
                .build();
        entityResponse.setMessage("Otp verified successfully");
        entityResponse.setStatusCode(HttpStatus.OK.value());
        entityResponse.setEntity(authenticationResponse);
        return entityResponse;
    }


    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)//
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            var user = this.userRepository.findByEmailAddress(userEmail)
                    .orElseThrow();
            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);
                var authResponse = AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }

    public EntityResponse updatePassword(AuthenticationRequest request) {
        EntityResponse response = new EntityResponse<>();
        try {
            Optional<User> useropt = userRepository.findByEmailAddress(request.getEmailAddress());

            if (useropt.isPresent()) {
                User user = useropt.get(); // Retrieve the user from Optional

                user.setPassword(passwordEncoder.encode(request.getPassword())); // Update the password field

                userRepository.save(user); // Save the updated user

                response.setStatusCode(HttpStatus.OK.value());
                response.setMessage("Password Updated Successfully");
                response.setEntity(user.getPassword());
                return response;
            } else {
                response.setMessage("User not found");
                response.setStatusCode(HttpStatus.NOT_FOUND.value());
                return response;
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception
            response.setMessage("Something went wrong");
            response.setEntity(null);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return response;
        }
    }

}
//    public void generateOtp(String key,String userEmail) throws MessagingException {
//        // generate otp
//        Integer otpValue = otpGenerator.generateOTP(key);
//        if (otpValue == -1)
//        {
//            log.error("OTP generator is not working...");
//        }
//
//        log.info("Generated OTP: {}", otpValue);
//        // fetch user e-mail from database
//
////        emailNotificationService.sendNotification(otpValue, userEmail);
//
//    }
