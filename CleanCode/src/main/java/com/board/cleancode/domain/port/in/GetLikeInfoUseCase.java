package com.board.cleancode.domain.port.in;

public interface GetLikeInfoUseCase {

    int getLikeCount(Long postId);

    boolean isLikedBy(Long postId, String guestId);
}
