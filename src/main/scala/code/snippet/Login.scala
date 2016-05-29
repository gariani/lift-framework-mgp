package code.snippet

import code.lib.Util._
import net.liftweb.http._
import _root_.net.liftweb.http._
import net.liftweb.util.Helpers._
import scala.language.postfixOps
import code.lib.session.SessionState

import scala.xml.Text

object linkOrigemUsuario extends SessionVar[Option[String]](None)

class Login extends StatefulSnippet {

  private var email: Option[String] = None
  private var senha: Option[String] = None

  def dispatch = {
    case "render" => render
  }

  private def login() {

    S.clearCurrentNotices

    email = S.param("email")
    senha = S.param("senha")

      isValidoLogin(email, senha) match {
        case true => {
          SessionState.gravarSessao(email)
          S.redirectTo("/sistema/projeto/projetos")
        }
        case _ => {
          S.error("dados", <div class="alert alert-danger">Email ou senha inválidos!
                           <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
                           </div>)
        }
      }
  }

  def render =
    "email=email" #> SHtml.text(email.get, (String) => email) &
    "senha=senha" #> SHtml.text(senha.get, (String) => senha) &
    "type=submit" #> SHtml.onSubmitUnit(login)

}