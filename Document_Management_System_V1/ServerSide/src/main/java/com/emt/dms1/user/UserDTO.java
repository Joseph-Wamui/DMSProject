package com.emt.dms1.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private int  id;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String role;
    private String employeeNumber;
    private String phoneNumber;
    private String department;
    private  String createdOn;
    private String privileges;
}
