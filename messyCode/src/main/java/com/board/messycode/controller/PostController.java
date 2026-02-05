package com.board.messycode.controller;

import com.board.messycode.entity.Post;
import com.board.messycode.service.CommentService;
import com.board.messycode.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @GetMapping
    public ResponseEntity<?> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "latest") String sort) {
        // size 검증 (Controller에서 직접)
        if (size != 10 && size != 20) {
            return ResponseEntity.badRequest().body(Map.of("error", "size는 10 또는 20만 허용됩니다."));
        }
        // sort 검증 (Controller에서 직접)
        if (!"latest".equals(sort) && !"views".equals(sort) && !"likes".equals(sort)) {
            return ResponseEntity.badRequest().body(Map.of("error", "sort는 latest, views, likes만 허용됩니다."));
        }
        Map<String, Object> result = postService.getPostList(page, size, searchType, keyword, sort);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody Post post) {
        // Controller에서도 일부 검증 (비밀번호, 해시태그)
        if (post.getPassword() == null || post.getPassword().length() < 4) {
            return ResponseEntity.badRequest().body(Map.of("error", "비밀번호는 4자 이상이어야 합니다."));
        }
        if (post.getHashtags() != null && post.getHashtags().size() > 5) {
            return ResponseEntity.badRequest().body(Map.of("error", "해시태그는 최대 5개까지 가능합니다."));
        }
        try {
            Post saved = postService.createPost(post);
            return ResponseEntity.status(201).body(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPost(@PathVariable Long id,
                                     @RequestHeader(value = "X-Guest-Id", required = false) String guestId) {
        Post post = postService.getPostAndIncreaseViewCount(id);
        if (post == null) {
            return ResponseEntity.status(404).body(Map.of("error", "게시글을 찾을 수 없습니다."));
        }

        // 좋아요 여부 확인
        boolean liked = false;
        if (guestId != null && !guestId.trim().isEmpty()) {
            liked = postService.hasLiked(post.getId(), guestId);
        }

        // 게시글 상세에 댓글 포함
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", post.getId());
        response.put("title", post.getTitle());
        response.put("content", post.getContent());
        response.put("author", post.getAuthor());
        response.put("hashtags", post.getHashtags());
        response.put("viewCount", post.getViewCount());
        response.put("likeCount", post.getLikeCount());
        response.put("liked", liked);
        response.put("commentCount", commentService.countNonDeletedComments(post.getId()));
        response.put("createdAt", post.getCreatedAt());
        response.put("updatedAt", post.getUpdatedAt());
        response.put("comments", commentService.getComments(post.getId(), 0));

        return ResponseEntity.ok(response);
    }

    @SuppressWarnings("unchecked")
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Post post = postService.getPostById(id);
        if (post == null) {
            return ResponseEntity.status(404).body(Map.of("error", "게시글을 찾을 수 없습니다."));
        }

        // 비밀번호 검증 (Controller에서 직접)
        String password = (String) request.get("password");
        if (password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "비밀번호는 필수입니다."));
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(password, post.getPassword())) {
            return ResponseEntity.status(403).body(Map.of("error", "비밀번호가 일치하지 않습니다."));
        }

        // 유효성 검증 (Controller에서 직접)
        String title = (String) request.get("title");
        String content = (String) request.get("content");
        if (title == null || title.trim().isEmpty() || title.length() > 200) {
            return ResponseEntity.badRequest().body(Map.of("error", "제목은 1~200자여야 합니다."));
        }
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "내용은 필수입니다."));
        }

        List<String> hashtags = (List<String>) request.get("hashtags");
        if (hashtags != null) {
            if (hashtags.size() > 5) {
                return ResponseEntity.badRequest().body(Map.of("error", "해시태그는 최대 5개까지 가능합니다."));
            }
            for (String tag : hashtags) {
                if (tag == null || tag.trim().isEmpty() || tag.length() > 30) {
                    return ResponseEntity.badRequest().body(Map.of("error", "해시태그는 1~30자여야 합니다."));
                }
            }
        }

        Post updated = postService.updatePost(id, title, content, hashtags);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Post post = postService.getPostById(id);
        if (post == null) {
            return ResponseEntity.status(404).body(Map.of("error", "게시글을 찾을 수 없습니다."));
        }

        // 비밀번호 검증 (Controller에서 직접)
        String password = request.get("password");
        if (password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "비밀번호는 필수입니다."));
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(password, post.getPassword())) {
            return ResponseEntity.status(403).body(Map.of("error", "비밀번호가 일치하지 않습니다."));
        }

        commentService.deleteCommentsByPostId(id);
        postService.deleteLikesByPostId(id);
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{postId}/likes")
    public ResponseEntity<?> likePost(@PathVariable Long postId,
                                      @RequestHeader(value = "X-Guest-Id", required = false) String guestId) {
        // X-Guest-Id 헤더 검증 (Controller에서 직접)
        if (guestId == null || guestId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "X-Guest-Id 헤더는 필수입니다."));
        }

        Post post = postService.getPostById(postId);
        if (post == null) {
            return ResponseEntity.status(404).body(Map.of("error", "게시글을 찾을 수 없습니다."));
        }

        Map<String, Object> result = postService.likePost(postId, guestId);
        if (result == null) {
            return ResponseEntity.status(409).body(Map.of("error", "이미 좋아요한 게시글입니다."));
        }

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{postId}/likes")
    public ResponseEntity<?> unlikePost(@PathVariable Long postId,
                                        @RequestHeader(value = "X-Guest-Id", required = false) String guestId) {
        // X-Guest-Id 헤더 검증 (Controller에서 직접)
        if (guestId == null || guestId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "X-Guest-Id 헤더는 필수입니다."));
        }

        Post post = postService.getPostById(postId);
        if (post == null) {
            return ResponseEntity.status(404).body(Map.of("error", "게시글을 찾을 수 없습니다."));
        }

        Map<String, Object> result = postService.unlikePost(postId, guestId);
        if (result == null) {
            return ResponseEntity.status(409).body(Map.of("error", "좋아요하지 않은 게시글입니다."));
        }

        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<?> changePassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Post post = postService.getPostById(id);
        if (post == null) {
            return ResponseEntity.status(404).body(Map.of("error", "게시글을 찾을 수 없습니다."));
        }

        // 현재 비밀번호 검증 (Controller에서 직접)
        String currentPassword = request.get("currentPassword");
        if (currentPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "현재 비밀번호는 필수입니다."));
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(currentPassword, post.getPassword())) {
            return ResponseEntity.status(403).body(Map.of("error", "비밀번호가 일치하지 않습니다."));
        }

        // 새 비밀번호 유효성 검증 (Controller에서 직접)
        String newPassword = request.get("newPassword");
        if (newPassword == null || newPassword.length() < 4) {
            return ResponseEntity.badRequest().body(Map.of("error", "새 비밀번호는 4자 이상이어야 합니다."));
        }

        postService.changePassword(id, newPassword);
        return ResponseEntity.ok(Map.of("message", "비밀번호가 변경되었습니다."));
    }
}
