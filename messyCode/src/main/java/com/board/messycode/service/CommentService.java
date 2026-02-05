package com.board.messycode.service;

import com.board.messycode.entity.Comment;
import com.board.messycode.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Transactional
    public Comment createComment(Long postId, Comment comment) {
        // 유효성 검증
        if (comment.getAuthor() == null || comment.getAuthor().trim().isEmpty() || comment.getAuthor().length() > 50) {
            throw new RuntimeException("작성자는 1~50자여야 합니다.");
        }
        if (comment.getPassword() == null || comment.getPassword().length() < 4) {
            throw new RuntimeException("비밀번호는 4자 이상이어야 합니다.");
        }
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            throw new RuntimeException("내용은 필수입니다.");
        }

        // BCrypt 암호화
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        comment.setPassword(encoder.encode(comment.getPassword()));

        comment.setPostId(postId);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setDeleted(false);

        return commentRepository.save(comment);
    }

    public Map<String, Object> getComments(Long postId, int page) {
        Page<Comment> commentPage = commentRepository.findByPostIdAndParentIdIsNullOrderByCreatedAtDesc(
                postId, PageRequest.of(page, 5));

        List<Map<String, Object>> content = new ArrayList<>();
        for (Comment c : commentPage.getContent()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", c.getId());
            map.put("parentId", c.getParentId());
            map.put("author", c.getAuthor());
            if (c.isDeleted()) {
                map.put("content", "삭제된 댓글입니다.");
            } else {
                map.put("content", c.getContent());
            }
            map.put("createdAt", c.getCreatedAt());
            map.put("deleted", c.isDeleted());
            map.put("replyCount", commentRepository.countByParentIdAndDeletedFalse(c.getId()));
            content.add(map);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", content);
        result.put("page", page);
        result.put("size", 5);
        result.put("totalElements", commentPage.getTotalElements());
        result.put("hasMore", commentPage.hasNext());

        return result;
    }

    public Comment getCommentById(Long id) {
        return commentRepository.findById(id).orElse(null);
    }

    @Transactional
    public void softDeleteComment(Long id) {
        Comment comment = commentRepository.findById(id).orElse(null);
        if (comment != null) {
            comment.setDeleted(true);
            commentRepository.save(comment);
        }
    }

    public long countNonDeletedComments(Long postId) {
        return commentRepository.countByPostIdAndDeletedFalse(postId);
    }

    @Transactional
    public void deleteCommentsByPostId(Long postId) {
        commentRepository.deleteByPostId(postId);
    }

    @Transactional
    public Comment createReply(Long postId, Long parentId, Comment reply) {
        // 유효성 검증
        if (reply.getAuthor() == null || reply.getAuthor().trim().isEmpty() || reply.getAuthor().length() > 50) {
            throw new RuntimeException("작성자는 1~50자여야 합니다.");
        }
        if (reply.getPassword() == null || reply.getPassword().length() < 4) {
            throw new RuntimeException("비밀번호는 4자 이상이어야 합니다.");
        }
        if (reply.getContent() == null || reply.getContent().trim().isEmpty()) {
            throw new RuntimeException("내용은 필수입니다.");
        }

        // BCrypt 암호화
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        reply.setPassword(encoder.encode(reply.getPassword()));

        reply.setPostId(postId);
        reply.setParentId(parentId);
        reply.setCreatedAt(LocalDateTime.now());
        reply.setDeleted(false);

        return commentRepository.save(reply);
    }

    public Map<String, Object> getReplies(Long parentId, int page, int size) {
        Page<Comment> replyPage = commentRepository.findByParentIdOrderByCreatedAtAsc(
                parentId, PageRequest.of(page, size));

        List<Map<String, Object>> content = new ArrayList<>();
        for (Comment c : replyPage.getContent()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", c.getId());
            map.put("parentId", c.getParentId());
            map.put("author", c.getAuthor());
            if (c.isDeleted()) {
                map.put("content", "삭제된 댓글입니다.");
            } else {
                map.put("content", c.getContent());
            }
            map.put("createdAt", c.getCreatedAt());
            map.put("deleted", c.isDeleted());
            content.add(map);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", content);
        result.put("page", page);
        result.put("size", size);
        result.put("totalElements", replyPage.getTotalElements());
        result.put("hasMore", replyPage.hasNext());

        return result;
    }
}
