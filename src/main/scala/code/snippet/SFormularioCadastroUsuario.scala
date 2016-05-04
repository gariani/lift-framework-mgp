package code.snippet

import code.lib.Util._
import code.model.Usuario
import net.liftweb.common.{Empty, Full}
import net.liftweb.http.{S, StatefulSnippet, SHtml}
import net.liftweb.http.js.{JsCmds, JsCmd}
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.util.Helpers
import org.joda.time.DateTime
import Helpers._
import scala.xml.Text

/**
  * Created by daniel on 01/05/16.
  */
class SFormularioCadastroUsuario extends StatefulSnippet {

  private var nome: String = ""
  private var email: String = ""
  private var senha: String = ""

  private def limparCampos = {
    nome = ""
    senha = ""
    email = ""
  }

  def dispatch = {
    case "render" => formularioUsuario
  }

  def formularioUsuario = {
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

}
