package com.example.adminservice.repository;

import com.example.adminservice.domain.UserSnsPlatform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSnsPlatformRepository extends JpaRepository<UserSnsPlatform, Long> {
    List<UserSnsPlatform> findByUserId(Long userId);
    Optional<UserSnsPlatform> findByUserIdAndId(Long userId, Long platformId);
    Optional<UserSnsPlatform> findByUserIdAndPlatformTypeAndAccountUrl(Long userId, String platformType, String accountUrl);
    Optional<UserSnsPlatform> findByPlatformTypeAndAccountUrl(String platformType, String accountUrl);
    void deleteByUserId(Long userId);
}