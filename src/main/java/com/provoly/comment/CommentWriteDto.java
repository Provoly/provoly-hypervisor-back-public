package com.provoly.comment;

import java.util.UUID;

public record CommentWriteDto(UUID id, String message) {
}
