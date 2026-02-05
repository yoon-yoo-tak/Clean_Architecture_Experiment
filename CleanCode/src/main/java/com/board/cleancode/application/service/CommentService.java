package com.board.cleancode.application.service;

import com.board.cleancode.domain.exception.CommentNotFoundException;
import com.board.cleancode.domain.exception.NestedReplyNotAllowedException;
import com.board.cleancode.domain.exception.PasswordMismatchException;
import com.board.cleancode.domain.exception.PostNotFoundException;
import com.board.cleancode.domain.model.Comment;
import com.board.cleancode.domain.port.in.CreateCommentUseCase;
import com.board.cleancode.domain.port.in.CreateReplyUseCase;
import com.board.cleancode.domain.port.in.DeleteCommentUseCase;
import com.board.cleancode.domain.port.in.GetCommentListUseCase;
import com.board.cleancode.domain.port.in.GetReplyListUseCase;
import com.board.cleancode.domain.port.out.CommentRepositoryPort;
import com.board.cleancode.domain.port.out.PasswordEncryptorPort;
import com.board.cleancode.domain.port.out.PostRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CommentService implements CreateCommentUseCase, GetCommentListUseCase, DeleteCommentUseCase,
        CreateReplyUseCase, GetReplyListUseCase {

    private final CommentRepositoryPort commentRepository;
    private final PostRepositoryPort postRepository;
    private final PasswordEncryptorPort passwordEncryptor;

    public CommentService(CommentRepositoryPort commentRepository,
                          PostRepositoryPort postRepository,
                          PasswordEncryptorPort passwordEncryptor) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.passwordEncryptor = passwordEncryptor;
    }

    @Override
    public Comment createComment(CreateCommentCommand command) {
        postRepository.findById(command.postId())
                .orElseThrow(() -> new PostNotFoundException(command.postId()));

        String encodedPassword = passwordEncryptor.encode(command.password());
        Comment comment = Comment.create(
                command.postId(),
                command.author(),
                encodedPassword,
                command.content()
        );
        return commentRepository.save(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentPageResult getComments(Long postId, int page, int size) {
        postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        List<Comment> comments = commentRepository.findRootCommentsByPostIdOrderByCreatedAtDesc(postId, page, size);
        long totalElements = commentRepository.countRootCommentsByPostId(postId);
        boolean hasMore = (long) (page + 1) * size < totalElements;

        return new CommentPageResult(comments, page, size, totalElements, hasMore);
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveComments(Long postId) {
        return commentRepository.countByPostIdAndDeletedFalse(postId);
    }

    @Override
    public void deleteComment(DeleteCommentCommand command) {
        postRepository.findById(command.postId())
                .orElseThrow(() -> new PostNotFoundException(command.postId()));

        Comment comment = commentRepository.findById(command.commentId())
                .orElseThrow(() -> new CommentNotFoundException(command.commentId()));

        if (!passwordEncryptor.matches(command.password(), comment.getPassword())) {
            throw new PasswordMismatchException();
        }

        comment.markDeleted();
        commentRepository.save(comment);
    }

    @Override
    public Comment createReply(CreateReplyCommand command) {
        postRepository.findById(command.postId())
                .orElseThrow(() -> new PostNotFoundException(command.postId()));

        Comment parentComment = commentRepository.findById(command.parentCommentId())
                .orElseThrow(() -> new CommentNotFoundException(command.parentCommentId()));

        if (parentComment.isReply()) {
            throw new NestedReplyNotAllowedException();
        }

        String encodedPassword = passwordEncryptor.encode(command.password());
        Comment reply = Comment.createReply(
                command.postId(),
                command.parentCommentId(),
                command.author(),
                encodedPassword,
                command.content()
        );
        return commentRepository.save(reply);
    }

    @Override
    @Transactional(readOnly = true)
    public ReplyPageResult getReplies(Long postId, Long parentCommentId, int page, int size) {
        postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new CommentNotFoundException(parentCommentId));

        List<Comment> replies = commentRepository.findRepliesByParentIdOrderByCreatedAtAsc(parentCommentId, page, size);
        long totalElements = commentRepository.countRepliesByParentId(parentCommentId);
        boolean hasMore = (long) (page + 1) * size < totalElements;

        return new ReplyPageResult(replies, page, size, totalElements, hasMore);
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveReplies(Long parentCommentId) {
        return commentRepository.countRepliesByParentIdAndDeletedFalse(parentCommentId);
    }
}
