package hello.connectme.domain.message;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MessageTest {

    @Test
    void create_textMessage_setsAllFields() {
        Message message = Message.create(1L, 2L, MessageType.TEXT, "안녕하세요", null, null, null);

        assertThat(message.getChatRoomId()).isEqualTo(1L);
        assertThat(message.getSenderId()).isEqualTo(2L);
        assertThat(message.getType()).isEqualTo(MessageType.TEXT);
        assertThat(message.getContent()).isEqualTo("안녕하세요");
        assertThat(message.getFileUrl()).isNull();
        assertThat(message.getFileName()).isNull();
        assertThat(message.getFileSize()).isNull();
    }

    @Test
    void create_imageMessage_setsImageFields() {
        Message message = Message.create(1L, 2L, MessageType.IMAGE, null, "https://cdn.example.com/img.jpg", "img.jpg", null);

        assertThat(message.getType()).isEqualTo(MessageType.IMAGE);
        assertThat(message.getContent()).isNull();
        assertThat(message.getFileUrl()).isEqualTo("https://cdn.example.com/img.jpg");
        assertThat(message.getFileName()).isEqualTo("img.jpg");
    }

    @Test
    void create_fileMessage_setsFileFields() {
        Message message = Message.create(1L, 2L, MessageType.FILE, null, "https://cdn.example.com/doc.pdf", "doc.pdf", 204800L);

        assertThat(message.getType()).isEqualTo(MessageType.FILE);
        assertThat(message.getFileUrl()).isEqualTo("https://cdn.example.com/doc.pdf");
        assertThat(message.getFileName()).isEqualTo("doc.pdf");
        assertThat(message.getFileSize()).isEqualTo(204800L);
    }

    @Test
    void isDeleted_returnsFalseByDefault() {
        Message message = Message.create(1L, 2L, MessageType.TEXT, "hello", null, null, null);

        assertThat(message.isDeleted()).isFalse();
    }

    @Test
    void softDelete_setsIsDeletedTrue() {
        Message message = Message.create(1L, 2L, MessageType.TEXT, "hello", null, null, null);

        message.softDelete();

        assertThat(message.isDeleted()).isTrue();
    }
}