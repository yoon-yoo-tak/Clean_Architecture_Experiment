package com.board.cleancode.domain.port.out;

import com.board.cleancode.domain.model.Like;

public interface LikeRepositoryPort {

    Like save(Like like);

    void deleteByPostIdAndGuestId(Long postId, String guestId);

    boolean existsByPostIdAndGuestId(Long postId, String guestId);

    int countByPostId(Long postId);

    void deleteByPostId(Long postId);
}
