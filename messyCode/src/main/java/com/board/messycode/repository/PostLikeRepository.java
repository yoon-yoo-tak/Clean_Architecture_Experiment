package com.board.messycode.repository;

import com.board.messycode.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByPostIdAndGuestId(Long postId, String guestId);

    void deleteByPostIdAndGuestId(Long postId, String guestId);

    long countByPostId(Long postId);

    void deleteByPostId(Long postId);
}
