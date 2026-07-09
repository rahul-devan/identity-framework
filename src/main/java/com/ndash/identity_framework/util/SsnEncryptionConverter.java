package com.ndash.identity_framework.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Converter
public class SsnEncryptionConverter implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final String PREFIX = "enc:";

    private final SecretKeySpec secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public SsnEncryptionConverter() {
        final String encryptionKey = System.getenv().getOrDefault(
                "SSN_ENCRYPTION_KEY",
                "change-me-ssn-key-32chars-min!!"
        );
        try {
            final byte[] key = MessageDigest.getInstance("SHA-256")
                    .digest(encryptionKey.getBytes(StandardCharsets.UTF_8));
            this.secretKey = new SecretKeySpec(key, "AES");
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to initialize SSN encryption", ex);
        }
    }

    @Override
    public String convertToDatabaseColumn(final String attribute) {
        if (attribute == null || attribute.isBlank()) {
            return attribute;
        }
        if (attribute.startsWith(PREFIX)) {
            return attribute;
        }
        try {
            final byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            final Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            final byte[] cipherText = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));

            final ByteBuffer buffer = ByteBuffer.allocate(iv.length + cipherText.length);
            buffer.put(iv);
            buffer.put(cipherText);
            return PREFIX + Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to encrypt SSN", ex);
        }
    }

    @Override
    public String convertToEntityAttribute(final String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return dbData;
        }
        if (!dbData.startsWith(PREFIX)) {
            return dbData;
        }
        try {
            final byte[] decoded = Base64.getDecoder().decode(dbData.substring(PREFIX.length()));
            final ByteBuffer buffer = ByteBuffer.wrap(decoded);
            final byte[] iv = new byte[GCM_IV_LENGTH];
            buffer.get(iv);
            final byte[] cipherText = new byte[buffer.remaining()];
            buffer.get(cipherText);

            final Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return dbData;
        }
    }
}
