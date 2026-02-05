package com.board.cleancode.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentJpaRepository extends JpaRepository<CommentJpaEntity, Long> {

    Page<CommentJpaEntity> findByPostIdOrderByCreatedAtDesc(Long postId, Pageable pageable);

    Page<CommentJpaEntity> findByPostIdAndParentIdIsNullOrderByCreatedAtDesc(Long postId, Pageable pageable);

    Page<CommentJpaEntity> findByParentIdOrderByCreatedAtAsc(Long parentId, Pageable pageable);

    long countByPostId(Long postId);

    long countByPostIdAndDeletedFalse(Long postId);

    long countByPostIdAndParentIdIsNull(Long postId);

    long countByParentId(Long parentId);

    long countByParentIdAndDeletedFalse(Long parentId);

    void deleteByPostId(Long postId);

    long countByDeletedFalse();
}
