package code.lib.session

import net.liftweb.common.{Full, Box, Empty}
import net.liftweb.http.{S, SessionVar}

object loggedInUserName extends SessionVar[Box[String]](Empty)

object SessionState {

  def gravarSessao(usuario: String): Unit ={
    loggedInUserName.set(Full(usuario))
  }

  def estaLogado: Boolean = {
    ! (loggedInUserName.is == Empty)
  }

  def limparSessao = {
    loggedInUserName.set(Empty)
  }
}