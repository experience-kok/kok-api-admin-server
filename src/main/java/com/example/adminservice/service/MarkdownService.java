package com.example.adminservice.service;

import com.example.adminservice.constant.UserRole;
import com.example.adminservice.domain.Markdown;
import com.example.adminservice.dto.MarkdownCreateRequest;
import com.example.adminservice.dto.MarkdownDetailResponse;
import com.example.adminservice.dto.MarkdownListResponse;
import com.example.adminservice.dto.MarkdownUpdateRequest;
import com.example.adminservice.repository.MarkdownRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MarkdownService {

    private final MarkdownRepository markdownRepository;

    /**
     * 마크다운 생성
     */
    @Transactional
    public MarkdownDetailResponse createMarkdown(MarkdownCreateRequest request, Long authorId, String authorName) {
        log.info("마크다운 생성 요청 - 제목: {}, 작성자: {}", request.getTitle(), authorName);

        Markdown markdown = Markdown.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .authorId(authorId)
                .authorName(authorName)
                .build();

        Markdown savedMarkdown = markdownRepository.save(markdown);
        log.info("마크다운 생성 완료 - ID: {}", savedMarkdown.getId());

        return MarkdownDetailResponse.from(savedMarkdown);
    }

    /**
     * 마크다운 전체 목록 조회
     */
    public List<MarkdownListResponse> getAllMarkdowns() {
        log.info("마크다운 전체 목록 조회 요청");

        List<Markdown> markdowns = markdownRepository.findAllByOrderByCreatedAtDesc();
        
        log.info("마크다운 목록 조회 완료 - 총 {}개", markdowns.size());
        
        return markdowns.stream()
                .map(MarkdownListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 마크다운 개별 조회 (조회수 증가)
     */
    @Transactional
    public MarkdownDetailResponse getMarkdown(Long id) {
        log.info("마크다운 개별 조회 요청 - ID: {}", id);

        Markdown markdown = markdownRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("마크다운을 찾을 수 없습니다 - ID: {}", id);
                    return new IllegalArgumentException("마크다운을 찾을 수 없습니다. ID: " + id);
                });

        // 조회수 증가
        markdownRepository.incrementViewCount(id);
        
        log.info("마크다운 조회 완료 - ID: {}, 제목: {}", id, markdown.getTitle());

        return MarkdownDetailResponse.from(markdown);
    }

    /**
     * 마크다운 수정 (작성자 권한 또는 ADMIN 권한 검증)
     */
    @Transactional
    public MarkdownDetailResponse updateMarkdown(Long id, MarkdownUpdateRequest request, Long requestUserId, UserRole userRole) {
        log.info("마크다운 수정 요청 - ID: {}, 요청자: {}, 권한: {}", id, requestUserId, userRole);

        Markdown markdown = markdownRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("수정할 마크다운을 찾을 수 없습니다 - ID: {}", id);
                    return new IllegalArgumentException("마크다운을 찾을 수 없습니다. ID: " + id);
                });

        // 권한 검증: 작성자이거나 ADMIN이면 수정 가능
        if (!markdown.getAuthorId().equals(requestUserId) && userRole != UserRole.ADMIN) {
            log.error("마크다운 수정 권한 없음 - ID: {}, 작성자: {}, 요청자: {}, 권한: {}", 
                    id, markdown.getAuthorId(), requestUserId, userRole);
            throw new IllegalArgumentException("이 마크다운을 수정할 권한이 없습니다.");
        }

        // 내용 업데이트
        markdown.updateContent(request.getTitle(), request.getContent());

        Markdown updatedMarkdown = markdownRepository.save(markdown);
        log.info("마크다운 수정 완료 - ID: {}", id);

        return MarkdownDetailResponse.from(updatedMarkdown);
    }

    /**
     * 마크다운 삭제 (작성자 권한 또는 ADMIN 권한 검증)
     */
    @Transactional
    public void deleteMarkdown(Long id, Long requestUserId, UserRole userRole) {
        log.info("마크다운 삭제 요청 - ID: {}, 요청자: {}, 권한: {}", id, requestUserId, userRole);

        Markdown markdown = markdownRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("삭제할 마크다운을 찾을 수 없습니다 - ID: {}", id);
                    return new IllegalArgumentException("마크다운을 찾을 수 없습니다. ID: " + id);
                });

        // 권한 검증: 작성자이거나 ADMIN이면 삭제 가능
        if (!markdown.getAuthorId().equals(requestUserId) && userRole != UserRole.ADMIN) {
            log.error("마크다운 삭제 권한 없음 - ID: {}, 작성자: {}, 요청자: {}, 권한: {}", 
                    id, markdown.getAuthorId(), requestUserId, userRole);
            throw new IllegalArgumentException("이 마크다운을 삭제할 권한이 없습니다.");
        }

        markdownRepository.deleteById(id);
        log.info("마크다운 삭제 완료 - ID: {} ({}이 삭제)", id, 
                userRole == UserRole.ADMIN ? "관리자" : "작성자");
    }

    /**
     * 제목으로 마크다운 검색
     */
    public List<MarkdownListResponse> searchMarkdownsByTitle(String title) {
        log.info("마크다운 제목 검색 요청 - 키워드: {}", title);

        List<Markdown> markdowns = markdownRepository.findByTitleContainingIgnoreCase(
                title, 
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        log.info("마크다운 검색 완료 - 키워드: {}, 결과: {}개", title, markdowns.size());

        return markdowns.stream()
                .map(MarkdownListResponse::from)
                .collect(Collectors.toList());
    }
}
