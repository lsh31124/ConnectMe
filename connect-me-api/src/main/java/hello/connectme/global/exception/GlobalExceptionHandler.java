package hello.connectme.global.exception;

import hello.connectme.common.exception.BusinessException;
import hello.connectme.global.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * 전역 예외 처리 핸들러
 * BusinessException, 유효성 검사 실패, Spring MVC 내부 예외, 예상치 못한 예외를 통일된 응답으로 변환
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 비즈니스 규칙 위반 예외 처리
     * @param e BusinessException 인스턴스
     * @return 400 Bad Request + 에러 응답
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getErrorCode().getCode(), e.getErrorCode().getMessage()));
    }

    /**
     * @Valid 유효성 검사 실패 예외 처리 — 첫 번째 필드 오류 메시지를 반환
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        // 첫 번째 필드 오류 메시지 추출
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst()
                .orElse("입력값이 올바르지 않습니다.");
        return ResponseEntity.badRequest()
                .headers(headers)
                .body(ApiResponse.error("INVALID_INPUT", message));
    }

    /**
     * Spring MVC 내부 예외 처리 — HTTP 상태 코드별 표준 에러 메시지 반환
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception e,
            Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request) {
        int status = statusCode.value();
        ApiResponse<Void> apiResponse;
        // HTTP 상태 코드에 따른 에러 코드 분기
        if (status == 404) {
            apiResponse = ApiResponse.error("NOT_FOUND", "요청한 리소스를 찾을 수 없습니다.");
        } else if (status == 405) {
            apiResponse = ApiResponse.error("METHOD_NOT_ALLOWED", "지원하지 않는 HTTP 메서드입니다.");
        } else if (status == 400) {
            apiResponse = ApiResponse.error("INVALID_INPUT", "요청을 처리할 수 없습니다.");
        } else if (status >= 500) {
            apiResponse = ApiResponse.error("SERVER_ERROR", "서버 오류가 발생했습니다.");
        } else {
            apiResponse = ApiResponse.error("REQUEST_ERROR", "요청을 처리할 수 없습니다.");
        }
        return ResponseEntity.status(statusCode)
                .headers(headers)
                .body(apiResponse);
    }

    /**
     * 잘못된 Enum 변환 등 IllegalArgumentException 처리 — 400 반환
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("INVALID_INPUT", e.getMessage()));
    }

    /**
     * 예상치 못한 예외 처리 — 500 Internal Server Error 반환
     * @param e 처리되지 않은 예외
     * @return 500 에러 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception e) {
        log.error("Unexpected error: {}", e.getMessage(), e);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.error("SERVER_ERROR", "서버 오류가 발생했습니다."));
    }
}