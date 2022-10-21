/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.utils;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.JsonKeysetWriter;
import com.google.crypto.tink.KeyTemplates;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TinkHelper {

    private static KeysetHandle keysetHandle;

    private static Aead aead;

    private static void register() {
        try {
            AeadConfig.register();

            String keysetFilename = "./keyset.json";
            File keysetFile = new File(keysetFilename);

            if (!keysetFile.exists()) {
                generateKeysetFile(keysetFilename);
            }

            keysetHandle = CleartextKeysetHandle.read(JsonKeysetReader.withFile(keysetFile));

            aead = keysetHandle.getPrimitive(Aead.class);
        } catch (GeneralSecurityException | IOException ex) {
            Logger.getLogger(TinkHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void generateKeysetFile(String keysetFilename) throws IOException, GeneralSecurityException {
        // Generate the key material...
        KeysetHandle keysetHandle1 = KeysetHandle.generateNew(KeyTemplates.get("AES128_GCM"));
        CleartextKeysetHandle.write(keysetHandle1, JsonKeysetWriter.withFile(new File(keysetFilename)));
    }

    public static byte[] encryptBytes(byte[] plaintext, byte[] aad) {
        try {
            if (keysetHandle == null || aead == null) {
                register();
            }

            if (aad == null) {
                return plaintext;
            }

            return aead.encrypt(plaintext, aad);
        } catch (GeneralSecurityException ex) {
            Logger.getLogger(TinkHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new byte[1];
    }

    public static byte[] decryptBytes(byte[] plaintext, byte[] aad) {
        try {
            if (keysetHandle == null || aead == null) {
                register();
            }

            if (aad == null) {
                return plaintext;
            }

            return aead.decrypt(plaintext, aad);
        } catch (GeneralSecurityException ex) {
            Logger.getLogger(TinkHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new byte[1];
    }

    private TinkHelper() {
    }

}
