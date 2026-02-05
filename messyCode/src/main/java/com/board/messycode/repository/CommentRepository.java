package com.board.messycode.repository;

import com.board.messycode.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByPostIdOrderByCreatedAtDesc(Long postId, Pageable pageable);

    Page<Comment> findByPostIdAndParentIdIsNullOrderByCreatedAtDesc(Long postId, Pageable pageable);

    Page<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId, Pageable pageable);

    long countByPostIdAndDeletedFalse(Long postId);

    long countByParentIdAndDeletedFalse(Long parentId);

    long countByDeletedFalse();

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.postId = :postId")
    void deleteByPostId(@Param("postId") Long postId);
}
