// src/main/java/com/site/lms/controller/InstructorSwitchController.java
package com.site.lms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InstructorSwitchController {

    /**
     * 강사용 네비바에서 "일반 메뉴 전환" 클릭 시
     * 일반 사용자용 홈으로 리다이렉트합니다.
     */
    @GetMapping("/switch/normal")
    public String switchToNormal() {
        return "redirect:/";
    }
}
