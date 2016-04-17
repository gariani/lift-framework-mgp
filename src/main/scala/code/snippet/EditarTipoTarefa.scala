package code.snippet

import java.sql.Time
import java.text.SimpleDateFormat

import code.lib.GenericSnippet
import code.model.TipoTarefa
import net.liftweb.common.{Logger, Full, Empty, Box}
import net.liftweb.http.js.jquery.JqJsCmds.DisplayMessage
import net.liftweb.http.{S, RequestVar, SHtml, StatefulSnippet}
import net.liftweb.http.js.{JsCmds, JsCmd}
import net.liftweb.util
import net.liftweb.util.FieldError
import util.Helpers._
import org.joda.time.DateTime
import scala.xml.{NodeSeq, Text}

/**
  * Created by daniel on 09/04/16.
  */
class EditarTipoTarefa extends StatefulSnippet with Logger {


  private var idTipoTarefa: Long = 0
  private var descricao: String = ""
  private var estimativa: Box[Time] = Empty
  private var foraUso: Boolean = false;
  private var estimativaHora: String = "0"
  private var estimativaMin: String = "0"
  private lazy val internvaloMinuto = intervaloMin
  private lazy val internvaloHora = intervaloHora

  private def intervaloHora = {
    (0 to 23).toList.map(i => (formataNum(i), formataNum(i)))
  }

  private def intervaloMin = {
    (0 to 59).toList.map(i => (formataNum(i), formataNum(i)))
  }

  private def formataNum(i: Int): String = {
    i.toString.length match {
      case 1 => "0" + i.toString
      case _ => i.toString
    }
  }

  //inicia os valores vindo de STipoTarefa
  setValores


  def dispatch = {
    case "render" => render
  }

  def render = {
    "#descricao" #> SHtml.text(descricao, descricao = _) &
      "#hora" #> SHtml.ajaxSelect(internvaloHora, Full(estimativaHora), v => estimativaHora = v, "style" -> "width:70px;") &
      "#min" #> SHtml.ajaxSelect(internvaloMinuto, Full(estimativaMin), v => estimativaMin = v, "style" -> "width:70px;") &
      "type=submit" #> SHtml.ajaxOnSubmit(salvar) &
      "#cancelar" #> link("/sistema/tarefa/tipo_tarefa/tipo_tarefa", () => JsCmds.Noop, Text("Cancelar"))
  }

  private def salvar() = {

    try {
      var formatar = new SimpleDateFormat("HH:mm")
      formatar.format(new java.util.Date())
      var d1 = formatar.parse(estimativaHora + ":" + estimativaMin)
      var ppstime = new java.sql.Time(d1.getTime)

      estimativa = Full(ppstime)
    } catch {
      case e: Exception => _logger.info("EditarTipoTarefa: erro na conversao da estimativa")
        S.error("error", Util.mensagemErro("Erro ao salvar estimativa"))
    }

    try {

      val tipoTarefa = new TipoTarefa(idTipoTarefa,
        descricao,
        estimativa,
        foraUso,
        DateTime.now,
        None)

      TipoTarefa.save(tipoTarefa)
    }
    catch {
      case e: Exception => {
        _logger.info("Erro ao salvar tipo de tarefa"+e.getMessage)
        S.error("error", Util.mensagemErro("Erro ao salvar tipo de tarefa"))
      }
    }
    redirectTo("/sistema/tarefa/tipo_tarefa/tipo_tarefa")
  }

  private def setValores = {
    var tipoTarefa: Option[TipoTarefa] = None
    tipoTarefa = tipoTarefaSelecioada.get
    tipoTarefa match {
      case Some(tt) => {
        idTipoTarefa = tt.idTipoTarefa
        descricao = tt.nomeTipoTarefa
        tt.estimativa match {
          case Some(e) => formataHoraMin(Some(e))
          case None => Empty
        }
      }
      case None => {
        descricao = ""
      }
    }
  }

  private def formataHoraMin(horaMin: Option[Time]) = {
    var min = new SimpleDateFormat("mm")
    var hora = new SimpleDateFormat("HH")

    horaMin match {
      case Some(hm) => {
        estimativaHora = hora.format(hm)
        estimativaMin = min.format(hm)
      }
      case None => {
        estimativaHora = "00"
        estimativaMin = "00"
      }
    }
  }

}
