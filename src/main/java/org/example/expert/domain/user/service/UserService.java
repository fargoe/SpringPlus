package org.example.expert.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse getUser(long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new InvalidRequestException("User not found"));
        return new UserResponse(user.getId(), user.getEmail());
    }

    @Transactional
    public void changePassword(long userId, UserChangePasswordRequest userChangePasswordRequest) {
        validateNewPassword(userChangePasswordRequest);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("User not found"));

        if (passwordEncoder.matches(userChangePasswordRequest.getNewPassword(), user.getPassword())) {
            throw new InvalidRequestException("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");
        }

        if (!passwordEncoder.matches(userChangePasswordRequest.getOldPassword(), user.getPassword())) {
            throw new InvalidRequestException("잘못된 비밀번호입니다.");
        }

        user.changePassword(passwordEncoder.encode(userChangePasswordRequest.getNewPassword()));
    }

    private static void validateNewPassword(UserChangePasswordRequest userChangePasswordRequest) {
        if (userChangePasswordRequest.getNewPassword().length() < 8 ||
                !userChangePasswordRequest.getNewPassword().matches(".*\\d.*") ||
                !userChangePasswordRequest.getNewPassword().matches(".*[A-Z].*")) {
            throw new InvalidRequestException("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.");
        }
    }

    // JPA 방식
    public UserResponse getUserByNicknameJPA(String nickname) {
        long startTime = System.currentTimeMillis();  // 시작 시간 기록

        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new InvalidRequestException("유저를 찾을 수 없습니다."));

        long endTime = System.currentTimeMillis();    // 종료 시간 기록
        log.info("JPA 실행 시간 (닉네임 검색): {} ms", (endTime - startTime));  // 시간 로그 출력

        return new UserResponse(user.getId(), user.getEmail());
    }

    // JPQL 방식
    public UserResponse getUserByNicknameJPQL(String nickname) {
        long startTime = System.currentTimeMillis();  // 시작 시간 기록

        User user = userRepository.findByNicknameWithJPQL(nickname);

        long endTime = System.currentTimeMillis();    // 종료 시간 기록
        log.info("JPQL 실행 시간 (닉네임 검색): {} ms", (endTime - startTime));  // 시간 로그 출력

        return new UserResponse(user.getId(), user.getEmail());
    }
}
