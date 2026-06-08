package com.alex.bank.services.user;

import com.alex.bank.dto.user.UserCreateDTO;
import com.alex.bank.dto.user.UserDTO;
import com.alex.bank.dto.user.UserDTOAdmin;
import com.alex.bank.mapper.UserMapper;
import com.alex.bank.models.user.RoleUser;
import com.alex.bank.models.user.User;
import com.alex.bank.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserMapper userMapper;

    @Transactional
    @Override
    public UserDTOAdmin createUser(UserCreateDTO createDTO) {
        if (userRepository.findByUsername(createDTO.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        if (userRepository.findByEmail(createDTO.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        User user = userMapper.toEntity(createDTO);

        user.setPassword(passwordEncoder.encode(createDTO.getPassword()));

        userRepository.save(user);
        log.info("Admin created user userId={} role={}", user.getId(), user.getRoleUser());
        return userMapper.toDTOAdmin(user);
    }


    @Transactional
    @Override
    public UserDTO getUserById(Long user_id){
        User user = userRepository.findById(user_id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        return userMapper.toDTO(user);
    }

    @Transactional
    @Override
    public List<UserDTOAdmin> getAllUsers(){
        return userRepository.findAll().stream()
                .map(userMapper::toDTOAdmin)
                .toList();
    }

    @Transactional
    @Override
    public List<UserDTOAdmin> searchUsers(String query) {
        if (query == null || query.isBlank()) {
            return getAllUsers();
        }
        return userRepository.findByEmailOrUsernameOrId(query).stream()
                .map(userMapper::toDTOAdmin)
                .toList();
    }

    @Transactional
    @Override
    public UserDTO findUserByUsername(String username){
        User user = userRepository.findByUsername(username).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        return userMapper.toDTO(user);
    }

    @Transactional
    @Override
    public UserDTO findUserByEmail(String email){
        User user = userRepository.findByEmail(email).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        return userMapper.toDTO(user);
    }

    @Transactional
    @Override
    public Boolean checkEmail(String email){
        return userRepository.findByEmail(email).isPresent();
    }

    @Transactional
    @Override
    public UserDTO updateUser(Long user_id, UserDTO userDTO){
        User user = userRepository.findById(user_id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        if (userDTO.getEmail() != null) {
            Optional<User> userWithSameEmail = userRepository.findByEmail(userDTO.getEmail());
            if (userWithSameEmail.isPresent() && !user_id.equals(userWithSameEmail.get().getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already taken");
            }
        }
        if(userDTO.getUsername() != null && !userDTO.getUsername().equals(user.getRealUsername())){
            user.setUsername(userDTO.getUsername());
        }
        if(userDTO.getEmail() != null && !userDTO.getEmail().equals(user.getEmail())){
            user.setEmail(userDTO.getEmail());
        }
        log.info("User profile updated userId={}", user.getId());
        return userMapper.toDTO(user);
    }

    @Transactional
    @Override
    public UserDTOAdmin changeRole(Long user_id, String role){
        User user = userRepository.findById(user_id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        user.setRoleUser(parseRole(role));
        log.info("User role changed userId={} role={}", user.getId(), user.getRoleUser());
        return userMapper.toDTOAdmin(user);
    }

    @Transactional
    @Override
    public UserDTO changePassword(Long user_id, String oldPassword, String newPassword){
        User user = userRepository.findById(user_id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        if(!passwordEncoder.matches(oldPassword,user.getPassword())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Old Password not match");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        log.info("User password changed userId={}", user.getId());
        return userMapper.toDTO(user);
    }

    @Transactional
    @Override
    public void deleteUserById(Long user_id){
       User user = userRepository.findById(user_id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
       userRepository.delete(user);
       log.info("User deleted userId={}", user_id);
    }

    private RoleUser parseRole(String role) {
        try {
            return RoleUser.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user role");
        }
    }

}
