package com.example.demo.services.user;

import com.example.demo.dto.user.UserCreateDTO;
import com.example.demo.dto.user.UserDTO;
import com.example.demo.dto.user.UserDTOAdmin;
import com.example.demo.models.user.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserService {

    @Transactional
    UserDTOAdmin createUser(UserCreateDTO createDTO);

    @Transactional
    User getUserById(Long user_id);

    @Transactional
    List<UserDTOAdmin> getAllUsers();

    @Transactional
    List<UserDTOAdmin> searchUsers(String query);

    @Transactional
    User findUserByUsername(String userName);

    @Transactional
    User findUserByEmail(String email);

    @Transactional
    Boolean checkEmail(String email);

    @Transactional
    UserDTO updateUser(Long user_id, UserDTO userDTO);

    @Transactional
    UserDTOAdmin changeRole(Long user_id, String role);

    @Transactional
    UserDTO changePassword(Long user_id, String oldPassword, String newPassword);

    @Transactional
    void deleteUserById(Long user_id);


}
