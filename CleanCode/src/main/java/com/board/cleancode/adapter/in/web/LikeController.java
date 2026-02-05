package com.board.cleancode.adapter.in.web;

import com.board.cleancode.adapter.in.web.dto.ErrorResponse;
import com.board.cleancode.adapter.in.web.dto.LikeResponse;
import com.board.cleancode.domain.port.in.LikePostUseCase;
import com.board.cleancode.domain.port.in.LikePostUseCase.LikeResult;
import com.board.cleancode.domain.port.in.UnlikePostUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts/{postId}/likes")
public class LikeController {

    private final LikePostUseCase likePostUseCase;
    private final UnlikePostUseCase unlikePostUseCase;

    public LikeController(LikePostUseCase likePostUseCase, UnlikePostUseCase unlikePostUseCase) {
        this.likePostUseCase = likePostUseCase;
        this.unlikePostUseCase = unlikePostUseCase;
    }

    @PostMapping
    public ResponseEntity<?> likePost(@PathVariable Long postId,
                                      @RequestHeader(value = "X-Guest-Id", required = false) String guestId) {
        if (guestId == null || guestId.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("X-Guest-Id 헤더가 필요합니다."));
        }

        LikeResult result = likePostUseCase.likePost(postId, guestId);
        return ResponseEntity.ok(LikeResponse.from(result));
    }

    @DeleteMapping
    public ResponseEntity<?> unlikePost(@PathVariable Long postId,
                                        @RequestHeader(value = "X-Guest-Id", required = false) String guestId) {
        if (guestId == null || guestId.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("X-Guest-Id 헤더가 필요합니다."));
        }

        LikeResult result = unlikePostUseCase.unlikePost(postId, guestId);
        return ResponseEntity.ok(LikeResponse.from(result));
    }
}
