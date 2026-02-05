package com.board.cleancode.adapter.in.web;

import com.board.cleancode.adapter.in.web.dto.CommentListResponse;
import com.board.cleancode.adapter.in.web.dto.CommentResponse;
import com.board.cleancode.adapter.in.web.dto.CreateCommentRequest;
import com.board.cleancode.adapter.in.web.dto.DeleteCommentRequest;
import com.board.cleancode.adapter.in.web.dto.ReplyListResponse;
import com.board.cleancode.domain.model.Comment;
import com.board.cleancode.domain.port.in.CreateCommentUseCase;
import com.board.cleancode.domain.port.in.CreateReplyUseCase;
import com.board.cleancode.domain.port.in.DeleteCommentUseCase;
import com.board.cleancode.domain.port.in.GetCommentListUseCase;
import com.board.cleancode.domain.port.in.GetCommentListUseCase.CommentPageResult;
import com.board.cleancode.domain.port.in.GetReplyListUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    private final CreateCommentUseCase createCommentUseCase;
    private final GetCommentListUseCase getCommentListUseCase;
    private final DeleteCommentUseCase deleteCommentUseCase;
    private final CreateReplyUseCase createReplyUseCase;
    private final GetReplyListUseCase getReplyListUseCase;

    public CommentController(CreateCommentUseCase createCommentUseCase,
                             GetCommentListUseCase getCommentListUseCase,
                             DeleteCommentUseCase deleteCommentUseCase,
                             CreateReplyUseCase createReplyUseCase,
                             GetReplyListUseCase getReplyListUseCase) {
        this.createCommentUseCase = createCommentUseCase;
        this.getCommentListUseCase = getCommentListUseCase;
        this.deleteCommentUseCase = deleteCommentUseCase;
        this.createReplyUseCase = createReplyUseCase;
        this.getReplyListUseCase = getReplyListUseCase;
    }

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(@PathVariable Long postId,
                                                         @Valid @RequestBody CreateCommentRequest request) {
        Comment comment = createCommentUseCase.createComment(request.toCommand(postId));
        return ResponseEntity.status(HttpStatus.CREATED).body(CommentResponse.from(comment));
    }

    @GetMapping
    public ResponseEntity<CommentListResponse> getComments(@PathVariable Long postId,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "5") int size) {
        CommentPageResult result = getCommentListUseCase.getComments(postId, page, size);
        Map<Long, Long> replyCounts = new HashMap<>();
        for (Comment comment : result.content()) {
            replyCounts.put(comment.getId(), getReplyListUseCase.countActiveReplies(comment.getId()));
        }
        return ResponseEntity.ok(CommentListResponse.fromWithReplyCounts(result, replyCounts));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long postId,
                                              @PathVariable Long commentId,
                                              @Valid @RequestBody DeleteCommentRequest request) {
        deleteCommentUseCase.deleteComment(request.toCommand(postId, commentId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{commentId}/replies")
    public ResponseEntity<CommentResponse> createReply(@PathVariable Long postId,
                                                       @PathVariable Long commentId,
                                                       @Valid @RequestBody CreateCommentRequest request) {
        Comment reply = createReplyUseCase.createReply(request.toReplyCommand(postId, commentId));
        return ResponseEntity.status(HttpStatus.CREATED).body(CommentResponse.from(reply));
    }

    @GetMapping("/{commentId}/replies")
    public ResponseEntity<ReplyListResponse> getReplies(@PathVariable Long postId,
                                                        @PathVariable Long commentId,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "5") int size) {
        GetReplyListUseCase.ReplyPageResult result = getReplyListUseCase.getReplies(postId, commentId, page, size);
        return ResponseEntity.ok(ReplyListResponse.from(result));
    }
}
