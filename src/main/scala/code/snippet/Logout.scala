package code.snippet

import net.liftweb.common.{Empty, Full}
import net.liftweb.http.{SHtml, StatefulSnippet}
import scala.xml.{NodeSeq, Text}
import net.liftweb.util.Helpers._

/**
  * Created by daniel on 23/03/16.
  */
class Logout extends StatefulSnippet{

  def dispatch = {case "render" => render}

  def render = ".logout" #> SHtml.link("/", () => logout, <i class="fa fa-sign-out fa-fw">Sair</i>)

  private def logout: Any = {
      Empty
  }

}
