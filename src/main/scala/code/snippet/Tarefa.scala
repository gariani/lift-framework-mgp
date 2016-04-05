package code.snippet

import net.liftweb.http.{StatefulSnippet, SHtml}
import net.liftweb.util
import util.Helpers._

/**
  * Created by daniel on 03/04/16.
  */
class Tarefa extends StatefulSnippet {

  private var nome: String = ""

  def dispatch = { case "render" => render}

  def render = {
    "#nome" #> SHtml.text(nome, nome = _)
  }

}
