package code.snippet

import code.dao.UsuarioDAO
import net.liftweb.common.{Box, Full, Empty}
import net.liftweb.http.js.{JsCmds, JsCmd}
import net.liftweb.http._
import net.liftweb.util.Helpers._
import net.liftweb.util.Helpers

import scala.xml.{NodeSeq, Text}

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
      "#adicinarNovo" #> SHtml.link("/sistema/usuario/configuracao/cadastrar_usuario", () => JsCmds.Noop, Text("Adicionar"))
  }

  def listaUsuario = {

    var usuarioDao = new UsuarioDAO

    val lista = usuarioDao.findAllUsuarios()

    ".linha *" #> lista.map(u =>
      ".id *" #> adicionarIdUsuario(u.idUsuario) &
      ".nome *" #> u.nome &
        ".email *" #> u.email &
        "#editar [onclick]" #> SHtml.ajaxInvoke(() => editar(u.email)) &
        "#deletar [onclick]" #> SHtml.ajaxInvoke(() => deletar(u.email)))
  }

  private def adicionarIdUsuario(idUsuario: Long) = {
    idUsuario + 1
  }

  private def adicionar() = {
    println("adicionar")
  }

  private def editar(email: String) = {
    println("editar" + email)
  }

  private def deletar(email: String) = {
    println("deletar" + email)
  }

}
