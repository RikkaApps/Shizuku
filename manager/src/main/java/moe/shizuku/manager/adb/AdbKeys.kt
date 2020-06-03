package moe.shizuku.manager.adb

import android.annotation.SuppressLint
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi
import rikka.core.ktx.unsafeLazy
import java.io.ByteArrayInputStream
import java.net.Socket
import java.security.KeyFactory
import java.security.Principal
import java.security.PrivateKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import javax.net.ssl.SSLEngine
import javax.net.ssl.X509ExtendedKeyManager
import javax.net.ssl.X509ExtendedTrustManager

/*
Generate the key:

$ adb keygen adbkey
$ openssl req -x509 -key adbkey -keyform pem -out adbkey.cer -outform pem

adbPublicKeyBase64 is adbkey.pub ends with \u0000
privateKey is adbkey (remove \n, -----BEGIN, -----END)
certificate is adbkey.cer (remove \n, -----BEGIN, -----END)
*/

val adbPublicKeyBase64Bytes = "QAAAANfX4XkZ9G+neAd/akZYRWgeFgx0g9yxQK1pUb+6nskHyIRCJSWTaPT9bDOHvhJCYAMP/LlsBXB/6Oh0t9X5cuiJMc6abQooFRumhHGPFiP5RSXspaoSuBRjlAmlOju9SezZJsmqZRSvt/qRZjv/ZN7urIvxZyTsrwSLW6Zyk17w1VHXA9yZDtxMUHofOECJ/H07jtgd6FtpsevvlJh4VTYkEw/N6hQscVLwJpZ3cKT36Hi9oajpnm0sN7uSro0mGYxCEQNG9N+0iXto7TyE2SIK7tvn341XoX/Hej4NcpJ+3gaGPGtwn5cchsMcuOIY66X6YIak7rh2NClcnQuT6e8VU8+qqlnyNrTELLDaNrjSMq+tk3ZY5rKuul1TjSedChz0h+pMp477cQZcjKbPGL/LAnUxLj8OBaD727WvAZphLLOziIqAeVxeGPJ2kiv1MmH7WajqvUm41YC6HdueKoKMwp76yr2hjh5vz4XwH7PvG/eLbj6EcuSDr1gX6s+kHuCEUnNJt9pDpI4hg9V8sVUbwapxc2fsvs/PKR0S9og/tbnWMRSvEsblh4OfCvbnPHhmg3Mqi+QDBtbnB6Hd7ljiQCiltEBcgUj1vQG1Huoch8qRkI3Rgbhu8VG55TNIwEXlWRTzOqm3xaQ9x9i8fSow6fwtvdbeTVMvvf1/mVHhQQFaBgEAAQA= shizuku\u0000".toByteArray()

