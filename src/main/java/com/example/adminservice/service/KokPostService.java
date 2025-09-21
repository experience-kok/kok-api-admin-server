package com.example.adminservice.service;

import com.example.adminservice.constant.SortOption;
import com.example.adminservice.constant.UserRole;
import com.example.adminservice.domain.KokPost;
import com.example.adminservice.domain.KokPostVisitInfo;
import com.example.adminservice.dto.KokPostCreateRequest;
import com.example.adminservice.dto.KokPostDetailResponse;
import com.example.adminservice.dto.KokPostListResponse;
import com.example.adminservice.dto.KokPostUpdateRequest;
import com.example.adminservice.dto.PagedResponse;
import com.example.adminservice.repository.CampaignRepository;
import com.example.adminservice.repository.KokPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KokPostService {

    private final KokPostRepository kokPostRepository;
    private final CampaignRepository campaignRepository; // 참조 확인용 (선택사항)

    /**
     * 콕포스트 생성 (홍보 글 작성)
     */
    @Transactional
    public KokPostDetailResponse createKokPost(KokPostCreateRequest request, Long authorId, String authorName) {
        log.info("홍보용 콕포스트 생성 요청 - 제목: {}, 작성자: {}, 캠페인ID: {}", request.getTitle(), authorName, request.getCampaignId());

        // 홍보 전략 관점: 캠페인 ID는 참조 정보로만 활용 (존재 여부 검증 안함)
        // - 종료된 캠페인의 홍보 글도 작성 가능
        // - 예정된 캠페인의 사전 홍보 글도 작성 가능  
        // - 가상 시나리오 홍보 글도 작성 가능
        if (request.getCampaignId() != null) {
            boolean campaignExists = campaignRepository.existsById(request.getCampaignId());
            if (!campaignExists) {
                log.info("참조된 캠페인이 존재하지 않습니다 - 캠페인ID: {} (홍보용 글이므로 진행)", request.getCampaignId());
            } else {
                log.info("기존 캠페인 연결 - 캠페인ID: {}", request.getCampaignId());
            }
        }

        KokPostVisitInfo visitInfo = request.getVisitInfo().toEntity();

        KokPost kokPost = KokPost.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .campaignId(request.getCampaignId())
                .authorId(authorId)
                .authorName(authorName)
                .visitInfo(visitInfo)
                .build();

        KokPost savedKokPost = kokPostRepository.save(kokPost);
        log.info("홍보용 체험콕 글 생성 완료 - ID: {}, 캠페인참조: {}", 
                savedKokPost.getId(), request.getCampaignId());

        return KokPostDetailResponse.from(savedKokPost);
    }

    /**
     * 콕포스트 전체 목록 조회 (정렬 옵션 포함)
     */
    public List<KokPostListResponse> getAllKokPosts(SortOption sortOption) {
        log.info("콕포스트 전체 목록 조회 요청 - 정렬: {}", sortOption.getDescription());

        List<KokPost> kokPosts;
        
        switch (sortOption) {
            case VIEW_COUNT_DESC -> kokPosts = kokPostRepository.findAll(sortOption.getSort());
            case VIEW_COUNT_ASC -> kokPosts = kokPostRepository.findAll(sortOption.getSort());
            default -> kokPosts = kokPostRepository.findAllByOrderByCreatedAtDesc();
        }
        
        log.info("콕포스트 목록 조회 완료 - 정렬: {}, 총 {}개", sortOption.getDescription(), kokPosts.size());
        
        return kokPosts.stream()
                .map(KokPostListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 정렬 옵션에 따른 Sort 객체 생성
     */
    private Sort getSortFromOption(SortOption sortOption) {
        return switch (sortOption) {
            case VIEW_COUNT_DESC, VIEW_COUNT_ASC -> sortOption.getSort();
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    /**
     * 콕포스트 전체 목록 조회 (페이지네이션, 정렬 옵션 포함)
     */
    public PagedResponse<KokPostListResponse> getAllKokPosts(SortOption sortOption, Pageable pageable) {
        log.info("콕포스트 전체 목록 조회 요청 (페이지네이션) - 정렬: {}, 페이지: {}, 크기: {}", 
                sortOption.getDescription(), pageable.getPageNumber(), pageable.getPageSize());

        Sort sort = getSortFromOption(sortOption);
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        Page<KokPost> kokPostPage = kokPostRepository.findAll(sortedPageable);
        
        log.info("콕포스트 목록 조회 완료 (페이지네이션) - 정렬: {}, 페이지: {}/{}, 총 {}개", 
                sortOption.getDescription(), kokPostPage.getNumber() + 1, kokPostPage.getTotalPages(), kokPostPage.getTotalElements());
        
        Page<KokPostListResponse> responsePage = kokPostPage.map(KokPostListResponse::from);
        return PagedResponse.from(responsePage);
    }

    /**
     * 콕포스트 전체 목록 조회 (기본 정렬: 최신순)
     */
    public List<KokPostListResponse> getAllKokPosts() {
        return getAllKokPosts(SortOption.LATEST);
    }

    /**
     * 콕포스트 개별 조회 (조회수 증가)
     */
    @Transactional
    public KokPostDetailResponse getKokPost(Long id) {
        log.info("콕포스트 개별 조회 요청 - ID: {}", id);

        KokPost kokPost = kokPostRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("콕포스트를 찾을 수 없습니다 - ID: {}", id);
                    return new IllegalArgumentException("콕포스트를 찾을 수 없습니다. ID: " + id);
                });

        // 조회수 증가
        kokPostRepository.incrementViewCount(id);

        log.info("콕포스트 조회 완료 - ID: {}, 제목: {}", id, kokPost.getTitle());

        return KokPostDetailResponse.from(kokPost);
    }

    /**
     * 콕포스트 수정 (작성자 권한 또는 ADMIN 권한 검증)
     */
    @Transactional
    public KokPostDetailResponse updateKokPost(Long id, KokPostUpdateRequest request, Long requestUserId, UserRole userRole) {
        log.info("콕포스트 수정 요청 - ID: {}, 요청자: {}, 권한: {}", id, requestUserId, userRole);

        KokPost kokPost = kokPostRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("수정할 콕포스트를 찾을 수 없습니다 - ID: {}", id);
                    return new IllegalArgumentException("콕포스트를 찾을 수 없습니다. ID: " + id);
                });

        // 권한 검증: 작성자이거나 ADMIN이면 수정 가능
        if (!kokPost.getAuthorId().equals(requestUserId) && userRole != UserRole.ADMIN) {
            log.error("콕포스트 수정 권한 없음 - ID: {}, 작성자: {}, 요청자: {}, 권한: {}",
                    id, kokPost.getAuthorId(), requestUserId, userRole);
            throw new IllegalArgumentException("이 콕포스트를 수정할 권한이 없습니다.");
        }

        // 내용 업데이트
        kokPost.updateContent(request.getTitle(), request.getContent());

        // 방문 정보 업데이트
        if (request.getVisitInfo() != null) {
            KokPostVisitInfo newVisitInfo = request.getVisitInfo().toEntity();
            kokPost.updateVisitInfo(newVisitInfo);
        }

        KokPost updatedKokPost = kokPostRepository.save(kokPost);
        log.info("콕포스트 수정 완료 - ID: {}", id);

        return KokPostDetailResponse.from(updatedKokPost);
    }

    /**
     * 콕포스트 삭제 (작성자 권한 또는 ADMIN 권한 검증)
     */
    @Transactional
    public void deleteKokPost(Long id, Long requestUserId, UserRole userRole) {
        log.info("콕포스트 삭제 요청 - ID: {}, 요청자: {}, 권한: {}", id, requestUserId, userRole);

        KokPost kokPost = kokPostRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("삭제할 콕포스트를 찾을 수 없습니다 - ID: {}", id);
                    return new IllegalArgumentException("콕포스트를 찾을 수 없습니다. ID: " + id);
                });

        // 권한 검증: 작성자이거나 ADMIN이면 삭제 가능
        if (!kokPost.getAuthorId().equals(requestUserId) && userRole != UserRole.ADMIN) {
            log.error("콕포스트 삭제 권한 없음 - ID: {}, 작성자: {}, 요청자: {}, 권한: {}",
                    id, kokPost.getAuthorId(), requestUserId, userRole);
            throw new IllegalArgumentException("이 콕포스트를 삭제할 권한이 없습니다.");
        }

        kokPostRepository.deleteById(id);
        log.info("콕포스트 삭제 완료 - ID: {} ({}이 삭제)", id,
                userRole == UserRole.ADMIN ? "관리자" : "작성자");
    }

    /**
     * 제목으로 콕포스트 검색 (정렬 옵션 포함)
     */
    public List<KokPostListResponse> searchKokPostsByTitle(String title, SortOption sortOption) {
        log.info("콕포스트 제목 검색 요청 - 키워드: {}, 정렬: {}", title, sortOption.getDescription());

        List<KokPost> kokPosts = kokPostRepository.findByTitleContainingIgnoreCase(title, sortOption.getSort());

        log.info("콕포스트 검색 완료 - 키워드: {}, 정렬: {}, 결과: {}개",
                title, sortOption.getDescription(), kokPosts.size());

        return kokPosts.stream()
                .map(KokPostListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 제목으로 콕포스트 검색 (페이지네이션, 정렬 옵션 포함)
     */
    public PagedResponse<KokPostListResponse> searchKokPostsByTitle(String title, SortOption sortOption, Pageable pageable) {
        log.info("콕포스트 제목 검색 요청 (페이지네이션) - 키워드: {}, 정렬: {}, 페이지: {}, 크기: {}", 
                title, sortOption.getDescription(), pageable.getPageNumber(), pageable.getPageSize());

        Sort sort = getSortFromOption(sortOption);
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        Page<KokPost> kokPostPage = kokPostRepository.findByTitleContainingIgnoreCase(title, sortedPageable);

        log.info("콕포스트 검색 완료 (페이지네이션) - 키워드: {}, 정렬: {}, 페이지: {}/{}, 총 {}개",
                title, sortOption.getDescription(), kokPostPage.getNumber() + 1, kokPostPage.getTotalPages(), kokPostPage.getTotalElements());

        Page<KokPostListResponse> responsePage = kokPostPage.map(KokPostListResponse::from);
        return PagedResponse.from(responsePage);
    }

    /**
     * 제목으로 콕포스트 검색 (기본 정렬: 최신순)
     */
    public List<KokPostListResponse> searchKokPostsByTitle(String title) {
        return searchKokPostsByTitle(title, SortOption.LATEST);
    }

    /**
     * 인기 콕포스트 조회 (조회수 순)
     */
    public List<KokPostListResponse> getPopularKokPosts() {
        log.info("인기 콕포스트 조회 요청");

        List<KokPost> kokPosts = kokPostRepository.findTop10ByOrderByViewCountDesc();

        log.info("인기 콕포스트 조회 완료 - 총 {}개", kokPosts.size());

        return kokPosts.stream()
                .map(KokPostListResponse::from)
                .collect(Collectors.toList());
    }
}
