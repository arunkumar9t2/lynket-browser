/*
 *
 *  Lynket
 *
 *  Copyright (C) 2022 Arunkumar
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.payments.billing;

import android.text.TextUtils;
import android.util.Base64;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import arun.com.chromer.BuildConfig;
import timber.log.Timber;

/**
 * Security-related methods. For a secure implementation, all of this code
 * should be implemented on a server that communicates with the
 * application on the device. For the sake of simplicity and clarity of this
 * example, this code is included here and is executed on the device. If you
 * must verify the purchases on the phone, you should obfuscate this code to
 * make it harder for an attacker to replace the code with stubs that treat all
 * purchases as verified.
 */
@SuppressWarnings("WeakerAccess")
public class Security {
  private static final String TAG = "IABUtil/Security";

  private static final String KEY_FACTORY_ALGORITHM = "RSA";
  private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";

  /**
   * Verifies that the data was signed with the given signature, and returns
   * the verified purchase. The data is in JSON format and signed
   * with a private key. The data also contains the {@link PurchaseState}
   * and product ID of the purchase.
   *
   * @param base64PublicKey the base64-encoded public key to use for verifying.
   * @param signedData      the signed JSON string (signed, not encrypted)
   * @param signature       the signature for the data, signed with the private key
   */
  @SuppressWarnings("JavadocReference")
  public static boolean verifyPurchase(String base64PublicKey, String signedData, String signature) {
    if (TextUtils.isEmpty(signedData) ||
      TextUtils.isEmpty(base64PublicKey) ||
      TextUtils.isEmpty(signature)) {
      Timber.tag(TAG).e("Purchase verification failed: missing data.");
      return BuildConfig.DEBUG;
    }

    PublicKey key = Security.generatePublicKey(base64PublicKey);
    return Security.verify(key, signedData, signature);
  }

  /**
   * Generates a PublicKey instance from a string containing the
   * Base64-encoded public key.
   *
   * @param encodedPublicKey Base64-encoded public key
   * @throws IllegalArgumentException if encodedPublicKey is invalid
   */
  public static PublicKey generatePublicKey(String encodedPublicKey) {
    try {
      byte[] decodedKey = Base64.decode(encodedPublicKey, Base64.DEFAULT);
      KeyFactory keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
      return keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    } catch (InvalidKeySpecException e) {
      Timber.e("Invalid key specification.");
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * Verifies that the signature from the server matches the computed
   * signature on the data.  Returns true if the data is correctly signed.
   *
   * @param publicKey  public key associated with the developer account
   * @param signedData signed data from server
   * @param signature  server signature
   * @return true if the data and signature match
   */
  public static boolean verify(PublicKey publicKey, String signedData, String signature) {
    byte[] signatureBytes;
    try {
      signatureBytes = Base64.decode(signature, Base64.DEFAULT);
    } catch (IllegalArgumentException e) {
      Timber.e("Base64 decoding failed.");
      return false;
    }
    try {
      Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
      sig.initVerify(publicKey);
      sig.update(signedData.getBytes());
      if (!sig.verify(signatureBytes)) {
        Timber.e("Signature verification failed.");
        return false;
      }
      return true;
    } catch (NoSuchAlgorithmException e) {
      Timber.e("NoSuchAlgorithmException.");
    } catch (InvalidKeyException e) {
      Timber.e("Invalid key specification.");
    } catch (SignatureException e) {
      Timber.e("Signature exception.");
    }
    return false;
  }
}
