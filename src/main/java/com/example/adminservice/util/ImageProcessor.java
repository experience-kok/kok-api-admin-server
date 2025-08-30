package com.example.adminservice.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Component
@Slf4j
public class ImageProcessor {
    
    /**
     * 이미지를 16:9 비율로 크롭하여 ByteArrayOutputStream으로 반환
     */
    public ByteArrayOutputStream cropTo16x9(InputStream inputStream, String format) throws IOException {
        BufferedImage originalImage = ImageIO.read(inputStream);
        
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        // 16:9 비율 계산
        double targetRatio = 16.0 / 9.0;
        double currentRatio = (double) originalWidth / originalHeight;
        
        int newWidth, newHeight;
        int x = 0, y = 0;
        
        if (Math.abs(currentRatio - targetRatio) < 0.01) {
            // 이미 16:9 비율이면 그대로 반환
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(originalImage, format, outputStream);
            log.info("이미지가 이미 16:9 비율입니다: {}x{}", originalWidth, originalHeight);
            return outputStream;
        }
        
        if (currentRatio > targetRatio) {
            // 이미지가 더 넓음 - 좌우 크롭
            newWidth = (int) (originalHeight * targetRatio);
            newHeight = originalHeight;
            x = (originalWidth - newWidth) / 2;
        } else {
            // 이미지가 더 높음 - 상하 크롭
            newWidth = originalWidth;
            newHeight = (int) (originalWidth / targetRatio);
            y = (originalHeight - newHeight) / 2;
        }
        
        // 크롭된 이미지 생성
        BufferedImage croppedImage = originalImage.getSubimage(x, y, newWidth, newHeight);
        
        // ByteArrayOutputStream으로 변환
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(croppedImage, format, outputStream);
        
        log.info("이미지 크롭 완료: {}x{} -> {}x{} (16:9 비율 적용)", originalWidth, originalHeight, newWidth, newHeight);
        
        return outputStream;
    }
    
    /**
     * 파일 확장자 추출
     */
    public String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
