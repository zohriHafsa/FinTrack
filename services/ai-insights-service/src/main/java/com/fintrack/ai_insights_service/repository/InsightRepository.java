package com.fintrack.ai_insights_service.repository;

import com.fintrack.ai_insights_service.model.Insight;
import com.fintrack.ai_insights_service.model.InsightType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface InsightRepository extends JpaRepository<Insight, UUID> {

    Page<Insight> findByCompanyIdOrderByGeneratedAtDesc(UUID companyId, Pageable pageable);

    List<Insight> findByCompanyId(UUID companyId);

    List<Insight> findByCompanyIdAndTypeOrderByGeneratedAtDesc(UUID companyId, InsightType type);

    @Query("SELECT i FROM Insight i WHERE i.companyId = :companyId AND i.validUntil > :now ORDER BY i.generatedAt DESC")
    Page<Insight> findValidInsightsByCompanyId(@Param("companyId") UUID companyId,
                                               @Param("now") LocalDateTime now,
                                               Pageable pageable);

    @Query("SELECT i FROM Insight i WHERE i.validUntil <= :now")
    List<Insight> findExpiredInsights(@Param("now") LocalDateTime now);

    @Query("SELECT i FROM Insight i WHERE i.companyId = :companyId AND i.validUntil <= :now")
    List<Insight> findExpiredInsightsByCompanyId(@Param("companyId") UUID companyId,
                                                 @Param("now") LocalDateTime now);
}