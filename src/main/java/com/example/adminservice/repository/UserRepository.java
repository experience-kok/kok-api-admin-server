package com.example.adminservice.repository;

import com.example.adminservice.constant.AccountType;
import com.example.adminservice.constant.UserRole;
import com.example.adminservice.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
