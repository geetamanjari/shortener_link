package com.example.shortener.link;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

    long countByLinkId(Long linkId);

    @Query(value = "SELECT CAST(clicked_at AS DATE) AS click_date, COUNT(*) AS total "
            + "FROM click_events WHERE link_id = :linkId "
            + "GROUP BY CAST(clicked_at AS DATE) "
            + "ORDER BY CAST(clicked_at AS DATE)", nativeQuery = true)
    List<Object[]> countClicksPerDay(@Param("linkId") Long linkId);

    @Query("SELECT c.referrer, COUNT(c) FROM ClickEvent c "
            + "WHERE c.link.id = :linkId "
            + "GROUP BY c.referrer ORDER BY COUNT(c) DESC")
    List<Object[]> countByReferrer(@Param("linkId") Long linkId, Pageable pageable);
}