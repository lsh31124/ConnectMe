package hello.connectme.message.controller;

import hello.connectme.message.dto.MessageResponse;
import hello.connectme.message.dto.SendMessageRequest;
import hello.connectme.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/{roomId}")
    public void sendMessage(
            @DestinationVariable Long roomId,
            @Payload SendMessageRequest request,
            SimpMessageHeaderAccessor headerAccessor) {
        Long senderId = (Long) headerAccessor.getSessionAttributes().get("userId");
        SendMessageRequest fullRequest = new SendMessageRequest(
                roomId,
                request.type(),
                request.content(),
                request.fileUrl(),
                request.fileName(),
                request.fileSize()
        );
        MessageResponse response = messageService.sendMessage(senderId, fullRequest);
        messagingTemplate.convertAndSend("/sub/chat/" + roomId, response);
    }
}