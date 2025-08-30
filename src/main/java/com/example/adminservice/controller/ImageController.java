package com.example.adminservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

@Slf4j
@RestController
@RequestMapping("/images")
@Tag(name = "이미지 리소스", description = "정적 이미지 리소스 제공")
public class ImageController {

    @Operation(summary = "플레이스홀더 이미지", description = "배너 이미지가 없을 때 표시할 플레이스홀더 이미지")
    @GetMapping(value = "/placeholder.jpg", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getPlaceholderImage() {
        try {
            // 플레이스홀더 이미지 생성
            BufferedImage image = new BufferedImage(300, 180, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            
            // 배경색 설정
            g2d.setColor(new Color(248, 249, 250));
            g2d.fillRect(0, 0, 300, 180);
            
            // 테두리 그리기
            g2d.setColor(new Color(222, 226, 230));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(1, 1, 298, 178);
            
            // 텍스트 설정
            g2d.setColor(new Color(108, 117, 125));
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            
            // 텍스트 그리기
            FontMetrics fm = g2d.getFontMetrics();
            String text = "No Image";
            int x = (300 - fm.stringWidth(text)) / 2;
            int y = (180 + fm.getAscent()) / 2;
            g2d.drawString(text, x, y);
            
            g2d.dispose();
            
            // 이미지를 바이트 배열로 변환
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(baos.toByteArray());
                    
        } catch (IOException e) {
            log.error("플레이스홀더 이미지 생성 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}