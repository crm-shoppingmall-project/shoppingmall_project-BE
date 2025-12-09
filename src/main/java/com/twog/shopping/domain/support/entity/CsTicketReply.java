package com.twog.shopping.domain.support.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "CS_ticket_reply")
public class CsTicketReply {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "reply_id")
  private Long replyId;

  // FK: cs_ticket_id
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cs_ticket_id", nullable = false)
  private CsTicket csTicket;

  @Column(name = "reply_responder_id", nullable = false)
  private Long replyResponderId;

  @Column(name = "reply_content", nullable = false, columnDefinition = "text")
  private String replyContent;

  @Column(name = "reply_created", nullable = false)
  private LocalDateTime replyCreatedAt;

  // 생성자 (정적 팩토리 메서드 권장)
  private CsTicketReply(CsTicket csTicket, Long replyResponderId, String replyContent) {
      this.csTicket = csTicket;
      this.replyResponderId = replyResponderId;
      this.replyContent = replyContent;
      this.replyCreatedAt = LocalDateTime.now();
  }

  public static CsTicketReply create(CsTicket csTicket, Long replyResponderId, String replyContent) {
      return new CsTicketReply(csTicket, replyResponderId, replyContent);
  }
}
