package code.snippet

import code.lib.session.SessionState
import code.model.Usuario
import net.liftweb.common.{Box, Empty, Full}
import net.liftweb.http.{RequestVar, SHtml, StatefulSnippet}
import net.liftweb.util.Helpers._
import scala.xml.Text

/**
  * Created by daniel on 27/03/16.
  */
class MenuDropDownUsuario extends StatefulSnippet {

  def dispatch = {case "render" => render}

  def render = ".perfil" #> SHtml.link("/sistema/usuario/perfil/perfil", () => Empty, <i class="perfil fa fa-gear fa-fw"> Perfil</i>) &
               ".sair"   #> SHtml.link("/", () => SessionState.limparSessao, <i class="fa fa-sign-out fa-fw"> Sair</i>)
}
