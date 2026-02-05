package com.board.cleancode.domain.port.in;

import com.board.cleancode.domain.model.Post;

public interface GetPostUseCase {

    Post getPost(Long id);
}
