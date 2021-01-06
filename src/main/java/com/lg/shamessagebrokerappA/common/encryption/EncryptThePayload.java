package com.lg.shamessagebrokerappA.common.encryption;

import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncryptThePayload {

    private static final String KEY = "aesEncryptionKey";
    private static final String INITVECTOR = "encryptionIntVec";
    private static final Logger log = LoggerFactory.getLogger(EncryptThePayload.class);

    /*
     * Encrypt the message before it is sent to the queue.
     * 
     * @param messageToEncrypt The original message from the request
     * 
     * @return The encrypted string
     */

    public static String encrypt(String messageToEncrypt) {
        try {
            IvParameterSpec iv = new IvParameterSpec(INITVECTOR.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec skeySpec = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encrypted = cipher.doFinal(messageToEncrypt.getBytes());
            return Base64.encodeBase64String(encrypted);
        } catch (Exception ex) {
            log.error("Problem occured in Encrypting the Payload. Check the log trace for more information. {}",
                    ex.getMessage());
        }
        return StringUtils.EMPTY;
    }
}