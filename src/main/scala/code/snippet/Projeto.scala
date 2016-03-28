package code.snippet

import net.liftweb.http.{SHtml, StatefulSnippet}
import net.liftweb.util
import util.Helpers._

/**
  * Created by daniel on 27/03/16.
  */

class Projeto extends StatefulSnippet {

  private var nome: String = ""

  def dispatch = { case "render" => render}

  def render = {
    "#nome" #> SHtml.text(nome, nome = _)
  }

}
