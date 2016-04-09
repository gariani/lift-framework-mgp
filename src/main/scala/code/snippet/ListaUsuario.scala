package code.snippet

import code.dao.UsuarioDAO
import net.liftweb.common.{Box, Full, Empty}
import net.liftweb.http.js.{JsCmds, JsCmd}
import net.liftweb.http._
import net.liftweb.util.Helpers._
import net.liftweb.util.Helpers

import scala.xml.NodeSeq

object novoUsuarioVisivel extends RequestVar[Box[Boolean]](Full(true))

class ListaUsuario extends StatefulSnippet {

  private var nome: Option[String] = None
  private var email: Option[String] = None
  private var senha: Option[String] = None


  def dispatch = {
    case "render" => render
  }

  def render = {
    "#listaUsuario" #> listaUsuario &
      "#adicionar [onClick]" #> SHtml.onEvent(ocultar)
  }

  def ocultar(s: String): JsCmd = {
      if (novoUsuarioVisivel.get.get) {
        novoUsuarioVisivel.set(Full(false))
        JsCmds.JsShowId("btnAdicionarNovo")
        JsCmds.JsHideId("cadastrarNovo")
      }
      else {
        novoUsuarioVisivel.set(Full(true))
        JsCmds.JsHideId("btnAdicionarNovo")
        JsCmds.JsShowId("cadastrarNovo")
      }
  }

  def listaUsuario = {

    var usuarioDao = new UsuarioDAO

    val lista = usuarioDao.findAllUsuarios()

    ".linha *" #> lista.map( u =>
      ".nome *" #> u.nome &
      ".email *" #> u.email &
      ".cargo *" #> u.cargo &
      ".observacao *" #> u.observacao &
      ".telefone *" #> u.telefone &
      "#editar [onclick]" #> SHtml.ajaxInvoke(() => editar(u.email)) &
      "#deletar [onclick]" #> SHtml.ajaxInvoke(() => deletar(u.email)))
  }

  def adicionar() = {
    println("adicionar")
  }

  def editar(email: String) = {
    println("editar" + email)
  }

  def deletar(email: String) = {
    println("deletar" + email)
  }

}
