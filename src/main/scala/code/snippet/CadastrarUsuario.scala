package code.snippet

import net.liftmodules.validate.Validators._
import net.liftmodules.validate.global._
import net.liftweb.http.{S, SHtml, StatefulSnippet}
import net.liftweb.common.Logger
import net.liftweb.util
import util.Helpers._
import net.liftweb.http.js.{JsCmds, JsCmd}

import scala.xml.{Text, NodeSeq}

/**
  * Created by daniel on 21/04/16.
  */
class CadastrarUsuario extends StatefulSnippet with Logger {

  private var nome: String = ""
  private var senha: String = ""
  private var email: String = ""


  override def dispatch = {
    case "render" => render
  }

  def render = {
    "#nome" #> (SHtml.text(nome, nome = _) >> ValidateRequired( () => nome)
                                          >> ValidateLength(Some(6), Some(100), () => nome)) &
    "#email" #> (SHtml.text(email, email = _) >> ValidateEmail( () => email)) &
    "#senha" #> SHtml.text(senha, senha = _) &
    "#adicionarNovo" #> SHtml.ajaxButton(Text("Cadastrar"), () => cadastrar, "class" -> "btn btn-primary") &
    "#cancelar" #> SHtml.ajaxButton(Text("Cancelar"), () => redirecionar)
  }

  private def redirecionar = {
    S.redirectTo("/sistema/usuario/configuracao/configuracao_usuario")
  }

  private def cadastrar = {
    JsCmds.Noop
  }

}



