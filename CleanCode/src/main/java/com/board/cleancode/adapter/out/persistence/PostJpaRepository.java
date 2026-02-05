package com.board.cleancode.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostJpaRepository extends JpaRepository<PostJpaEntity, Long> {

    // 기본 조회 (정렬은 Pageable로 전달)
    Page<PostJpaEntity> findAll(Pageable pageable);

    Page<PostJpaEntity> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<PostJpaEntity> findByAuthorContainingIgnoreCase(String author, Pageable pageable);

    Page<PostJpaEntity> findByContentContainingIgnoreCase(String content, Pageable pageable);

    @Query("SELECT DISTINCT p FROM PostJpaEntity p JOIN p.hashtags h WHERE h = :hashtag")
    Page<PostJpaEntity> findByHashtag(@Param("hashtag") String hashtag, Pageable pageable);

    // 좋아요 수로 정렬하는 쿼리 (LEFT JOIN + COUNT)
    @Query("SELECT p FROM PostJpaEntity p LEFT JOIN LikeJpaEntity l ON p.id = l.postId " +
           "GROUP BY p ORDER BY COUNT(l) DESC, p.createdAt DESC")
    Page<PostJpaEntity> findAllOrderByLikeCountDesc(Pageable pageable);

    @Query("SELECT p FROM PostJpaEntity p LEFT JOIN LikeJpaEntity l ON p.id = l.postId " +
           "WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "GROUP BY p ORDER BY COUNT(l) DESC, p.createdAt DESC")
    Page<PostJpaEntity> findByTitleContainingIgnoreCaseOrderByLikeCountDesc(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM PostJpaEntity p LEFT JOIN LikeJpaEntity l ON p.id = l.postId " +
           "WHERE LOWER(p.author) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "GROUP BY p ORDER BY COUNT(l) DESC, p.createdAt DESC")
    Page<PostJpaEntity> findByAuthorContainingIgnoreCaseOrderByLikeCountDesc(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM PostJpaEntity p LEFT JOIN LikeJpaEntity l ON p.id = l.postId " +
           "WHERE LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "GROUP BY p ORDER BY COUNT(l) DESC, p.createdAt DESC")
    Page<PostJpaEntity> findByContentContainingIgnoreCaseOrderByLikeCountDesc(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM PostJpaEntity p LEFT JOIN LikeJpaEntity l ON p.id = l.postId " +
           "JOIN p.hashtags h WHERE h = :hashtag " +
           "GROUP BY p ORDER BY COUNT(l) DESC, p.createdAt DESC")
    Page<PostJpaEntity> findByHashtagOrderByLikeCountDesc(@Param("hashtag") String hashtag, Pageable pageable);
}
