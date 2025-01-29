package com.emt.dms1.auth;



import com.emt.dms1.user.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    private String firstname;
    private String lastname;
    private String emailAddress;
    private String password;
    private String department;
    private String phoneNumber;
    private String employeeNumber;
    private LocalDate createdOn;

}
