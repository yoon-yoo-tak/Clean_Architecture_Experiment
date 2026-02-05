package com.board.messycode.controller;

import com.board.messycode.entity.Comment;
import com.board.messycode.entity.Post;
import com.board.messycode.service.CommentService;
import com.board.messycode.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostService postService;

    @PostMapping
    public ResponseEntity<?> createComment(@PathVariable Long postId, @RequestBody Comment comment) {
        Post post = postService.getPostById(postId);
        if (post == null) {
            return ResponseEntity.status(404).body(Map.of("error", "게시글을 찾을 수 없습니다."));
        }

        // Controller에서도 일부 검증
        if (comment.getPassword() == null || comment.getPassword().length() < 4) {
            return ResponseEntity.badRequest().body(Map.of("error", "비밀번호는 4자 이상이어야 합니다."));
        }

        try {
            Comment saved = commentService.createComment(postId, comment);
            return ResponseEntity.status(201).body(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getComments(@PathVariable Long postId,
                                         @RequestParam(defaultValue = "0") int page) {
        Post post = postService.getPostById(postId);
        if (post == null) {
            return ResponseEntity.status(404).body(Map.of("error", "게시글을 찾을 수 없습니다."));
        }

        Map<String, Object> result = commentService.getComments(postId, page);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long postId, @PathVariable Long commentId,
                                           @RequestBody Map<String, String> request) {
        Post post = postService.getPostById(postId);
        if (post == null) {
            return ResponseEntity.status(404).body(Map.of("error", "게시글을 찾을 수 없습니다."));
        }

        Comment comment = commentService.getCommentById(commentId);
        if (comment == null || !comment.getPostId().equals(postId)) {
            return ResponseEntity.status(404).body(Map.of("error", "댓글을 찾을 수 없습니다."));
        }

        // 비밀번호 검증 (Controller에서 직접)
        String password = request.get("password");
        if (password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "비밀번호는 필수입니다."));
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(password, comment.getPassword())) {
            return ResponseEntity.status(403).body(Map.of("error", "비밀번호가 일치하지 않습니다."));
        }

        commentService.softDeleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{commentId}/replies")
    public ResponseEntity<?> createReply(@PathVariable Long postId, @PathVariable Long commentId,
                                         @RequestBody Comment reply) {
        Post post = postService.getPostById(postId);
        if (post == null) {
            return ResponseEntity.status(404).body(Map.of("error", "게시글을 찾을 수 없습니다."));
        }

        Comment parentComment = commentService.getCommentById(commentId);
        if (parentComment == null || !parentComment.getPostId().equals(postId)) {
            return ResponseEntity.status(404).body(Map.of("error", "댓글을 찾을 수 없습니다."));
        }

        // 부모 댓글이 이미 답글인 경우 (parentId가 있는 경우) 400
        if (parentComment.getParentId() != null) {
            return ResponseEntity.badRequest().body(Map.of("error", "답글에는 답글을 달 수 없습니다."));
        }

        // Controller에서도 일부 검증
        if (reply.getPassword() == null || reply.getPassword().length() < 4) {
            return ResponseEntity.badRequest().body(Map.of("error", "비밀번호는 4자 이상이어야 합니다."));
        }

        try {
            Comment saved = commentService.createReply(postId, commentId, reply);
            return ResponseEntity.status(201).body(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{commentId}/replies")
    public ResponseEntity<?> getReplies(@PathVariable Long postId, @PathVariable Long commentId,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "5") int size) {
        Post post = postService.getPostById(postId);
        if (post == null) {
            return ResponseEntity.status(404).body(Map.of("error", "게시글을 찾을 수 없습니다."));
        }

        Comment parentComment = commentService.getCommentById(commentId);
        if (parentComment == null || !parentComment.getPostId().equals(postId)) {
            return ResponseEntity.status(404).body(Map.of("error", "댓글을 찾을 수 없습니다."));
        }

        Map<String, Object> result = commentService.getReplies(commentId, page, size);
        return ResponseEntity.ok(result);
    }
}
