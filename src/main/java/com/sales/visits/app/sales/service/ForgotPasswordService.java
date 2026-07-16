package com.sales.visits.app.sales.service;

import com.sales.visits.app.sales.dto.request.ForgetPasswordRequest;
import com.sales.visits.app.sales.dto.request.ResetPasswordRequest;
import com.sales.visits.app.sales.exception.EntityNotFoundException;
import com.sales.visits.app.sales.exception.InvalidOtpException;
import com.sales.visits.app.sales.exception.PasswordResetNotAllowedException;
import com.sales.visits.app.sales.model.entity.User;
import com.sales.visits.app.sales.model.enums.UserRole;
import com.sales.visits.app.sales.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Slf4j
public class ForgotPasswordService {
    private static final Set<UserRole> ALLOWED_ROLES = Set.of(
            UserRole.ADMIN, UserRole.SALES_REP, UserRole.TEAM_LEADER
    );

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public ForgotPasswordService(UserRepository userRepository, OtpService otpService, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.otpService = otpService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    public void requestOtp(ForgetPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("No account found with this email."));
        checkRoles(user);
        String otp = otpService.generateAndStoreOtp(email);
        emailService.sendOtpEmail(email, otp);
        log.info("Password reset OTP issued for {}", email);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (!otpService.isValidOtp(email, request.getOtp())) {
            throw new InvalidOtpException("Invalid or expired code. Please request a new one.");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("No account found with this email."));
        checkRoles(user);
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        otpService.invalidateOtp(email);
        log.info("Password reset completed for {}", email);
    }

    private void checkRoles(User user) {
        if (!ALLOWED_ROLES.contains(user.getRole())) {
            throw new PasswordResetNotAllowedException("Password reset is not available for this account.");
        }
    }
}
