package com.emt.dms1.document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

@Component
public class EncryptionUtils {

    @Value("${app.secretKey}")
    private String secretKey;

    public byte[] encrypt(byte[] data) throws Exception {
        Key key = new SecretKeySpec(Base64.getDecoder().decode(secretKey), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    public byte[] decrypt(byte[] data) throws Exception {
        Key key = new SecretKeySpec(Base64.getDecoder().decode(secretKey), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(data);
    }


}

