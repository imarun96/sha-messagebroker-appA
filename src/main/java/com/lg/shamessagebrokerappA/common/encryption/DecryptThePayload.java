package com.lg.shamessagebrokerappA.common.encryption;

import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DecryptThePayload {
    private static final String KEY = "aesEncryptionKey";
    private static final String INITVECTOR = "encryptionIntVec";
    private static final Logger log = LoggerFactory.getLogger(DecryptThePayload.class);

    /*
     * Decrypt the message once the consumer consumed it.
     * 
     * @param encypted The encrypted message from the queue
     * 
     * @return The original string
     */

    public static String decrypt(String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(INITVECTOR.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec skeySpec = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));
            return new String(original);
        } catch (Exception ex) {
            log.info("Problem occured in Decrypting the Payload. Check the log trace for more information. {}",
                    ex.getMessage());
        }
        return StringUtils.EMPTY;
    }
}