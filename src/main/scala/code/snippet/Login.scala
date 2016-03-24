package code.snippet

import code.lib.Validador
import net.liftweb.common.{Empty, Full}
import net.liftweb.http._
import scala.xml.{NodeSeq, Text}
import js._
import JsCmds._
import SHtml._
import _root_.net.liftweb.http._
import net.liftweb.util.Helpers._
import scala.language.postfixOps
import code.lib.session.SessionState

class Login extends StatefulSnippet {

  private var email: Option[String] = None
  private var senha: Option[String] = None

  def dispatch = {
    case "render" => render
  }

  private def process() {

    S.clearCurrentNotices

    email = S.param("email").toOption
    senha = S.param("senha").toOption

      Validador.isValidoLogin(email, senha) match {
        case true => {
          SessionState.gravarSessao(email)
          S.redirectTo("/sistema/index")
        }
        case _ => {
          S.error("dados", <div class="alert alert-danger">Email ou senha inválidos!
                           <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
                           </div>)
          Noop
        }
      }
  }

  def render =
    "email=email" #> SHtml.text(email.get, (String) => email) &
    "senha=senha" #> SHtml.text(senha.get, (String) => senha) &
    "type=submit" #> SHtml.onSubmitUnit(process)


  def doLogout(s: String): JsCmd = {
    SessionState.limparSessao
    S.redirectTo("/")
  }
}