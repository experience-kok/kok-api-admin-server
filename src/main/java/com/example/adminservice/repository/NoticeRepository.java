package com.example.adminservice.repository;

import com.example.adminservice.domain.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    /**
     * 모든 공지사항을 페이지네이션으로 조회 (정렬 옵션 포함)
     */
    Page<Notice> findAll(Pageable pageable);

    /**
     * 필독 공지사항만 조회 (페이지네이션, 정렬 옵션)
     */
    Page<Notice> findByIsMustReadTrue(Pageable pageable);

    /**
     * 일반 공지사항만 조회 (페이지네이션, 정렬 옵션)
     */
    Page<Notice> findByIsMustReadFalse(Pageable pageable);

    /**
     * 제목으로 공지사항 검색 (페이지네이션, 정렬 옵션)
     */
    Page<Notice> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    /**
     * 조회수 상위 N개 공지사항 조회
     */
    List<Notice> findTop10ByOrderByViewCountDesc();

    /**
     * 필독 공지사항 목록 조회 (페이지네이션 없이)
     */
    List<Notice> findByIsMustReadTrueOrderByCreatedAtDesc();

    /**
     * 조회수 증가
     */
    @Modifying
    @Query("UPDATE Notice n SET n.viewCount = n.viewCount + 1 WHERE n.id = :id")
    void incrementViewCount(@Param("id") Long id);

    /**
     * 필독 공지사항 개수 조회
     */
    long countByIsMustReadTrue();

    /**
     * 일반 공지사항 개수 조회
     */
    long countByIsMustReadFalse();
}
