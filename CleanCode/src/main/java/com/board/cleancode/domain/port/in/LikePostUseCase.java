package com.board.cleancode.domain.port.in;

public interface LikePostUseCase {

    LikeResult likePost(Long postId, String guestId);

    record LikeResult(int likeCount, boolean liked) {
    }
}
