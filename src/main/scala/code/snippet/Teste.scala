package code.snippet


import net.liftweb._
import net.liftweb.json.JValue
import json._
import scala.xml._
import net.liftweb.json.JsonDSL._
import net.liftweb._
import net.liftweb.http.{S, SHtml, StatefulSnippet}
import net.liftweb.http.js.{JsCmds, JsCmd}
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE._
import net.liftweb.util.Helpers._

import net.liftmodules.extras._

class Teste extends StatefulSnippet {

  private var texto: String = ""
  private var error: String = "teste"

  val koModule = KoModule("app.views.notice.Teste", "forms-test-ajax")


  def dispatch = {
    case "render" => render
    case "resultado" => resultado
  }

  def render(in: NodeSeq): NodeSeq = {

    val opts: JValue = ("alertid" -> "texto")
    val bindNoticeId = Call("$('#ajaxerr').bsFormAlerts", opts)

    S.appendJs(koModule.init() & bindNoticeId)

    "#texto" #> SHtml.ajaxText(texto, (s) => texto = s) &
      "#cadastrar" #> SHtml.ajaxSubmit("Cadastrar", () => process)
  }.apply(in)

  def process: JsCmd = {
    S.error("ajaxerr", error + " (ajaxerr)")
  }

  def resultado = {
    "#result" #> Text(texto)
  }

}