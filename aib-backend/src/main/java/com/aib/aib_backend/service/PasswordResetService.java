package com.aib.aib_backend.service;




public interface PasswordResetService {


     String forgotPassword(String email) ;

     String validateResetToken(String token) ;

     String resetPassword(String token, String newPassword, String confirmPassword);

    // Cleanup expired tokens (can be called by a scheduled task)
     void cleanupExpiredTokens() ;
}