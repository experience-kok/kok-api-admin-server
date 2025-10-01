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
import com.example.adminservice.util.MarkdownConverter;
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
    private final MarkdownConverter markdownConverter; // Markdown → HTML 변환

    /**
     * 콕포스트 생성 (홍보 글 작성)
     */
    @Transactional
    public KokPostDetailResponse createKokPost(KokPostCreateRequest request, Long authorId, String authorName) {
        log.info("홍보용 콕포스트 생성 요청 - 제목: {}, 작성자: {}, 캠페인ID: {}, 활성: {}", 
                request.getTitle(), authorName, request.getCampaignId(), request.getActive());

        // 활성화 상태일 때 campaignId 검증
        request.validateCampaignIdForActive();

        // 캠페인 ID가 제공된 경우에만 캠페인 존재 여부 로깅
        if (request.getCampaignId() != null) {
            boolean campaignExists = campaignRepository.existsById(request.getCampaignId());
            if (!campaignExists) {
                log.info("참조된 캠페인이 존재하지 않습니다 - 캠페인ID: {} (홍보용 글이므로 진행)", 
                        request.getCampaignId());
            } else {
                log.info("기존 캠페인 연결 - 캠페인ID: {}", request.getCampaignId());
            }
        } else {
            log.info("캠페인 없이 작성 (비활성화 상태 임시 저장)");
        }

        KokPostVisitInfo visitInfo = request.getVisitInfo().toEntity();

        KokPost kokPost = KokPost.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .campaignId(request.getCampaignId())
                .authorId(authorId)
                .authorName(authorName)
                .visitInfo(visitInfo)
                .active(request.getActive())  // 사용자가 선택한 active 상태
                .build();

        KokPost savedKokPost = kokPostRepository.save(kokPost);
        
        if (savedKokPost.isActive()) {
            log.info("체험콕 글 생성 완료 (활성화) - ID: {}, 캠페인ID: {}", 
                    savedKokPost.getId(), savedKokPost.getCampaignId());
        } else {
            log.info("체험콕 글 생성 완료 (비활성화) - ID: {}, 캠페인ID: {}", 
                    savedKokPost.getId(), savedKokPost.getCampaignId());
        }

        return KokPostDetailResponse.from(savedKokPost);
    }

    /**
     * 콕포스트 전체 목록 조회 (정렬 옵션 포함, 활성 상태 필터링)
     */
    public List<KokPostListResponse> getAllKokPosts(SortOption sortOption, Boolean active) {
        log.info("콕포스트 전체 목록 조회 요청 - 정렬: {}, 활성 필터: {}", 
                sortOption.getDescription(), active);

        List<KokPost> kokPosts;
        
        if (active != null) {
            // 활성 상태 필터링
            if (sortOption == SortOption.VIEW_COUNT_DESC || sortOption == SortOption.VIEW_COUNT_ASC) {
                kokPosts = kokPostRepository.findAllByActive(active, 
                        PageRequest.of(0, Integer.MAX_VALUE, sortOption.getSort())).getContent();
            } else {
                kokPosts = kokPostRepository.findAllByActiveOrderByCreatedAtDesc(active);
            }
        } else {
            // 필터링 없이 전체 조회
            switch (sortOption) {
                case VIEW_COUNT_DESC, VIEW_COUNT_ASC -> 
                    kokPosts = kokPostRepository.findAll(sortOption.getSort());
                default -> 
                    kokPosts = kokPostRepository.findAllByOrderByCreatedAtDesc();
            }
        }
        
        log.info("콕포스트 목록 조회 완료 - 정렬: {}, 활성 필터: {}, 총 {}개", 
                sortOption.getDescription(), active, kokPosts.size());
        
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
     * 콕포스트 전체 목록 조회 (페이지네이션, 정렬 옵션 포함, 활성 상태 필터링)
     */
    public PagedResponse<KokPostListResponse> getAllKokPosts(SortOption sortOption, Pageable pageable, Boolean active) {
        log.info("콕포스트 전체 목록 조회 요청 (페이지네이션) - 정렬: {}, 페이지: {}, 크기: {}, 활성 필터: {}", 
                sortOption.getDescription(), pageable.getPageNumber(), pageable.getPageSize(), active);

        Sort sort = getSortFromOption(sortOption);
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        
        Page<KokPost> kokPostPage;
        if (active != null) {
            kokPostPage = kokPostRepository.findAllByActive(active, sortedPageable);
        } else {
            kokPostPage = kokPostRepository.findAll(sortedPageable);
        }
        
        log.info("콕포스트 목록 조회 완료 (페이지네이션) - 정렬: {}, 활성 필터: {}, 페이지: {}/{}, 총 {}개", 
                sortOption.getDescription(), active, kokPostPage.getNumber() + 1, 
                kokPostPage.getTotalPages(), kokPostPage.getTotalElements());
        
        Page<KokPostListResponse> responsePage = kokPostPage.map(KokPostListResponse::from);
        return PagedResponse.from(responsePage);
    }

    /**
     * 콕포스트 전체 목록 조회 (기본 정렬: 최신순, 활성 상태만)
     */
    public List<KokPostListResponse> getAllKokPosts() {
        return getAllKokPosts(SortOption.LATEST, true);  // 기본값: 활성화된 것만
    }

    /**
     * 콕포스트 개별 조회 (조회수 증가, Markdown → HTML 변환)
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
       //kokPostRepository.incrementViewCount(id);

        log.info("콕포스트 조회 완료 - ID: {}, 제목: {}", id, kokPost.getTitle());

        // Markdown → HTML 변환
        String htmlContent = markdownConverter.toHtml(kokPost.getContent());
        
        return KokPostDetailResponse.fromWithHtml(kokPost, htmlContent);
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

        // 캠페인 ID 업데이트 (명시적으로 포함된 경우에만)
        if (request.getCampaignId() != null) {
            Long oldCampaignId = kokPost.getCampaignId();
            kokPost.updateCampaignId(request.getCampaignId());
            log.info("캠페인 ID 변경 - KokPost ID: {}, 이전: {}, 변경: {}", 
                    id, oldCampaignId, request.getCampaignId());
        }

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
     * 제목으로 콕포스트 검색 (정렬 옵션 포함, 활성 상태 필터링)
     */
    public List<KokPostListResponse> searchKokPostsByTitle(String title, SortOption sortOption, Boolean active) {
        log.info("콕포스트 제목 검색 요청 - 키워드: {}, 정렬: {}, 활성 필터: {}", 
                title, sortOption.getDescription(), active);

        List<KokPost> kokPosts;
        if (active != null) {
            kokPosts = kokPostRepository.findByActiveAndTitleContainingIgnoreCase(active, title, sortOption.getSort());
        } else {
            kokPosts = kokPostRepository.findByTitleContainingIgnoreCase(title, sortOption.getSort());
        }

        log.info("콕포스트 검색 완료 - 키워드: {}, 정렬: {}, 활성 필터: {}, 결과: {}개",
                title, sortOption.getDescription(), active, kokPosts.size());

        return kokPosts.stream()
                .map(KokPostListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 제목으로 콕포스트 검색 (페이지네이션, 정렬 옵션 포함, 활성 상태 필터링)
     */
    public PagedResponse<KokPostListResponse> searchKokPostsByTitle(String title, SortOption sortOption, Pageable pageable, Boolean active) {
        log.info("콕포스트 제목 검색 요청 (페이지네이션) - 키워드: {}, 정렬: {}, 페이지: {}, 크기: {}, 활성 필터: {}", 
                title, sortOption.getDescription(), pageable.getPageNumber(), pageable.getPageSize(), active);

        Sort sort = getSortFromOption(sortOption);
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        
        Page<KokPost> kokPostPage;
        if (active != null) {
            kokPostPage = kokPostRepository.findByActiveAndTitleContainingIgnoreCase(active, title, sortedPageable);
        } else {
            kokPostPage = kokPostRepository.findByTitleContainingIgnoreCase(title, sortedPageable);
        }

        log.info("콕포스트 검색 완료 (페이지네이션) - 키워드: {}, 정렬: {}, 활성 필터: {}, 페이지: {}/{}, 총 {}개",
                title, sortOption.getDescription(), active, kokPostPage.getNumber() + 1, 
                kokPostPage.getTotalPages(), kokPostPage.getTotalElements());

        Page<KokPostListResponse> responsePage = kokPostPage.map(KokPostListResponse::from);
        return PagedResponse.from(responsePage);
    }

    /**
     * 제목으로 콕포스트 검색 (기본 정렬: 최신순, 활성화된 것만)
     */
    public List<KokPostListResponse> searchKokPostsByTitle(String title) {
        return searchKokPostsByTitle(title, SortOption.LATEST, true);
    }

    /**
     * 인기 콕포스트 조회 (조회수 순, 활성 상태 필터링)
     */
    public List<KokPostListResponse> getPopularKokPosts(Boolean active) {
        log.info("인기 콕포스트 조회 요청 - 활성 필터: {}", active);

        List<KokPost> kokPosts;
        if (active != null) {
            kokPosts = kokPostRepository.findTop10ByActiveOrderByViewCountDesc(active);
        } else {
            kokPosts = kokPostRepository.findTop10ByOrderByViewCountDesc();
        }

        log.info("인기 콕포스트 조회 완료 - 활성 필터: {}, 총 {}개", active, kokPosts.size());

        return kokPosts.stream()
                .map(KokPostListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 인기 콕포스트 조회 (활성화된 것만, 기본값)
     */
    public List<KokPostListResponse> getPopularKokPosts() {
        return getPopularKokPosts(true);
    }

    /**
     * 캠페인 ID로 KokPost 비활성화 처리 및 참조 제거
     * (캠페인 삭제 시 외래키 제약 조건 위반을 방지하기 위해 campaign_id를 NULL로 설정)
     */
    @Transactional
    public void deactivateKokPostsByCampaignId(Long campaignId) {
        log.info("캠페인 삭제로 인한 KokPost 비활성화 및 참조 제거 시작 - 캠페인ID: {}", campaignId);
        
        List<KokPost> kokPosts = kokPostRepository.findAllByCampaignId(campaignId);
        
        if (kokPosts.isEmpty()) {
            log.info("처리할 KokPost가 없습니다 - 캠페인ID: {}", campaignId);
            return;
        }
        
        // 비활성화 처리 + 캠페인 참조 제거 (campaign_id를 NULL로 설정)
        kokPosts.forEach(kokPost -> {
            kokPost.deactivate();
            kokPost.removeCampaignReference(); // 외래키 제약 조건 위반 방지
        });
        kokPostRepository.saveAll(kokPosts);
        
        log.info("캠페인 관련 KokPost 비활성화 및 참조 제거 완료 - 캠페인ID: {}, 처리된 글 수: {}", 
                campaignId, kokPosts.size());
    }

    /**
     * 콕포스트 비활성화 (작성자 권한 또는 ADMIN 권한 검증)
     */
    @Transactional
    public void deactivateKokPost(Long id, Long requestUserId, UserRole userRole) {
        log.info("콕포스트 비활성화 요청 - ID: {}, 요청자: {}, 권한: {}", id, requestUserId, userRole);

        KokPost kokPost = kokPostRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("비활성화할 콕포스트를 찾을 수 없습니다 - ID: {}", id);
                    return new IllegalArgumentException("콕포스트를 찾을 수 없습니다. ID: " + id);
                });

        // 권한 검증: 작성자이거나 ADMIN이면 비활성화 가능
        if (!kokPost.getAuthorId().equals(requestUserId) && userRole != UserRole.ADMIN) {
            log.error("콕포스트 비활성화 권한 없음 - ID: {}, 작성자: {}, 요청자: {}, 권한: {}",
                    id, kokPost.getAuthorId(), requestUserId, userRole);
            throw new IllegalArgumentException("이 콕포스트를 비활성화할 권한이 없습니다.");
        }

        // 이미 비활성화된 경우
        if (!kokPost.isActive()) {
            log.warn("이미 비활성화된 콕포스트 - ID: {}", id);
            throw new IllegalStateException("이미 비활성화된 콕포스트입니다.");
        }

        kokPost.deactivate();
        kokPostRepository.save(kokPost);
        
        log.info("콕포스트 비활성화 완료 - ID: {} ({}이 비활성화)", id,
                userRole == UserRole.ADMIN ? "관리자" : "작성자");
    }

    /**
     * 콕포스트 활성화 (작성자 권한 또는 ADMIN 권한 검증)
     */
    @Transactional
    public void activateKokPost(Long id, Long requestUserId, UserRole userRole) {
        log.info("콕포스트 활성화 요청 - ID: {}, 요청자: {}, 권한: {}", id, requestUserId, userRole);

        KokPost kokPost = kokPostRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("활성화할 콕포스트를 찾을 수 없습니다 - ID: {}", id);
                    return new IllegalArgumentException("콕포스트를 찾을 수 없습니다. ID: " + id);
                });

        // 권한 검증: 작성자이거나 ADMIN이면 활성화 가능
        if (!kokPost.getAuthorId().equals(requestUserId) && userRole != UserRole.ADMIN) {
            log.error("콕포스트 활성화 권한 없음 - ID: {}, 작성자: {}, 요청자: {}, 권한: {}",
                    id, kokPost.getAuthorId(), requestUserId, userRole);
            throw new IllegalArgumentException("이 콕포스트를 활성화할 권한이 없습니다.");
        }

        // 이미 활성화된 경우
        if (kokPost.isActive()) {
            log.warn("이미 활성화된 콕포스트 - ID: {}", id);
            throw new IllegalStateException("이미 활성화된 콕포스트입니다.");
        }

        kokPost.activate();
        kokPostRepository.save(kokPost);
        
        log.info("콕포스트 활성화 완료 - ID: {} ({}이 활성화)", id,
                userRole == UserRole.ADMIN ? "관리자" : "작성자");
    }
}
