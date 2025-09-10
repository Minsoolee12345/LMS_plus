package com.site.lms.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.site.lms.service.UserService;
import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    public AuthController(UserService us) { this.userService = us; }

    @GetMapping("/login-info")
    public ResponseEntity<String> loginInfo(Principal principal) {
        return ResponseEntity.ok("로그인 사용자: " + principal.getName());
    }
}