package com.elibrary.dto.response;

import java.util.List;

import com.elibrary.model.entity.BookRequest;
import com.elibrary.model.entity.Borrow;
import com.elibrary.model.entity.Gender;
import com.elibrary.model.entity.UserRole;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    private Long id;
    private String numberIdentity;
    private String name;
    private Gender gender;
    private String noHp;
    private String address;
    private String email;
    private UserRole userRole;
    private String image;
    private List<Borrow> borrows;
    private List<BookRequest> bookRequests;
    
}
