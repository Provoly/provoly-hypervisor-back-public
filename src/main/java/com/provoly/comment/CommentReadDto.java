package com.provoly.comment;

import java.time.Instant;
import java.util.UUID;

public record CommentReadDto(UUID id,
        String creator,
        String message,
        Instant creationDate,
        Instant lastModificationDate) {
}
