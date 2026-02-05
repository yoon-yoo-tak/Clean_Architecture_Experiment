package com.board.cleancode.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeJpaRepository extends JpaRepository<LikeJpaEntity, Long> {

    boolean existsByPostIdAndGuestId(Long postId, String guestId);

    int countByPostId(Long postId);

    void deleteByPostIdAndGuestId(Long postId, String guestId);

    void deleteByPostId(Long postId);
}
