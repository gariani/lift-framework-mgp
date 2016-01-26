package code.snippet

import code.model.{Validador}
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

class LoginForm extends StatefulSnippet {

  private var email: String = ""
  private var senha: String = ""

  private object referer extends RequestVar(S.referer openOr "/")

  var isLoggedIn = false

  def dispatch = {
    case "render" => render
  }

  //private val whence = S.referer.openOr("/")

  def render =
    "email=email" #> SHtml.text(email, email = _) &
      "senha=senha" #> SHtml.text(senha, senha = _) &
      ".logout [onclick]" #> SHtml.onEvent(doLogout) &
      "type=submit" #> SHtml.onSubmitUnit(process)

  private def process() {

    S.clearCurrentNotices

    email = S.param("email").openOr("-1")
    senha = S.param("senha").openOr("-1")

    Validador.isValidEmail(email) match {
      case true => {
        Validador.isValidLogin(email, senha) match {
          case true => {
            SessionState.gravarSessao(email)
            S.redirectTo("/sistema/index")
          }
          case _ => S.error("senha", <div class="alert alert-danger">Email ou senha inválidos!</div>)
        }
      }
      case _ => S.error("email", <div class="alert alert-danger">Email não cadastrado</div>)
    }
  }

  def doLogout(s: String): JsCmd = {
    SessionState.limparSessao
    S.redirectTo("/")
  }
}