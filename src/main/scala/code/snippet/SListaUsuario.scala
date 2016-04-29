package code.snippet

import code.model.Usuario
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

object novoUsuarioVisivel extends RequestVar[Option[Boolean]](Some(true))
object editarPerfilUsuario extends SessionVar[Option[String]](None)
private object listTamplateRV extends RequestVar[NodeSeq](Nil)
private object guidToIdRV extends RequestVar[Map[String, Long]](Map())
private object usuarioRV extends RequestVar[Option[Usuario]](None)

class SListaUsuario extends StatefulSnippet with Logger {

  private val TEMPLATE_LIST_ID = "lista-usuarios";

  private var nome: String = ""
  private var email: String = ""
  private var senha: String = ""

  def dispatch = {
    case "render" => render
    case "cadastrarUsuario" => cadastrarUsuario
    case "lista" => lista
  }

  private def adicionarFormulario = {
    novoUsuarioVisivel.is match {
      case Some(true) =>
        novoUsuarioVisivel.set(Full(false))
        JsCmds.SetHtml("formNovoUsuario", formCadstroUsuario) &
    JsCmds.JsHideId("adicionaNovoUsuario")
      case _ => novoUsuarioVisivel.set(Empty)
        JsCmds.Noop
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

  private def limparCampos = {
    nome = ""
    senha = ""
    email = ""
  }

  def render = {
    "#adicionaNovoUsuario" #> SHtml.ajaxButton(Text("Cadastrar novo"), () => adicionarFormulario)
  }

  private def editar(email: String) = {
    editarPerfilUsuario.set(Some(email))
    S.redirectTo("/sistema/usuario/perfil/perfil")
  }

  def cadastrarUsuario = {
    "#nome" #> SHtml.ajaxText(nome, nome = _) &
      "#email" #> SHtml.ajaxText(email, email = _) &
      "#senha" #> SHtml.password(senha, senha = _, "type" -> "password") &
      "#cancelar" #> SHtml.ajaxButton(Text("Cancelar"), () => cancelarNovoUsuario) &
      "#adicionarNovo" #> SHtml.ajaxSubmit("Cadastrar", () => adicionarUsuario)
  }

  private def adicionarUsuario: JsCmd = {
    if (validarEmail(email)) {
      SetHtml("alertaMensagem", mensagemErro(MensagemUsuario.EMAIL_INVALIDO))
    }
    else if (validarNome(nome)) {
      SetHtml("alertaMensagem", mensagemErro(MensagemUsuario.INTERVALO_VALOR.format(5, 100)))
    }
    else if (validarSenha(senha)) {
      SetHtml("alertaMensagem", mensagemErro(MensagemUsuario.TAM_SENHA.format(6)))
    }
    else {
      //Email.sendEMail("danielgrafael@gmail.com", "danielgrafael@gmail.com", "", "teste", <div></div>)
      Usuario.isExistsEmail(email) match {
        case Some(e) => SetHtml("alertaMensagem", mensagemErro(MensagemUsuario.EMAIL_JA_USADO))
        case None => val u = Usuario.create(
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
          _ajaxRenderRow(u, true, false) &
            SetHtml("alertaMensagem", mensagemSucesso(MensagemUsuario.DADOS_SALVOS_SUCESSO))
      }
    }
  }

  def lista(in: NodeSeq): NodeSeq = {
    val products = Usuario.findAll()
    listTamplateRV(in)
    _rowTemplate(products);
  }

  private def associatedGuid(l: Long): Option[String] = {
    val map = guidToIdRV.is;
    map.find(e => l == e._2) match {
      case Some(e) => Some(e._1)
      case None =>
        val guid = nextFuncName
        guidToIdRV.set(map + (guid -> l))
        Some(guid)
    }
  }

  private def _rowTemplate(usuarios: List[Usuario]): NodeSeq = {
    val in = listTamplateRV.is
    val cssSel =
      "#row" #> usuarios.map(u => {
        val guid = associatedGuid(u.idUsuario).get
        "#row [id]" #> (guid) &
        ".listaUsuario [class]" #> "gradeA" &
          cellSelector("id") #> Text(u.idUsuario.toString) &
          cellSelector("nome") #> Text(u.nome) &
          cellSelector("email") #> Text(u.email) &
          "#editar [onclick]" #> SHtml.ajaxInvoke(() => editar(u.email)) &
          "#deletar [onclick]" #> {
            SHtml.ajaxInvoke(() => {
              S.runTemplate(List("/sistema/templates-hidden/_confirmar_exclusao"))
            }.map(ns => ModalDialog(ns)) openOr Alert("Erro ao excluir!")
            )}
      })
    cssSel.apply(in)
  }

  private def cellSelector(p: String): String = {
    "#" + p + " *"
  }

  private def _ajaxDelete(p: Usuario, guid: String): JsCmd = {
    guidToIdRV.set(guidToIdRV.is - guid)
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
    <div class="col-md-4">
      <div class="panel panel-default">
        <div class="panel-heading">
          <i class="fa fa-bell fa-fw"></i>
          Cadastrar novo usuário
        </div>
        <div class="panel-body">
          <div class="lift:SListaUsuario.cadastrarUsuario">
            <div id="alertaMensagem"></div>
            <form data-lift="form.ajax" method="post">
              <div class="col-lg-12">
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
                  <input type="submit" id="adicionarNovo" value="Cadastrar" name="adicionarNovo" class="btn btn-primary">
                    <span class="glyphicon glyphicon-ok"></span>
                  </input>
                  <button type="button" id="cancelar" value="Cancelar" name="cancelar" class="btn btn-danger">
                    <span class="glyphicon glyphicon-remove-sign"></span>
                  </button>
                </div>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>

}
