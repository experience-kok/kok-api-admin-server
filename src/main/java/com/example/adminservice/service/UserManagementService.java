package com.example.adminservice.service;

import com.example.adminservice.domain.User;
import com.example.adminservice.dto.UserDetailResponseDTO;
import com.example.adminservice.dto.UserListResponseDTO;
import com.example.adminservice.dto.UserSearchRequestDTO;
import com.example.adminservice.dto.UserSearchResponseDTO;
import com.example.adminservice.repository.UserRepository;
import com.example.adminservice.constant.UserRole;
import com.example.adminservice.constant.AccountType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserManagementService {

    private final UserRepository userRepository;

    /**
     * 사용자 목록 조회 (페이지네이션)
     */
    public Page<UserListResponseDTO> getUserList(int page, int size) {
        log.info("사용자 목록 조회: page={}, size={}", page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> users = userRepository.findAll(pageable);
        
        return users.map(this::convertToListDTO);
    }

    /**
     * 사용자 상세 조회
     */
    public UserDetailResponseDTO getUserDetail(Long userId) {
        log.info("사용자 상세 조회: userId={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        
        return convertToDetailDTO(user);
    }

    /**
     * 사용자 검색 - 레포지토리의 @Query 메서드 사용
     */
    public Page<UserListResponseDTO> searchUsers(String keyword, int page, int size, String role, Boolean active) {
        log.info("사용자 검색: keyword={}, page={}, size={}, role={}, active={}", 
                keyword, page, size, role, active);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("검색 키워드는 필수입니다");
        }
        
        // role 문자열을 UserRole enum으로 변환
        UserRole userRole = null;
        if (role != null && !role.trim().isEmpty()) {
            try {
                userRole = UserRole.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("유효하지 않은 권한입니다: " + role);
            }
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<User> users = userRepository.findByKeyword(keyword.trim(), userRole, active, pageable);
        
        return users.map(this::convertToListDTO);
    }

    /**
     * 고급 사용자 검색
     */
    public UserSearchResponseDTO advancedSearchUsers(UserSearchRequestDTO request) {
        log.info("고급 사용자 검색: keyword={}, role={}, provider={}, page={}, size={}", 
                request.getKeyword(), request.getRole(), request.getProvider(), 
                request.getPage(), request.getSize());
        
        // 입력값 검증
        validateSearchRequest(request);
        
        // 정렬 조건 생성
        Sort sort = createSort(request.getSortBy(), request.getSortDirection());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        
        // 열거형 변환
        UserRole userRole = parseUserRole(request.getRole());
        AccountType accountType = parseAccountType(request.getAccountType());
        
        // 검색 실행
        Page<User> userPage = userRepository.findByAdvancedSearch(
                request.getKeyword(),
                userRole,
                accountType,
                request.getProvider(),
                request.getActive(),
                request.getEmailVerified(),
                request.getGender(),
                request.getMinAge(),
                request.getMaxAge(),
                request.getStartDate(),
                request.getEndDate(),
                pageable
        );
        
        // 검색 통계 생성
        UserSearchResponseDTO.SearchStats stats = generateSearchStats(request, userPage.getTotalElements());
        
        // 응답 DTO 생성
        return UserSearchResponseDTO.builder()
                .users(userPage.getContent().stream()
                        .map(this::convertToSearchUserInfo)
                        .collect(Collectors.toList()))
                .pagination(createPageInfo(userPage))
                .stats(stats)
                .build();
    }

    /**
     * 검색 요청 유효성 검증
     */
    private void validateSearchRequest(UserSearchRequestDTO request) {
        if (request.getMinAge() != null && request.getMaxAge() != null) {
            if (request.getMinAge() > request.getMaxAge()) {
                throw new IllegalArgumentException("최소 나이는 최대 나이보다 클 수 없습니다");
            }
        }
        
        if (request.getStartDate() != null && request.getEndDate() != null) {
            if (request.getStartDate().isAfter(request.getEndDate())) {
                throw new IllegalArgumentException("시작일은 종료일보다 늦을 수 없습니다");
            }
        }
        
        // 정렬 필드 유효성 검증
        List<String> allowedSortFields = List.of("createdAt", "updatedAt", "email", "nickname", "id");
        if (!allowedSortFields.contains(request.getSortBy())) {
            throw new IllegalArgumentException("유효하지 않은 정렬 필드입니다: " + request.getSortBy());
        }
        
        // 정렬 방향 유효성 검증
        if (!List.of("asc", "desc").contains(request.getSortDirection().toLowerCase())) {
            throw new IllegalArgumentException("정렬 방향은 'asc' 또는 'desc'여야 합니다");
        }
    }

    /**
     * 정렬 조건 생성
     */
    private Sort createSort(String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC;
        return Sort.by(direction, sortBy);
    }

    /**
     * UserRole 문자열을 enum으로 변환
     */
    private UserRole parseUserRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            return null;
        }
        try {
            return UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 권한입니다: " + role);
        }
    }

    /**
     * AccountType 문자열을 enum으로 변환
     */
    private AccountType parseAccountType(String accountType) {
        if (accountType == null || accountType.trim().isEmpty()) {
            return null;
        }
        try {
            return AccountType.valueOf(accountType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 계정 타입입니다: " + accountType);
        }
    }

    /**
     * 검색 통계 생성
     */
    private UserSearchResponseDTO.SearchStats generateSearchStats(UserSearchRequestDTO request, long totalCount) {
        // 활성/비활성 사용자 수 계산
        Long activeCount = userRepository.countActiveBySearchConditions(
                request.getKeyword(),
                parseUserRole(request.getRole()),
                parseAccountType(request.getAccountType()),
                request.getProvider(),
                request.getEmailVerified(),
                request.getGender(),
                request.getMinAge(),
                request.getMaxAge(),
                request.getStartDate(),
                request.getEndDate()
        );
        
        Long inactiveCount = totalCount - activeCount;
        
        // 이메일 인증 완료 사용자 수 (임시로 전체의 90%로 설정)
        Long verifiedCount = Math.round(totalCount * 0.9);
        
        // 역할별 통계 (전체 통계 기반으로 계산)
        List<Object[]> roleStats = userRepository.countByRoleGrouped();
        UserSearchResponseDTO.RoleStats roleStatsDto = createRoleStats(roleStats);
        
        // 제공자별 통계 (전체 통계 기반으로 계산)
        List<Object[]> providerStats = userRepository.countByProviderGrouped();
        UserSearchResponseDTO.ProviderStats providerStatsDto = createProviderStats(providerStats);
        
        return UserSearchResponseDTO.SearchStats.builder()
                .totalCount(totalCount)
                .activeCount(activeCount)
                .inactiveCount(inactiveCount)
                .verifiedCount(verifiedCount)
                .roleStats(roleStatsDto)
                .providerStats(providerStatsDto)
                .build();
    }

    /**
     * 역할별 통계 생성
     */
    private UserSearchResponseDTO.RoleStats createRoleStats(List<Object[]> roleStats) {
        Map<UserRole, Long> roleMap = roleStats.stream()
                .collect(Collectors.toMap(
                        row -> (UserRole) row[0],
                        row -> (Long) row[1]
                ));
        
        return UserSearchResponseDTO.RoleStats.builder()
                .userCount(roleMap.getOrDefault(UserRole.USER, 0L))
                .clientCount(roleMap.getOrDefault(UserRole.CLIENT, 0L))
                .adminCount(roleMap.getOrDefault(UserRole.ADMIN, 0L))
                .build();
    }

    /**
     * 제공자별 통계 생성
     */
    private UserSearchResponseDTO.ProviderStats createProviderStats(List<Object[]> providerStats) {
        Map<String, Long> providerMap = providerStats.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));
        
        return UserSearchResponseDTO.ProviderStats.builder()
                .googleCount(providerMap.getOrDefault("GOOGLE", 0L))
                .kakaoCount(providerMap.getOrDefault("KAKAO", 0L))
                .naverCount(providerMap.getOrDefault("NAVER", 0L))
                .localCount(providerMap.getOrDefault("LOCAL", 0L))
                .build();
    }

    /**
     * 페이지 정보 생성
     */
    private UserSearchResponseDTO.PageInfo createPageInfo(Page<User> userPage) {
        return UserSearchResponseDTO.PageInfo.builder()
                .currentPage(userPage.getNumber())
                .pageSize(userPage.getSize())
                .totalPages(userPage.getTotalPages())
                .totalElements(userPage.getTotalElements())
                .isFirst(userPage.isFirst())
                .isLast(userPage.isLast())
                .hasNext(userPage.hasNext())
                .hasPrevious(userPage.hasPrevious())
                .build();
    }

    /**
     * User 엔티티를 UserListResponseDTO로 변환
     */
    private UserListResponseDTO convertToListDTO(User user) {
        return UserListResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .provider(user.getProvider()) // 이미 String 타입이므로 그대로 사용
                .accountType(user.getAccountType() != null ? user.getAccountType().name() : null)
                .active(user.getActive())
                .emailVerified(user.getEmailVerified())
                .gender(user.getGender())
                .age(user.getAge())
                .phone(user.getPhone())
                .profileImg(user.getProfileImg())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * User 엔티티를 검색용 UserInfo로 변환
     */
    private UserSearchResponseDTO.UserInfo convertToSearchUserInfo(User user) {
        return UserSearchResponseDTO.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .provider(user.getProvider())
                .accountType(user.getAccountType() != null ? user.getAccountType().name() : null)
                .active(user.getActive())
                .emailVerified(user.getEmailVerified())
                .gender(user.getGender())
                .age(user.getAge())
                .phone(user.getPhone())
                .profileImg(user.getProfileImg())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .memo(user.getMemo())
                .build();
    }

    /**
     * User 엔티티를 UserDetailResponseDTO로 변환
     */
    private UserDetailResponseDTO convertToDetailDTO(User user) {
        return UserDetailResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .provider(user.getProvider()) // 이미 String 타입이므로 그대로 사용
                .accountType(user.getAccountType() != null ? user.getAccountType().name() : null)
                .active(user.getActive())
                .emailVerified(user.getEmailVerified())
                .gender(user.getGender())
                .age(user.getAge())
                .phone(user.getPhone())
                .profileImg(user.getProfileImg())
                .memo(user.getMemo())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                // User 엔티티에 없는 필드들은 null로 설정 (향후 추가 예정)
                .lastLoginDate(null)
                .loginCount(null)
                // TODO: 캠페인, 지원서, 통계 정보는 추후 구현
                .build();
    }
}
