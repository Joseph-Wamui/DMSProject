package com.emt.dms1.auth;


import com.emt.dms1.user.UserDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


@Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public class AuthenticationResponse {

        @JsonProperty("access_token")
        private String accessToken;
        @JsonProperty("refresh_token")
        private String refreshToken;
        private UserDTO user;

    public static ResponseEntity<String> getResponseEntity(String responseMessage, HttpStatus httpStatus){
        return new ResponseEntity<>("{\"message\":\""+responseMessage+ "\"}", httpStatus);
    }
        public static final String SOMETHING_WENT_WRONG = "Something Went Wrong.";
        public static final String INVALID_DATA = "Invalid Data.";
        public static final String UNAUTHORIZED_ACCESS = "Unauthorized access";
        public static final String  OTP_SENT="OTP sent";
    }












