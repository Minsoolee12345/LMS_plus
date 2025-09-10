package com.site.lms.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.site.lms.entity.Lecture;
import com.site.lms.mapper.LectureMapper;

@Service
public class LectureService {

    private final LectureMapper lectureMapper;

    public LectureService(LectureMapper lectureMapper) {
        this.lectureMapper = lectureMapper;
    }

    public boolean existsLecture(Long no) {
        return lectureMapper.existsById(no) > 0;
    }

    public Lecture findById(Long no) {
        return lectureMapper.findById(no);
    }

    public List<Lecture> findByInstructor(Long instructorNo) {
        return lectureMapper.findByInstructor(instructorNo);
    }

    public void deleteLecture(Long lectureNo) {
        lectureMapper.deleteById(lectureNo);
    }

    public void saveLecture(Lecture lec) {
        lectureMapper.update(lec);
    }

    // 전체 공개 강의 조회
    public List<Lecture> findPublicLectures(Long long1) {
        return lectureMapper.findByVisibility(1);
    }

    // ID 목록으로 강의 조회
    public List<Lecture> findByIds(List<Long> ids) {
        if (ids.isEmpty()) return List.of();
        return lectureMapper.findByIds(ids);
    }

	public List<Lecture> findPublicLectures(Object long1) {
		// TODO Auto-generated method stub
		return null;
	}
	
    /**
     * 관리자가 새 과목을 생성
     * @param instructorNo 과목 기본 담당자(관리자 or 더미값)
     * @param title 과목명
     * @param desc 설명
     * @param visibility 공개 여부 (0,1,2)
     * @return 생성된 Lecture 객체
     */
    public Lecture createLecture(Long instructorNo, String title, String desc, Integer visibility) {
        Lecture lec = new Lecture();
        lec.setInstructorNo(instructorNo);
        lec.setTitle(title);
        lec.setLectureDesc(desc);
        lec.setVisibility(visibility);
        lectureMapper.insertLecture(lec);
        return lec;
    }
    
    public List<Lecture> findByVisibility(Integer visibility) {
        return lectureMapper.findByVisibility(visibility);
    }
}