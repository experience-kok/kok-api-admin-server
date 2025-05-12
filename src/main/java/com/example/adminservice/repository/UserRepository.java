package com.example.adminservice.repository;

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
    Optional<User> findByEmailAndRole(String email, String role);
    long countByRole(String role);
    long countByActive(boolean active);
    Page<User> findAll(Pageable pageable);
    
    /**
     * 비밀번호가 null이 아닌 모든 사용자를 조회합니다.
     * 비밀번호 해시 마이그레이션에 사용됩니다.
     */
    List<User> findByPasswordIsNotNull();
}
