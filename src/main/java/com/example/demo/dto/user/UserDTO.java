package com.example.demo.dto.user;

import java.time.LocalDateTime;

public class UserDTO {
    private Long id;

    private String username;

    private String password;

    private String email;

    private LocalDateTime createdAt;

    public UserDTO() {}

    public UserDTO(Long id, String username, String email, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
    }

    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public String getEmail() {return email;}

    public void setEmail(String email) {this.email = email;}

    public String getUsername() {return username;}

    public void setUsername(String username) {this.username = username;}

    public LocalDateTime getCreatedAt() {return createdAt;}

    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}

    public String getPassword() {return password;}

    public void setPassword(String password) {this.password = password;}

    public boolean isHasPassword() {
        return this.password != null && !this.password.isEmpty();
    }
}
