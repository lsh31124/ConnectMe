package hello.connectme.friend.dto;

import hello.connectme.domain.friend.Friend;
import hello.connectme.domain.user.User;

/**
 * 친구 목록 요약 응답 DTO
 * 친구 관계 ID와 상대방 회원의 기본 정보(ID, 이름, 이메일)를 담아 반환
 */
public record FriendSummaryResponse(
        Long id,
        Long userId,
        String name,
        String email
) {
    /**
     * Friend 엔티티와 상대방 User 엔티티를 FriendSummaryResponse로 변환
     * @param friend 친구 관계 엔티티
     * @param partner 상대방 회원 엔티티
     * @return FriendSummaryResponse 인스턴스
     */
    public static FriendSummaryResponse from(Friend friend, User partner) {
        return new FriendSummaryResponse(
                friend.getId(),
                partner.getId(),
                partner.getName(),
                partner.getEmail()
        );
    }
}