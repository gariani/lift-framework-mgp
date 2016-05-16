package code.snippet

import code.lib.session.SessionState
import code.model.Usuario
import net.liftmodules.widgets.bootstrap.Bs3ConfirmDialog
import net.liftweb.http.js.JsCmds.{Alert, SetHtml}
import net.liftweb.http.js.jquery.JqJsCmds
import net.liftweb.http.js.jquery.JqJsCmds.ModalDialog
import net.liftweb.util.Helpers
import net.liftweb.common._
import net.liftweb.http.js.{JsCmds, JsCmd}
import net.liftweb.http._
import net.liftweb.util.Helpers._
import org.joda.time.DateTime
import code.lib.Util._
import scala.xml.{NodeSeq, Text}
import net.liftweb.util.Helpers.strToCssBindPromoter

object novoUsuarioVisivel extends RequestVar[Option[Boolean]](Some(true))
object editarPerfilUsuario extends SessionVar[Option[String]](None)
object listTamplateRVUsuario extends RequestVar[NodeSeq](Nil)
object guidToIdRVUsuario extends RequestVar[Map[String, Long]](Map())
object usuarioRV extends RequestVar[Option[Usuario]](None)
private object formTemplateUsuarioRV extends RequestVar[NodeSeq](Nil)

class SListaUsuario extends StatefulSnippet with Logger {

  private val TEMPLATE_LIST_ID = "lista-usuarios";

  private var nome: String = ""
  private var email: String = ""
  private var senha: String = ""

  private def limparCampos = {
    nome = ""
    senha = ""
    email = ""
  }

  def dispatch = {
    case "render" => adicionaNovoUsuario
    case "lista" => lista
    case "addNovoUsuario" => addNovoUsuario
  }

  private def adicionarFormulario = {
    novoUsuarioVisivel.is match {
      case Some(true) =>
        novoUsuarioVisivel.set(Full(false))
        JsCmds.SetHtml("formNovoUsuario", formCadstroUsuario) &
          JsCmds.JsHideId("adicionaNovoUsuario") &
          SetHtml("mensageSucesso", Text(Mensagem.MSN_VAZIA))
      case _ => novoUsuarioVisivel.set(Empty)
        JsCmds.Noop
    }
  }

  def addNovoUsuario = {
    "#nome" #> SHtml.ajaxText(nome, nome = _) &
      "#email" #> SHtml.ajaxText(email, (e) => validarEmailUsado(e)) &
      "#senha" #> SHtml.password(senha, senha = _, "type" -> "password") &
      "#cancelar" #> SHtml.ajaxButton(Text("Cancelar"), () => cancelarNovoUsuario) &
      "#adicionarNovo" #> SHtml.ajaxSubmit("Cadastrar", () => adicionarUsuario)
  }

  private def validarEmailUsado(em: String) = {
    Usuario.isExistsEmail(em) match {
      case Some(e) => SetHtml("mensagem", mensagemErro(Mensagem.EMAIL_JA_USADO))
      case None => email = em; SetHtml("mensagem", Text(""))
    }
  }

  private def adicionarUsuario: JsCmd = {
    if (validarEmail(email)) {
      SetHtml("mensagem", mensagemErro(Mensagem.EMAIL_INVALIDO))
    }
    else if (validarNome(nome)) {
      SetHtml("mensagem", mensagemErro(Mensagem.INTERVALO_VALOR.format(5, 100)))
    }
    else if (validarSenha(senha)) {
      SetHtml("mensagem", mensagemErro(Mensagem.TAM_SENHA.format(6)))
    }
    else {
      //Email.sendEMail("danielgrafael@gmail.com", "danielgrafael@gmail.com", "", "teste", <div></div>)
      val u = Usuario.create(
        None,
        email,
        nome,
        None,
        None,
        None,
        senha,
        None,
        None,
        None,
        None,
        DateTime.now)
      usuarioRV.set(Full(u))
      SetHtml("mensageSucesso", mensagemSucesso(Mensagem.CADASTRO_SALVO_SUCESSO.format("Usuário"))) &
        cancelarNovoUsuario
    }
  }


  private def cancelarNovoUsuario = {
    limparCampos
    novoUsuarioVisivel.is match {
      case Some(false) => novoUsuarioVisivel.set(Full(true))
        JsCmds.SetHtml("formNovoUsuario", <div></div>) &
          JsCmds.JsShowId("adicionaNovoUsuario")
      case _ => novoUsuarioVisivel.set(Empty)
        JsCmds.Noop
    }
  }

  def adicionaNovoUsuario = {
    "#adicionaNovoUsuario" #> SHtml.ajaxButton(Text("Cadastrar novo"), () => adicionarFormulario)
  }

