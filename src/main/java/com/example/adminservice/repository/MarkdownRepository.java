package com.example.adminservice.repository;

import com.example.adminservice.domain.Markdown;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarkdownRepository extends JpaRepository<Markdown, Long> {

    /**
     * 모든 마크다운을 최신순으로 조회
     */
    List<Markdown> findAllByOrderByCreatedAtDesc();

    /**
     * 제목으로 마크다운 검색 (대소문자 구분 없이, 최신순)
     */
    List<Markdown> findByTitleContainingIgnoreCase(String title, Sort sort);

    /**
     * 조회수 증가
     */
    @Modifying
    @Query("UPDATE Markdown m SET m.viewCount = m.viewCount + 1 WHERE m.id = :id")
    void incrementViewCount(@Param("id") Long id);
}
