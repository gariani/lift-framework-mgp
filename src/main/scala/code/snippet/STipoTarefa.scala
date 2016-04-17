package code.snippet

import java.sql.Time

import code.dao.TipoTarefaDAO
import code.model.TipoTarefa
import net.liftweb.common.{Full, Empty, Box}
import net.liftweb.http.js.JE.ValById
import net.liftweb.http.js.{JsCmd, JsCmds}
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.http.js.jquery.JqJsCmds
import net.liftweb.http.{RequestVar, StatefulSnippet, SHtml}
import net.liftweb._
import org.joda.time.DateTime
import util.Helpers._
import scala.xml.{NodeSeq, Text, Group}

/**
  * Created by daniel on 03/04/16.
  */

object tipoTarefaSelecioada extends RequestVar[Option[TipoTarefa]](Empty)

class STipoTarefa extends StatefulSnippet {

  private var descricao: Option[String] = None
  private var estimativa: Option[String] = None
  private var flForaUso: Boolean = false;

  def dispatch = {
    case "render" => render
  }

  def render = {
    "#descricaoTipoTarefa" #> listaTipoTarefa
  }

  def listaTipoTarefa = {

    var tipoTarefaDao = new TipoTarefaDAO
    val lista = tipoTarefaDao.findAll()

    ".listaTipoTarefa *" #> lista.map(tt =>
      ".id *" #> tt.idTipoTarefa &
        ".descricao *" #> tt.nomeTipoTarefa &
        ".estimativa *" #> tt.estimativa.toString &
        "#foraUso *" #> SHtml.ajaxCheckbox(retornaForaUso(tt.idTipoTarefa), (v) => setForaUso(tt, v)) &
        "#editar" #> link("/sistema/tarefa/tipo_tarefa/editar", () => tipoTarefaSelecioada.set(Some(tt)), <i class="fa fa-pencil-square-o"></i>) &
        "#deletar [onclick]" #> SHtml.ajaxButton("Detele", () => deletar(tt.idTipoTarefa)))
  }

  private def retornaForaUso(idTipoTarefa: Long): Boolean = {
    TipoTarefa.getForaUso(idTipoTarefa) match {
      case Some(f) => f
      case None => false
    }
  }

  private def setForaUso(tipoTarefa: TipoTarefa, v: Boolean) = {
    flForaUso = TipoTarefa.foraUsoTipoTarefa(tipoTarefa.idTipoTarefa, v)
    JsCmds.Noop
  }

  private def deletar(idTipoTarefa: Long) = {
    var tipoTarefaDAO = new TipoTarefaDAO
    tipoTarefaDAO.delete(idTipoTarefa)
    JsCmds.Noop
  }


}
