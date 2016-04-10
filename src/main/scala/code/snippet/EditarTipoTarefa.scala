package code.snippet

import java.sql.Time
import java.text.SimpleDateFormat

import code.lib.GenericSnippet
import code.model.TipoTarefa
import code.snippet.STipoTarefa
import net.liftweb.common.{Full, Empty, Box}
import net.liftweb.http.js.jquery.JqJsCmds.DisplayMessage
import net.liftweb.http.{S, RequestVar, SHtml, StatefulSnippet}
import net.liftweb.http.js.{JsCmds, JsCmd}
import net.liftweb.util
import util.Helpers._
import org.joda.time.DateTime
import scala.xml.Text

/**
  * Created by daniel on 09/04/16.
  */
class EditarTipoTarefa extends StatefulSnippet {

  private var descricao: String = ""
  private var estimativa: String = ""
  private var estimativaHora: String = "0"
  private var estimativaMin: String = "0"
  private var internvaloMinuto = (0 to 59).toList.map(i=> (i.toString, i.toString))
  private var internvaloHora = (0 to 23).toList.map(i=> (i.toString, i.toString))
  private var tipoTarefa: Option[TipoTarefa] = None

  def dispatch = {
    case "render" => render
  }

  def render = {
    setValores
    "#descricao" #> SHtml.text(descricao, descricao = _) &
    "#hora" #> SHtml.select(internvaloHora, Full(estimativaHora), v => "", "style" -> "width:70px;") &
    "#min" #> SHtml.select(internvaloMinuto, Full(estimativaMin), v => "", "style" -> "width:70px;float:left;") &
    "type=submit" #> SHtml.ajaxOnSubmit(salvar) &
    "#cancelar" #> link("/sistema/tarefa/tipo_tarefa/tipo_tarefa", () => JsCmds.Noop, Text("Cancelar"))
  }

  private def setarValores(tt: TipoTarefa) = {
    tt.save()
  }

  private def validarValores(tt: TipoTarefa) : Boolean = {
    false
  }

  private def salvar() = {
    tipoTarefa match {
      case Some(tt) => {
        setarValores(tt)
        if (validarValores(tt)) S.redirectTo("/sistema/tarefa/tipo_tarefa/tipo_tarefa") else JsCmds.Noop
      }
      case None => JsCmds.Noop
    }
  }

  private def setValores = {
    tipoTarefa = tipoTarefaSelecioada.get
    tipoTarefa match {
      case Some(tt) =>
        descricao = tipoTarefa.get.nomeTipoTarefa
        tipoTarefa.get.estimativa match {
          case Some(e) => formataHoraMin(e)
          case _ => estimativa = ""
        }
      case None => JsCmds.Noop
    }
  }

  private def formataHoraMin(horaMin: Time) = {
    var min = new SimpleDateFormat("mm")
    var hora = new SimpleDateFormat("HH")
    estimativaHora = hora.format(horaMin).toString
    estimativaMin = min.format(horaMin).toString
  }

}
