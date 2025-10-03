
package com.example.adminservice.service;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

/**
 * AWS SES 이메일 발송 서비스
 * EC2 IAM Role을 통한 인증 사용
 */
@Slf4j
@Service
public class SESService {

    private AmazonSimpleEmailService sesClient;
    
    @Value("${aws.ses.from-email}")
    private String fromEmail;
    
    @Value("${aws.ses.from-name:ChkokTeam}")
    private String fromName;
    
    @Value("${aws.ses.region:ap-northeast-2}")
    private String region;
    
    @PostConstruct
    public void initializeSESClient() {
        try {
            // EC2 IAM Role을 통한 자동 인증
            this.sesClient = AmazonSimpleEmailServiceClientBuilder.standard()
                    .withCredentials(InstanceProfileCredentialsProvider.getInstance())
                    .withRegion(region)
                    .build();
            
            log.info("SES 클라이언트 초기화 완료 (IAM Role 사용) - region: {}, from: {}", region, fromEmail);
        } catch (Exception e) {
            log.error("SES 클라이언트 초기화 실패: {}", e.getMessage(), e);
            throw new RuntimeException("SES 클라이언트 초기화에 실패했습니다.", e);
        }
    }



/**
     * 텍스트 이메일 발송
     */


    public void sendEmail(String toEmail, String subject, String body) {
        try {
            validateEmailParameters(toEmail, subject, body);
            
            // 영문 발신자 이름 사용 (한글 인코딩 문제 방지)
            String fromAddress = fromName + " <" + fromEmail + ">";
            
            SendEmailRequest request = new SendEmailRequest()
                    .withDestination(new Destination().withToAddresses(toEmail))
                    .withMessage(new Message()
                            .withBody(new Body()
                                    .withText(new Content()
                                            .withCharset("UTF-8")
                                            .withData(body)))
                            .withSubject(new Content()
                                    .withCharset("UTF-8")
                                    .withData(subject)))
                    .withSource(fromAddress);

            SendEmailResult result = sesClient.sendEmail(request);
            log.info("이메일 발송 성공: messageId={}, to={}, subject={}", 
                    result.getMessageId(), toEmail, subject);
                    
        } catch (MessageRejectedException e) {
            log.error("SES 메시지 거부: to={}, subject={}, reason={}", toEmail, subject, e.getMessage());
            throw new RuntimeException("이메일이 거부되었습니다. 수신자 주소를 확인해주세요.", e);
        } catch (MailFromDomainNotVerifiedException e) {
            log.error("발신자 도메인 미인증: from={}, to={}", fromEmail, toEmail);
            throw new RuntimeException("발신자 이메일 도메인이 인증되지 않았습니다.", e);
        } catch (ConfigurationSetDoesNotExistException e) {
            log.error("SES 설정 오류: {}", e.getMessage());
            throw new RuntimeException("이메일 서비스 설정 오류입니다.", e);
        } catch (AccountSendingPausedException e) {
            log.error("SES 계정 발송 중단: {}", e.getMessage());
            throw new RuntimeException("이메일 발송 계정이 일시 중단되었습니다. 관리자에게 문의하세요.", e);
        } catch (Exception e) {
            log.error("이메일 발송 중 예상치 못한 오류: to={}, subject={}, error={}", 
                    toEmail, subject, e.getMessage(), e);
            throw new RuntimeException("이메일 발송 중 오류가 발생했습니다.", e);
        }
    }



/**
     * HTML 이메일 발송
     */


