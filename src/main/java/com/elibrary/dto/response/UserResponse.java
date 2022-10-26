package com.elibrary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

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

    private List<String> images;
}
