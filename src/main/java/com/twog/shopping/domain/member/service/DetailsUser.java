package com.twog.shopping.domain.member.service;

import com.twog.shopping.domain.member.entity.Member;
import com.twog.shopping.domain.member.entity.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/* 인증된 사용자의 정보를 security context에 전달하는 역할 */
public class DetailsUser implements UserDetails {


    private final Member member;

    public DetailsUser(Member member) {
        this.member = member;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Enum -> ROLE 변환
        UserRole role = member.getMemberRole();

        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    public Member getMember() {
        return member;
    }

    @Override
    public String getPassword() {
        return member.getMemberPwd();
    }

    @Override
    public String getUsername() {
        return member.getMemberEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 필요 시 수정
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 필요 시 수정
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 필요 시 수정
    }

    @Override
    public boolean isEnabled() {
        return member.getMemberStatus().equals("active");
    }
}