    public void sendHtmlEmail(String toEmail, String subject, String htmlBody) {
        try {
            validateEmailParameters(toEmail, subject, htmlBody);
            
            // 영문 발신자 이름 사용 (한글 인코딩 문제 방지)
            String fromAddress = fromName + " <" + fromEmail + ">";
            
            SendEmailRequest request = new SendEmailRequest()
                    .withDestination(new Destination().withToAddresses(toEmail))
                    .withMessage(new Message()
                            .withBody(new Body()
                                    .withHtml(new Content()
                                            .withCharset("UTF-8")
                                            .withData(htmlBody)))
                            .withSubject(new Content()
                                    .withCharset("UTF-8")
                                    .withData(subject)))
                    .withSource(fromAddress);

            SendEmailResult result = sesClient.sendEmail(request);
            log.info("HTML 이메일 발송 성공: messageId={}, to={}, subject={}", 
                    result.getMessageId(), toEmail, subject);
                    
        } catch (MessageRejectedException e) {
            log.error("SES 메시지 거부: to={}, subject={}, reason={}", toEmail, subject, e.getMessage());
            throw new RuntimeException("이메일이 거부되었습니다. 수신자 주소를 확인해주세요.", e);
        } catch (MailFromDomainNotVerifiedException e) {
            log.error("발신자 도메인 미인증: from={}, to={}", fromEmail, toEmail);
            throw new RuntimeException("발신자 이메일 도메인이 인증되지 않았습니다.", e);
        } catch (Exception e) {
            log.error("HTML 이메일 발송 중 예상치 못한 오류: to={}, subject={}, error={}", 
                    toEmail, subject, e.getMessage(), e);
            throw new RuntimeException("HTML 이메일 발송 중 오류가 발생했습니다.", e);
        }
    }
    


/**
     * 안전한 이메일 발송 (예외를 던지지 않음)
     */


    public boolean sendEmailSafe(String toEmail, String subject, String body) {
        try {
            sendEmail(toEmail, subject, body);
            return true;
        } catch (Exception e) {
            log.warn("이메일 발송 실패했지만 무시함: to={}, error={}", toEmail, e.getMessage());
            return false;
        }
    }
    


/**
     * 안전한 HTML 이메일 발송 (예외를 던지지 않음)
     */


    public boolean sendHtmlEmailSafe(String toEmail, String subject, String htmlBody) {
        try {
            sendHtmlEmail(toEmail, subject, htmlBody);
            return true;
        } catch (Exception e) {
            log.warn("HTML 이메일 발송 실패했지만 무시함: to={}, error={}", toEmail, e.getMessage());
            return false;
        }
    }



/**
     * 캠페인 승인 알림 이메일 발송
     */