private val privateKeyBytes = Base64.decode(
        "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCqz1MV7+mTC51c" +
                "KTR2uO6khmD6pesY4rgcw4Ycl59wazyGBt5+knINPnrHf6FXjd/n2+4KItmEPO1o" +
                "e4m03/RGAxFCjBkmja6SuzcsbZ7pqKG9eOj3pHB3libwUnEsFOrNDxMkNlV4mJTv" +
                "67FpW+gd2I47ffyJQDgfelBM3A6Z3APXUdXwXpNypluLBK/sJGfxi6zu3mT/O2aR" +
                "+revFGWqySbZ7Em9OzqlCZRjFLgSqqXsJUX5IxaPcYSmGxUoCm2azjGJ6HL51bd0" +
                "6Oh/cAVsufwPA2BCEr6HM2z99GiTJSVChMgHyZ66v1FprUCx3IN0DBYeaEVYRmp/" +
                "B3inb/QZAgMBAAECggEAO2YuwA22vdaHqgGAR5sHDjrW5cMmLvjEyufpru7BWOBJ" +
                "41fnLr/xno5wNwk4y9BAIYq3TFoTJ2viqXdwi3BoK6KscWZz5pbVsxwc0zvpy9rD" +
                "KDcwWNIb9FJIh0E8Lva1Mos//lNQxMfW31ooz3lRDnP8/k5RME+fVL79xsSt5S3t" +
                "9V6ogDyAuvPWUiN6963i14Gx0oHQUVCGD7esOTcxmTKEAIj9eLvjhQ6R0SmW5cqv" +
                "5PO0IMaLakbhmZ5aEoqekhetPTjKkpwk0P1HF7VuiOZXobcJ9Wz1SPcouiEcYjVW" +
                "CA/MZONtIBWVzDxDog9zL/xZmUWpsZAyZ9yk3tCyEwKBgQDo5kVTtjmORHPRVDFS" +
                "kQ0AXE9dy3ls+otcHFYapGzmg/xkV/1g2C03wnO2jpApTQoSYi2Wsm4PRSdofl/V" +
                "QGAF+/lrh/pblb9fitq0EDgFiqVUCvpICbWMPlFypSrIKt4chBECBjwVWR6s6p+A" +
                "gE4HJKSCN8Dd4Iqz+NH/bHVd7wKBgQC7wH0F4ytciy5uZvLwUVZhfBoVubQ05IwD" +
                "YVTiULPglH3y1fztmrfMDCPVQze1SljMBXFC7lH/l5cHEkK1dmXmjFKJiBOqz4mO" +
                "/hlqCBuBQ4sD9IbcMshFL0XOjA5CMGXPJ6BbU5fhwUFSoN/00LwmSO2yIe/8JzCm" +
                "4cn3yN1WdwKBgEFjZVN7de1biqja2n4z+1J+mEndNtpB/Z5+1i3kvC58IACMM7dc" +
                "+lUNYy9+FzuBRbKjnekRb5UZ9VaBJKbazBriA8UNCF1uTaylb4Hei9wCcSiHbH2N" +
                "PEuDs5pchQZuAq4+5geLeJMF0nb56I5Ld5zJzOZCgQPKZybgVRVBfjJBAoGAcli3" +
                "TCCgxgoWSzLz/pzrCRA2KskhEZ2oEF70/ai9BKgrVPwywNsb2XADWt1HTmhrUEZ4" +
                "RpUy54GBf9MFQdNMGG+ZzR1NLRic5LOo8sThS4bBYbVJdU6QXmJ+F1+BR5qGRvYR" +
                "Wc29kMbMa/CCtW1zSbXc3kKBEIclwF1/TiPNed0CgYAj2n9rX849nPBnBDzWQmEC" +
                "tELn/GvDafesqGh3Hj92x5TG20LE6s0tCxWQ7kLt4+bZ9t7a4oZsH/qaa/YXUFaP" +
                "rhVSDVKzRYmD285qDl8PWg71rQ1afD3oZiHwP97utf6NhHqdsOSE7GzhaXK21Uii" +
                "YesQoA28ErXQKgvyGdyNvA==", 0)

val privateKey: PrivateKey by unsafeLazy {
    KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(privateKeyBytes))
}