  private def editar(email: String) = {
    editarPerfilUsuario.set(Some(email))
    linkOrigemUsuario.set(Some("/sistema/usuario/configuracao/configuracao_usuario"))
    S.redirectTo("/sistema/usuario/perfil/perfil")
  }

  def lista(in: NodeSeq): NodeSeq = {
    val u = Usuario.findAll()
    listTamplateRVUsuario(in)
    _rowTemplate(u);
  }

  private def associatedGuid(l: Long): Option[String] = {
    val map = guidToIdRVUsuario.is;
    map.find(e => l == e._2) match {
      case Some(e) => Some(e._1)
      case None =>
        val guid = nextFuncName
        guidToIdRVUsuario.set(map + (guid -> l))
        Some(guid)
    }
  }

  private def _rowTemplate(usuarios: List[Usuario]): NodeSeq = {
    val in = listTamplateRVUsuario.is
    val cssSel =
      "#row" #> usuarios.map(u => {
        val guid = associatedGuid(u.idUsuario).get
        "#row [id]" #> (guid) &
          ".listaUsuario [class]" #> "gradeA" &
          cellSelector("id") #> Text(u.idUsuario.toString) &
          cellSelector("nome") #> Text(u.nome) &
          cellSelector("email") #> Text(u.email) &
          "#editar [onclick]" #> SHtml.ajaxInvoke(() => editar(u.email)) &
          "#deletar [onclick]" #> SHtml.ajaxInvoke(() => notificarExcluirProjeto(u, guid))
      })
    cssSel.apply(in)
  }

  private def notificarExcluirProjeto(p: Usuario, guid: String) = {
    val node = S.runTemplate("sistema" :: "usuario" :: "configuracao" :: "configuracao-hidden" :: "_modal_excluir" :: Nil)
    node match {
      case Full(nd) => Bs3ConfirmDialog("Excluir usuário", nd, () => excluirUsuario(p, guid), () => JsCmds.Noop)
      case _ => Bs3ConfirmDialog("Erro ao carregar tela", NodeSeq.Empty, () => JsCmds.Noop, () => JsCmds.Noop)
    }
  }

  private def cellSelector(p: String): String = {
    "#" + p + " *"
  }

  private def excluirUsuario(u: Usuario, guid: String): JsCmd = {
    if (u.email == SessionState.getLogin) {
      SetHtml("mensagem", mensagemErro("Não é possível excluir usuário ao qual está logado"))
    }
    else {
      _ajaxDelete(u, guid)
    }
  }

  private def _ajaxDelete(p: Usuario, guid: String): JsCmd = {
    guidToIdRVUsuario.set(guidToIdRVUsuario.is - guid)
    Usuario.destroy(p.idUsuario);
    JsCmds.Replace(guid, NodeSeq.Empty)
  }

  private def _ajaxRenderRow(u: Usuario, isNew: Boolean, selected: Boolean): JsCmd = {
    val templateRowRoot = TEMPLATE_LIST_ID;
    var xml: NodeSeq = NodeSeq.Empty
    var idUsuario: Long = -1

    usuarioRV.is match {
      case Some(uRV) => xml = _rowTemplate(List(uRV)); idUsuario = uRV.idUsuario
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

  private val formCadstroUsuario: NodeSeq =
    <div class="lift:SListaUsuario.addNovoUsuario">
      <div class="col-md-4">
        <div class="panel panel-default">
          <div class="panel-heading">
            <i class="fa fa-bell fa-fw"></i>
            Cadastrar novo usuário
          </div>
          <div class="panel-body">
            <div id="mensagem"></div>
            <form class="lift:form.ajax" method="post">
              <fieldset style="margin-bottom:5px;">
                <div>
                  <label>* Nome</label> <br/>
                  <input class="form-control" type="text" id="nome" name="nome"/>
                </div>
                <div class="form-group">
                  <label>* E-mail</label>
                  <div class="form-group input-group">
                    <span class="input-group-addon">@</span>
                    <input class="form-control" type="text" id="email" name="email"/>
                  </div>
                </div>
                <label>* Senha</label> <br/>
                <input class="form-control" type="password" id="senha" name="senha"/>
              </fieldset>
              <div>
                <input type="submit" id="adicionarNovo" value="Cadastrar" name="adicionarNovo" class="btn btn-default">
                  <span class="glyphicon glyphicon-ok"></span>
                </input>
                <button type="button" id="cancelar" value="Cancelar" name="cancelar" class="btn btn-default">
                  <span class="glyphicon glyphicon-remove-sign"></span>
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>

}
