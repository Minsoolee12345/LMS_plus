// src/main/java/com/site/lms/controller/RegistrationController.java
package com.site.lms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.site.lms.service.UserService;

@Controller
public class RegistrationController {
    private final UserService userService;

    public RegistrationController(UserService us) {
        this.userService = us;
    }

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(
        @RequestParam("username") String username,
        @RequestParam("password") String password,
        @RequestParam("passwordCheck") String passwordCheck,
        @RequestParam("email") String email,
        @RequestParam("code") String code,
        RedirectAttributes redirectAttributes
    ) {
        // UserService#register에서 null 반환 시 에러 메시지로 리다이렉트
        if (userService.register(username, password, passwordCheck, email, code) == null) {
            redirectAttributes.addFlashAttribute("message", "비밀번호 또는 코드가 일치하지 않습니다. 다시 입력해주세요.");
            return "redirect:/register";
        }
        redirectAttributes.addFlashAttribute("message", "회원가입이 완료되었습니다. 로그인 해주세요.");
        return "redirect:/login";
    }

    @PostMapping("/toAdmin")
    public String registerAdmin(
        @RequestParam("username") String username,
        @RequestParam("password") String password,
        @RequestParam("email") String email,
        @RequestParam("adminname") String adminname
    ) {
        if (userService.findAuthorityByUsername(adminname).equals(0)) {
            userService.messageSave(
                adminname,
                adminname,
                "<회원가입 요청> id:" + username + " pw:" + password + " em:" + email
            );
        }
        return "redirect:/admin/users";
    }
}