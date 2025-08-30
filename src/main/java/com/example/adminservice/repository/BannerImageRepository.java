package com.example.adminservice.repository;

import com.example.adminservice.domain.BannerImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 배너 이미지 엔티티에 대한 데이터 접근 레포지토리
 */
@Repository
public interface BannerImageRepository extends JpaRepository<BannerImage, Long> {

    /**
     * 모든 배너를 생성일 기준 내림차순으로 조회합니다.
     * @return 배너 목록 (최신순)
     */
    @Query("SELECT b FROM BannerImage b ORDER BY b.createdAt DESC")
    List<BannerImage> findAllOrderByCreatedAtDesc();

    /**
     * 모든 배너를 ID 기준 오름차순으로 조회합니다.
     * @return 배너 목록 (ID 순)
     */
    @Query("SELECT b FROM BannerImage b ORDER BY b.id ASC")
    List<BannerImage> findAllOrderByIdAsc();

    /**
     * 모든 배너를 표시 순서 기준 오름차순으로 조회합니다.
     * @return 배너 목록 (표시 순서순)
     */
    @Query("SELECT b FROM BannerImage b ORDER BY b.displayOrder ASC, b.id ASC")
    List<BannerImage> findAllOrderByDisplayOrder();

    /**
     * 최대 표시 순서 값을 조회합니다.
     * @return 최대 표시 순서 값
     */
    @Query("SELECT COALESCE(MAX(b.displayOrder), 0) FROM BannerImage b")
    Integer findMaxDisplayOrder();
}
