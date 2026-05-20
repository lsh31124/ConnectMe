package hello.connectme.user.service;

import hello.connectme.common.exception.BusinessException;
import hello.connectme.common.exception.ErrorCode;
import hello.connectme.domain.user.User;
import hello.connectme.domain.user.UserRepository;
import hello.connectme.user.dto.UpdateProfileRequest;
import hello.connectme.user.dto.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return UserProfileResponse.from(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.updateProfile(request.name(), request.statusMessage(), request.profileImage());
        return UserProfileResponse.from(user);
    }

    public List<UserProfileResponse> searchUsers(String query) {
        return userRepository.findByEmailOrPhoneNumber(query).stream()
                .map(UserProfileResponse::from)
                .toList();
    }

    @Transactional
    public void deleteAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.softDelete();
    }
}