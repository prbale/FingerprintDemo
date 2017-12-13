package com.bale.fingerprint

import android.Manifest
import android.annotation.TargetApi
import android.app.KeyguardManager
import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.support.design.widget.Snackbar
import android.support.v4.view.ViewCompat.onInitializeAccessibilityNodeInfo
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.android.synthetic.main.activity_fingerprint.activity_fingerprint
import kotlinx.android.synthetic.main.activity_fingerprint.errorText
import kotlinx.android.synthetic.main.activity_fingerprint.icon
import java.io.IOException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.UnrecoverableKeyException
import java.security.cert.CertificateException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey

/**
 * This activity can be used as a Authentication screen. It will validate the fingerprint and
 * Move to the appropriate screen.
 */
class FingerprintActivity : AppCompatActivity(), FingerprintHandler.AuthenticationCallback {

  private var keyStore: KeyStore? = null
  private var cipher: Cipher? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_fingerprint)

    // Initializing both Android Keyguard Manager and Fingerprint Manager
    val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    val fingerprintManager = getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager

    if (!fingerprintManager.isHardwareDetected) {

      // An error message will be displayed if the device does not contain the fingerprint hardware.
      // However if you plan to implement a default authentication method
      errorText.setText(R.string.error_message_fingerprint_sensor_missing)

    } else {

      if (!isPermissionGranted(Manifest.permission.USE_FINGERPRINT)) {
        errorText.setText(R.string.error_message_fingerprint_authenticaion_not_enabled)
      } else {
        // Check whether at least one fingerprint is registered
        if (!fingerprintManager.hasEnrolledFingerprints()) {
          errorText.setText(R.string.error_message_register_atleast_one_finger)
        }
        else {
          // Checks whether lock screen security is enabled or not
          if (!keyguardManager.isKeyguardSecure) {
            errorText.setText(R.string.error_message_lock_screen_security_not_enabled)
          }
          else {
            generateKey()
            if (cipherInit()) {
              val cryptoObject = FingerprintManager.CryptoObject(cipher)
              val helper = FingerprintHandler(this)
              helper.startAuth(fingerprintManager, cryptoObject, this)
            }
          }
        }
      }
    }
  }

  /**
   * Generates the encryption key which is then stored securely on the device.
   */
  @TargetApi(Build.VERSION_CODES.M)
  private fun generateKey() {
    try {
      keyStore = KeyStore.getInstance("AndroidKeyStore")
    } catch (e: Exception) {
      e.printStackTrace()
    }

    val keyGenerator: KeyGenerator
    try {
      keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
    } catch (e: NoSuchAlgorithmException) {
      throw RuntimeException("Failed to get KeyGenerator instance", e)
    } catch (e: NoSuchProviderException) {
      throw RuntimeException("Failed to get KeyGenerator instance", e)
    }

    try {
      keyStore!!.load(null)
      keyGenerator.init(KeyGenParameterSpec.Builder(KEY_NAME,
          KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT).setBlockModes(
          KeyProperties.BLOCK_MODE_CBC)
          .setUserAuthenticationRequired(true)
          .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
          .build())
      keyGenerator.generateKey()
    } catch (e: NoSuchAlgorithmException) {
      throw RuntimeException(e)
    } catch (e: InvalidAlgorithmParameterException) {
      throw RuntimeException(e)
    } catch (e: CertificateException) {
      throw RuntimeException(e)
    } catch (e: IOException) {
      throw RuntimeException(e)
    }

  }

  /**
   * Function that initializes the cipher that will used to create the
   * encrypted FingerprintManager.
   *
   * @return boolean when cipher generated successfully else false
   */
  @TargetApi(Build.VERSION_CODES.M)
  private fun cipherInit(): Boolean {
    try {
      cipher = Cipher.getInstance("${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}")
    } catch (e: NoSuchAlgorithmException) {
      throw RuntimeException("Failed to get Cipher", e)
    } catch (e: NoSuchPaddingException) {
      throw RuntimeException("Failed to get Cipher", e)
    }

    try {
      keyStore!!.load(null)
      val key = keyStore!!.getKey(KEY_NAME, null) as SecretKey
      cipher!!.init(Cipher.ENCRYPT_MODE, key)
      return true
    } catch (e: KeyPermanentlyInvalidatedException) {
      return false
    } catch (e: KeyStoreException) {
      throw RuntimeException("Failed to init Cipher", e)
    } catch (e: CertificateException) {
      throw RuntimeException("Failed to init Cipher", e)
    } catch (e: UnrecoverableKeyException) {
      throw RuntimeException("Failed to init Cipher", e)
    } catch (e: IOException) {
      throw RuntimeException("Failed to init Cipher", e)
    } catch (e: NoSuchAlgorithmException) {
      throw RuntimeException("Failed to init Cipher", e)
    } catch (e: InvalidKeyException) {
      throw RuntimeException("Failed to init Cipher", e)
    }

  }

  companion object {
    // Variable used for storing the key in the Android Keystore container
    private val KEY_NAME = "androidHive"
  }

  override fun onAuthenticationSucceeded() {
    icon.setImageResource(R.drawable.ic_fingerprint_success)
    errorText.text = ""
  }

  override fun onAuthenticationFailed(errorMessage: String) {
    errorText.text = errorMessage
    icon.setImageResource(R.drawable.ic_fingerprint_error)

    var snack: Snackbar = activity_fingerprint.snack("Snack message", Snackbar.LENGTH_INDEFINITE) {
      action("Action") {
        performAction()
      }
    }

    if(isAccessibilityEnabled()) {
      snack.view.setOnClickListener {
        performAction()
        snack.dismiss()
      }
    }
  }

  private fun performAction() {
    val alertDilog = AlertDialog.Builder(this@FingerprintActivity).create()
    alertDilog.setTitle("Error")
    alertDilog.setMessage("Snackbar action performed")
    alertDilog.show()
  }

}