package hello.connectme.message.dto;

import java.util.List;

public record MessagePageResponse(
        List<MessageResponse> messages,
        Long nextCursor
) {}