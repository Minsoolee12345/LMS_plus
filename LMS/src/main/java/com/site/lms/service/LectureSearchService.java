package com.site.lms.service;

import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.site.lms.entity.Lecture;
import com.site.lms.mapper.LectureMapper;
import com.site.lms.mapper.LectureVideoMapper;

@Service
public class LectureSearchService {

    private final LectureMapper lectureMapper;
    private final LectureVideoMapper videoMapper;

    @Autowired
    public LectureSearchService(LectureMapper lm, LectureVideoMapper vm) {
        this.lectureMapper = lm;
        this.videoMapper   = vm;
    }

    public List<Lecture> searchByTopKeyword(String keyword) {
        List<Long> nos = videoMapper.findLectureNosByTopKeyword(keyword);
        LinkedHashMap<Long,Lecture> map = new LinkedHashMap<>();
        for (Long no : nos) {
            Lecture l = lectureMapper.findById(no);
            if (l != null) map.put(no, l);
        }
        return new ArrayList<>(map.values());
    }
}
