package code.snippet

import java.util.Locale

import code.lib.Util._
import code.model.{Projeto, Cliente}
import net.liftmodules.widgets.bootstrap.{Bs3ConfirmDialog, ConfirmDialog}
import net.liftweb.common.{Empty, Logger, Full}
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.http.js.jquery.JqJsCmds
import net.liftweb.http.js.jquery.JqJsCmds.{Unblock, ModalDialog}
import net.liftweb.http._
import net.liftweb.http.js.{JsCmd, JsCmds}
import net.liftweb.util.Helpers
import Helpers._
import org.joda.time.DateTime
import scala.xml.{NodeSeq, Text}

/**
  * Created by daniel on 04/05/16.
  */

object clienteExcluirProjeto extends SessionVar[List[(Projeto, String)]](List())

object clienteRVProjeto extends RequestVar[Option[Projeto]](None)

private object listTamplateRVClienteProjeto extends RequestVar[NodeSeq](Nil)

private object guidToIdRVClienteProjeto extends RequestVar[Map[String, Long]](Map())

object confirmarExcluirProjeto extends SessionVar[Option[Boolean]](Some(false))

class EditarCliente extends StatefulSnippet with Logger {

  private val TEMPLATE_LIST_ID = "lista-projetos"
  private var nomeCliente: String = ""
  private var cliente: Option[Cliente] = None
  private var idCliente: Long = 0
  private var createdAt: DateTime = DateTime.now
  private var nomeProjeto: String = ""
  private var descricaoProjeto: String = ""
  private var idProjeto: Long = 0

  def dispatch = {
    case "render" => render
    case "listaProjeto" => listaProjeto
    case "listaCliente" => listaCliente
    case "editarProjeto" => editarProjetoSelecionado
    case "novoProjeto" => novoProjeto
    case "formularioNovoProjeto" => formularioNovoProjeto
  }

  def render = {
    "#render" #> NodeSeq.Empty
  }

  def listaCliente = {
    cliente = iniciaValores
    nomeCliente = cliente match {
      case Some(c) => c.nomeCliente
      case None => nomeCliente
    }

    idCliente = cliente match {
      case Some(c) => c.idCliente
      case None => idCliente
    }

    createdAt = cliente match {
      case Some(c) => c.createdAt
      case None => createdAt
    }

    "#nomeCliente" #> SHtml.ajaxText(nomeCliente, nomeCliente = _) &
      "#cancelar" #> link("/sistema/cliente/cliente", () => JsCmds.Noop, Text("Voltar")) &
      "type=submit" #> SHtml.ajaxOnSubmit(() => salvarCliente)
  }

  def listaProjeto(in: NodeSeq): NodeSeq = {
    listTamplateRVClienteProjeto(in)
    val projeto = cliente match {
      case Some(c) => c.projetos
      case None => Seq()
    }
    _rowTemplate(projeto)
  }

