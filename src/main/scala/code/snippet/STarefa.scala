package code.snippet

import code.lib.Util._
import code.model.{TipoTarefa, Usuario, Tarefa, Cliente}
import net.liftmodules.widgets.bootstrap.{Bs3InfoDialogLg, Bs3InfoDialog, Bs3ConfirmDialogLg, Bs3ConfirmDialog}
import net.liftweb.common.{Box, Empty, Full, Logger}
import net.liftweb.http.js.JsCmds.{Alert, SetHtml}
import net.liftweb.http.js.jquery.JqJsCmds.ModalDialog
import net.liftweb.http.js.{JE, JsCmd, JsCmds}
import net.liftweb.http.{S, StatefulSnippet, SHtml}
import net.liftweb.util
import org.joda.time.DateTime
import util.Helpers._

import scala.xml.{Text, NodeSeq}

/**
  * Created by daniel on 03/04/16.
  */
class STarefa extends StatefulSnippet with Logger {

  private var nomeTarefa: String = ""
  private var idLider: String = ""
  private var descricao: String = ""
  private var idCliente: String = ""
  private var idTipoTarefa: String = ""
  private var idStatusTarefa: String = ""
  private var esforco: Option[DateTime] = None

  def dispatch = {
    case "adicionarNovaTarefa" => adicionarNovaTarefa
    case "render" => render
    case "novaTarefa" => novaTarefa
  }

  def adicionarNovaTarefa = {
    "#adicionarNovaTarefa" #> SHtml.a(() => criarNovaTarefa, Text("Nova Tarefa"), "class" -> "fa fa-tasks")
  }

  def render(node: NodeSeq): NodeSeq = {
    node
  }

  def novaTarefa = {
    "#nomeTarefa" #> SHtml.ajaxText(nomeTarefa, (s) => nomeTarefa = s, "class" -> "form-control") &
      "#responsavel" #> SHtml.ajaxSelect(selecionarUsuarios, Empty, (s) => idLider = s, "class" -> "form-control",
        "data-placeholder" -> "Selecione usuário responsável...") &
      "#tipoTarefa" #> SHtml.ajaxSelect(selecionarTipoTarefa, Empty, (s) => idTipoTarefa = s, "class" -> "form-control",
        "data-placeholder" -> "Selecione tipo de tarefa...") &
      "#cliente_projeto" #> SHtml.ajaxSelect(selecionarClienteProjeto, Empty, (s) => idCliente = s, "class" -> "form-control",
        "data-placeholder" -> "Selecione o cliente...") &
      "#editor" #> SHtml.textarea("", (s) => descricao = s, "class" -> "form-control") &
    "#cadastrar [onclick]" #> SHtml.ajaxCall(JE.JsRaw("CKEDITOR.instances.bodyText.getData()"), (x) => salvarNovaTarefa)
  }

  private def criarNovaTarefa = {
    val node = S.runTemplate("sistema" :: "tarefa" :: "tarefa-hidden" :: "_modal_nova_tarefa" :: Nil)
    node match {
      case Full(nd) => {
        var dialog = new Bs3InfoDialogLg("Incluir nova tarefa", nd)
        dialog
      }//Bs3ConfirmDialogLg("Incluir nova tarefa", nd, () => salvarNovaTarefa, () => JsCmds.Noop)
      case Empty => JsCmds.Noop
    }
  }

  private def salvarNovaTarefa: JsCmd = {
    if (nomeTarefa.isEmpty) {
      SetHtml("mensagem", mensagemSucesso(Mensagem.DADOS_SALVOS_SUCESSO))
    }
    JsCmds.Noop
  }

  private def selecionarUsuarios = {
    var lista = Map("" -> "").toList.map { case (s1, s2) => (s1, s2) }
    lista ++ Usuario.findAllUsuariosLivresPorEquipe().map { case (i, u) => (i.toString, u) }
  }

  private def selecionarTipoTarefa = {
    var lista = Map("" -> "").toList.map { case (s1, s2) => (s1, s2) }
    lista ++ TipoTarefa.findAllTipoTarefaLista().map { case (i, tt) => (i.toString, tt) }
  }

  private def selecionarClienteProjeto = {
    var lista = Map("" -> "").toList.map { case (s1, s2) => (s1, s2) }
    lista ++ Cliente.findAllClienteLista().map { case (i, c) => (i.toString, c) }
  }

}
