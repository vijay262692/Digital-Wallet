
import com.digitalwallet.common.CryptoUtil;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class EncryptTest {
    public static void main(String[] args) throws Exception {
        // Paste the public key you got from GE T /api/wallet/publicKey
        String publicKeyBase64 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAo1xcmYaZcI7x3c827BTNqv7/kPbS8gS6KdxP54XNoJvLZy+W31CY+EPhNqXK+GV8Rh9aUeKnJVFVrf6B845lflaBXd4kdBq4fkEYQ4XWXsHyNpV/87bGC83jCMdBfXtMC9T5rGxBi6MkeP9Mk1KKvYSMygqH3sMSVgDs1NtOAYcC//CaJ2Z2s8d7FOGM7rBfcwURr2BenjmnSh1yhbPf1nXwpjxmuE0XBVMbPKcmWFGlAdljH8urFBqU+hpLJejACjbQzmGR6NFVn0CF2fzhDmhB169ZySfDRb/ExvarS707uDpNsbE5zKmIcTDLxfLA/sT6GisIvFmx0eemT8677wIDAQAB";

        // Card data (format: PAN|MM/YY|CVV)
  //      String cardData = "5454123169699999|12/26|123";
        
        String cardData = "4123456789012345|12/26|123"; // VTS

        

        byte[] decodedKey = Base64.getDecoder().decode(publicKeyBase64);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);

        String encryptedBase64 = CryptoUtil.encrypt(cardData, publicKey);
        System.out.println("🔐 Encrypted Base64 payload:");
        System.out.println(encryptedBase64);
    }
}
