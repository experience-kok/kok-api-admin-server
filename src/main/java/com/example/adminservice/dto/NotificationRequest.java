package com.example.adminservice.dto;

import com.example.adminservice.constant.EntityType;
import com.example.adminservice.constant.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 알림 생성 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "알림 생성 요청")
public class NotificationRequest {

    @Schema(description = "수신자 사용자 ID", example = "1")
    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;

    @Schema(description = "알림 타입", example = "CAMPAIGN_APPROVED")
    @NotNull(message = "알림 타입은 필수입니다")
    private NotificationType notificationType;

    @Schema(description = "알림 제목", example = "캠페인이 승인되었습니다")
    @NotBlank(message = "알림 제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다")
    private String title;

    @Schema(description = "알림 내용", example = "귀하의 캠페인 '여름 프로모션'이 승인되어 진행 가능합니다.")
    @NotBlank(message = "알림 내용은 필수입니다")
    private String message;

    @Schema(description = "관련 엔티티 ID", example = "123")
    private Long relatedEntityId;

    @Schema(description = "관련 엔티티 타입", example = "CAMPAIGN")
    private EntityType relatedEntityType;
}
