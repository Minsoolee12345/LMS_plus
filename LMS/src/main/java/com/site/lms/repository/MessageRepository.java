package com.site.lms.repository;

import com.site.lms.entity.Message;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;


// JpaRepository<Message, Long> -> JPA에서 사용할 엔티티 타입과 그 기본키 타입
public interface MessageRepository extends JpaRepository<Message, Long> {
    Optional<List<Message>> findByReceiver_Id(Long receiverId);
    Optional<List<Message>> findBySender_Id(Long senderId);
}