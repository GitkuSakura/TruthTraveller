// 文件: main/SaveEncryptor.java

package main;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SaveEncryptor {

    // 定义一个固定的加密密钥。这个密钥是保密的，不要泄露给玩家。
    // 你可以随意更改这个字符串，越复杂越好。
    private static final String SECRET_KEY = "MS.TruthTraveller.net";

    /**
     * 加密字符串。
     * @param plainText 普通的文本数据（例如 "Player:100,200,10"）
     * @return 加密并经过Base64编码的字符串
     */
    public static String encrypt(String plainText) {
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        byte[] textBytes = plainText.getBytes(StandardCharsets.UTF_8);
        byte[] resultBytes = new byte[textBytes.length];

        // 核心的XOR加密逻辑
        for (int i = 0; i < textBytes.length; i++) {
            resultBytes[i] = (byte) (textBytes[i] ^ keyBytes[i % keyBytes.length]);
        }

        // 使用Base64编码，使加密后的二进制数据能以文本形式安全保存
        return Base64.getEncoder().encodeToString(resultBytes);
    }

    /**
     * 解密字符串。
     * @param encryptedText 加密后的文本数据
     * @return 解密后的原始文本数据
     */
    public static String decrypt(String encryptedText) {
        try {
            byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);

            // 先用Base64解码
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] resultBytes = new byte[decodedBytes.length];

            // 核心的XOR解密逻辑（与加密完全相同）
            for (int i = 0; i < decodedBytes.length; i++) {
                resultBytes[i] = (byte) (decodedBytes[i] ^ keyBytes[i % keyBytes.length]);
            }

            return new String(resultBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            // 如果解码失败（比如存档被损坏或格式不对），返回null
            System.err.println("存档解密失败：可能是文件损坏或格式不正确。");
            return null;
        }
    }
}