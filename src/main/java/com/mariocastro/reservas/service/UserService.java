package com.mariocastro.reservas.service;

import com.mariocastro.reservas.dto.ChangePasswordRequest;
import com.mariocastro.reservas.dto.UpdateProfileRequest;
import com.mariocastro.reservas.dto.UserDto;
import com.mariocastro.reservas.exception.ConflictException;
import com.mariocastro.reservas.exception.NotFoundException;
import com.mariocastro.reservas.exception.ValidateException;
import com.mariocastro.reservas.model.Role;
import com.mariocastro.reservas.model.User;
import com.mariocastro.reservas.repository.UserRepository;
import com.mariocastro.reservas.security.AuthorizationService;
import com.mariocastro.reservas.util.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    private final AuthorizationService authorizationService;

    @Transactional(readOnly = true)
    public UserDto getUser(String uuidStr) {
        UUID uuid = Utils.getUuid(uuidStr);
        authorizationService.requireSelfOrAdmin(uuid);
        return UserDto.fromUser(findUser(uuid));
    }

    @Transactional
    public UserDto saveUser(UserDto userDto) {
        assertEmailAndPassword(userDto);
        User save = userRepository.save(userDto.toUser());
        return UserDto.fromUser(save);
    }

    private void assertEmailAndPassword(UserDto userDto) {
        String email = userDto.getEmail().trim().toLowerCase();
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ConflictException("Email is already registered");
        }
        userDto.setEmail(email);
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
    }

    public UserDto saveUserAsRole(UserDto userDto, Role role){
        assertEmailAndPassword(userDto);
        User user = userDto.toUser();
        user.setRole(role);
        User save = userRepository.save(user);
        return UserDto.fromUser(save);
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUser() {
        return UserDto.fromUser(findUser(authorizationService.currentUser().getUuid()));
    }

    @Transactional
    public UserDto updateCurrentUser(UpdateProfileRequest request) {
        User user = findUser(authorizationService.currentUser().getUuid());
        user.setName(request.getName().trim());
        user.setLastName(request.getLastName().trim());
        user.setBirthDate(request.getBirthDate());
        return UserDto.fromUser(userRepository.save(user));
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = findUser(authorizationService.currentUser().getUuid());
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ValidateException("Current password is incorrect");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new ValidateException("New password must be different from the current one");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private User findUser(UUID uuid) {
        return findUserOptional(uuid).orElseThrow(() -> new NotFoundException("User not found"));
    }

    public Optional<User> findUserOptional(UUID uuid) {
        return userRepository.findById(uuid);
    }
}
