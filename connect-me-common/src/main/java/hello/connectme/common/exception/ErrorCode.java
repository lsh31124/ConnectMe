package hello.connectme.common.exception;

public enum ErrorCode {

    DUPLICATE_EMAIL("AUTH_001", "이미 사용 중인 이메일입니다."),
    DUPLICATE_PHONE("AUTH_002", "이미 사용 중인 전화번호입니다."),
    INVALID_PASSWORD("AUTH_003", "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_TOKEN("AUTH_004", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN("AUTH_005", "만료된 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND("AUTH_006", "리프레시 토큰을 찾을 수 없습니다."),

    USER_NOT_FOUND("USER_001", "존재하지 않는 회원입니다."),

    FRIEND_NOT_FOUND("FRIEND_001", "친구 관계를 찾을 수 없습니다."),
    FRIEND_ALREADY_EXISTS("FRIEND_002", "이미 친구 요청이 존재합니다."),
    FRIEND_INVALID_STATUS("FRIEND_003", "현재 상태에서 수행할 수 없는 작업입니다."),

    CHAT_ROOM_NOT_FOUND("CHAT_001", "채팅방을 찾을 수 없습니다."),
    CHAT_ROOM_MEMBER_NOT_FOUND("CHAT_002", "채팅방 멤버를 찾을 수 없습니다."),
    CHAT_ROOM_ALREADY_MEMBER("CHAT_003", "이미 채팅방에 참여한 사용자입니다."),
    CHAT_ROOM_NOT_OWNER("CHAT_004", "채팅방 OWNER만 수행할 수 있는 작업입니다."),

    MESSAGE_NOT_FOUND("MSG_001", "메시지를 찾을 수 없습니다."),
    MESSAGE_NOT_SENDER("MSG_002", "메시지를 삭제할 권한이 없습니다.");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
}