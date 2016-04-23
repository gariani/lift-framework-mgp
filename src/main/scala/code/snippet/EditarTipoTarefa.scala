package code.snippet

import java.sql.Time
import java.text.SimpleDateFormat

import code.lib.Util._
import code.model.TipoTarefa
import net.liftweb.common.{Logger, Full, Empty, Box}
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.http.{S, SHtml, StatefulSnippet}
import net.liftweb.http.js.{JsCmds}
import net.liftweb.util
import util.Helpers._
import org.joda.time.DateTime
import scala.xml.{Text}

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
        SetHtml("mensagem", mensagemErro("Erro ao salvar estimativa"))
    }

    try {

      val tipoTarefa = new TipoTarefa(idTipoTarefa,
        descricao,
        estimativa,
        foraUso,
        DateTime.now,
        None)

      TipoTarefa.save(tipoTarefa)
      redirectTo("/sistema/tarefa/tipo_tarefa/tipo_tarefa")
    }
    catch {
      case e: Exception => {
        _logger.info("Erro ao salvar tipo de tarefa: "+e.getMessage)
        SetHtml("mensagem", mensagemErro("Erro ao salvar tipo de tarefa"))
      }
    }
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
