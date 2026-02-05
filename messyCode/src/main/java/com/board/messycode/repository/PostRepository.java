package com.board.messycode.repository;

import com.board.messycode.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Post> findByTitleContainingOrderByCreatedAtDesc(String title, Pageable pageable);

    Page<Post> findByAuthorContainingOrderByCreatedAtDesc(String author, Pageable pageable);

    Page<Post> findByContentContainingOrderByCreatedAtDesc(String content, Pageable pageable);

    @Query(value = "SELECT DISTINCT p FROM Post p JOIN p.hashtags h WHERE h = :hashtag ORDER BY p.createdAt DESC",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Post p JOIN p.hashtags h WHERE h = :hashtag")
    Page<Post> findByHashtag(@Param("hashtag") String hashtag, Pageable pageable);

    // 조회수순 정렬
    Page<Post> findAllByOrderByViewCountDescCreatedAtDesc(Pageable pageable);

    Page<Post> findByTitleContainingOrderByViewCountDescCreatedAtDesc(String title, Pageable pageable);

    Page<Post> findByAuthorContainingOrderByViewCountDescCreatedAtDesc(String author, Pageable pageable);

    Page<Post> findByContentContainingOrderByViewCountDescCreatedAtDesc(String content, Pageable pageable);

    @Query(value = "SELECT DISTINCT p FROM Post p JOIN p.hashtags h WHERE h = :hashtag ORDER BY p.viewCount DESC, p.createdAt DESC",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Post p JOIN p.hashtags h WHERE h = :hashtag")
    Page<Post> findByHashtagOrderByViewCountDesc(@Param("hashtag") String hashtag, Pageable pageable);

    // 좋아요순 정렬
    Page<Post> findAllByOrderByLikeCountDescCreatedAtDesc(Pageable pageable);

    Page<Post> findByTitleContainingOrderByLikeCountDescCreatedAtDesc(String title, Pageable pageable);

    Page<Post> findByAuthorContainingOrderByLikeCountDescCreatedAtDesc(String author, Pageable pageable);

    Page<Post> findByContentContainingOrderByLikeCountDescCreatedAtDesc(String content, Pageable pageable);

    @Query(value = "SELECT DISTINCT p FROM Post p JOIN p.hashtags h WHERE h = :hashtag ORDER BY p.likeCount DESC, p.createdAt DESC",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Post p JOIN p.hashtags h WHERE h = :hashtag")
    Page<Post> findByHashtagOrderByLikeCountDesc(@Param("hashtag") String hashtag, Pageable pageable);
}
