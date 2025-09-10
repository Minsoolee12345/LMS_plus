package com.site.lms.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "MEMBER")
@SequenceGenerator(name = "member_seq", sequenceName = "SEQ_MEMBER_NO", allocationSize = 1)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "member_seq")
    @Column(name = "MEMBER_NO")
    private Long id;

    @Column(name = "MEMBER_ID", unique = true, nullable = false)
    private String username;

    @Column(name = "MEMBER_PW", nullable = false)
    private String password;

    @Column(name = "MEMBER_EMAIL")
    private String email;

    @Column(name = "AUTHORITY")
    private Integer authority;   // 0=관리자,1=수강생,2=강사
}