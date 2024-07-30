package com.onion.backend.mapper;

import com.onion.backend.dto.board.Board;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BoardMapper {
    Board findById(@Param("boardId") Long boardId);
}
