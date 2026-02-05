package com.board.cleancode.domain.port.in;

import com.board.cleancode.domain.port.in.LikePostUseCase.LikeResult;

public interface UnlikePostUseCase {

    LikeResult unlikePost(Long postId, String guestId);
}
