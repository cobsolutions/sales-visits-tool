package com.sales.visits.app.sales.controller;

import com.sales.visits.app.sales.dto.request.ForgetPasswordRequest;
import com.sales.visits.app.sales.dto.request.ResetPasswordRequest;
import com.sales.visits.app.sales.dto.response.MessageResponse;
import com.sales.visits.app.sales.service.EmailService;
import com.sales.visits.app.sales.service.ForgotPasswordService;
import com.sales.visits.app.sales.service.OtpService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/forgot-password")
public class ForgetPasswordController {
    private final EmailService emailService;
    private final ForgotPasswordService forgotPasswordService;
    private final OtpService otpService;

    public ForgetPasswordController(EmailService emailService, ForgotPasswordService forgotPasswordService, OtpService otpService) {
        this.emailService = emailService;
        this.forgotPasswordService = forgotPasswordService;
        this.otpService = otpService;
    }

    @PostMapping("/request-otp")
    public ResponseEntity<MessageResponse> requestOtp(@Valid @RequestBody ForgetPasswordRequest request) {
        forgotPasswordService.requestOtp(request);
        return ResponseEntity.ok(new MessageResponse("A verification code has been sent to your email."));
    }

    @PostMapping("/reset")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        forgotPasswordService.resetPassword(request);
        return ResponseEntity.ok(new MessageResponse("Your password has been reset successfully."));
    }
}
