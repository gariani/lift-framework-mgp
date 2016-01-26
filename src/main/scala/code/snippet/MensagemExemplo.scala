package code.snippet

import code.lib.session.SessionState
import net.liftweb.common.{Full, Box, Empty}
import net.liftmodules.ng.Angular._

object Resultado {

  def getResultado: Box[String] = Full("daniel") //SessionState.loggedInUserName.get

}

object MensagemExemplo{

  def render = renderIfNotAlreadyDefined(
    angular.module("Mensagem")
    .factory("mensagem", jsObjFactory()
      .jsonCall("getResultado", Resultado.getResultado)
    )
  )
}
