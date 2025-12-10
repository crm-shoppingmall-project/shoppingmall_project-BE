package com.twog.shopping.domain.support.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import com.twog.shopping.domain.support.repository.CsTicketReplyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import com.twog.shopping.domain.member.entity.Member;
import com.twog.shopping.domain.member.repository.MemberRepository;
import com.twog.shopping.domain.support.dto.CsTicketReplyRequest;
import com.twog.shopping.domain.support.dto.CsTicketReplyResponse;
import com.twog.shopping.domain.support.dto.CsTicketRequest;
import com.twog.shopping.domain.support.dto.CsTicketResponse;
import com.twog.shopping.domain.support.entity.CsTicket;
import com.twog.shopping.domain.support.entity.CsTicketReply;
import com.twog.shopping.domain.support.entity.TicketChannel;
import com.twog.shopping.domain.support.repository.CsTicketRepository;

@ExtendWith(MockitoExtension.class)
class CsTicketServiceTest {

    @InjectMocks
    private CsTicketService csTicketService;

    @Mock
    private CsTicketRepository csTicketRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private CsTicketReplyRepository csTicketReplyRepository;

    @Test
    @DisplayName("문의 등록 성공")
    void createTicket_Success() {
        // given
        Long memberId = 1L;
        CsTicketRequest req = new CsTicketRequest(
            TicketChannel.WEB,
            "배송",
            "배송 언제 오나요?",
            "빨리 보내주세요."
        );

        Member member = org.mockito.Mockito.mock(Member.class);
        // Member 엔티티에 기본 생성자가 있다고 가정하거나,
        // 단순 테스트라면 특별한 필드를 사용하지 않는 경우 그냥 mock 객체로 만들어도 됩니다.
        // 하지만 CsTicket.create() 내부에서 member 값을 사용하기 때문에,
        // 실제 Member 구조를 확인하는 것이 더 안전할 수도 있습니다.

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(csTicketRepository.save(any(CsTicket.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        CsTicketResponse response = csTicketService.createTicket(req, memberId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.csTicketTitle()).isEqualTo(req.csTicketTitle());
        verify(csTicketRepository).save(any(CsTicket.class));
    }

    @Test
    @DisplayName("문의 등록 실패 - 회원 없음")
    void createTicket_Fail_MemberNotFound() {
        // given
        Long memberId = 1L;
        CsTicketRequest req = new CsTicketRequest(
            TicketChannel.WEB,
            "배송",
            "제목",
            "내용"
        );

        given(memberRepository.findById(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> csTicketService.createTicket(req, memberId))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("회원 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("내 문의 내역 조회 성공")
    void getMyTickets_Success() {
        // given
        Long memberId = 1L;
        int page = 0;
        int size = 10;
        String sort = "csTicketCreatedAt,desc";
        
        // Member member = new Member(); // 아래에서 mock 처리할 것이므로 제거됨
        // 필요하다면 리플렉션으로 ID를 설정할 수도 있지만, 그냥 ticket 자체를 mock하는 방법도 있습니다.
        // CsTicketResponse.from(ticket)이 내부에서 ticket.getMember().getMemberId()를 호출하므로
        // ID가 있는 member가 필요합니다.
        // 따라서 CsTicket을 mock해서 동작을 제어하는 방식이 더 적절합니다.

        CsTicket ticket = org.mockito.Mockito.mock(CsTicket.class);
        Member mockMember = org.mockito.Mockito.mock(Member.class);
        
        given(ticket.getMember()).willReturn(mockMember);
        given(mockMember.getMemberId()).willReturn(memberId);
        given(ticket.getCsTicketTitle()).willReturn("Title");
        // ... 필요하다면 다른 필드들도 추가 설정

        Page<CsTicket> tickets = new PageImpl<>(List.of(ticket));
        given(csTicketRepository.findByMember_MemberId(any(Long.class), any(Pageable.class))).willReturn(tickets);

        // when
        Page<CsTicketResponse> result = csTicketService.getMyTickets(memberId, page, size, sort);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).memberId()).isEqualTo(memberId);
    }

    @Test
    @DisplayName("문의 상세 조회 성공")
    void getTicket_Success() {
        // given
        Long ticketId = 1L;
        Long memberId = 1L;
        
        CsTicket ticket = org.mockito.Mockito.mock(CsTicket.class);
        Member mockMember = org.mockito.Mockito.mock(Member.class);
        
        given(ticket.getMember()).willReturn(mockMember);
        given(mockMember.getMemberId()).willReturn(memberId);
        given(ticket.getCsTicketId()).willReturn(ticketId);

        given(csTicketRepository.findById(ticketId)).willReturn(Optional.of(ticket));

        // when
        CsTicketResponse response = csTicketService.getTicket(ticketId);

        // then
        assertThat(response.csTicketId()).isEqualTo(ticketId);
    }

    @Test
    @DisplayName("문의 답변 등록 성공")
    void createReply_Success() {
        // given
        Long ticketId = 1L;
        CsTicketReplyRequest req = new CsTicketReplyRequest(999L, "답변입니다.");
        
        CsTicket ticket = org.mockito.Mockito.mock(CsTicket.class);
        
        given(csTicketRepository.findById(ticketId)).willReturn(Optional.of(ticket));
        given(csTicketReplyRepository.save(any(CsTicketReply.class))).willAnswer(invocation -> {
            CsTicketReply reply = invocation.getArgument(0);
            // 엔티티의 ID는 리플렉션 없이 쉽게 설정할 수 없기 때문에,
            // ID 자체보다 내용 검증 위주로 테스트하는 것이 좋습니다.
            return reply;
        });

        // when
        CsTicketReplyResponse response = csTicketService.createReply(ticketId, req);

        // then
        assertThat(response.replyContent()).isEqualTo(req.replyContent());
        verify(csTicketReplyRepository).save(any(CsTicketReply.class));
    }
}
