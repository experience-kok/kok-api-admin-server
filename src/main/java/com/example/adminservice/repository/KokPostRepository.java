package com.example.adminservice.repository;

import com.example.adminservice.domain.KokPost;
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
public interface KokPostRepository extends JpaRepository<KokPost, Long> {

    /**
     * 모든 콕포스트를 최신순으로 조회
     */
    List<KokPost> findAllByOrderByCreatedAtDesc();

    /**
     * 활성 상태로 필터링하여 최신순으로 조회
     */
    List<KokPost> findAllByActiveOrderByCreatedAtDesc(boolean active);

    /**
     * 활성 상태로 필터링하여 조회 (페이지네이션)
     */
    Page<KokPost> findAllByActive(boolean active, Pageable pageable);

    /**
     * 제목으로 콕포스트 검색 (대소문자 구분 없이, Sort 옵션)
     */
    List<KokPost> findByTitleContainingIgnoreCase(String title, Sort sort);

    /**
     * 제목으로 콕포스트 검색 (활성 상태 필터링, Sort 옵션)
     */
    List<KokPost> findByActiveAndTitleContainingIgnoreCase(boolean active, String title, Sort sort);

    /**
     * 제목으로 콕포스트 검색 (페이지네이션, 대소문자 구분 없이)
     */
    Page<KokPost> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    /**
     * 제목으로 콕포스트 검색 (활성 상태 필터링, 페이지네이션)
     */
    Page<KokPost> findByActiveAndTitleContainingIgnoreCase(boolean active, String title, Pageable pageable);

    /**
     * 조회수 상위 N개 콕포스트 조회
     */
    List<KokPost> findTop10ByOrderByViewCountDesc();

    /**
     * 조회수 상위 N개 콕포스트 조회 (활성 상태 필터링)
     */
    List<KokPost> findTop10ByActiveOrderByViewCountDesc(boolean active);

    /**
     * 캠페인별 콕포스트 개수 조회
     */
    long countByCampaignId(Long campaignId);

    /**
     * 캠페인 ID로 모든 KokPost 조회 (비활성화 처리용)
     */
    List<KokPost> findAllByCampaignId(Long campaignId);

    /**
     * 조회수 증가
     */
/*    @Modifying
    @Query("UPDATE KokPost k SET k.viewCount = k.viewCount + 1 WHERE k.id = :id")
    void incrementViewCount(@Param("id") Long id);*/
}
