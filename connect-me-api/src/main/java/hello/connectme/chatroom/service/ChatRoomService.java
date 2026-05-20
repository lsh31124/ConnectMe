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
import hello.connectme.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatRoomResponse createDirectRoom(Long userId, Long targetUserId) {
        userRepository.findById(targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        ChatRoom room = chatRoomRepository.save(ChatRoom.createDirect(userId));
        chatRoomMemberRepository.save(ChatRoomMember.join(room.getId(), userId, ChatRoomMemberRole.OWNER));
        chatRoomMemberRepository.save(ChatRoomMember.join(room.getId(), targetUserId, ChatRoomMemberRole.MEMBER));

        List<ChatRoomMember> members = chatRoomMemberRepository.findByChatRoomId(room.getId());
        return ChatRoomResponse.from(room, members);
    }

    @Transactional
    public ChatRoomResponse createGroupRoom(Long userId, CreateGroupRoomRequest request) {
        for (Long memberId : request.memberIds()) {
            userRepository.findById(memberId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        }

        ChatRoom room = chatRoomRepository.save(ChatRoom.createGroup(request.name(), userId));
        chatRoomMemberRepository.save(ChatRoomMember.join(room.getId(), userId, ChatRoomMemberRole.OWNER));
        for (Long memberId : request.memberIds()) {
            chatRoomMemberRepository.save(ChatRoomMember.join(room.getId(), memberId, ChatRoomMemberRole.MEMBER));
        }

        List<ChatRoomMember> members = chatRoomMemberRepository.findByChatRoomId(room.getId());
        return ChatRoomResponse.from(room, members);
    }

    public List<ChatRoomResponse> getMyChatRooms(Long userId) {
        List<ChatRoomMember> memberships = chatRoomMemberRepository.findByUserIdAndLeftAtIsNull(userId);
        List<Long> roomIds = memberships.stream().map(ChatRoomMember::getChatRoomId).toList();
        List<ChatRoom> rooms = chatRoomRepository.findAllById(roomIds);

        return rooms.stream()
                .map(room -> {
                    List<ChatRoomMember> members = chatRoomMemberRepository.findByChatRoomId(room.getId());
                    return ChatRoomResponse.from(room, members);
                })
                .toList();
    }

    public ChatRoomDetailResponse getChatRoomDetail(Long userId, Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        chatRoomMemberRepository.findByChatRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_MEMBER_NOT_FOUND));

        List<ChatRoomMemberResponse> members = chatRoomMemberRepository.findByChatRoomId(roomId).stream()
                .map(ChatRoomMemberResponse::from)
                .toList();
        return ChatRoomDetailResponse.from(room, members);
    }

    @Transactional
    public ChatRoomResponse updateChatRoomName(Long userId, Long roomId, UpdateChatRoomRequest request) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        ChatRoomMember member = chatRoomMemberRepository.findByChatRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_MEMBER_NOT_FOUND));

        if (member.getRole() != ChatRoomMemberRole.OWNER) {
            throw new BusinessException(ErrorCode.CHAT_ROOM_NOT_OWNER);
        }

        room.updateName(request.name());

        List<ChatRoomMember> members = chatRoomMemberRepository.findByChatRoomId(roomId);
        return ChatRoomResponse.from(room, members);
    }

    @Transactional
    public void inviteMember(Long userId, Long roomId, Long targetUserId) {
        chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        chatRoomMemberRepository.findByChatRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_MEMBER_NOT_FOUND));
        userRepository.findById(targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        chatRoomMemberRepository.findByChatRoomIdAndUserId(roomId, targetUserId)
                .ifPresent(m -> { throw new BusinessException(ErrorCode.CHAT_ROOM_ALREADY_MEMBER); });

        chatRoomMemberRepository.save(ChatRoomMember.join(roomId, targetUserId, ChatRoomMemberRole.MEMBER));
    }

    @Transactional
    public void leaveChatRoom(Long userId, Long roomId) {
        chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        ChatRoomMember member = chatRoomMemberRepository.findByChatRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_MEMBER_NOT_FOUND));
        member.leave();
    }

    @Transactional
    public void kickMember(Long userId, Long roomId, Long targetUserId) {
        chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        ChatRoomMember requester = chatRoomMemberRepository.findByChatRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_MEMBER_NOT_FOUND));

        if (requester.getRole() != ChatRoomMemberRole.OWNER) {
            throw new BusinessException(ErrorCode.CHAT_ROOM_NOT_OWNER);
        }

        ChatRoomMember target = chatRoomMemberRepository.findByChatRoomIdAndUserId(roomId, targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_MEMBER_NOT_FOUND));
        target.leave();
    }
}