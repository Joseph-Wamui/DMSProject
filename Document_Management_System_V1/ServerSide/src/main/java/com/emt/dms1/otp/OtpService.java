package com.emt.dms1.otp;//package com.example.demo.Otp;
//
//import com.example.demo.User.User;
//import com.example.demo.utils.EmailNotificationService;
//import com.example.demo.utils.EmailUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import javax.mail.MessagingException;
//
//@Service
//@Slf4j
//public class OtpService {
//
//
//
//    private OtpGenerator otpGenerator;
//    private EmailUtil emailUtil;
//    private User   userModel;
//    private EmailNotificationService emailNotificationService;
//
//    public OtpService(OtpGenerator otpGenerator, EmailUtil emailUtil, User userModel, EmailNotificationService emailNotificationService)
//    {
//        this.otpGenerator = otpGenerator;
//      this.emailUtil= emailUtil;
//        this.userModel= userModel;
//        this.emailNotificationService = emailNotificationService;
//    }
//    public boolean generateOtp(String key) throws MessagingException {
//        // generate otp
//        Integer otpValue = otpGenerator.generateOTP(key);
//        if (otpValue == -1)
//        {
//            log.error("OTP generator is not working...");
//            return  false;
//        }
//
//       log.info("Generated OTP: {}", otpValue);
//
//        // fetch user e-mail from database
//        String userEmail = userModel.getEmailAddress();
//        emailNotificationService.sendNotification(otpValue,userEmail);
//        return  true;
//
//    }
//
////    public Boolean validateOTP(String key, Integer otpNumber)
////    {
////        // get OTP from cache
////        Integer cacheOTP = otpGenerator.getOPTByKey(key);
////        if (cacheOTP!=null && cacheOTP.equals(otpNumber))
////        {
////            otpGenerator.clearOTPFromCache(key);
////            return true;
////        }
////        return false;
////    }
//}