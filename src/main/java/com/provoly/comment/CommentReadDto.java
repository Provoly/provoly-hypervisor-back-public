package com.provoly.comment;

import java.time.Instant;
import java.util.UUID;

public record CommentReadDto(UUID id,
        UUID creator,
        String creatorName,
        String message,
        Instant creationDate,
        Instant lastModificationDate) {
}
