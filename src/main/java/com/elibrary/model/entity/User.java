package com.elibrary.model.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.util.Date;
import lombok.*;
import javax.persistence.*;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numberIdentity;

    private String username;

    private String firstName;

    private String lastName;

    private boolean enabled;

    private String gender;

    private String noHp;

    private String address;

    private String email;

    private String password;

    private String userRole;

    private String emailVerificationToken;

    private Date emailVerificationTokenExpiry;

    private String passwordResetToken;

    private Date passwordResetTokenExpiry;

    // constructor without id
    public User(String numberIdentity, String username, String firstName, String lastName, boolean enabled, String gender, String noHp,
                String address, String email, String password, String userRole) {
        this.numberIdentity = numberIdentity;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.enabled = enabled;
        this.gender = gender;
        this.noHp = noHp;
        this.address = address;
        this.email = email;
        this.password = password;
        this.userRole = userRole;
    }
}
