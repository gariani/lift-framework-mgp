package code.snippet

import java.sql.Time

import code.dao.TipoTarefaDAO
import net.liftweb.http.js.JsCmds
import net.liftweb.http.{StatefulSnippet, SHtml}
import net.liftweb.util.{Helpers}
import scala.xml.{NodeSeq}
import net.liftweb._
import util.Helpers._

/**
  * Created by daniel on 03/04/16.
  */
class TipoTarefa extends StatefulSnippet {

  private var descricao: Option[String] = None
  private var estimativa: Option[String] =  None

  def dispatch = { case "render" => render}

  def render = {
    "#descricaoTipoTarefa" #> listaTipoTarefa
    //"#adicionar [onClick]" #> SHtml.onEvent(testFunction)
  }

  def listaTipoTarefa = {

    var tipoTarefaDao = new TipoTarefaDAO
    val lista = tipoTarefaDao.findAll()

      ".listaTipoTarefa *" #> lista.map( tt =>
            ".descricao *" #> tt.nomeTipoTarefa &
            ".estimativa *" #> tt.estimativa.toString &
            ".foraUso *" #> tt.estimativa.toString &
            "#editar [onclick]" #> SHtml.ajaxInvoke(() => editar(tt.idTipoTarefa)) &
            "#deletar [onclick]" #> SHtml.ajaxInvoke(() => deletar(tt.idTipoTarefa)))
  }

  def editar(idTipoTarefa: Long) = {
    println("editar")

  }

  def deletar(idTipoTarefa: Long) = {
    println("deletar")
  }

}
