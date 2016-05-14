package code.snippet

import javax.mail.Session

import code.lib.Util._
import code.lib.session.SessionState
import code.model.{TipoTarefa, Usuario, Tarefa, Cliente}
import net.liftmodules.widgets.bootstrap.{Bs3InfoDialogLg, Bs3InfoDialog, Bs3ConfirmDialogLg, Bs3ConfirmDialog}
import net.liftweb.common.{Box, Empty, Full, Logger}
import net.liftweb.http.js.JE.JsRaw
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
  private var idUsuarioResponsavel: Long = 0
  private var idCreatedBy: Long = 0
  private var descricao: String = ""
  private var idCliente: Long = 0
  private var idTipoTarefa: Long = 0
  private var idStatusTarefa: String = ""
  private var esforco: Option[DateTime] = None

  def dispatch = {
    case "adicionarNovaTarefa" => adicionarNovaTarefa
    case "render" => render
    case "novaTarefa" => novaTarefa
  }

/*  def adicionarNovaTarefa = {
    "#adicionarNovaTarefa" #> SHtml.a(
      () => S.runTemplate("sistema" :: "tarefa" :: "tarefa-hidden" :: "_modal_nova_tarefa" :: Nil)
        .map(ns => ModalDialog(ns)) openOr Alert("error"),
      Text("Nova Tarefa"), "class" -> "fa fa-tasks")
  }*/


  def adicionarNovaTarefa = {
    "#newlocation" #> SHtml.ajaxButton(Text("Teste"),
      () =>  { JsRaw("$( \"#dialog-form\" ).dialog( \"open\" )").cmd}, "class" -> "fa fa-tasks")
  }

  def render(node: NodeSeq): NodeSeq = {
    node
  }

  def novaTarefa = {
    "#nomeTarefa" #> SHtml.ajaxText(nomeTarefa, (s) => nomeTarefa = s, "class" -> "form-control") &
      "#responsavel" #> SHtml.ajaxSelect(selecionarUsuarios, Empty, (s) => idUsuarioResponsavel = s.toLong, "class" -> "form-control",
        "data-placeholder" -> "Selecione usuário responsável...") &
      "#tipoTarefa" #> SHtml.ajaxSelect(selecionarTipoTarefa, Empty, (s) => idTipoTarefa = s.toLong, "class" -> "form-control",
        "data-placeholder" -> "Selecione tipo de tarefa...") &
      "#cliente_projeto" #> SHtml.ajaxSelect(selecionarClienteProjeto, Empty, (s) => idCliente = s.toLong, "class" -> "form-control",
        "data-placeholder" -> "Selecione o cliente...") &
      "#cadastrar [onclick]" #> SHtml.ajaxCall(JE.JsRaw("CKEDITOR.instances.bodyText.getData()"), (desc) => salvarNovaTarefa(desc))
  }

  private def criarNovaTarefa = {
    val node = S.runTemplate("sistema" :: "tarefa" :: "tarefa-hidden" :: "_modal_nova_tarefa" :: Nil)
      .map(ns => ModalDialog(ns)) openOr Alert("error")
    /*node match {
      case Full(nd) => {
        /*var dialog = new */ Bs3ConfirmDialogLg("Incluir nova tarefa", nd, () => JsCmds.Noop)
        //dialog
      }
      case Empty => JsCmds.Noop
    }*/
  }

  private def salvarNovaTarefa(desc: String): JsCmd = {
    if (nomeTarefa.isEmpty) {
      SetHtml("mensagem", mensagemErro(Mensagem.MIN))
    }
    else if (idUsuarioResponsavel.toString.isEmpty) {
      SetHtml("mensagem", mensagemErro(Mensagem.MIN))
    }
    else if (idTipoTarefa.toString.isEmpty) {
      SetHtml("mensagem", mensagemErro(Mensagem.MIN))
    }
    else if (idCliente.toString.isEmpty) {
      SetHtml("mensagem", mensagemErro(Mensagem.MIN))
    }
    else {
      Usuario.findIdByEmail(SessionState.getLogin) match {
        case None => Alert("Erro")
        case Some(u) => idCreatedBy = u
          Tarefa.create(idUsuarioResponsavel,
            nomeTarefa,
            Some(desc),
            Some(idTipoTarefa),
            None,
            None,
            None,
            None,
            idCreatedBy,
            DateTime.now)
      }
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
