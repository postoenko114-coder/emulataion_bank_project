package com.example.demo.models.user;

import com.example.demo.dto.user.UserDTO;
import com.example.demo.dto.user.UserDTOAdmin;
import com.example.demo.models.account.Account;
import com.example.demo.models.card.Card;
import com.example.demo.models.branch.reservation.Reservation;
import com.example.demo.models.notification.Notification;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String password;

    @Enumerated(EnumType.STRING)
    private RoleUser roleUser;

    private String email;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY,  cascade = CascadeType.ALL,  orphanRemoval = true)
    private List<Account> accounts;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL,  orphanRemoval = true)
    private List<Card> cards;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL,  orphanRemoval = true)
    private List<Reservation> reservations;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL,  orphanRemoval = true)
    private List<Notification> notifications;

    private LocalDateTime createdAt;

    public User() {}

    public User(String username, String password, String email, RoleUser roleUser) {
        this.username = username;
        this.password = password;
        this.roleUser = roleUser;
        this.email = email;
        this.createdAt = LocalDateTime.now();
    }

    public User(String username, String password, String email, LocalDateTime createdAt) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.createdAt = createdAt;
    }

    public UserDTO toDTO(){
        UserDTO userDTO = new UserDTO();
        userDTO.setId(id);
        userDTO.setUsername(username);
        userDTO.setPassword(password);
        userDTO.setEmail(email);
        userDTO.setCreatedAt(createdAt);
        return userDTO;
    }

    public UserDTOAdmin  toDTOAdmin(){
        UserDTOAdmin userDTOAdmin = new UserDTOAdmin();
        userDTOAdmin.setId(id);
        userDTOAdmin.setUsername(username);
        userDTOAdmin.setPassword(password);
        userDTOAdmin.setEmail(email);
        userDTOAdmin.setCreatedAt(createdAt);
        userDTOAdmin.setRoleUser(roleUser);
        return userDTOAdmin;
    }

    public List<Account> getAccounts() {return accounts;}

    public void setAccounts(List<Account> accounts) {this.accounts = accounts;}

    public List<Card> getCards() {return cards;}

    public void setCards(List<Card> cards) {this.cards = cards;}

    public LocalDateTime getCreatedAt() {return createdAt;}

    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}

    public String getEmail() {return email;}

    public void setEmail(String email) {this.email = email;}

    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public List<Notification> getNotifications() {return notifications;}

    public void setNotifications(List<Notification> notifications) {this.notifications = notifications;}

    @Override
    public String getPassword() {return password;}

    public void setPassword(String password) {this.password = password;}

    public List<Reservation> getReservations() {return reservations;}

    public void setReservations(List<Reservation> reservations) {this.reservations = reservations;}

    public RoleUser getRoleUser() {return roleUser;}

    public void setRoleUser(RoleUser role) {this.roleUser = role;}

    public void setUsername(String username) {this.username = username;}

    //For userDetailsService
    @Override
    public String getUsername() {return email;}

    //For business-logic
    public String getRealUsername() {return this.username;}

    @Override
    public boolean isAccountNonExpired() {return UserDetails.super.isAccountNonExpired();}

    @Override
    public boolean isAccountNonLocked() {return UserDetails.super.isAccountNonLocked();}

    @Override
    public boolean isCredentialsNonExpired() {return UserDetails.super.isCredentialsNonExpired();}

    @Override
    public boolean isEnabled() {return UserDetails.super.isEnabled();}

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + roleUser.name()));
    }


}
