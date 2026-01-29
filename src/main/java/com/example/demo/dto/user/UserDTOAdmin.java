package com.example.demo.dto.user;

import com.example.demo.models.user.RoleUser;

import java.time.LocalDateTime;

public class UserDTOAdmin extends UserDTO {

    private RoleUser roleUser;

    public UserDTOAdmin() {}

    public UserDTOAdmin(Long id, String username, String email, RoleUser roleUser, LocalDateTime createdAt) {
        super(id, username, email,  createdAt);
        this.roleUser = roleUser;
    }

    public RoleUser getRoleUser() {return roleUser;}

    public void setRoleUser(RoleUser roleUser) {this.roleUser = roleUser;}

}
