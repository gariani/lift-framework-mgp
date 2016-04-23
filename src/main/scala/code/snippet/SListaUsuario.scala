package code.snippet

import code.dao.UsuarioDAO
import code.model.Usuario
import net.liftweb.http.js.JsCmds.{SetHtml}
import net.liftweb.util.Helpers
import net.liftweb.common.{Box, Full, Empty}
import net.liftweb.http.js.{JsCmds, JsCmd}
import net.liftweb.http._
import net.liftweb.util.Helpers._
import org.joda.time.DateTime
import code.lib.Util._
import scala.xml.{NodeSeq, Text}

object novoUsuarioVisivel extends RequestVar[Box[Boolean]](Full(true))
object editarPerfilUsuario extends SessionVar[Box[String]](Empty)

class ListaUsuario extends StatefulSnippet {

  private var nome: String = ""
  private var email: String = ""
  private var senha: String = ""

  def dispatch = {
    case "render" => render
    case "cadastrarUsuario" => cadastrarUsuario
  }

  private def adicionarFormulario = {
    novoUsuarioVisivel.is match {
      case Full(true) =>
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
      case Full(false) => novoUsuarioVisivel.set(Full(true))
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
    "#listaUsuario" #> listaUsuario &
      "#adicionaNovoUsuario" #> SHtml.ajaxButton(Text("Cadastrar novo"), () => adicionarFormulario)
  }

  def listaUsuario = {

    var usuarioDao = new UsuarioDAO

    val lista = usuarioDao.findAllUsuarios()

    ".linha *" #> lista.map(u =>
      ".id *" #> adicionarIdUsuario(u.idUsuario) &
        ".nome *" #> u.nome &
        ".email *" #> u.email &
        "#editar [onclick]" #> SHtml.ajaxInvoke(() => editar(u.email)) &
        "#deletar [onclick]" #> SHtml.ajaxInvoke(() => deletar(u.email)))
  }

  private def adicionarIdUsuario(idUsuario: Long) = {
    idUsuario + 1
  }

  private def editar(email: String) = {
    editarPerfilUsuario.set(Full(email))
    S.redirectTo("/sistema/usuario/perfil/perfil")
  }

  private def deletar(email: String) = {
    println("deletar" + email)
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
        case None => Usuario.create(
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
          JsCmds.Noop
      }
    }
  }

  private val formCadstroUsuario: NodeSeq =
    <div class="col-md-4">
      <div class="panel panel-default">
        <div class="panel-heading">
          <i class="fa fa-bell fa-fw"></i>
          Cadastrar novo usuário
        </div>
        <div class="panel-body">
          <div id="alertaMensagem"></div>
          <form data-lift="form.ajax" method="post">
            <div class="lift:ListaUsuario.cadastrarUsuario">
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
            </div>
          </form>
        </div>
      </div>
    </div>

}