  private def _rowTemplate(projeto: Seq[Projeto]): NodeSeq = {
    val in = listTamplateRVClienteProjeto.is
    val cssSel =
      "#row" #> projeto.map(p => {
        val guid = associatedGuid(p.idProjeto).get
        "#row [id]" #> (guid) &
          ".listaProjeto [class]" #> "gradeA" &
          cellSelector("id") #> Text(p.idProjeto.toString) &
          cellSelector("nomeProjeto") #> Text(p.nomeProjeto) &
          "#editar [onclick]" #> SHtml.ajaxInvoke(() => notificarEditarProjeto(p, guid)) &
          "#deletar [onclick]" #> SHtml.ajaxInvoke(() => notificarExcluirProjeto(p, guid))
      })
    cssSel.apply(in)
  }

  private def notificarExcluirProjeto(p: Projeto, guid: String) = {
    val node = S.runTemplate("sistema" :: "projeto" :: "projeto-hidden" :: "_modal_excluir" :: Nil)
    node match {
      case Full(nd) => Bs3ConfirmDialog("Excluir projeto", nd, () => excluirProjeto(p, guid), () => JsCmds.Noop)
      case _ => Bs3ConfirmDialog("Erro ao carregar tela", NodeSeq.Empty, () => JsCmds.Noop, () => JsCmds.Noop)
    }
  }

  private def notificarEditarProjeto(p: Projeto, guid: String) = {
    nomeProjeto = p.nomeProjeto

    descricaoProjeto = p.descricaoProjeto match {
      case Some(d) => d
      case None => ""
    }

    idProjeto = p.idProjeto
    val node = S.runTemplate("sistema" :: "projeto" :: "projeto-hidden" :: "_modal_projeto" :: Nil)
    node match {
      case Full(nd) => Bs3ConfirmDialog("Editar projeto", nd, () => salvarProjeto, () => JsCmds.Noop)
      case _ => Bs3ConfirmDialog("Erro ao carregar tela", NodeSeq.Empty, () => JsCmds.Noop, () => JsCmds.Noop)
    }
  }

  private def adicionarNovoProjeto = {
    val node = S.runTemplate("sistema" :: "projeto" :: "projeto-hidden" :: "_modal_novo_projeto" :: Nil)
    node match {
      case Full(nd) => Bs3ConfirmDialog("Novo projeto", nd, () => criarNovoProjeto, () => JsCmds.Noop)
      case _ => Bs3ConfirmDialog("Erro ao carregar tela", NodeSeq.Empty, () => JsCmds.Noop, () => JsCmds.Noop)
    }
  }

  private def criarNovoProjeto = {
    if (validarProjeto) {
      val p = Projeto.create(Some(idCliente),
                              None,
                              nomeProjeto,
                              None,
                              None,
                              None,
                              DateTime.now)

      clienteRVProjeto.set(Some(p))
      _ajaxRenderRow(p, true, false) &
        SetHtml("mensagemEditarProjeto", mensagemSucesso(Mensagem.DADOS_SALVOS_SUCESSO))
    } else {
      SetHtml("mensagemEditarProjeto", mensagemSucesso(Mensagem.ERRO_SALVAR_DADOS))
    }
  }

  def novoProjeto = {
    "#adicionarNovoProjeto" #> SHtml.ajaxButton(Text("Novo Projeto"), () => adicionarNovoProjeto)
  }

  def formularioNovoProjeto = {
    nomeProjeto = ""
    "#nomeNovoProjeto" #> SHtml.ajaxText(nomeProjeto, nomeProjeto = _) &
      "#nomeClienteProjeto" #> Text("Cliente: " + nomeCliente)
  }

  private def excluirProjeto(p: Projeto, guid: String): JsCmd = {
    _ajaxDelete(p, guid)
  }

  private def _ajaxDelete(p: Projeto, guid: String): JsCmd = {
    guidToIdRVClienteProjeto.set(guidToIdRVClienteProjeto.is - guid)
    Projeto.destroy(p.idProjeto);
    JsCmds.Replace(guid, NodeSeq.Empty)
  }

  private def cellSelector(p: String): String = {
    "#" + p + " *"
  }

  private def validarNomeExiste = {
    Cliente.findClienteByNome(nomeCliente) match {
      case Some(c) if c > 0 => true
      case _ => false
    }
  }

  private def salvarCliente: JsCmd = {
    if (nomeCliente.isEmpty | nomeCliente.length < 5) {
      SetHtml("mensagem", mensagemErro(Mensagem.MIN.format(5, "Nome")))
    }
    else if (validarNomeExiste) {
      SetHtml("mensagem", mensagemErro(Mensagem.NOME_EXISTENTE))
    }
    else {
      try {
        Cliente(idCliente,
          nomeCliente,
          createdAt,
          None).save()
        SetHtml("mensagem", mensagemSucesso(Mensagem.DADOS_SALVOS_SUCESSO))
      }
      catch {
        case e: Exception => SetHtml("mensagem", mensagemErro(Mensagem.ERRO_SALVAR_DADOS))
      }
    }
  }

  private def associatedGuid(l: Long): Option[String] = {
    val map = guidToIdRVClienteProjeto.is;
    map.find(e => l == e._2) match {
      case Some(e) => Some(e._1)
      case None =>
        val guid = nextFuncName
        guidToIdRVClienteProjeto.set(map + (guid -> l))
        Some(guid)
    }
  }

  private def _ajaxRenderRow(p: Projeto, isNew: Boolean, selected: Boolean): JsCmd = {
    val templateRowRoot = TEMPLATE_LIST_ID;
    var xml: NodeSeq = NodeSeq.Empty
    var idProjeto: Long = -1

    clienteRVProjeto.is match {
      case Some(cRV) => xml = _rowTemplate(List(cRV)); idProjeto = cRV.idProjeto
      case _ => xml = NodeSeq.Empty
    }

    var op: Option[JsCmd] = None;
    for {
      elem <- xml \\ "_"
      tr <- (elem \ "tr") if (elem \ "@id").text == templateRowRoot
    } yield {
      val ajaxRow = if (selected) ("td [class+]" #> "selected-row").apply(tr) else tr
      if (isNew) {
        op = Some(JqJsCmds.AppendHtml(templateRowRoot, ajaxRow));
      } else {
        val guid = associatedGuid(idProjeto).get
        op = Some(JsCmds.Replace(guid, ajaxRow));
      }
    }
    op.get
  }

  def editarProjetoSelecionado = {
    "#nomeProjeto" #> SHtml.ajaxText(nomeProjeto, nomeProjeto = _) &
      "#descricaoProjeto" #> SHtml.ajaxTextarea(descricaoProjeto, descricaoProjeto = _)
  }

  private def validarProjeto: Boolean = {
    if (nomeProjeto.length < 5 || nomeProjeto.isEmpty) {
      false
    } else if (nomeProjeto.length > 50) {
      false
    }
    else {
      true
    }
  }

  def salvarProjeto: JsCmd = {
    if (validarProjeto) {
      val p = Projeto(idProjeto,
        Some(idCliente),
        None,
        nomeProjeto,
        Some(descricaoProjeto),
        None,
        None,
        DateTime.now,
        None).criarMinimoProjeto()

      clienteRVProjeto.set(Some(p))
      _ajaxRenderRow(p, false, true) &
        SetHtml("mensagemEditarProjeto", mensagemSucesso(Mensagem.DADOS_SALVOS_SUCESSO))
    }
    else {
      SetHtml("mensagemEditarProjeto", mensagemErro(Mensagem.ERRO_SALVAR_DADOS))
    }
  }

  private def iniciaValores = {
    editarPerfilCliente.is match {
      case Some(id) => Cliente.findClienteById(id)
      case None => None
    }
  }

}
