package com.site.lms.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.site.lms.controller.AuthController;
import com.site.lms.entity.MemberRole;
import com.site.lms.entity.Message;
import com.site.lms.entity.User;
import com.site.lms.repository.MemberRoleRepository;
import com.site.lms.repository.MessageRepository;
import com.site.lms.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class UserService {

    private final AuthController authController;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberRoleRepository memberRoleRepository;
    private final MessageRepository messageRepository;

    public UserService(UserRepository ur,
                       PasswordEncoder pe,
                       MemberRoleRepository mrr,
                       MessageRepository mrp,
                       @Lazy AuthController authController) {
        this.userRepository = ur;
        this.passwordEncoder = pe;
        this.memberRoleRepository = mrr;
        this.authController = authController;
        this.messageRepository = mrp;
    }

    // ────────────────────────────────────────────────────────
    // 사용자 조회
    // ────────────────────────────────────────────────────────

    /** username으로 User 조회 */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /** ID로 User 조회 */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /** 전체 사용자 리스트 조회 (Admin 전용) */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // ────────────────────────────────────────────────────────
    // 회원가입 및 인증 관련
    // ────────────────────────────────────────────────────────

    /**
     * 회원가입
     * - username, rawPassword, rawPasswordCheck, email, code(=roleCode)
     * - code가 MEMBER_ROLE 테이블에 존재하고, password 확인이 맞아야 저장
     */
    public User register(String username,
                         String rawPassword,
                         String rawPasswordCheck,
                         String email,
                         String code) {
        if (!memberRoleRepository.existsByRoleCode(code)
                || !rawPassword.equals(rawPasswordCheck)) {
            return null;
        }
        // 사용된 코드 삭제
        deleteRole(code);

        // 권한 코드는 code의 첫 글자를 Integer로 변환 (예: "1_ABCDEF" → authority=1)
        Integer authority = Integer.parseInt(code.substring(0, 1));
        return createUser(username, rawPassword, email, authority);
    }

    // ────────────────────────────────────────────────────────
    // Admin: 사용자 관리(CRUD)
    // ────────────────────────────────────────────────────────

    /** 새 사용자 생성 (Admin 전용) */
    public User createUser(String username,
                           String rawPassword,
                           String email,
                           Integer authority) {
        User u = new User();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setEmail(email);
        u.setAuthority(authority);

        try {
            return userRepository.save(u);
        } catch (DataIntegrityViolationException e) {
            // 예: 중복된 username 등
            return null;
        }
    }

    /** 사용자 수정 (Admin 전용) */
    public User updateUser(Long id,
                           String username,
                           String rawPassword,
                           String email,
                           Integer authority) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자: " + id));
        u.setUsername(username);
        if (rawPassword != null && !rawPassword.isBlank()) {
            u.setPassword(passwordEncoder.encode(rawPassword));
        }
        u.setEmail(email);
        u.setAuthority(authority);
        return userRepository.save(u);
    }

    /** 사용자 삭제 (Admin 전용) */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // ────────────────────────────────────────────────────────
    // 역할 코드(MemberRole) 관리
    // ────────────────────────────────────────────────────────

    /** 모든 MemberRole 조회 (Admin 전용) */
    public List<MemberRole> findAllRole() {
        return memberRoleRepository.findAll();
    }

    /** 새로운 역할 코드 생성 (Admin 전용) */
    public void createRole(String code) {
        MemberRole mr = new MemberRole();
        mr.setRoleCode(code);
        memberRoleRepository.save(mr);
    }

    /** 역할 코드 삭제 (Admin 전용) */
    public void deleteRole(String code) {
        memberRoleRepository.deleteById(code);
    }

    /** username으로 해당 사용자의 authority(Integer) 조회 */
    public Integer findAuthorityByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                    new IllegalArgumentException("해당 사용자를 찾을 수 없습니다: " + username)
                );
        return user.getAuthority();
    }

    // ────────────────────────────────────────────────────────
    // 메시지 기능
    // ────────────────────────────────────────────────────────

    /**
     * 메시지 저장 (senderUsername, receiverUsername, content)
     */
    public void messageSave(String receiverUsername,
                            String senderUsername,
                            String content) {
        User receiver = userRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> new IllegalArgumentException("수신자(" + receiverUsername + ")를 찾을 수 없습니다."));
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new IllegalArgumentException("발신자(" + senderUsername + ")를 찾을 수 없습니다."));

        Message msg = new Message();
        msg.setReceiver(receiver);
        msg.setSender(sender);
        msg.setContent(content);
        msg.setCreatedDate(LocalDateTime.now());

        messageRepository.save(msg);
    }

    /**
     * 특정 사용자가 받은 메시지 목록 조회
     * - Optional 내부가 비어 있으면 빈 리스트를 반환
     */
    public List<Message> findMessagesByReceiver(Long receiverId) {
        return messageRepository.findByReceiver_Id(receiverId)
                .orElse(List.of());
    }

    /**
     * 특정 사용자가 보낸 메시지 목록 조회
     * - Optional 내부가 비어 있으면 빈 리스트를 반환
     */
    public List<Message> findMessagesBySender(Long senderId) {
        return messageRepository.findBySender_Id(senderId)
                .orElse(List.of());
    }

    /**
     * Message 엔티티를 직접 저장할 때 사용
     */
    public void saveMessage(Message msg) {
        messageRepository.save(msg);
    }
    
    public User findByUsernameById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    public void deleteMessage(Long messageId, User currentUser) {
        Message msg = messageRepository.findById(messageId)
            .orElseThrow(() -> new EntityNotFoundException("메시지를 찾을 수 없습니다."));
        
        messageRepository.delete(msg);
    }
}











