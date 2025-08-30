package com.example.adminservice.repository;

import com.example.adminservice.constant.AccountType;
import com.example.adminservice.constant.UserRole;
import com.example.adminservice.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndRole(String email, UserRole role);
    Optional<User> findBySocialId(String socialId);
    Optional<User> findByProviderAndSocialId(String provider, String socialId);
    
    long countByRole(UserRole role);
    long countByActive(boolean active);
    long countByAccountType(AccountType accountType);
    
    List<User> findByRole(UserRole role);
    List<User> findByActive(boolean active);
    List<User> findByAccountType(AccountType accountType);
    
    Page<User> findAll(Pageable pageable);
    Page<User> findByRole(UserRole role, Pageable pageable);
    Page<User> findByActive(boolean active, Pageable pageable);
    
    /**
     * 비밀번호가 null이 아닌 모든 사용자를 조회합니다.
     * 비밀번호 해시 마이그레이션에 사용됩니다.
     */
    List<User> findByPasswordIsNotNull();
    
    /**
     * 이메일 인증이 완료되지 않은 사용자들을 조회합니다.
     */
    List<User> findByEmailVerifiedFalse();
    
    /**
     * 특정 제공자의 사용자들을 조회합니다.
     */
    List<User> findByProvider(String provider);

    /**
     * 키워드로 사용자 검색 (이메일, 닉네임, 전화번호, 메모에서 검색)
     */
    @Query("SELECT u FROM User u " +
           "WHERE (LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.memo) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:role IS NULL OR u.role = :role) " +
           "AND (:active IS NULL OR u.active = :active) " +
           "ORDER BY u.id DESC")
    Page<User> findByKeyword(@Param("keyword") String keyword, 
                            @Param("role") UserRole role, 
                            @Param("active") Boolean active, 
                            Pageable pageable);

    /**
     * 고급 사용자 검색 (다중 조건)
     */
    @Query("SELECT u FROM User u " +
           "WHERE (:keyword IS NULL OR " +
           "       LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "       LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "       LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "       LOWER(u.memo) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:role IS NULL OR u.role = :role) " +
           "AND (:accountType IS NULL OR u.accountType = :accountType) " +
           "AND (:provider IS NULL OR u.provider = :provider) " +
           "AND (:active IS NULL OR u.active = :active) " +
           "AND (:emailVerified IS NULL OR u.emailVerified = :emailVerified) " +
           "AND (:gender IS NULL OR u.gender = :gender) " +
           "AND (:minAge IS NULL OR u.age >= :minAge) " +
           "AND (:maxAge IS NULL OR u.age <= :maxAge) " +
           "AND (:startDate IS NULL OR u.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR u.createdAt <= :endDate)")
    Page<User> findByAdvancedSearch(@Param("keyword") String keyword,
                                   @Param("role") UserRole role,
                                   @Param("accountType") AccountType accountType,
                                   @Param("provider") String provider,
                                   @Param("active") Boolean active,
                                   @Param("emailVerified") Boolean emailVerified,
                                   @Param("gender") String gender,
                                   @Param("minAge") Integer minAge,
                                   @Param("maxAge") Integer maxAge,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate,
                                   Pageable pageable);

    /**
     * 역할별 사용자 통계 조회
     */
    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> countByRoleGrouped();

    /**
     * 제공자별 사용자 통계 조회
     */
    @Query("SELECT u.provider, COUNT(u) FROM User u GROUP BY u.provider")
    List<Object[]> countByProviderGrouped();

    /**
     * 검색 조건에 맞는 활성 사용자 수 조회
     */
    @Query("SELECT COUNT(u) FROM User u " +
           "WHERE (:keyword IS NULL OR " +
           "       LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "       LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "       LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "       LOWER(u.memo) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:role IS NULL OR u.role = :role) " +
           "AND (:accountType IS NULL OR u.accountType = :accountType) " +
           "AND (:provider IS NULL OR u.provider = :provider) " +
           "AND (:emailVerified IS NULL OR u.emailVerified = :emailVerified) " +
           "AND (:gender IS NULL OR u.gender = :gender) " +
           "AND (:minAge IS NULL OR u.age >= :minAge) " +
           "AND (:maxAge IS NULL OR u.age <= :maxAge) " +
           "AND (:startDate IS NULL OR u.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR u.createdAt <= :endDate) " +
           "AND u.active = true")
    Long countActiveBySearchConditions(@Param("keyword") String keyword,
                                      @Param("role") UserRole role,
                                      @Param("accountType") AccountType accountType,
                                      @Param("provider") String provider,
                                      @Param("emailVerified") Boolean emailVerified,
                                      @Param("gender") String gender,
                                      @Param("minAge") Integer minAge,
                                      @Param("maxAge") Integer maxAge,
                                      @Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);
}
