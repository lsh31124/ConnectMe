package hello.connectme.chatroom.service;

import hello.connectme.chatroom.dto.ChatRoomDetailResponse;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

    @InjectMocks
    private ChatRoomService chatRoomService;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @Mock
    private UserRepository userRepository;

    private ChatRoom createTestDirectRoom(Long id) {
        ChatRoom room = ChatRoom.createDirect(1L, 2L);
        ReflectionTestUtils.setField(room, "id", id);
        return room;
    }

    private ChatRoom createTestGroupRoom(Long id, String name) {
        ChatRoom room = ChatRoom.createGroup(name, 1L);
        ReflectionTestUtils.setField(room, "id", id);
        return room;
    }

    private ChatRoomMember createTestMember(Long id, Long chatRoomId, Long userId, ChatRoomMemberRole role) {
        ChatRoomMember member = ChatRoomMember.join(chatRoomId, userId, role);
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    private User createTestUser(Long id, String email) {
        User user = User.createLocal(email, null, "이름", "encodedPw");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Test
    void createDirectRoom_success() {
        User target = createTestUser(2L, "target@email.com");
        ChatRoom room = createTestDirectRoom(10L);
        ChatRoomMember ownerMember = createTestMember(1L, 10L, 1L, ChatRoomMemberRole.OWNER);
        ChatRoomMember targetMember = createTestMember(2L, 10L, 2L, ChatRoomMemberRole.MEMBER);

        given(userRepository.findById(2L)).willReturn(Optional.of(target));
        given(chatRoomRepository.findByDirectRoomKey("1_2")).willReturn(Optional.empty());
        given(chatRoomRepository.save(any(ChatRoom.class))).willReturn(room);
        given(chatRoomMemberRepository.save(any(ChatRoomMember.class)))
                .willReturn(ownerMember).willReturn(targetMember);
        given(chatRoomMemberRepository.findByChatRoomId(10L)).willReturn(List.of(ownerMember, targetMember));

        ChatRoomResponse response = chatRoomService.createDirectRoom(1L, 2L);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.type()).isEqualTo(ChatRoomType.DIRECT.name());
        assertThat(response.memberCount()).isEqualTo(2);
    }

    @Test
    void createDirectRoom_existingRoom_returnsExisting() {
        User target = createTestUser(2L, "target@email.com");
        ChatRoom existingRoom = createTestDirectRoom(10L);
        ChatRoomMember ownerMember = createTestMember(1L, 10L, 1L, ChatRoomMemberRole.OWNER);
        ChatRoomMember targetMember = createTestMember(2L, 10L, 2L, ChatRoomMemberRole.MEMBER);

        given(userRepository.findById(2L)).willReturn(Optional.of(target));
        given(chatRoomRepository.findByDirectRoomKey("1_2")).willReturn(Optional.of(existingRoom));
        given(chatRoomMemberRepository.findByChatRoomId(10L)).willReturn(List.of(ownerMember, targetMember));

        ChatRoomResponse response = chatRoomService.createDirectRoom(1L, 2L);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.memberCount()).isEqualTo(2);
        then(chatRoomRepository).should(org.mockito.Mockito.never()).save(any(ChatRoom.class));
    }

    @Test
    void createDirectRoom_targetUserNotFound_throwsException() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> chatRoomService.createDirectRoom(1L, 99L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void createGroupRoom_success() {
        CreateGroupRoomRequest request = new CreateGroupRoomRequest("테스트 그룹", List.of(2L, 3L));
        User user2 = createTestUser(2L, "user2@email.com");
        User user3 = createTestUser(3L, "user3@email.com");
        ChatRoom room = createTestGroupRoom(10L, "테스트 그룹");
        ChatRoomMember ownerMember = createTestMember(1L, 10L, 1L, ChatRoomMemberRole.OWNER);

        given(userRepository.findById(2L)).willReturn(Optional.of(user2));
        given(userRepository.findById(3L)).willReturn(Optional.of(user3));
        given(chatRoomRepository.save(any(ChatRoom.class))).willReturn(room);
        given(chatRoomMemberRepository.save(any(ChatRoomMember.class))).willReturn(ownerMember);
        given(chatRoomMemberRepository.findByChatRoomId(10L)).willReturn(List.of(ownerMember));

        ChatRoomResponse response = chatRoomService.createGroupRoom(1L, request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.type()).isEqualTo(ChatRoomType.GROUP.name());
        assertThat(response.name()).isEqualTo("테스트 그룹");
    }

    @Test
    void getMyChatRooms_success() {
        ChatRoomMember membership = createTestMember(1L, 10L, 1L, ChatRoomMemberRole.OWNER);
        ChatRoom room = createTestGroupRoom(10L, "그룹방");
        ChatRoomMember member2 = createTestMember(2L, 10L, 2L, ChatRoomMemberRole.MEMBER);

        given(chatRoomMemberRepository.findByUserIdAndLeftAtIsNull(1L)).willReturn(List.of(membership));
        given(chatRoomRepository.findAllById(List.of(10L))).willReturn(List.of(room));
        given(chatRoomMemberRepository.findByChatRoomIdIn(List.of(10L))).willReturn(List.of(membership, member2));

        List<ChatRoomResponse> result = chatRoomService.getMyChatRooms(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(10L);
        assertThat(result.get(0).memberCount()).isEqualTo(2);
    }

    @Test
    void getChatRoomDetail_success() {
        ChatRoom room = createTestGroupRoom(10L, "그룹방");
        ChatRoomMember member1 = createTestMember(1L, 10L, 1L, ChatRoomMemberRole.OWNER);
        ChatRoomMember member2 = createTestMember(2L, 10L, 2L, ChatRoomMemberRole.MEMBER);

        given(chatRoomRepository.findById(10L)).willReturn(Optional.of(room));
        given(chatRoomMemberRepository.findFirstByChatRoomIdAndUserIdAndLeftAtIsNull(10L, 1L)).willReturn(Optional.of(member1));
        given(chatRoomMemberRepository.findByChatRoomId(10L)).willReturn(List.of(member1, member2));

        ChatRoomDetailResponse response = chatRoomService.getChatRoomDetail(1L, 10L);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.members()).hasSize(2);
    }

    @Test
    void getChatRoomDetail_notMember_throwsException() {
        ChatRoom room = createTestGroupRoom(10L, "그룹방");
        given(chatRoomRepository.findById(10L)).willReturn(Optional.of(room));
        given(chatRoomMemberRepository.findFirstByChatRoomIdAndUserIdAndLeftAtIsNull(10L, 99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> chatRoomService.getChatRoomDetail(99L, 10L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.CHAT_ROOM_MEMBER_NOT_FOUND);
    }

    @Test
    void updateChatRoomName_success() {
        ChatRoom room = createTestGroupRoom(10L, "기존이름");
        ChatRoomMember ownerMember = createTestMember(1L, 10L, 1L, ChatRoomMemberRole.OWNER);
        UpdateChatRoomRequest request = new UpdateChatRoomRequest("새이름");

        given(chatRoomRepository.findById(10L)).willReturn(Optional.of(room));
        given(chatRoomMemberRepository.findFirstByChatRoomIdAndUserIdAndLeftAtIsNull(10L, 1L)).willReturn(Optional.of(ownerMember));
        given(chatRoomMemberRepository.findByChatRoomId(10L)).willReturn(List.of(ownerMember));

        ChatRoomResponse response = chatRoomService.updateChatRoomName(1L, 10L, request);

        assertThat(response.name()).isEqualTo("새이름");
    }

    @Test
    void updateChatRoomName_notOwner_throwsException() {
        ChatRoom room = createTestGroupRoom(10L, "기존이름");
        ChatRoomMember memberRole = createTestMember(2L, 10L, 2L, ChatRoomMemberRole.MEMBER);
        UpdateChatRoomRequest request = new UpdateChatRoomRequest("새이름");

        given(chatRoomRepository.findById(10L)).willReturn(Optional.of(room));
        given(chatRoomMemberRepository.findFirstByChatRoomIdAndUserIdAndLeftAtIsNull(10L, 2L)).willReturn(Optional.of(memberRole));

        assertThatThrownBy(() -> chatRoomService.updateChatRoomName(2L, 10L, request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.CHAT_ROOM_NOT_OWNER);
    }

    @Test
    void inviteMember_success() {
        ChatRoom room = createTestGroupRoom(10L, "그룹방");
        ChatRoomMember ownerMember = createTestMember(1L, 10L, 1L, ChatRoomMemberRole.OWNER);
        User targetUser = createTestUser(3L, "new@email.com");

        given(chatRoomRepository.findById(10L)).willReturn(Optional.of(room));
        given(chatRoomMemberRepository.findFirstByChatRoomIdAndUserIdAndLeftAtIsNull(10L, 1L)).willReturn(Optional.of(ownerMember));
        given(userRepository.findById(3L)).willReturn(Optional.of(targetUser));
        given(chatRoomMemberRepository.findFirstByChatRoomIdAndUserIdAndLeftAtIsNull(10L, 3L)).willReturn(Optional.empty());

        chatRoomService.inviteMember(1L, 10L, 3L);

        then(chatRoomMemberRepository).should().save(any(ChatRoomMember.class));
    }

    @Test
    void inviteMember_alreadyMember_throwsException() {
        ChatRoom room = createTestGroupRoom(10L, "그룹방");
        ChatRoomMember ownerMember = createTestMember(1L, 10L, 1L, ChatRoomMemberRole.OWNER);
        User targetUser = createTestUser(2L, "existing@email.com");
        ChatRoomMember existingMember = createTestMember(2L, 10L, 2L, ChatRoomMemberRole.MEMBER);

        given(chatRoomRepository.findById(10L)).willReturn(Optional.of(room));
        given(chatRoomMemberRepository.findFirstByChatRoomIdAndUserIdAndLeftAtIsNull(10L, 1L)).willReturn(Optional.of(ownerMember));
        given(userRepository.findById(2L)).willReturn(Optional.of(targetUser));
        given(chatRoomMemberRepository.findFirstByChatRoomIdAndUserIdAndLeftAtIsNull(10L, 2L)).willReturn(Optional.of(existingMember));

        assertThatThrownBy(() -> chatRoomService.inviteMember(1L, 10L, 2L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.CHAT_ROOM_ALREADY_MEMBER);
    }

    @Test
    void leaveChatRoom_success() {
        ChatRoom room = createTestGroupRoom(10L, "그룹방");
        ChatRoomMember member = createTestMember(2L, 10L, 2L, ChatRoomMemberRole.MEMBER);

        given(chatRoomRepository.findById(10L)).willReturn(Optional.of(room));
        given(chatRoomMemberRepository.findFirstByChatRoomIdAndUserIdAndLeftAtIsNull(10L, 2L)).willReturn(Optional.of(member));

        chatRoomService.leaveChatRoom(2L, 10L);

        assertThat(member.getLeftAt()).isNotNull();
    }

    @Test
    void kickMember_success() {
        ChatRoom room = createTestGroupRoom(10L, "그룹방");
        ChatRoomMember ownerMember = createTestMember(1L, 10L, 1L, ChatRoomMemberRole.OWNER);
        ChatRoomMember targetMember = createTestMember(2L, 10L, 2L, ChatRoomMemberRole.MEMBER);

        given(chatRoomRepository.findById(10L)).willReturn(Optional.of(room));
        given(chatRoomMemberRepository.findFirstByChatRoomIdAndUserIdAndLeftAtIsNull(10L, 1L)).willReturn(Optional.of(ownerMember));
        given(chatRoomMemberRepository.findFirstByChatRoomIdAndUserIdAndLeftAtIsNull(10L, 2L)).willReturn(Optional.of(targetMember));

        chatRoomService.kickMember(1L, 10L, 2L);

        assertThat(targetMember.getLeftAt()).isNotNull();
    }

    @Test
    void kickMember_notOwner_throwsException() {
        ChatRoom room = createTestGroupRoom(10L, "그룹방");
        ChatRoomMember memberRole = createTestMember(2L, 10L, 2L, ChatRoomMemberRole.MEMBER);

        given(chatRoomRepository.findById(10L)).willReturn(Optional.of(room));
        given(chatRoomMemberRepository.findFirstByChatRoomIdAndUserIdAndLeftAtIsNull(10L, 2L)).willReturn(Optional.of(memberRole));

        assertThatThrownBy(() -> chatRoomService.kickMember(2L, 10L, 3L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.CHAT_ROOM_NOT_OWNER);
    }
}