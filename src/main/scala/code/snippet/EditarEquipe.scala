package code.snippet

import code.lib.Util._
import code.model.{Equipe, Usuario}
import net.liftmodules.widgets.bootstrap.Bs3ConfirmDialog
import net.liftweb.common.{Empty, Full, Logger}
import net.liftweb.http.js.jquery.JqJsCmds
import net.liftweb.http.js.{JsCmd, JsCmds}
import net.liftweb.http._
import net.liftweb.http.js.JsCmds.{ReplaceOptions, SetHtml}
import net.liftweb.util.Helpers
import Helpers._
import org.joda.time.DateTime
import scala.xml.{Text, NodeSeq}


object equipeExcluirUsuario extends SessionVar[List[(Usuario, String)]](List())

object equipeRVUsuario extends RequestVar[Option[Usuario]](None)

private object listTamplateRVEquipeUsuario extends RequestVar[NodeSeq](Nil)

private object guidToIdRVEquipeUsuario extends RequestVar[Map[String, Long]](Map())

object confirmarExcluirUsuario extends SessionVar[Option[Boolean]](Some(false))

class EditarEquipe extends StatefulSnippet with Logger {


  private val TEMPLATE_LIST_ID = "lista-usuarios"
  private var nomeEquipe: String = ""
  private var equipe: Option[(Int, String, Option[String], Int)] = None
  private var idEquipe: Long = 0
  private var createdAt: DateTime = DateTime.now
  private var descricaoUsuario: String = ""
  private var idUsuario: Long = 0
  private var idLider: Option[Long] = None
  private var nomeUsuario: String = ""
  private var email: String = ""
  private lazy val listaUsuariosLivres: List[(String, String)] = montaListaUsuariosLivres


  def dispatch = {
    case "render" => render
    case "listaUsuario" => listaUsuario
    case "listaEquipe" => listaEquipe
    case "novoUsuario" => novoUsuario
  }

  def render(nodeSeq: NodeSeq) = {
    nodeSeq
  }

  def listaEquipe = {
    equipe = iniciaValores
    nomeEquipe = equipe match {
      case Some(e) => e._2
      case None => nomeEquipe
    }

    idEquipe = equipe match {
      case Some(c) => c._1
      case None => idEquipe
    }

    nomeUsuario = equipe match {
      case Some(c) => c._3.getOrElse("Não encontrado líder...")
      case None => "Não encontrado líder..."
    }

    "#nomeEquipe" #> SHtml.ajaxText(nomeEquipe, nomeEquipe = _) &
      "#nomeLider" #> SHtml.ajaxSelect(retornarMembrosEquipe, Full(nomeUsuario), (s) => idLider = Some(s.toLong) ) &
      "#cancelar" #> link("/sistema/equipe/equipe", () => JsCmds.Noop, Text("Voltar")) &
      "type=submit" #> SHtml.ajaxOnSubmit(() => salvarEquipe)
  }

  def retornarMembrosEquipe = {
    val itemVazio = Map("-1" -> "").toList.map{case (i, u) => (i, u)}
    val usuarios = Usuario.findByEquipe(idEquipe)
    val listaUsuario = usuarios.map( u => (u.idUsuario.toString, u.nome))
    itemVazio ++ listaUsuario
  }

  def listaUsuario(in: NodeSeq): NodeSeq = {
    listTamplateRVEquipeUsuario(in)
    val usuario = Usuario.findByEquipe(idEquipe)
    _rowTemplate(usuario)
  }

