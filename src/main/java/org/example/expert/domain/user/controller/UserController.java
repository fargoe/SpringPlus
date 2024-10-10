package org.example.expert.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @PutMapping("/users")
    public void changePassword(@AuthenticationPrincipal AuthUser authUser, @RequestBody UserChangePasswordRequest userChangePasswordRequest) {
        userService.changePassword(authUser.getId(), userChangePasswordRequest);
    }

    // JPA 방식
    @GetMapping("/users/search/jpa")
    public ResponseEntity<UserResponse> getUserByNicknameJPA(@RequestParam String nickname) {
        return ResponseEntity.ok(userService.getUserByNicknameJPA(nickname));
    }

    // JPQL 방식
    @GetMapping("/users/search/jpql")
    public ResponseEntity<UserResponse> getUserByNicknameJPQL(@RequestParam String nickname) {
        return ResponseEntity.ok(userService.getUserByNicknameJPQL(nickname));
    }
}
