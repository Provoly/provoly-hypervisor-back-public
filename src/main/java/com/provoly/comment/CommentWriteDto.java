package com.provoly.comment;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CommentWriteDto(@NotNull UUID id, @NotNull String message) {
}
