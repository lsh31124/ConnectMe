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

/**
 * 회원 비즈니스 로직 서비스
 * 프로필 조회/수정, 회원 검색, 회원 탈퇴 기능 제공
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    /**
     * 회원 프로필 조회
     * @param userId 조회할 회원 ID
     * @return 회원 프로필 응답
     */
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return UserProfileResponse.from(user);
    }

    /**
     * 회원 프로필 수정 — null이 아닌 필드만 업데이트
     * @param userId 수정할 회원 ID
     * @param request 수정할 프로필 정보
     * @return 수정된 회원 프로필 응답
     */
    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.updateProfile(request.name(), request.statusMessage(), request.profileImage());
        return UserProfileResponse.from(user);
    }

    /**
     * 이메일 또는 전화번호로 회원 검색
     * @param query 검색할 이메일 또는 전화번호
     * @return 일치하는 회원 프로필 목록
     */
    public List<UserProfileResponse> searchUsers(String query) {
        return userRepository.findByEmailOrPhoneNumber(query).stream()
                .map(UserProfileResponse::from)
                .toList();
    }

    /**
     * 회원 탈퇴 처리 — 논리 삭제(soft delete)
     * @param userId 탈퇴할 회원 ID
     */
    @Transactional
    public void deleteAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.softDelete();
    }
}