    public void sendCampaignApprovedEmail(String toEmail, String nickname, String campaignTitle) {
        String subject = "🎉 캠페인이 승인되었어요!";
        String htmlBody = String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h1 style="color: #2388FF; text-align: center;">
                        <img src="https://ckokservice.s3.ap-northeast-2.amazonaws.com/email/Frame+59.svg" style="width: 40px; height: 40px; vertical-align: middle;"> 축하해요!
                    </h1>
                    <div style="background-color: #2388FF; padding: 20px; border-radius: 8px; margin: 20px 0; text-align: center;">
                        <h2 style="margin-top: 0; color: white;">%s님의 캠페인이 승인되었어요!</h2>
                        <p style="font-size: 18px; margin: 15px 0; color: white;">
                            <strong style="color: white;">캠페인: %s</strong>
                        </p>
                    </div>
                                            
                    <div style="background-color: #f9f9f9; padding: 20px; border-radius: 5px; margin: 20px 0;">
                        <h3 style="margin-top: 0; color: #555;">다음 단계</h3>
                        <ol style="color: #666; padding-left: 20px;">
                            <li>체험콕에 로그인해주세요</li>
                            <li>마이페이지에서 승인된 캠페인을 확인해주세요</li>
                        </ol>
                    </div>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="https://chkok.kr" 
                           style="display: inline-block; padding: 15px 30px; background-color: #2388FF; color: white; text-decoration: none; border-radius: 5px; font-size: 16px;">
                            캠페인 확인하기
                        </a>
                    </div>
                    
                    <p style="color: #666; text-align: center;">
                        승인을 축하드려요! 🌟
                    </p>
                    
                    <hr style="margin: 30px 0; border: none; border-top: 1px solid #ddd;">
                    <p style="font-size: 12px; color: #888; text-align: center;">
                        체험콕<br>
                        이 이메일은 발송 전용이에요.
                    </p>
                </div>
            </body>
            </html>
            """, nickname, campaignTitle);
            
        sendHtmlEmail(toEmail, subject, htmlBody);
    }



/**
     * 안전한 캠페인 승인 이메일 발송 (예외를 던지지 않음)
     */


    public boolean sendCampaignApprovedEmailSafe(String toEmail, String nickname, String campaignTitle) {
        try {
            sendCampaignApprovedEmail(toEmail, nickname, campaignTitle);
            return true;
        } catch (Exception e) {
            log.warn("캠페인 승인 이메일 발송 실패했지만 무시함: to={}, error={}", toEmail, e.getMessage());
            return false;
        }
    }



/**
     * 캠페인 거절 알림 이메일 발송
     */


    public void sendCampaignRejectedEmail(String toEmail, String nickname, String campaignTitle, String rejectionReason) {
        String subject = "캠페인이 거절 되었어요!";
        String htmlBody = String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h1 style="color: #2388FF; text-align: center;">
                        <img src="https://ckokservice.s3.ap-northeast-2.amazonaws.com/email/Frame+59.svg" style="width: 40px; height: 40px; vertical-align: middle;"> 캠페인 심사 결과
                    </h1>
                    <div style="background-color: #2388FF; padding: 20px; border-radius: 8px; margin: 20px 0; text-align: center; border-left: 4px solid #2388FF;">
                        <h2 style="margin-top: 0; color: white;">%s님의 캠페인이 승인되지 않았어요</h2>
                        <p style="font-size: 18px; margin: 15px 0; color: #666;">
                            <strong style="color: white;">캠페인: %s</strong>
                        </p>
                    </div>
                    
                    <div style="background-color: #2388FF; padding: 20px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #2388FF;">
                        <h3 style="margin-top: 0; color: white;">거절 사유</h3>
                        <p style="color: white; margin-bottom: 0;">"%s"</p>
                    </div>
                                            
                    <div style="background-color: #f9f9f9; padding: 20px; border-radius: 5px; margin: 20px 0;">
                        <h3 style="margin-top: 0; color: #555;">다음 단계</h3>
                        <ol style="color: #666; padding-left: 20px;">
                            <li>거절 사유를 참고해주세요</li>
                            <li>내용을 보완하여 새 캠페인으로 다시 신청해주세요</li>
                            <li>추가 문의사항은 고객센터로 연락해주세요</li>
                        </ol>
                    </div>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="https://chkok.kr" 
                           style="display: inline-block; padding: 15px 30px; background-color: #2388FF; color: white; text-decoration: none; border-radius: 5px; font-size: 16px;">
                            새 캠페인 만들기
                        </a>
                    </div>
                    
                    <p style="color: #666; text-align: center;">
                        더 나은 캠페인으로 다시 만나요!
                    </p>
                    
                    <hr style="margin: 30px 0; border: none; border-top: 1px solid #ddd;">
                    <p style="font-size: 12px; color: #888; text-align: center;">
                        체험콕<br>
                        이 이메일은 발송 전용이에요.
                    </p>
                </div>
            </body>
            </html>
            """, nickname, campaignTitle, rejectionReason != null ? rejectionReason : "승인 기준을 충족하지 않습니다.");
            
        sendHtmlEmail(toEmail, subject, htmlBody);
    }



/**
     * 안전한 캠페인 거절 이메일 발송 (예외를 던지지 않음)
     */


    public boolean sendCampaignRejectedEmailSafe(String toEmail, String nickname, String campaignTitle, String rejectionReason) {
        try {
            sendCampaignRejectedEmail(toEmail, nickname, campaignTitle, rejectionReason);
            return true;
        } catch (Exception e) {
            log.warn("캠페인 거절 이메일 발송 실패했지만 무시함: to={}, error={}", toEmail, e.getMessage());
            return false;
        }
    }



