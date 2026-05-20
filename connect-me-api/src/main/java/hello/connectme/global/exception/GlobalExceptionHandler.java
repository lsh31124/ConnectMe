package hello.connectme.global.exception;

import hello.connectme.common.exception.BusinessException;
import hello.connectme.global.response.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getErrorCode().getCode(), e.getErrorCode().getMessage()));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst()
                .orElse("입력값이 올바르지 않습니다.");
        return ResponseEntity.badRequest()
                .headers(headers)
                .body(ApiResponse.error("INVALID_INPUT", message));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception e,
            Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request) {
        int status = statusCode.value();
        ApiResponse<Void> apiResponse;
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception e) {
        return ResponseEntity.internalServerError()
                .body(ApiResponse.error("SERVER_ERROR", "서버 오류가 발생했습니다."));
    }
}