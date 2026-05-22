package hello.connectme.friend.dto;

import hello.connectme.domain.friend.Friend;

/**
 * 친구 요청 상태 응답 DTO
 * 친구 요청 생성/수락/차단 등 상태 변경 시 반환
 */
public record FriendStatusResponse(
        Long id,
        Long requesterId,
        Long receiverId,
        String status
) {
    /**
     * Friend 엔티티를 FriendStatusResponse로 변환
     * @param friend 친구 관계 엔티티
     * @return FriendStatusResponse 인스턴스
     */
    public static FriendStatusResponse from(Friend friend) {
        return new FriendStatusResponse(
                friend.getId(),
                friend.getRequesterId(),
                friend.getReceiverId(),
                friend.getStatus().name()
        );
    }
}