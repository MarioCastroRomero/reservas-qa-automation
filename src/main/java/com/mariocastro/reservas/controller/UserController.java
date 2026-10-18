package com.mariocastro.reservas.controller;

import com.mariocastro.reservas.dto.ChangePasswordRequest;
import com.mariocastro.reservas.dto.UpdateProfileRequest;
import com.mariocastro.reservas.dto.UserDto;
import com.mariocastro.reservas.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserDto getCurrentUser() {
        return userService.getCurrentUser();
    }

    @PutMapping("/me")
    public UserDto updateCurrentUser(@Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateCurrentUser(request);
    }

    @PatchMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
    }

    @GetMapping("/{uuid}")
    public UserDto getUser(@PathVariable String uuid) {
        return userService.getUser(uuid);
    }
}
