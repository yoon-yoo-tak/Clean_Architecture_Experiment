package com.board.cleancode.adapter.in.web.dto;

import com.board.cleancode.domain.port.in.LikePostUseCase.LikeResult;

public record LikeResponse(int likeCount, boolean liked) {

    public static LikeResponse from(LikeResult result) {
        return new LikeResponse(result.likeCount(), result.liked());
    }
}
