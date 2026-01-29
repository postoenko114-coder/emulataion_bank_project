package com.example.demo.services.user;

import com.example.demo.dto.user.UserCreateDTO;
import com.example.demo.dto.user.UserDTO;
import com.example.demo.dto.user.UserDTOAdmin;
import com.example.demo.models.user.RoleUser;
import com.example.demo.models.user.User;
import com.example.demo.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,  PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @Override
    public UserDTOAdmin createUser(UserCreateDTO createDTO){
        User user = new User();
        user.setEmail(createDTO.getEmail());
        user.setPassword(passwordEncoder.encode(createDTO.getPassword()));
        user.setRoleUser(RoleUser.valueOf(createDTO.getRoleUser()));
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);
        return user.toDTOAdmin();
    }


    @Transactional
    @Override
    public User getUserById(Long user_id){
        User user = userRepository.findById(user_id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        return user;
    }

    @Transactional
    @Override
    public List<UserDTOAdmin> getAllUsers(){
        List<User> users = userRepository.findAll();
        List<UserDTOAdmin> userDTOAdmins = new ArrayList<>();
        for(User user : users){
            userDTOAdmins.add(user.toDTOAdmin());
        }
        return userDTOAdmins;
    }

    @Transactional
    @Override
    public List<UserDTOAdmin> searchUsers(String query) {
        if (query == null || query.isBlank()) {
            return getAllUsers();
        }
        List<User> users = userRepository.findByEmailOrUsernameOrId(query);
        List<UserDTOAdmin> userDTOAdmins = new ArrayList<>();
        for(User user : users){
            userDTOAdmins.add(user.toDTOAdmin());
        }
        return userDTOAdmins;
    }


    @Transactional
    @Override
    public User findUserByUsername(String username){
        return userRepository.findByUsername(username).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
    }

    @Transactional
    @Override
    public User findUserByEmail(String email){
        return userRepository.findByEmail(email).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
    }

    @Transactional
    @Override
    public UserDTO updateUser(Long user_id, UserDTO userDTO){
        User user = userRepository.findById(user_id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        if(!userDTO.getUsername().equals(user.getUsername())){
            user.setUsername(userDTO.getUsername());
        }
        if(!userDTO.getEmail().equals(user.getEmail())){
            user.setEmail(userDTO.getEmail());
        }
        return user.toDTO();
    }

    @Transactional
    @Override
    public UserDTOAdmin changeRole(Long user_id, String role){
        User user = userRepository.findById(user_id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        user.setRoleUser(RoleUser.valueOf(role.toUpperCase()));
        return user.toDTOAdmin();
    }

    @Transactional
    @Override
    public UserDTO changePassword(Long user_id, String oldPassword, String newPassword){
        User user = userRepository.findById(user_id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        if(!passwordEncoder.matches(oldPassword,user.getPassword())){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Old Password not match");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        return user.toDTO();
    }

    @Transactional
    @Override
    public void deleteUserById(Long user_id){
       userRepository.deleteById(user_id);
    }

}
