package com.twog.shopping.domain.promotion.repository;

import com.twog.shopping.domain.promotion.entity.MessageSendLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageSendLogRepository extends JpaRepository<MessageSendLog, Long> {
}
