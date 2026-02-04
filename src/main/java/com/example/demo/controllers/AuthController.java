package com.example.demo.controllers;

import com.example.demo.dto.AuthenticationResponse;
import com.example.demo.dto.user.UserDTO;
import com.example.demo.models.user.RoleUser;
import com.example.demo.models.user.User;
import com.example.demo.services.AuthenticationService;
import com.example.demo.services.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationService authService;
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody UserDTO request) {
        if(userService.checkEmail(request.getEmail())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User with this email already exists");
        }
        String token = authService.register(request);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestParam String email, @RequestParam String password) {
        var user = userService.findUserByEmail(email);
        String targetUrl = "/dashboard.html";
        if (user.getRoleUser() == RoleUser.ADMIN) {
            targetUrl = "/admin.html";
        }
        return ResponseEntity.ok(new AuthenticationResponse(authService.authenticate(email, password), targetUrl));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        UserDTO userDto = userService.findUserByEmail(authentication.getName()).toDTO();
        return ResponseEntity.ok(userDto);
    }
}
