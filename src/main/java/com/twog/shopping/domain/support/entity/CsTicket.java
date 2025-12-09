package com.twog.shopping.domain.support.entity;

import java.time.LocalDateTime;

import com.twog.shopping.domain.member.entity.Member;
import com.twog.shopping.domain.support.dto.CsTicketRequest;

import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "CS_ticket")
public class CsTicket {
  
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "cs_ticket_id")
	private Long csTicketId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Enumerated(EnumType.STRING)
	@Column(name = "cs_ticket_channel", nullable = false)
	private TicketChannel csTicketChannel;

	@Column(name = "cs_ticket_category", nullable = false, length = 50)
	private String csTicketCategory;

	@Enumerated(EnumType.STRING)
	@Column(name = "cs_ticket_status", nullable = false)
	private TicketStatus csTicketStatus;

	@Column(name = "cs_ticket_title", nullable = false, length = 200)
	private String csTicketTitle;

	@Column(name = "cs_ticket_content", nullable = false, columnDefinition = "TEXT")
	private String csTicketContent;

	@Column(name = "cs_ticket_created", nullable = false)
	private LocalDateTime csTicketCreatedAt;

	// ==== 생성 메서드 ==== //
	public static CsTicket create(
			CsTicketRequest req,
			Member member
	) {
			CsTicket ticket = new CsTicket();
			ticket.member = member;
			ticket.csTicketChannel = req.csTicketChannel();
			ticket.csTicketCategory = req.csTicketCategory();
			ticket.csTicketTitle = req.csTicketTitle();
			ticket.csTicketContent = req.csTicketContent();
			ticket.csTicketStatus = TicketStatus.RECEIVED;
			ticket.csTicketCreatedAt = LocalDateTime.now();
			return ticket;
	}

	// ==== 상태 변경 메서드 ==== //
	public void changeStatus(TicketStatus csTicketStatus) {
			this.csTicketStatus = csTicketStatus;
	}
}
