package com.board.cleancode.domain.port.out;

import com.board.cleancode.domain.model.Post;

import java.util.List;
import java.util.Optional;

public interface PostRepositoryPort {

    Post save(Post post);

    Optional<Post> findById(Long id);

    void deleteById(Long id);

    long count();

    record PostPage(List<Post> content, long totalElements) {
    }

    PostPage findAll(int page, int size, String sort);

    PostPage searchByTitle(String keyword, int page, int size, String sort);

    PostPage searchByAuthor(String keyword, int page, int size, String sort);

    PostPage searchByContent(String keyword, int page, int size, String sort);

    PostPage searchByHashtag(String keyword, int page, int size, String sort);
}
