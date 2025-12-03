package com.twog.shopping.domain.member.service;

import com.twog.shopping.domain.member.repository.MemberGradeRepository;
import com.twog.shopping.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberGradeRepository memberGradeRepository;





}
