package com.site.lms.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "FAVORITES")
@SequenceGenerator(name = "fav_seq", sequenceName = "SEQ_FAVORITE_NO", allocationSize = 1)
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fav_seq")
    @Column(name = "FAVORITE_NO")
    private Long id;

    @Column(name = "MEMBER_NO")
    private Long memberNo;

    @Column(name = "TARGET_TYPE")
    private String targetType;

    @Column(name = "TARGET_ID")
    private Long targetId;

    @Column(name = "CREATED_DATE")
    private Date createdDate;
}
