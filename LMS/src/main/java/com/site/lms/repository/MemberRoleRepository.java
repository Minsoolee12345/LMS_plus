package com.site.lms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.site.lms.entity.MemberRole;

public interface MemberRoleRepository extends JpaRepository<MemberRole, String> {
    boolean existsByRoleCode(String roleCode); // roleCode 존재 여부 확인
}
