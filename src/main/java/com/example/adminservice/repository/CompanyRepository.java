package com.example.adminservice.repository;

import com.example.adminservice.domain.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 회사 정보 Repository
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    /**
     * 사용자 ID로 회사 정보 조회
     */
    Optional<Company> findByUserId(Long userId);

    /**
     * 사업자등록번호로 회사 정보 조회
     */
    Optional<Company> findByBusinessRegistrationNumber(String businessRegistrationNumber);

    /**
     * 회사명으로 회사 정보 조회
     */
    Optional<Company> findByCompanyName(String companyName);

    /**
     * USER 롤을 가진 사용자들의 회사 목록 조회
     */
    @Query("SELECT c FROM Company c JOIN User u ON c.userId = u.id WHERE u.role = 'USER'")
    List<Company> findCompaniesByUserRole();

    /**
     * USER 롤을 가진 사용자들의 회사 목록 조회 (페이징)
     */
    @Query("SELECT c FROM Company c JOIN User u ON c.userId = u.id WHERE u.role = 'USER'")
    Page<Company> findCompaniesByUserRole(Pageable pageable);
}