private val certificateBytes = Base64.decode(
        "MIIDOzCCAiOgAwIBAgIUEPBjrgbIp25NNBNSHnGP1nQ79HEwDQYJKoZIhvcNAQEL" +
                "BQAwLDELMAkGA1UEBhMCMDAxCzAJBgNVBAgMAjAwMRAwDgYDVQQKDAdTaGl6dWt1" +
                "MCAXDTIwMDYwMjA4Mjg1NVoYDzIxMjAwNTA5MDgyODU1WjAsMQswCQYDVQQGEwIw" +
                "MDELMAkGA1UECAwCMDAxEDAOBgNVBAoMB1NoaXp1a3UwggEiMA0GCSqGSIb3DQEB" +
                "AQUAA4IBDwAwggEKAoIBAQCqz1MV7+mTC51cKTR2uO6khmD6pesY4rgcw4Ycl59w" +
                "azyGBt5+knINPnrHf6FXjd/n2+4KItmEPO1oe4m03/RGAxFCjBkmja6SuzcsbZ7p" +
                "qKG9eOj3pHB3libwUnEsFOrNDxMkNlV4mJTv67FpW+gd2I47ffyJQDgfelBM3A6Z" +
                "3APXUdXwXpNypluLBK/sJGfxi6zu3mT/O2aR+revFGWqySbZ7Em9OzqlCZRjFLgS" +
                "qqXsJUX5IxaPcYSmGxUoCm2azjGJ6HL51bd06Oh/cAVsufwPA2BCEr6HM2z99GiT" +
                "JSVChMgHyZ66v1FprUCx3IN0DBYeaEVYRmp/B3inb/QZAgMBAAGjUzBRMB0GA1Ud" +
                "DgQWBBShNb2SxrzXzI79qOPmrKYNqY7u/DAfBgNVHSMEGDAWgBShNb2SxrzXzI79" +
                "qOPmrKYNqY7u/DAPBgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBAQBD" +
                "6EWA+n9rcePRnTUpn7A6uMpfPigdhagO3cMLC+NQtsHtdVeq7Z+VkBw6iBhsFRVe" +
                "mJ0kf9m92E/YZJuNVrwwRDENysbH7wL5nXaFXCjz9lnqDhsFzzcYgDOnulcJG7c4" +
                "rjGk7NB2GSSmmGY9hfvpJOX3+tumrhsKhlcccsnpiDa1MjkRvbbe6Ii0PQdAo1HA" +
                "FCfOVCQYgKS0i9eReASb2ykVimzUtLKt8rD4AAKiO7AFPOSI++2xAcePsLhm+7kD" +
                "noBvhrWBS8Sp4W3L8LfdaxPnI9+lTUBcjqioviI8tM2FELzbdTZ8qz6zSrarPgyb" +
                "BYKgm8QLyzJC4KbOOggb", 0)

@Suppress("UNCHECKED_CAST")
private val certificate by unsafeLazy {
    val inputStream = ByteArrayInputStream(certificateBytes)
    CertificateFactory.getInstance("X.509").generateCertificates(inputStream) as Collection<X509Certificate>
}

@RequiresApi(Build.VERSION_CODES.N)
object AdbKeyManager : X509ExtendedKeyManager() {

    private const val TAG = "AdbKeyManager"

    private const val alias = "key"

    override fun chooseClientAlias(keyTypes: Array<out String>, issuers: Array<out Principal>?, socket: Socket?): String? {
        Log.d(TAG, "chooseClientAlias: keyType=${keyTypes.contentToString()}, issuers=${issuers?.contentToString()}")
        for (keyType in keyTypes) {
            if (keyType == "RSA") return alias
        }
        return null
    }

    override fun getCertificateChain(alias: String?): Array<X509Certificate>? {
        Log.d(TAG, "getCertificateChain: alias=$alias")
        return if (alias == this.alias) certificate.toTypedArray() else null
    }

    override fun getPrivateKey(alias: String?): PrivateKey? {
        Log.d(TAG, "getPrivateKey: alias=$alias")
        return if (alias == this.alias) privateKey else null
    }

    override fun getClientAliases(keyType: String?, issuers: Array<out Principal>?): Array<String>? {
        return null
    }

    override fun getServerAliases(keyType: String, issuers: Array<out Principal>?): Array<String>? {
        return null
    }

    override fun chooseServerAlias(keyType: String, issuers: Array<out Principal>?, socket: Socket?): String? {
        return null
    }
}

@SuppressLint("TrustAllX509TrustManager")
@RequiresApi(Build.VERSION_CODES.N)
object AdbTrustManager : X509ExtendedTrustManager() {

    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?, socket: Socket?) {
    }

    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?, engine: SSLEngine?) {
    }

    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?, socket: Socket?) {
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?, engine: SSLEngine?) {
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return emptyArray()
    }
}