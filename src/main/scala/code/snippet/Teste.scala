package code.snippet

import code.model.Tarefa
import net.liftweb.http.rest.RestHelper
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json._
import net.liftweb.util.Mailer.{From, Subject, To, PlainMailBodyType}
import net.liftweb.util._
import net.liftweb.util.Helpers._
import scala.xml._
import net.liftweb.http.js.JsCmds.Alert._
import net.liftweb.http._
import net.liftweb.http.js.JsCmds.{Replace, SetHtml, Alert, Noop}
import net.liftweb.http.js.{JsCmds, JsCmd}
import net.liftweb.common.{Empty, Box, Loggable, Full}

object Teste extends RestHelper {

  private var name: Box[String] = Empty
  private var primary: Box[String] = Empty
  private var value: Box[String] = Empty

  def salvarDescricaoTarefa(req: Req) = {
    name = req.param("name")
    primary = req.param("pk")
    value = req.param("value")

    Tarefa.saveDescricaoTarefa(name, primary, value)
    Full(JsonResponse(parse("""{"error" : "teste"}""")))
  }

  def salvarEstimativa(req: Req) = {
    name = req.param("name")
    primary = req.param("pk")
    value = req.param("value")

    Tarefa.salvarEstimativa(name, primary, value)
    Full(JsonResponse(parse("""{"error" : "teste"}""")))
  }

  def salvarDataDesejada(req: Req) = {
    name = req.param("name")
    primary = req.param("pk")
    value = req.param("value")

    Tarefa.salvarDataDesejada(name, primary, value)
    Full(JsonResponse(parse("""{"error" : "teste"}""")))
  }

  def salvarDataEntrega(req: Req) = {
    name = req.param("name")
    primary = req.param("pk")
    value = req.param("value")

    Tarefa.salvarDataEntrega(name, primary, value)
    Full(JsonResponse(parse("""{"error" : "teste"}""")))
  }

  def salvarStatus(req: Req) = {
    name = req.param("name")
    primary = req.param("pk")
    value = req.param("value")

    Tarefa.salvarStatus(name, primary, value)
    Full(JsonResponse(parse("""{"error" : "teste"}""")))
  }


  serve ( "sistema" / "api" / "tarefa" prefix {
    case Post("esforcoEstimado"  :: Nil, req) => salvarEstimativa(req)
    case Post("dtDesejada" :: nil, req) => salvarDataDesejada(req)
    case Post("dtEntrega" :: nil, req) => salvarDataEntrega(req)
    case Post("statusTarefa" :: nil, req) => salvarStatus(req)
  })


}
