package com.gsitm.intern.repository;

import com.gsitm.intern.entity.AuthToken;
import com.gsitm.intern.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {
    AuthToken findFirstByMemberOrderByRegTimeDesc(Member member);
    AuthToken findFirstByCodeOrderByRegTimeDesc(String code);
}
