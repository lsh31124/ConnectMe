package hello.connectme.common.exception;

/**
 * 비즈니스 규칙 위반 시 발생하는 런타임 예외
 * GlobalExceptionHandler에서 포착하여 표준 에러 응답으로 변환
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * 에러 코드를 기반으로 예외 생성
     * @param errorCode 발생한 비즈니스 에러 코드
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 발생한 에러 코드 반환
     * @return 에러 코드
     */
    public ErrorCode getErrorCode() { return errorCode; }
}