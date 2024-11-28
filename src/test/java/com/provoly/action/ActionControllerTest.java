package com.provoly.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.UUID;

import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;

import com.provoly.TestDataService;
import com.provoly.action.dto.ActionWriteDto;
import com.provoly.comment.CommentWriteDto;
import com.provoly.event.Criticality;
import com.provoly.event.Status;
import com.provoly.event.dto.InternalEventWriteDto;
import com.provoly.procedure.ProcedureController;
import com.provoly.procedure.ProcedureWriteDto;
import com.provoly.user.Role;
import com.provoly.user.UserService;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ActionControllerTest {
    @Inject
    ActionController actionController;

    @Inject
    ProcedureController procedureController;

    @Inject
    TestDataService dataService;

    @InjectMock
    UserService mock;

    @BeforeEach
    public void init() {
        dataService.init();
        given(mock.getCurrentUserName()).willReturn("reader");
        given(mock.getCurrentUserFullName()).willReturn("name");
        given(mock.getCurrentUserSubject()).willReturn(dataService.getUser().getSubject());
        given(mock.getCurrentUser()).willReturn(dataService.getUser());
        given(mock.hasRole(Role.STR_EVENT_WRITE)).willReturn(true);
    }

    @AfterEach
    public void clean() {
        dataService.clean();
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_write", "event_read", "event_proc_write", "event_proc_comment_write" })
    void should_increment_comment_count_and_get_last_comment_when_add_new_comment_on_event() {
        // given
        Integer procedureId = dataService.getProcedure3().getId();
        UUID actionId = UUID.randomUUID();
        ProcedureWriteDto dto = new ProcedureWriteDto(
                procedureId,
                "procedure maintenance",
                "desc",
                List.of(),
                List.of(new ActionWriteDto(actionId, "CSU", Status.NEW)));
        procedureController.updateProcedure(procedureId, dto);

        var comment = new CommentWriteDto(UUID.randomUUID(), "message");
        actionController.saveOrUpdateCommentForAction(actionId, comment);

        var comment2 = new CommentWriteDto(UUID.randomUUID(), "message2");

        // when
        actionController.saveOrUpdateCommentForAction(actionId, comment2);
        var procedureWithCommentsOnAction = procedureController.getProcedureDetails(procedureId);

        //then
        assertThat(procedureWithCommentsOnAction.actions().stream().toList().getFirst().getCommentsCount()).isEqualTo(2);
        assertThat(procedureWithCommentsOnAction.actions().stream().toList().getFirst().getLastComment().id())
                .isEqualTo(comment2.id());
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_write", "event_read", "event_proc_write", "event_proc_comment_write" })
    void should_sort_comment_on_modification_date_when_add_new_comment_on_event() {
        // given
        Integer procedureId = dataService.getProcedure3().getId();
        UUID actionId = UUID.randomUUID();
        ProcedureWriteDto dto = new ProcedureWriteDto(
                procedureId,
                "procedure maintenance",
                "desc",
                List.of(),
                List.of(new ActionWriteDto(actionId, "CSU", Status.NEW)));
        procedureController.updateProcedure(procedureId, dto);

        var comment = new CommentWriteDto(UUID.randomUUID(), "message");
        var comment2 = new CommentWriteDto(UUID.randomUUID(), "message2");
        actionController.saveOrUpdateCommentForAction(actionId, comment);
        actionController.saveOrUpdateCommentForAction(actionId, comment2);

        // when
        actionController.saveOrUpdateCommentForAction(actionId,
                new CommentWriteDto(comment.id(), "message updated"));
        var comments = actionController.getCommentsForAction(actionId);

        //then
        assertThat(comments).extracting("message").containsExactly("message updated", "message2");
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_write", "event_read", "event_proc_write", "event_proc_comment_write" })
    void should_throw_error_when_comment_done_procedure() {
        // given
        var procedure = dataService.getProcedure3();
        var procedureId = procedure.getId();
        var event = dataService.getEvent1();
        UUID actionId = UUID.randomUUID();

        var reportDto = new InternalEventWriteDto(event.getId(),
                event.getName(),
                "description",
                Criticality.HIGH,
                event.getCategory().getCode(),
                null,
                "new address",
                null,
                null,
                event.getStartDate(),
                event.getEndDate(),
                event.getCreationDate(),
                null,
                event.getCreator());

        ProcedureWriteDto dto = new ProcedureWriteDto(
                procedureId,
                "procedure maintenance",
                "desc",
                List.of(reportDto),
                List.of(new ActionWriteDto(actionId, "CSU", Status.NEW)));

        procedureController.updateProcedure(procedureId, dto);
        procedureController.closeAllProcedureEvents(procedureId, new CommentWriteDto(UUID.randomUUID(), "close proc"));

        var comment = new CommentWriteDto(UUID.randomUUID(), "message");

        assertThatThrownBy(() -> actionController.saveOrUpdateCommentForAction(actionId, comment))
                .isInstanceOf(ForbiddenException.class);

    }
}
