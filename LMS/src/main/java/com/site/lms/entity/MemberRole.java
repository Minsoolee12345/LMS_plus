package com.site.lms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "MEMBER_ROLE")
public class MemberRole {

    @Id
    @Column(name = "ROLE_CODE")
    private String roleCode;

    // getters & setters
    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
}