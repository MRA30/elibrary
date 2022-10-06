package com.elibrary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    private Long id;
    private String numberIdentity;
    private String username;
    private String fullName;
    private String gender;
    private String noHp;
    private String address;
    private String email;
    private String userRole;
    private String image;
}
