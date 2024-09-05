package com.provoly.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.NoSuchElementException;
import java.util.UUID;

import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;

import com.provoly.TestDataService;

import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CommentServiceTest {

    @Inject
    CommentService commentService;

    @Inject
    TestDataService dataService;

    @BeforeAll
    public void init() {
        dataService.init();
    }

    @AfterAll
    public void clean() {
        dataService.clean();
    }

    @Test
    void should_throw_error_when_comment_on_invalid_event() {
        // when
        assertThatThrownBy(() -> commentService.saveOrUpdateCommentForEvent(11111,
                new CommentWriteDto(UUID.randomUUID(), "message")))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void should_throw_error_when_comment_on_done_event() {
        var eventId = dataService.getDoneEvent().getId();
        // when
        assertThatThrownBy(() -> commentService.saveOrUpdateCommentForEvent(eventId,
                new CommentWriteDto(UUID.randomUUID(), "message")))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("done and can no longer be commented on");
    }

    @Test
    void should_sort_comment_on_modification_date_when_add_new_comment_on_event() {
        // given
        var eventId = dataService.getEvent1().getId();
        var comment = new CommentWriteDto(UUID.randomUUID(), "message");
        var comment2 = new CommentWriteDto(UUID.randomUUID(), "message2");
        commentService.saveOrUpdateCommentForEvent(eventId, comment);
        commentService.saveOrUpdateCommentForEvent(eventId, comment2);

        // when
        commentService.saveOrUpdateCommentForEvent(eventId,
                new CommentWriteDto(comment.id(), "message updated"));
        var comments = commentService.getCommentsForEvent(eventId);

        //then
        assertThat(comments).extracting("message").containsExactly("message updated", "message2");
    }

}
