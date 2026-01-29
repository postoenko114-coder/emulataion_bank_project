package com.example.demo.dto.user;

public class UserCreateDTO {

    private String username;

    private String email;

    private String password;

    private String roleUser;

    public UserCreateDTO() {}

    public UserCreateDTO(String username, String email, String password, String roleUser) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.roleUser = roleUser;
    }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    public String getRoleUser() { return roleUser; }

    public void setRoleUser(String roleUser) { this.roleUser = roleUser; }
}
