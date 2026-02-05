package com.board.cleancode.adapter.in.web;

import com.board.cleancode.adapter.in.web.dto.ChangePasswordRequest;
import com.board.cleancode.adapter.in.web.dto.CommentListResponse;
import com.board.cleancode.adapter.in.web.dto.CreatePostRequest;
import com.board.cleancode.adapter.in.web.dto.DeletePostRequest;
import com.board.cleancode.adapter.in.web.dto.ErrorResponse;
import com.board.cleancode.adapter.in.web.dto.PostListResponse;
import com.board.cleancode.adapter.in.web.dto.PostResponse;
import com.board.cleancode.adapter.in.web.dto.UpdatePostRequest;
import com.board.cleancode.domain.model.Comment;
import com.board.cleancode.domain.model.Post;
import com.board.cleancode.domain.port.in.ChangePostPasswordUseCase;
import com.board.cleancode.domain.port.in.CreatePostUseCase;
import com.board.cleancode.domain.port.in.DeletePostUseCase;
import com.board.cleancode.domain.port.in.GetCommentListUseCase;
import com.board.cleancode.domain.port.in.GetCommentListUseCase.CommentPageResult;
import com.board.cleancode.domain.port.in.GetLikeInfoUseCase;
import com.board.cleancode.domain.port.in.GetPostListUseCase;
import com.board.cleancode.domain.port.in.GetPostListUseCase.PostListQuery;
import com.board.cleancode.domain.port.in.GetPostListUseCase.PostListResult;
import com.board.cleancode.domain.port.in.GetPostUseCase;
import com.board.cleancode.domain.port.in.GetReplyListUseCase;
import com.board.cleancode.domain.port.in.UpdatePostUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private static final int COMMENT_PAGE_SIZE = 5;

    private final CreatePostUseCase createPostUseCase;
    private final GetPostUseCase getPostUseCase;
    private final GetPostListUseCase getPostListUseCase;
    private final UpdatePostUseCase updatePostUseCase;
    private final DeletePostUseCase deletePostUseCase;
    private final ChangePostPasswordUseCase changePostPasswordUseCase;
    private final GetCommentListUseCase getCommentListUseCase;
    private final GetReplyListUseCase getReplyListUseCase;
    private final GetLikeInfoUseCase getLikeInfoUseCase;

    public PostController(CreatePostUseCase createPostUseCase,
                          GetPostUseCase getPostUseCase,
                          GetPostListUseCase getPostListUseCase,
                          UpdatePostUseCase updatePostUseCase,
                          DeletePostUseCase deletePostUseCase,
                          ChangePostPasswordUseCase changePostPasswordUseCase,
                          GetCommentListUseCase getCommentListUseCase,
                          GetReplyListUseCase getReplyListUseCase,
                          GetLikeInfoUseCase getLikeInfoUseCase) {
        this.createPostUseCase = createPostUseCase;
        this.getPostUseCase = getPostUseCase;
        this.getPostListUseCase = getPostListUseCase;
        this.updatePostUseCase = updatePostUseCase;
        this.deletePostUseCase = deletePostUseCase;
        this.changePostPasswordUseCase = changePostPasswordUseCase;
        this.getCommentListUseCase = getCommentListUseCase;
        this.getReplyListUseCase = getReplyListUseCase;
        this.getLikeInfoUseCase = getLikeInfoUseCase;
    }

    private static final java.util.Set<String> ALLOWED_SORT_VALUES = java.util.Set.of("latest", "views", "likes");

    @GetMapping
    public ResponseEntity<?> getPostList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort) {
        if (size != 10 && size != 20) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("size는 10 또는 20만 허용됩니다."));
        }

        String effectiveSort = (sort == null || sort.isEmpty()) ? "latest" : sort;
        if (!ALLOWED_SORT_VALUES.contains(effectiveSort)) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("sort는 latest, views, likes만 허용됩니다."));
        }

        PostListResult result = getPostListUseCase.getPostList(
                new PostListQuery(page, size, searchType, keyword, effectiveSort));
        return ResponseEntity.ok(PostListResponse.from(result));
    }

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody CreatePostRequest request) {
        Post post = createPostUseCase.createPost(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(PostResponse.from(post));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long id,
                                                @RequestHeader(value = "X-Guest-Id", required = false) String guestId) {
        Post post = getPostUseCase.getPost(id);

        CommentPageResult commentPage = getCommentListUseCase.getComments(id, 0, COMMENT_PAGE_SIZE);
        Map<Long, Long> replyCounts = new HashMap<>();
        for (Comment comment : commentPage.content()) {
            replyCounts.put(comment.getId(), getReplyListUseCase.countActiveReplies(comment.getId()));
        }
        CommentListResponse commentsResponse = CommentListResponse.fromWithReplyCounts(commentPage, replyCounts);
        int commentCount = (int) getCommentListUseCase.countActiveComments(id);

        int likeCount = getLikeInfoUseCase.getLikeCount(id);
        boolean liked = getLikeInfoUseCase.isLikedBy(id, guestId);

        return ResponseEntity.ok(PostResponse.from(post, commentsResponse, commentCount, likeCount, liked));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(@PathVariable Long id,
                                                   @Valid @RequestBody UpdatePostRequest request) {
        Post post = updatePostUseCase.updatePost(request.toCommand(id));
        return ResponseEntity.ok(PostResponse.from(post));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id,
                                           @Valid @RequestBody DeletePostRequest request) {
        deletePostUseCase.deletePost(request.toCommand(id));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(@PathVariable Long id,
                                               @Valid @RequestBody ChangePasswordRequest request) {
        changePostPasswordUseCase.changePassword(request.toCommand(id));
        return ResponseEntity.ok().build();
    }
}
