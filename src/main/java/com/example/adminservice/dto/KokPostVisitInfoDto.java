package com.example.adminservice.dto;

import com.example.adminservice.domain.KokPostVisitInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "콕포스트 방문 정보")
public class KokPostVisitInfoDto {

    @NotBlank(message = "연락처는 필수입니다")
    @Schema(description = "연락처", example = "02-1234-5678", required = true)
    private String contactPhone;

    @Schema(description = "홈페이지 주소 (선택)", example = "https://example.com")
    private String homepage;

    @Schema(description = "위치 정보 (선택)", example = "서울시 강남구 테헤란로")
    private String businessAddress;

    @Schema(description = "위치 정보 상세 (선택)", example = "123-45 건물 2층")
    private String businessDetailAddress;

    @Schema(description = "위도 (선택)", example = "37.5665")
    private Double lat;

    @Schema(description = "경도 (선택)", example = "126.9780")
    private Double lng;

    @Builder
    public KokPostVisitInfoDto(String contactPhone, String homepage, String businessAddress,
                              String businessDetailAddress, Double lat, Double lng) {
        this.contactPhone = contactPhone;
        this.homepage = homepage;
        this.businessAddress = businessAddress;
        this.businessDetailAddress = businessDetailAddress;
        this.lat = lat;
        this.lng = lng;
    }

    public static KokPostVisitInfoDto from(KokPostVisitInfo visitInfo) {
        if (visitInfo == null) {
            return null;
        }
        return KokPostVisitInfoDto.builder()
                .contactPhone(visitInfo.getContactPhone())
                .homepage(visitInfo.getHomepage())
                .businessAddress(visitInfo.getBusinessAddress())
                .businessDetailAddress(visitInfo.getBusinessDetailAddress())
                .lat(visitInfo.getLat())
                .lng(visitInfo.getLng())
                .build();
    }

    public KokPostVisitInfo toEntity() {
        return KokPostVisitInfo.builder()
                .contactPhone(contactPhone)
                .homepage(homepage)
                .businessAddress(businessAddress)
                .businessDetailAddress(businessDetailAddress)
                .lat(lat)
                .lng(lng)
                .build();
    }
}
