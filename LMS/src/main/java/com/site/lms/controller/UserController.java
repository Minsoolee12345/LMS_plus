package com.site.lms.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.site.lms.entity.Message;
import com.site.lms.entity.User;
import com.site.lms.service.UserService;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    
    public UserController(UserService us) {
        this.userService = us;
    }

    /**
     * 1) GET /user
     *    - 전체 사용자 목록을 조회하여 adminUsers.html로 전달
     */
    @GetMapping
    public String listUsers(Model model) {
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        return "adminUsers";
    }

    /**
     * 2) POST /user/update
     *    - id, username, newPassword, confirmPassword, email, authority를 받아서 회원 정보 수정
     *    - 비밀번호 확인 불일치 시 /mypage로 리다이렉트
     */
    @PostMapping("/update")
    public String updateUser(
            @RequestParam(name = "id") Long id,
            @RequestParam(name = "username") String username,
            @RequestParam(name = "newPassword", required = false) String password,
            @RequestParam(name = "confirmPassword", required = false) String confirmPassword,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "authority") Integer authority,
            RedirectAttributes redirectAttributes) {
    	
        // 비밀번호 확인 체크
        if (password != null && !password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "비밀번호가 일치하지 않습니다. 다시 입력해주세요.");
            return "redirect:/mypage";
        }
    	
        userService.updateUser(id, username, password, email, authority);
        redirectAttributes.addFlashAttribute("errorMessage", "수정되었습니다");
        return "redirect:/mypage";
    }

    /**
     * 3) POST /user/delete
     *    - id를 받아서 사용자 삭제
     *    - 삭제 후 /logout으로 리다이렉트
     */
    @PostMapping("/delete")
    public String deleteUser(@RequestParam(name = "id") Long id) {
        userService.deleteUser(id);
        return "redirect:/logout";
    }
    
    
    
    /**
     * GET /user/messages
     *   → 현재 로그인된 사용자의 username을 Spring Security 세션에서 가져와서
     *      User 엔티티를 조회하고, 받은/보낸 메시지를 model에 담아서
     *      userMessages.html 뷰를 반환.
     */
    @GetMapping("/messages")
    public String listMessages(
            @AuthenticationPrincipal UserDetails userDetails, 
            Model model) {

        // 1) 인증 사용자 조회
        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        // 2) 전체 받은/보낸 메시지
        List<Message> recvMessages = userService.findMessagesByReceiver(currentUser.getId());
        List<Message> sentMessages = userService.findMessagesBySender(currentUser.getId());

        // 3) self / only-recv / only-sent 분리
        List<Message> selfMessages = sentMessages.stream()
            .filter(m -> m.getSender().getId().equals(currentUser.getId())
                      && m.getReceiver().getId().equals(currentUser.getId()))
            .collect(Collectors.toList());
        List<Message> recvOnly = recvMessages.stream()
            .filter(m -> !m.getSender().getId().equals(currentUser.getId()))
            .collect(Collectors.toList());
        List<Message> sentOnly = sentMessages.stream()
            .filter(m -> !m.getReceiver().getId().equals(currentUser.getId()))
            .collect(Collectors.toList());

        // 4) 전체 사용자 목록
        List<User> allUsers = userService.findAll();

        // 5) 모델에 속성 추가
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("selfMessages", selfMessages);
        model.addAttribute("recvMessages", recvOnly);
        model.addAttribute("sentMessages", sentOnly);
        model.addAttribute("allUsers", allUsers);

        return "userMessages";
    }

    /**
     * POST /user/sendMessage
     *   → form에서 “receiverId” 와 “content” 만 받아오고,
     *      sender는 세션(인증)에서 꺼낸 사용자로 결정.
     *   → 메시지 저장 후 → GET /user/messages 로 리다이렉트
     */
    @PostMapping("/sendMessage")
    public String sendMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("receiverId") Long receiverId,
            @RequestParam("content") String content) {

        // 1) 인증된 사용자(username) 로부터 sender User 엔티티 조회
        String username = userDetails.getUsername();
        User sender = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("발신자(" + username + ")가 없습니다."));

        // 2) receiverId 로부터 수신자 User 엔티티 조회
        User receiver = userService.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("수신자(ID=" + receiverId + ")가 없습니다."));

        // 3) Message 엔티티 생성 및 저장
        Message msg = new Message();
        msg.setSender(sender);
        msg.setReceiver(receiver);
        msg.setContent(content);
        msg.setCreatedDate(LocalDateTime.now());
        userService.saveMessage(msg);

        // 4) 메시지 센터(GET /user/messages) 로 리다이렉트
        return "redirect:/user/messages";
    }
    
    @PostMapping("/messages/delete")
    public String deleteMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("messageId") Long messageId) {

        // 1) 인증된 사용자 조회
        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        // 2) 메시지 삭제 (권한 체크: 발신자나 수신자만 삭제 가능하도록 서비스에서 처리)
        userService.deleteMessage(messageId, currentUser);

        // 3) 메시지 센터로 리다이렉트
        return "redirect:/user/messages";
    }
}
