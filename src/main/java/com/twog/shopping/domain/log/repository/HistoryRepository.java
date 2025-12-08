package com.twog.shopping.domain.log.repository;

import com.twog.shopping.domain.log.entity.History;
import com.twog.shopping.domain.log.entity.HistoryActionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface HistoryRepository extends JpaRepository<History, Long> {

    @Query("""
        SELECT h
        FROM History h
        WHERE h.memberId = :memberId
          AND h.actionType IN :actionTypes
          AND h.datetime BETWEEN :start and :end
     """)
    List<History> getMemberLogs(
            @Param("memberId") Long memberId,
            @Param("actionTypes") List<HistoryActionType> actionTypes,
            @Param("start")LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT h
        FROM History h
        WHERE h.actionType in :actionTypes
          AND h.datetime between :start and :end
    """)
    List<History> getLogsForPeriod(
            @Param("actionTypes") List<HistoryActionType> actionTypes,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


}
