package hello.connectme.domain.message;

import hello.connectme.domain.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채팅 메시지 엔티티
 * 텍스트(TEXT), 이미지(IMAGE), 파일(FILE) 세 가지 유형 지원
 * 논리 삭제(soft delete) 방식으로 메시지 삭제 처리
 */
@Entity
@Table(name = "messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long chatRoomId;

    @Column(nullable = false)
    private Long senderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MessageType type;

    @Column(length = 4000)
    private String content;

    @Column(length = 1000)
    private String fileUrl;

    @Column(length = 255)
    private String fileName;

    private Long fileSize;

    @Column(nullable = false)
    private boolean isDeleted = false;

    /**
     * 메시지 생성 팩토리 메서드
     * @param chatRoomId 메시지를 전송할 채팅방 ID
     * @param senderId 발신자 회원 ID
     * @param type 메시지 유형 (TEXT/IMAGE/FILE)
     * @param content 텍스트 내용 (TEXT 타입 시 사용)
     * @param fileUrl 파일 URL (IMAGE/FILE 타입 시 사용)
     * @param fileName 원본 파일명 (FILE 타입 시 사용)
     * @param fileSize 파일 크기 (바이트 단위)
     * @return 생성된 Message 엔티티
     */
    public static Message create(Long chatRoomId, Long senderId, MessageType type,
                                 String content, String fileUrl, String fileName, Long fileSize) {
        Message message = new Message();
        message.chatRoomId = chatRoomId;
        message.senderId = senderId;
        message.type = type;
        message.content = content;
        message.fileUrl = fileUrl;
        message.fileName = fileName;
        message.fileSize = fileSize;
        return message;
    }

    /**
     * 메시지 논리 삭제 처리 — isDeleted 플래그를 true로 설정
     */
    public void softDelete() {
        this.isDeleted = true;
    }
}