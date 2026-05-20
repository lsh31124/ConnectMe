package hello.connectme.global.response;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void ok_withData_returnsSuccessWithData() {
        ApiResponse<String> response = ApiResponse.ok("hello");

        assertThat(response.getCode()).isEqualTo("SUCCESS");
        assertThat(response.getMessage()).isEqualTo("OK");
        assertThat(response.getData()).isEqualTo("hello");
    }

    @Test
    void ok_withoutData_returnsSuccessWithNullData() {
        ApiResponse<Void> response = ApiResponse.ok();

        assertThat(response.getCode()).isEqualTo("SUCCESS");
        assertThat(response.getMessage()).isEqualTo("OK");
        assertThat(response.getData()).isNull();
    }

    @Test
    void error_returnsSpecifiedCodeAndMessage() {
        ApiResponse<Void> response = ApiResponse.error("AUTH_001", "이미 사용 중인 이메일입니다.");

        assertThat(response.getCode()).isEqualTo("AUTH_001");
        assertThat(response.getMessage()).isEqualTo("이미 사용 중인 이메일입니다.");
        assertThat(response.getData()).isNull();
    }

    @Test
    void ok_withNullData_hasNullData() {
        ApiResponse<String> response = ApiResponse.ok(null);

        assertThat(response.getCode()).isEqualTo("SUCCESS");
        assertThat(response.getMessage()).isEqualTo("OK");
        assertThat(response.getData()).isNull();
    }
}