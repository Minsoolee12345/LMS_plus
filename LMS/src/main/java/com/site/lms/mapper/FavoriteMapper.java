package com.site.lms.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.site.lms.entity.Favorite;

@Mapper
public interface FavoriteMapper {
    void save(Favorite fav);

    void delete(@Param("memberNo") Long memberNo,
                @Param("targetType") String targetType,
                @Param("targetId") Long targetId);

    // 기존: 강의 즐겨찾기 ID 목록
    List<Long> findLectureNosByMember(@Param("memberNo") Long memberNo);

    // 추가: 챕터 즐겨찾기 ID 목록
    List<Long> findChapterNosByMember(@Param("memberNo") Long memberNo);
}
