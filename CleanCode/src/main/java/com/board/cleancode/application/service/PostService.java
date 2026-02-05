package com.board.cleancode.application.service;

import com.board.cleancode.domain.exception.PasswordMismatchException;
import com.board.cleancode.domain.exception.PostNotFoundException;
import com.board.cleancode.domain.model.Post;
import com.board.cleancode.domain.port.in.ChangePostPasswordUseCase;
import com.board.cleancode.domain.port.in.CreatePostUseCase;
import com.board.cleancode.domain.port.in.DeletePostUseCase;
import com.board.cleancode.domain.port.in.GetPostListUseCase;
import com.board.cleancode.domain.port.in.GetPostUseCase;
import com.board.cleancode.domain.port.in.UpdatePostUseCase;
import com.board.cleancode.domain.port.out.CommentRepositoryPort;
import com.board.cleancode.domain.port.out.LikeRepositoryPort;
import com.board.cleancode.domain.port.out.PasswordEncryptorPort;
import com.board.cleancode.domain.port.out.PostRepositoryPort;
import com.board.cleancode.domain.port.out.PostRepositoryPort.PostPage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PostService implements CreatePostUseCase, GetPostUseCase, UpdatePostUseCase, DeletePostUseCase, GetPostListUseCase, ChangePostPasswordUseCase {

    private final PostRepositoryPort postRepository;
    private final CommentRepositoryPort commentRepository;
    private final LikeRepositoryPort likeRepository;
    private final PasswordEncryptorPort passwordEncryptor;

    public PostService(PostRepositoryPort postRepository,
                       CommentRepositoryPort commentRepository,
                       LikeRepositoryPort likeRepository,
                       PasswordEncryptorPort passwordEncryptor) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.passwordEncryptor = passwordEncryptor;
    }

    @Override
    public Post createPost(CreatePostCommand command) {
        String encodedPassword = passwordEncryptor.encode(command.password());
        Post post = Post.create(
                command.title(),
                command.content(),
                command.author(),
                encodedPassword,
                command.hashtags()
        );
        return postRepository.save(post);
    }

    @Override
    @Transactional(readOnly = false)
    public Post getPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
        post.incrementViewCount();
        return postRepository.save(post);
    }

    @Override
    public Post updatePost(UpdatePostCommand command) {
        Post post = postRepository.findById(command.id())
                .orElseThrow(() -> new PostNotFoundException(command.id()));

        if (!passwordEncryptor.matches(command.password(), post.getPassword())) {
            throw new PasswordMismatchException();
        }

        post.update(command.title(), command.content(), command.hashtags());
        return postRepository.save(post);
    }

    @Override
    public void deletePost(DeletePostCommand command) {
        Post post = postRepository.findById(command.id())
                .orElseThrow(() -> new PostNotFoundException(command.id()));

        if (!passwordEncryptor.matches(command.password(), post.getPassword())) {
            throw new PasswordMismatchException();
        }

        commentRepository.deleteByPostId(command.id());
        likeRepository.deleteByPostId(command.id());
        postRepository.deleteById(command.id());
    }

    @Override
    @Transactional(readOnly = true)
    public PostListResult getPostList(PostListQuery query) {
        PostPage postPage;
        String sort = query.sort() != null ? query.sort() : "latest";

        if (query.searchType() != null && !query.searchType().isEmpty()
                && query.keyword() != null && !query.keyword().isEmpty()) {
            postPage = switch (query.searchType()) {
                case "title" -> postRepository.searchByTitle(query.keyword(), query.page(), query.size(), sort);
                case "author" -> postRepository.searchByAuthor(query.keyword(), query.page(), query.size(), sort);
                case "content" -> postRepository.searchByContent(query.keyword(), query.page(), query.size(), sort);
                case "hashtag" -> postRepository.searchByHashtag(query.keyword(), query.page(), query.size(), sort);
                default -> postRepository.findAll(query.page(), query.size(), sort);
            };
        } else {
            postPage = postRepository.findAll(query.page(), query.size(), sort);
        }

        List<PostSummary> summaries = postPage.content().stream()
                .map(post -> new PostSummary(
                        post,
                        (int) commentRepository.countByPostIdAndDeletedFalse(post.getId()),
                        likeRepository.countByPostId(post.getId())))
                .toList();

        long totalPostCount = postRepository.count();
        long totalCommentCount = commentRepository.countAllByDeletedFalse();
        int totalPages = query.size() > 0
                ? (int) Math.ceil((double) postPage.totalElements() / query.size())
                : 0;

        return new PostListResult(
                summaries,
                query.page(),
                query.size(),
                postPage.totalElements(),
                totalPages,
                totalPostCount,
                totalCommentCount
        );
    }

    @Override
    public void changePassword(ChangePostPasswordCommand command) {
        Post post = postRepository.findById(command.postId())
                .orElseThrow(() -> new PostNotFoundException(command.postId()));

        if (!passwordEncryptor.matches(command.currentPassword(), post.getPassword())) {
            throw new PasswordMismatchException();
        }

        String newEncodedPassword = passwordEncryptor.encode(command.newPassword());
        post.changePassword(newEncodedPassword);
        postRepository.save(post);
    }
}
