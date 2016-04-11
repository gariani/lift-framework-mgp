package code.snippet

import java.sql.Time

import code.dao.TipoTarefaDAO
import code.model.TipoTarefa
import net.liftweb.common.{Full, Empty, Box}
import net.liftweb.http.js.JsCmds
import net.liftweb.http.{RequestVar, StatefulSnippet, SHtml}
import net.liftweb._
import org.joda.time.DateTime
import util.Helpers._
import scala.xml.Text
import xml.Group

/**
  * Created by daniel on 03/04/16.
  */

object tipoTarefaSelecioada extends RequestVar[Option[TipoTarefa]](Empty)

class STipoTarefa extends StatefulSnippet {

  private var descricao: Option[String] = None
  private var estimativa: Option[String] = None

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
        ".foraUso *" #> SHtml.ajaxIn(Text(" "), () => desativarTipo(tt), defineIconeTipoTarefaForaUso(tt.deletedAt)) &
        "#editar" #> link("/sistema/tarefa/tipo_tarefa/editar", () => tipoTarefaSelecioada.set(Some(tt)), <i class="fa fa-pencil-square-o"></i>) &
        "#deletar [onclick]" #> SHtml.ajaxButton("Detele", () => deletar(tt.idTipoTarefa)))
  }

  private def defineIconeTipoTarefaForaUso(deleted: Option[DateTime]) = {
    deleted match {
      case Some(e) => "class" -> "glyphicon glyphicon-eye-close"
      case None => "class" -> "glyphicon glyphicon-eye-open"
    }
  }

  private def desativarTipo(tipoTarefa: TipoTarefa) = {
    tipoTarefa.deletedAt match {
      case Some(d) => {
        TipoTarefa.ativarDesativarTipoTarefa(tipoTarefa.idTipoTarefa, None)
        defineIconeTipoTarefaForaUso(None)
      }
      case None => {
        TipoTarefa.ativarDesativarTipoTarefa(tipoTarefa.idTipoTarefa, Some(DateTime.now))
        defineIconeTipoTarefaForaUso(Some(DateTime.now))
      }
    }
    JsCmds.Noop
  }

  private def deletar(idTipoTarefa: Long) = {
    var tipoTarefaDAO = new TipoTarefaDAO
    tipoTarefaDAO.delete(idTipoTarefa)
    JsCmds.Noop
  }


}