  private def _rowTemplate(usuario: Seq[Usuario]): NodeSeq = {
    val in = listTamplateRVEquipeUsuario.is
    val cssSel =
      "#row" #> usuario.map(u => {
        val guid = associatedGuid(u.idUsuario).get
        "#row [id]" #> (guid) &
          ".listaUsuario [class]" #> "gradeA" &
          cellSelector("id") #> Text(u.idUsuario.toString) &
          cellSelector("nomeUsuario") #> Text(u.nome) &
          "#deletar [onclick]" #> SHtml.ajaxInvoke(() => notificarExcluirUsuario(u, guid))
      })
    cssSel.apply(in)
  }

  private def notificarExcluirUsuario(u: Usuario, guid: String) = {
    val node = S.runTemplate("sistema" :: "usuario" :: "configuracao" :: "configuracao-hidden" :: "_modal_excluir" :: Nil)
    node match {
      case Full(nd) => Bs3ConfirmDialog("Excluir Usuario", nd, () => excluirUsuario(u, guid), () => JsCmds.Noop)
      case _ => Bs3ConfirmDialog("Erro ao carregar tela", NodeSeq.Empty, () => JsCmds.Noop, () => JsCmds.Noop)
    }
  }

  private def excluirUsuario(p: Usuario, guid: String): JsCmd = {
    _ajaxDelete(p, guid)
  }

  private def _ajaxDelete(p: Usuario, guid: String): JsCmd = {
    guidToIdRVEquipeUsuario.set(guidToIdRVEquipeUsuario.is - guid)
    Usuario.updateEquipe(p.idUsuario, None);
    ReplaceOptions("adicionarNovoUsuario", montaListaUsuariosLivres, Empty) &
      JsCmds.Replace(guid, NodeSeq.Empty)
  }

  private def cellSelector(p: String): String = {
    "#" + p + " *"
  }

  private def validarNomeExiste = {
    val nEquipeAtual = equipe.get
    Equipe.findEquipeByNome(nomeEquipe) match {
      case Some(c) if ((c > 0) && (nomeEquipe != nEquipeAtual._2)) => true
      case _ => false
    }
  }

  private def salvarEquipe: JsCmd = {
    if (nomeEquipe.isEmpty | nomeEquipe.length < 5) {
      SetHtml("mensagem", mensagemErro(Mensagem.MIN.format(5, "Nome")))
    }
    else if (validarNomeExiste) {
      SetHtml("mensagem", mensagemErro(Mensagem.NOME_EXISTENTE))
    }
    else {
      try {
        Equipe(idEquipe,
          idLider,
          nomeEquipe,
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
    val map = guidToIdRVEquipeUsuario.is;
    map.find(e => l == e._2) match {
      case Some(e) => Some(e._1)
      case None =>
        val guid = nextFuncName
        guidToIdRVEquipeUsuario.set(map + (guid -> l))
        Some(guid)
    }
  }

  private def _ajaxRenderRow(isNew: Boolean, selected: Boolean): JsCmd = {
    val templateRowRoot = TEMPLATE_LIST_ID;
    var xml: NodeSeq = NodeSeq.Empty
    var idUsuario: Long = -1

    equipeRVUsuario.is match {
      case Some(cRV) => xml = _rowTemplate(List(cRV)); idUsuario = cRV.idUsuario
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
        val guid = associatedGuid(idUsuario).get
        op = Some(JsCmds.Replace(guid, ajaxRow));
      }
    }
    op.get
  }

  private def iniciaValores = {
    editarPerfilEquipe.is match {
      case Some(id) => Equipe.findEquipeQuantUsuario(id)
      case None => None
    }
  }

  private def montaListaUsuariosLivres: List[(String, String)] = {
    var lista = Map("" -> "").toList.map { case (s1, s2) => (s1, s2) }
    lista ++ Usuario.findAllUsuariosLivres.map { case (i, n) => (i.toString, n) }
  }

  def novoUsuario = {
    "#adicionarNovoUsuario" #> SHtml.ajaxSelect(montaListaUsuariosLivres, Empty, (u) => relacionarEquipeUsuario(u),
      "class" -> "form-control", "data-placeholder" -> "Selecione um usuário para ser inserido...")
  }

  private def relacionarEquipeUsuario(u: String) = {
    if (!u.isEmpty) {
      val idUsuario = u.toLong
      Usuario.updateEquipe(idUsuario, Some(idEquipe))
      val usuario = Usuario.findById(idUsuario)
      equipeRVUsuario.set(usuario)
      ReplaceOptions("adicionarNovoUsuario", montaListaUsuariosLivres, Empty) &
        _ajaxRenderRow(true, false)
    }
    else {
      JsCmds.Noop
    }
  }

}
