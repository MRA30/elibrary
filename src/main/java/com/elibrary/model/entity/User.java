package com.elibrary.model.entity;

import java.util.List;
import java.util.Optional;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
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

    @OneToMany(targetEntity = Borrow.class, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private List<Borrow> borrows;

    @OneToMany(targetEntity = BookRequest.class, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private List<BookRequest> bookRequests;

    // contructor without id
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
