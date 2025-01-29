package com.emt.dms1.otp;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class OtpGenerator {


    private static final Integer EXPIRE_MIN = 5;
    private LoadingCache<String, Integer> otpCache;

    public OtpGenerator()
    {
        super();
        otpCache = CacheBuilder.newBuilder()
                .expireAfterWrite(EXPIRE_MIN, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Integer>() {
                    @Override
                    public Integer load(String s) throws Exception {
                        return 0;
                    }
                });
    }


    public Integer generateOTP(String emailAddress)
    {
        Random random = new Random();
        int OTP = 1234;
//                100000 + random.nextInt(900000);
        otpCache.put(emailAddress, OTP);

        return OTP;
    }
    public String getEmailByOtp(Integer otpValue) {
        String userEmail = null;
        // Iterate over the cache entries to find the email address associated with the provided OTP value
        for (Map.Entry<String, Integer> entry : otpCache.asMap().entrySet()) {
            if (entry.getValue().equals(otpValue)) {
                userEmail = entry.getKey();
                break;
            }
        }
        return userEmail;
    }


    public Integer getOPTByKey(String emailAddress)
    {
        return otpCache.getIfPresent(emailAddress);
    }
    public void clearOTPFromCache(String emailAddress) {
        otpCache.invalidate(emailAddress);
    }
}