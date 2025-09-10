package com.site.lms.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "MESSAGE")
@SequenceGenerator(name = "messageSeq", sequenceName = "MESSAGE_SEQ", allocationSize = 1)
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "messageSeq")
    @Column(name = "MESSAGE_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "R_MEMBER_NO", nullable = false)
    private User receiver;  // 수신자 (Foreign Key)

    @ManyToOne
    @JoinColumn(name = "S_MEMBER_NO", nullable = false)
    private User sender;    // 발신자 (Foreign Key)

    @Lob
    @Column(name = "CONTENT")
    private String content;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;
}