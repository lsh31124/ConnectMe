package hello.connectme.global.response;

/**
 * 모든 API 엔드포인트의 통일된 응답 래퍼 클래스
 * 성공/실패 여부와 무관하게 {"code", "message", "data"} 구조를 반환
 */
public class ApiResponse<T> {

    private final String code;
    private final String message;
    private final T data;

    private ApiResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 데이터가 있는 성공 응답 생성
     * @param data 응답에 포함할 데이터
     * @return 성공 응답 객체
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>("SUCCESS", "OK", data);
    }

    /**
     * 데이터 없는 성공 응답 생성 (204 No Content 등에 사용)
     * @return 성공 응답 객체
     */
    public static ApiResponse<Void> ok() {
        return new ApiResponse<>("SUCCESS", "OK", null);
    }

    /**
     * 에러 응답 생성
     * @param code 에러 코드 문자열
     * @param message 에러 메시지
     * @return 에러 응답 객체
     */
    public static ApiResponse<Void> error(String code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    /** @return 응답 코드 */
    public String getCode() { return code; }

    /** @return 응답 메시지 */
    public String getMessage() { return message; }

    /** @return 응답 데이터 */
    public T getData() { return data; }
}