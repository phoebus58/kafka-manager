package kafka.manager.utils

import play.api.libs.json.JsNumber


object JsonHelpers {
  implicit class LongWithToJsNumber(x: Long) {
    def toJsNumber = JsNumber(BigDecimal(x))
  }
}
