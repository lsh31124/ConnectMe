package hello.connectme.chatroom.service;

import hello.connectme.chatroom.dto.ChatRoomDetailResponse;
import hello.connectme.chatroom.dto.ChatRoomMemberResponse;
import hello.connectme.chatroom.dto.ChatRoomResponse;
import hello.connectme.chatroom.dto.CreateGroupRoomRequest;
import hello.connectme.chatroom.dto.UpdateChatRoomRequest;
import hello.connectme.common.exception.BusinessException;
import hello.connectme.common.exception.ErrorCode;
import hello.connectme.domain.chatroom.ChatRoom;
import hello.connectme.domain.chatroom.ChatRoomMember;
import hello.connectme.domain.chatroom.ChatRoomMemberRepository;
import hello.connectme.domain.chatroom.ChatRoomMemberRole;
import hello.connectme.domain.chatroom.ChatRoomRepository;
import hello.connectme.domain.chatroom.ChatRoomType;
import hello.connectme.domain.user.User;
import hello.connectme.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 채팅방 비즈니스 로직 서비스
 * 채팅방 생성(1:1/그룹), 조회, 이름 수정, 멤버 초대/퇴장/강퇴 기능 제공
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository userRepository;

    /**
     * 1:1 다이렉트 채팅방 생성 — 생성자는 OWNER, 상대방은 MEMBER로 입장
     * @param userId 채팅방을 생성하는 회원 ID
     * @param targetUserId 초대할 상대방 회원 ID
     * @return 생성된 채팅방 응답
     */
    @Transactional
    public ChatRoomResponse createDirectRoom(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new BusinessException(ErrorCode.CHAT_ROOM_SELF_CHAT_NOT_ALLOWED);
        }
        userRepository.findById(targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        ChatRoom room = chatRoomRepository.save(ChatRoom.createDirect(userId));
        chatRoomMemberRepository.save(ChatRoomMember.join(room.getId(), userId, ChatRoomMemberRole.OWNER));
        chatRoomMemberRepository.save(ChatRoomMember.join(room.getId(), targetUserId, ChatRoomMemberRole.MEMBER));

        List<ChatRoomMember> members = chatRoomMemberRepository.findByChatRoomId(room.getId());
        return ChatRoomResponse.from(room, members);
    }

    /**
     * 그룹 채팅방 생성 — 생성자는 OWNER, 초대 멤버 목록은 MEMBER로 입장
     * @param userId 채팅방을 생성하는 회원 ID
     * @param request 채팅방 이름과 초대할 멤버 ID 목록
     * @return 생성된 채팅방 응답
     */
    @Transactional
    public ChatRoomResponse createGroupRoom(Long userId, CreateGroupRoomRequest request) {
        // 초대할 모든 멤버의 존재 여부 사전 검증
        for (Long memberId : request.memberIds()) {
            userRepository.findById(memberId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        }

        ChatRoom room = chatRoomRepository.save(ChatRoom.createGroup(request.name(), userId));
        chatRoomMemberRepository.save(ChatRoomMember.join(room.getId(), userId, ChatRoomMemberRole.OWNER));
        // 초대 멤버 순서대로 MEMBER 역할로 입장 처리
        for (Long memberId : request.memberIds()) {
            chatRoomMemberRepository.save(ChatRoomMember.join(room.getId(), memberId, ChatRoomMemberRole.MEMBER));
        }

        List<ChatRoomMember> members = chatRoomMemberRepository.findByChatRoomId(room.getId());
        return ChatRoomResponse.from(room, members);
    }

    /**
     * 내가 참여 중인 채팅방 목록 조회 (퇴장하지 않은 방만)
     * @param userId 조회할 회원 ID
     * @return 채팅방 목록
     */
    public List<ChatRoomResponse> getMyChatRooms(Long userId) {
        List<ChatRoomMember> memberships = chatRoomMemberRepository.findByUserIdAndLeftAtIsNull(userId);
        List<Long> roomIds = memberships.stream().map(ChatRoomMember::getChatRoomId).toList();
        List<ChatRoom> rooms = chatRoomRepository.findAllById(roomIds);

        // 전체 채팅방 멤버를 한 번에 로드 (N+1 방지)
        Map<Long, List<ChatRoomMember>> membersByRoom = chatRoomMemberRepository.findByChatRoomIdIn(roomIds)
                .stream()
                .collect(Collectors.groupingBy(ChatRoomMember::getChatRoomId));

        // DIRECT 방의 상대방 유저를 한 번에 로드 (N+1 방지)
        List<Long> partnerUserIds = rooms.stream()
                .filter(r -> r.getType() == ChatRoomType.DIRECT)
                .flatMap(r -> membersByRoom.getOrDefault(r.getId(), List.of()).stream()
                        .filter(m -> !m.getUserId().equals(userId) && m.getLeftAt() == null)
                        .map(ChatRoomMember::getUserId))
                .toList();
        Map<Long, User> userMap = userRepository.findAllById(partnerUserIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return rooms.stream()
                .map(room -> {
                    List<ChatRoomMember> members = membersByRoom.getOrDefault(room.getId(), List.of());
                    if (room.getType() == ChatRoomType.DIRECT) {
                        Optional<User> other = members.stream()
                                .filter(m -> !m.getUserId().equals(userId) && m.getLeftAt() == null)
                                .findFirst()
                                .map(m -> userMap.get(m.getUserId()));
                        String displayName = other.map(User::getName).orElse("알 수 없음");
                        String profileImage = other.map(User::getProfileImage).orElse(null);
                        return ChatRoomResponse.from(room, members, displayName, profileImage);
                    }
                    return ChatRoomResponse.from(room, members);
                })
                .toList();
    }

    /**
     * 채팅방 상세 조회 — 참여자만 조회 가능
     * @param userId 조회 요청하는 회원 ID
     * @param roomId 조회할 채팅방 ID
     * @return 채팅방 상세 응답 (멤버 목록 포함)
     */
    public ChatRoomDetailResponse getChatRoomDetail(Long userId, Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        chatRoomMemberRepository.findFirstByChatRoomIdAndUserIdAndLeftAtIsNull(roomId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_MEMBER_NOT_FOUND));

        List<ChatRoomMember> allMembers = chatRoomMemberRepository.findByChatRoomId(roomId);
        List<ChatRoomMemberResponse> memberResponses = allMembers.stream()
                .map(ChatRoomMemberResponse::from)
                .toList();

        if (room.getType() == ChatRoomType.DIRECT) {
            Optional<User> other = allMembers.stream()
                    .filter(m -> !m.getUserId().equals(userId) && m.getLeftAt() == null)
                    .findFirst()
                    .flatMap(m -> userRepository.findById(m.getUserId()));
            String displayName = other.map(User::getName).orElse("알 수 없음");
            String profileImage = other.map(User::getProfileImage).orElse(null);
            return ChatRoomDetailResponse.from(room, memberResponses, displayName, profileImage);
        }
        return ChatRoomDetailResponse.from(room, memberResponses);
    }

    /**
     * 채팅방 이름 수정 — OWNER 권한 필요
     * @param userId 수정 요청하는 회원 ID
     * @param roomId 수정할 채팅방 ID
     * @param request 변경할 이름 정보
     * @return 수정된 채팅방 응답
     */
    @Transactional
    public ChatRoomResponse updateChatRoomName(Long userId, Long roomId, UpdateChatRoomRequest request) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        ChatRoomMember member = chatRoomMemberRepository.findFirstByChatRoomIdAndUserIdAndLeftAtIsNull(roomId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_MEMBER_NOT_FOUND));

        // OWNER 권한 검사
        if (member.getRole() != ChatRoomMemberRole.OWNER) {
            throw new BusinessException(ErrorCode.CHAT_ROOM_NOT_OWNER);
        }

        room.updateName(request.name());

        List<ChatRoomMember> members = chatRoomMemberRepository.findByChatRoomId(roomId);
        return ChatRoomResponse.from(room, members);
    }

    /**
     * 채팅방에 새 멤버 초대 — 기존 참여자만 초대 가능, 중복 참여 방지
     * @param userId 초대를 요청하는 회원 ID
     * @param roomId 초대할 채팅방 ID
     * @param targetUserId 초대할 회원 ID
     */
    @Transactional
    public void inviteMember(Long userId, Long roomId, Long targetUserId) {
        chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        // 초대 요청자가 채팅방 참여자인지 확인
        chatRoomMemberRepository.findFirstByChatRoomIdAndUserIdAndLeftAtIsNull(roomId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_MEMBER_NOT_FOUND));
        userRepository.findById(targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 이미 참여 중인 회원 초대 방지
        chatRoomMemberRepository.findFirstByChatRoomIdAndUserIdAndLeftAtIsNull(roomId, targetUserId)
                .ifPresent(m -> { throw new BusinessException(ErrorCode.CHAT_ROOM_ALREADY_MEMBER); });

        chatRoomMemberRepository.save(ChatRoomMember.join(roomId, targetUserId, ChatRoomMemberRole.MEMBER));
    }

    /**
     * 채팅방 자진 퇴장
     * @param userId 퇴장할 회원 ID
     * @param roomId 퇴장할 채팅방 ID
     */
    @Transactional
    public void leaveChatRoom(Long userId, Long roomId) {
        chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        ChatRoomMember member = chatRoomMemberRepository.findFirstByChatRoomIdAndUserIdAndLeftAtIsNull(roomId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_MEMBER_NOT_FOUND));
        member.leave();
    }

    /**
     * 멤버 강퇴 — OWNER 권한 필요
     * @param userId 강퇴를 요청하는 회원 ID
     * @param roomId 채팅방 ID
     * @param targetUserId 강퇴할 회원 ID
     */
    @Transactional
    public void kickMember(Long userId, Long roomId, Long targetUserId) {
        chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        ChatRoomMember requester = chatRoomMemberRepository.findFirstByChatRoomIdAndUserIdAndLeftAtIsNull(roomId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_MEMBER_NOT_FOUND));

        // 강퇴 권한(OWNER) 검사
        if (requester.getRole() != ChatRoomMemberRole.OWNER) {
            throw new BusinessException(ErrorCode.CHAT_ROOM_NOT_OWNER);
        }

        ChatRoomMember target = chatRoomMemberRepository.findFirstByChatRoomIdAndUserIdAndLeftAtIsNull(roomId, targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_MEMBER_NOT_FOUND));
        target.leave();
    }
}