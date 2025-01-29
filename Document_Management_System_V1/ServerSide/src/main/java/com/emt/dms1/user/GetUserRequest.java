package com.emt.dms1.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetUserRequest {
    private int id;
    private String firstname;
    private String lastname;
    private String emailAddress;
    private Role role;
}
