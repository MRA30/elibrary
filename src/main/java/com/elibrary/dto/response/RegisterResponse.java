package com.elibrary.dto.response;

import com.elibrary.model.entity.Gender;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse {
    
    private Long id;
    private String numberIdentity;
    private String name;
    private Gender gender;
    private String noHp;
    private String address;
    private String email;
    private String image;

}
