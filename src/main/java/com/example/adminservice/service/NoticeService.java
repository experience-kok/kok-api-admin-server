package com.example.adminservice.service;

import com.example.adminservice.constant.SortOption;
import com.example.adminservice.constant.UserRole;
import com.example.adminservice.domain.Notice;
import com.example.adminservice.dto.*;
import com.example.adminservice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;

    /**
     * 공지사항 생성
     */
    @Transactional
    public NoticeDetailResponse createNotice(NoticeCreateRequest request, Long authorId, String authorName) {
        log.info("공지사항 생성 요청 - 제목: {}, 작성자: {}, 필독여부: {}", request.getTitle(), authorName, request.getIsMustRead());

        Notice notice = Notice.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .isMustRead(request.getIsMustRead())
                .authorId(authorId)
                .authorName(authorName)
                .build();

        Notice savedNotice = noticeRepository.save(notice);
        log.info("공지사항 생성 완료 - ID: {}", savedNotice.getId());

        return NoticeDetailResponse.from(savedNotice);
    }

    /**
     * 공지사항 전체 목록 조회 (페이지네이션, 정렬 옵션)
     */
    public NoticePageResponse getAllNotices(int page, int size, SortOption sortOption) {
        log.info("공지사항 전체 목록 조회 요청 - page: {}, size: {}, 정렬: {}", page, size, sortOption.getDescription());

        Pageable pageable = PageRequest.of(page, size, sortOption.getSort());
        Page<Notice> noticePage = noticeRepository.findAll(pageable);

        Page<NoticeListResponse> responsePage = noticePage.map(NoticeListResponse::from);
        
        log.info("공지사항 목록 조회 완료 - 총 {}개, 현재 페이지: {}, 정렬: {}", 
                noticePage.getTotalElements(), page, sortOption.getDescription());
        
        return NoticePageResponse.from(responsePage);
    }

    /**
     * 공지사항 전체 목록 조회 (기본 정렬: 최신순)
     */
    public NoticePageResponse getAllNotices(int page, int size) {
        return getAllNotices(page, size, SortOption.LATEST);
    }

    /**
     * 필독 공지사항 목록 조회 (페이지네이션, 정렬 옵션)
     */
    public NoticePageResponse getMustReadNotices(int page, int size, SortOption sortOption) {
        log.info("필독 공지사항 목록 조회 요청 - page: {}, size: {}, 정렬: {}", page, size, sortOption.getDescription());

        Pageable pageable = PageRequest.of(page, size, sortOption.getSort());
        Page<Notice> noticePage = noticeRepository.findByIsMustReadTrue(pageable);

        Page<NoticeListResponse> responsePage = noticePage.map(NoticeListResponse::from);
        
        log.info("필독 공지사항 목록 조회 완료 - 총 {}개, 정렬: {}", 
                noticePage.getTotalElements(), sortOption.getDescription());
        
        return NoticePageResponse.from(responsePage);
    }

    /**
     * 필독 공지사항 목록 조회 (기본 정렬: 최신순)
     */
    public NoticePageResponse getMustReadNotices(int page, int size) {
        return getMustReadNotices(page, size, SortOption.LATEST);
    }

    /**
     * 일반 공지사항 목록 조회 (페이지네이션, 정렬 옵션)
     */
    public NoticePageResponse getRegularNotices(int page, int size, SortOption sortOption) {
        log.info("일반 공지사항 목록 조회 요청 - page: {}, size: {}, 정렬: {}", page, size, sortOption.getDescription());

        Pageable pageable = PageRequest.of(page, size, sortOption.getSort());
        Page<Notice> noticePage = noticeRepository.findByIsMustReadFalse(pageable);

        Page<NoticeListResponse> responsePage = noticePage.map(NoticeListResponse::from);
        
        log.info("일반 공지사항 목록 조회 완료 - 총 {}개, 정렬: {}", 
                noticePage.getTotalElements(), sortOption.getDescription());
        
        return NoticePageResponse.from(responsePage);
    }

    /**
     * 일반 공지사항 목록 조회 (기본 정렬: 최신순)
     */
    public NoticePageResponse getRegularNotices(int page, int size) {
        return getRegularNotices(page, size, SortOption.LATEST);
    }

    /**
     * 공지사항 개별 조회 (조회수 증가)
     */
    @Transactional
    public NoticeDetailResponse getNotice(Long id) {
        log.info("공지사항 개별 조회 요청 - ID: {}", id);

        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("공지사항을 찾을 수 없습니다 - ID: {}", id);
                    return new IllegalArgumentException("공지사항을 찾을 수 없습니다. ID: " + id);
                });

        // 조회수 증가
        noticeRepository.incrementViewCount(id);
        
        log.info("공지사항 조회 완료 - ID: {}, 제목: {}", id, notice.getTitle());

        return NoticeDetailResponse.from(notice);
    }

    /**
     * 공지사항 수정 (작성자 권한 또는 ADMIN 권한 검증)
     */
    @Transactional
    public NoticeDetailResponse updateNotice(Long id, NoticeUpdateRequest request, Long requestUserId, UserRole userRole) {
        log.info("공지사항 수정 요청 - ID: {}, 요청자: {}, 권한: {}", id, requestUserId, userRole);

        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("수정할 공지사항을 찾을 수 없습니다 - ID: {}", id);
                    return new IllegalArgumentException("공지사항을 찾을 수 없습니다. ID: " + id);
                });

        // 권한 검증: 작성자이거나 ADMIN이면 수정 가능
        if (!notice.getAuthorId().equals(requestUserId) && userRole != UserRole.ADMIN) {
            log.error("공지사항 수정 권한 없음 - ID: {}, 작성자: {}, 요청자: {}, 권한: {}", 
                    id, notice.getAuthorId(), requestUserId, userRole);
            throw new IllegalArgumentException("이 공지사항을 수정할 권한이 없습니다.");
        }

        // 내용 업데이트
        notice.updateContent(request.getTitle(), request.getContent(), request.getIsMustRead());

        Notice updatedNotice = noticeRepository.save(notice);
        log.info("공지사항 수정 완료 - ID: {}", id);

        return NoticeDetailResponse.from(updatedNotice);
    }

    /**
     * 공지사항 삭제 (작성자 권한 또는 ADMIN 권한 검증)
     */
    @Transactional
    public void deleteNotice(Long id, Long requestUserId, UserRole userRole) {
        log.info("공지사항 삭제 요청 - ID: {}, 요청자: {}, 권한: {}", id, requestUserId, userRole);

        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("삭제할 공지사항을 찾을 수 없습니다 - ID: {}", id);
                    return new IllegalArgumentException("공지사항을 찾을 수 없습니다. ID: " + id);
                });

        // 권한 검증: 작성자이거나 ADMIN이면 삭제 가능
        if (!notice.getAuthorId().equals(requestUserId) && userRole != UserRole.ADMIN) {
            log.error("공지사항 삭제 권한 없음 - ID: {}, 작성자: {}, 요청자: {}, 권한: {}", 
                    id, notice.getAuthorId(), requestUserId, userRole);
            throw new IllegalArgumentException("이 공지사항을 삭제할 권한이 없습니다.");
        }

        noticeRepository.deleteById(id);
        log.info("공지사항 삭제 완료 - ID: {} ({}이 삭제)", id, 
                userRole == UserRole.ADMIN ? "관리자" : "작성자");
    }

    /**
     * 제목으로 공지사항 검색 (페이지네이션, 정렬 옵션)
     */
    public NoticePageResponse searchNoticesByTitle(String title, int page, int size, SortOption sortOption) {
        log.info("공지사항 제목 검색 요청 - 키워드: {}, page: {}, size: {}, 정렬: {}", 
                title, page, size, sortOption.getDescription());

        Pageable pageable = PageRequest.of(page, size, sortOption.getSort());
        Page<Notice> noticePage = noticeRepository.findByTitleContainingIgnoreCase(title, pageable);

        Page<NoticeListResponse> responsePage = noticePage.map(NoticeListResponse::from);

        log.info("공지사항 검색 완료 - 키워드: {}, 정렬: {}, 결과: {}개", 
                title, sortOption.getDescription(), noticePage.getTotalElements());

        return NoticePageResponse.from(responsePage);
    }

    /**
     * 제목으로 공지사항 검색 (기본 정렬: 최신순)
     */
    public NoticePageResponse searchNoticesByTitle(String title, int page, int size) {
        return searchNoticesByTitle(title, page, size, SortOption.LATEST);
    }

    /**
     * 인기 공지사항 조회 (조회수 순)
     */
    public List<NoticeListResponse> getPopularNotices() {
        log.info("인기 공지사항 조회 요청");

        List<Notice> notices = noticeRepository.findTop10ByOrderByViewCountDesc();
        
        log.info("인기 공지사항 조회 완료 - 총 {}개", notices.size());
        
        return notices.stream()
                .map(NoticeListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 필독 공지사항 목록 조회 (페이지네이션 없이)
     */
    public List<NoticeListResponse> getMustReadNoticesList() {
        log.info("필독 공지사항 목록 조회 요청 (전체)");

        List<Notice> notices = noticeRepository.findByIsMustReadTrueOrderByCreatedAtDesc();
        
        log.info("필독 공지사항 목록 조회 완료 - 총 {}개", notices.size());
        
        return notices.stream()
                .map(NoticeListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 필독 공지사항 개수 조회
     */
    public long getMustReadNoticeCount() {
        log.info("필독 공지사항 개수 조회 요청");

        long count = noticeRepository.countByIsMustReadTrue();
        
        log.info("필독 공지사항 개수 조회 완료 - 개수: {}", count);
        
        return count;
    }

    /**
     * 일반 공지사항 개수 조회
     */
    public long getRegularNoticeCount() {
        log.info("일반 공지사항 개수 조회 요청");

        long count = noticeRepository.countByIsMustReadFalse();
        
        log.info("일반 공지사항 개수 조회 완료 - 개수: {}", count);
        
        return count;
    }
}