/**
     * 캠페인 수정 요청 알림 이메일 발송
     */

    public void sendCampaignRevisionRequestEmail(String toEmail, String nickname, String campaignTitle, String revisionReason) {
        String subject = "캠페인 수정이 요청되었어요!";
        String htmlBody = String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h1 style="color: #2388FF; text-align: center;">
                        <img src="https://ckokservice.s3.ap-northeast-2.amazonaws.com/email/Frame+59.svg" alt="축하 아이콘" style="width: 40px; height: 40px; vertical-align: middle;"> 캠페인 수정 요청!
                    </h1>
                    <div style="background-color: #2388FF; padding: 20px; border-radius: 8px; margin: 20px 0; text-align: center;">
                        <h2 style="margin-top: 0; color: white;">%s님의 캠페인 수정이 요청되었어요!</h2>
                        <p style="font-size: 18px; margin: 15px 0; color: white;">
                            <strong style="color: white;">캠페인: %s</strong>
                        </p>
                    </div>
                    
                    <div style="background-color: #fff3e0; padding: 20px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #FF9800;">
                        <h3 style="margin-top: 0; color: #F57C00;">👏 수정 요청 사유</h3>
                        <p style="color: #666; font-style: italic; margin-bottom: 0;">"%s"</p>
                    </div>
                                            
                    <div style="background-color: #f9f9f9; padding: 20px; border-radius: 5px; margin: 20px 0;">
                        <h3 style="margin-top: 0; color: #555;">다음 단계</h3>
                        <ol style="color: #666; padding-left: 20px;">
                            <li>수정 요청 사유를 확인해주세요</li>
                            <li>해당 부분을 보완하여 다시 제출해주세요</li>
                            <li>추가 문의사항은 고객센터로 연락해주세요</li>
                        </ol>
                    </div>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="https://chkok.kr/campaigns/my" 
                           style="display: inline-block; padding: 15px 30px; background-color: #2388FF; color: white; text-decoration: none; border-radius: 5px; font-size: 16px;">
                            캠페인 수정하러 가기
                        </a>
                    </div>
                    
                    <p style="color: #666; text-align: center;">
                        빠른 시일 내에 수정해서 다시 제출해주세요. 💪
                    </p>
                    
                    <hr style="margin: 30px 0; border: none; border-top: 1px solid #ddd;">
                    <p style="font-size: 12px; color: #888; text-align: center;">
                        체험콕<br>
                        이 이메일은 발송 전용이에요.
                    </p>
                </div>
            </body>
            </html>
            """, nickname, campaignTitle, revisionReason != null ? revisionReason : "세부 사항을 보완해 주세요.");
            
        sendHtmlEmail(toEmail, subject, htmlBody);
    }



/**
     * 안전한 캠페인 수정 요청 이메일 발송 (예외를 던지지 않음)
     */


    public boolean sendCampaignRevisionRequestEmailSafe(String toEmail, String nickname, String campaignTitle, String revisionReason) {
        try {
            sendCampaignRevisionRequestEmail(toEmail, nickname, campaignTitle, revisionReason);
            return true;
        } catch (Exception e) {
            log.warn("캠페인 수정 요청 이메일 발송 실패했지만 무시함: to={}, error={}", toEmail, e.getMessage());
            return false;
        }
    }
    


/**
     * 이메일 파라미터 검증
     */


    private void validateEmailParameters(String toEmail, String subject, String body) {
        if (toEmail == null || toEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("수신자 이메일은 필수입니다.");
        }
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일 제목은 필수입니다.");
        }
        if (body == null || body.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일 내용은 필수입니다.");
        }
        if (!isValidEmailFormat(toEmail)) {
            throw new IllegalArgumentException("올바르지 않은 이메일 형식입니다: " + toEmail);
        }
    }
    


    /**
     * 이메일 형식 검증
     */

    private boolean isValidEmailFormat(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }



    /**
     * SES 연결 테스트
     */


    public boolean testConnection() {
        try {
            GetSendQuotaResult quota = sesClient.getSendQuota();
            log.info("SES 연결 테스트 성공 - 일일 발송 한도: {}, 초당 발송율: {}", 
                    quota.getMax24HourSend(), quota.getMaxSendRate());
            return true;
        } catch (Exception e) {
            log.error("SES 연결 테스트 실패: {}", e.getMessage());
            return false;
        }
    }
}
