package com.site.lms.controller;

import com.site.lms.entity.User;
import com.site.lms.service.UserService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MypageController {

    private final UserService userService;

    public MypageController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/mypage")
    public String mypage(@AuthenticationPrincipal org.springframework.security.core.userdetails.User loginUser, Model model) {

        // 1. 로그인한 사용자의 아이디(username)를 기반으로
        String username = loginUser.getUsername();

        // 2. DB에서 사용자 정보 조회
        User dbUser = userService.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));

        // 3. 모델에 담기 (Thymeleaf에서 쓰려면 반드시 필요)
        model.addAttribute("u", dbUser);

        return "mypage";
    }

}
