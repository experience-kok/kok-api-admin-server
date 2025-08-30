package com.example.adminservice.dto;

import com.example.adminservice.domain.CampaignLocation;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "방문 정보 DTO (CampaignLocation 기반)")
public class VisitInfoDto {
    
    @Schema(description = "연락처", example = "010-1234-5678")
    @JsonProperty("contactPhone")
    private String contactPhone;
    
    @Schema(description = "홈페이지 주소", example = "https://example.com")
    @JsonProperty("homepage")
    private String homepage;
    
    @Schema(description = "위치 정보", example = "서울시 강남구 테헤란로")
    @JsonProperty("businessAddress")
    private String businessAddress;
    
    @Schema(description = "위치 정보 상세", example = "123-45번지 1층")
    @JsonProperty("businessDetailAddress")
    private String businessDetailAddress;
    
    @Schema(description = "방문 및 예약 안내", example = "평일 10시-22시 운영, 예약 필수")
    @JsonProperty("visitAndReservationInfo")
    private String visitAndReservationInfo;
    
    @Schema(description = "위도", example = "37.5665")
    @JsonProperty("lat")
    private Double lat;
    
    @Schema(description = "경도", example = "126.9780")
    @JsonProperty("lng")
    private Double lng;
    
    @Builder
    public VisitInfoDto(String contactPhone, String homepage, String businessAddress,
                       String businessDetailAddress, String visitAndReservationInfo,
                       Double lat, Double lng) {
        this.contactPhone = contactPhone;
        this.homepage = homepage;
        this.businessAddress = businessAddress;
        this.businessDetailAddress = businessDetailAddress;
        this.visitAndReservationInfo = visitAndReservationInfo;
        this.lat = lat;
        this.lng = lng;
    }
    
    /**
     * CampaignLocation 엔티티에서 DTO로 변환
     */
    public static VisitInfoDto from(CampaignLocation location) {
        if (location == null) {
            return null;
        }
        
        return VisitInfoDto.builder()
                .contactPhone(location.getContactPhone())
                .homepage(location.getHomepage())
                .businessAddress(location.getBusinessAddress())
                .businessDetailAddress(location.getBusinessDetailAddress())
                .visitAndReservationInfo(location.getVisitAndReservationInfo())
                .lat(location.getLatitude())
                .lng(location.getLongitude())
                .build();
    }
}
