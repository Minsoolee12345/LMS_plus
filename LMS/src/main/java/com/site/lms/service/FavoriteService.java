package com.site.lms.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.site.lms.entity.Favorite;
import com.site.lms.mapper.FavoriteMapper;

@Service
public class FavoriteService {

    private final FavoriteMapper favMapper;
    private final UserService userService;

    public FavoriteService(FavoriteMapper favMapper, UserService userService) {
        this.favMapper     = favMapper;
        this.userService   = userService;
    }

    // 강의 즐겨찾기 토글
    public boolean toggleLectureFavorite(String username, Long lectureNo) {
        var user     = userService.findByUsername(username).orElseThrow();
        Long memberNo = user.getId();
        List<Long> current = favMapper.findLectureNosByMember(memberNo);
        if (current.contains(lectureNo)) {
            favMapper.delete(memberNo, "LECTURE", lectureNo);
            return false;
        } else {
            Favorite fav = new Favorite();
            fav.setMemberNo(memberNo);
            fav.setTargetType("LECTURE");
            fav.setTargetId(lectureNo);
            favMapper.save(fav);
            return true;
        }
    }

    // 챕터 즐겨찾기 토글
    public boolean toggleChapterFavorite(String username, Long videoNo) {
        var user     = userService.findByUsername(username).orElseThrow();
        Long memberNo = user.getId();
        List<Long> current = favMapper.findChapterNosByMember(memberNo);
        if (current.contains(videoNo)) {
            favMapper.delete(memberNo, "CHAPTER", videoNo);
            return false;
        } else {
            Favorite fav = new Favorite();
            fav.setMemberNo(memberNo);
            fav.setTargetType("CHAPTER");
            fav.setTargetId(videoNo);
            favMapper.save(fav);
            return true;
        }
    }

    // 강의 즐겨찾기 목록 조회
    public List<Long> findFavoriteLectureNos(String username) {
        var user = userService.findByUsername(username).orElseThrow();
        return favMapper.findLectureNosByMember(user.getId());
    }

    // 챕터 즐겨찾기 목록 조회
    public List<Long> findFavoriteChapterNos(String username) {
        var user = userService.findByUsername(username).orElseThrow();
        return favMapper.findChapterNosByMember(user.getId());
    }
}