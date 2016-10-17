package models.messaging

import java.security.{KeyFactory, PublicKey, Security}
import java.util.Base64

import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECPublicKeySpec
import play.api.libs.functional.syntax._
import play.api.libs.json.{Reads, _}

object Subscription {
  implicit val reads: Reads[Subscription] =
    ((__ \ "endpoint").read[String]
      and (__ \ "keys" \ "p256dh").read[String]
      and (__ \ "keys" \ "auth").read[String]) (Subscription.apply _)

  implicit val writes = new Writes[Subscription] {
    override def writes(o: Subscription) = Json.obj(
      "endpoint" -> o.endpoint,
      "keys" -> Json.obj(
        "p256dh" -> o.key,
        "auth" -> o.auth
      )
    )
  }
}

// Source: https://github.com/MartijnDwars/web-push/blob/master/doc/UsageExample.md
case class Subscription(
  endpoint: String,
  key: String,
  auth: String
) {
  if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
    Security.addProvider(new BouncyCastleProvider)
  }

  def decodeUrlSafeBase64(encoded: String): Array[Byte] =
    Base64.getDecoder.decode(encoded.replace('-', '+').replace('_', '/'))

  def keyAsBytes: Array[Byte] = decodeUrlSafeBase64(key)

  def authAsBytes: Array[Byte] = decodeUrlSafeBase64(auth)

  def publicKey: PublicKey = {
    val keyFactory = KeyFactory.getInstance("ECDH", BouncyCastleProvider.PROVIDER_NAME)
    val curveParameterSpec = ECNamedCurveTable.getParameterSpec("secp256r1")
    val point = curveParameterSpec.getCurve.decodePoint(keyAsBytes)
    val publicKeySpec = new ECPublicKeySpec(point, curveParameterSpec)

    keyFactory.generatePublic(publicKeySpec)
  }
}
