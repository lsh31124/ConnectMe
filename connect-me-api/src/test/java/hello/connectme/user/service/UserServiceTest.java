package hello.connectme.user.service;

import hello.connectme.common.exception.BusinessException;
import hello.connectme.common.exception.ErrorCode;
import hello.connectme.domain.user.User;
import hello.connectme.domain.user.UserRepository;
import hello.connectme.user.dto.UpdateProfileRequest;
import hello.connectme.user.dto.UserProfileResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    private User createTestUser(Long id, String email, String name) {
        User user = User.createLocal(email, "01012345678", name, "encodedPw");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Test
    void getProfile_success() {
        User user = createTestUser(1L, "test@email.com", "홍길동");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        UserProfileResponse response = userService.getProfile(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("test@email.com");
        assertThat(response.name()).isEqualTo("홍길동");
        assertThat(response.provider()).isEqualTo("LOCAL");
    }

    @Test
    void getProfile_userNotFound_throwsException() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile(99L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void updateProfile_success() {
        User user = createTestUser(1L, "test@email.com", "원래이름");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        UpdateProfileRequest request = new UpdateProfileRequest("새이름", "새상태메시지", "new-image.jpg");

        UserProfileResponse response = userService.updateProfile(1L, request);

        assertThat(response.name()).isEqualTo("새이름");
        assertThat(response.statusMessage()).isEqualTo("새상태메시지");
        assertThat(response.profileImage()).isEqualTo("new-image.jpg");
    }

    @Test
    void updateProfile_userNotFound_throwsException() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());
        UpdateProfileRequest request = new UpdateProfileRequest("이름", null, null);

        assertThatThrownBy(() -> userService.updateProfile(99L, request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void searchUsers_returnsMappedList() {
        User user1 = createTestUser(1L, "test@email.com", "홍길동");
        User user2 = createTestUser(2L, "other@email.com", "김철수");
        given(userRepository.findByEmailOrPhoneNumber("test@email.com")).willReturn(List.of(user1, user2));

        List<UserProfileResponse> result = userService.searchUsers("test@email.com");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(1).id()).isEqualTo(2L);
    }

    @Test
    void searchUsers_returnsEmptyList_whenNoMatch() {
        given(userRepository.findByEmailOrPhoneNumber("noresult")).willReturn(List.of());

        List<UserProfileResponse> result = userService.searchUsers("noresult");

        assertThat(result).isEmpty();
    }

    @Test
    void deleteAccount_success() {
        User user = createTestUser(1L, "test@email.com", "홍길동");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        userService.deleteAccount(1L);

        assertThat(user.isDeleted()).isTrue();
    }

    @Test
    void deleteAccount_userNotFound_throwsException() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteAccount(99L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }
}