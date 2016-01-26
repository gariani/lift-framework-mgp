package code.snippet

import code.dao.UsuarioDAO
import net.liftweb.common.{Box, Full, Empty}
import net.liftweb.http.{S, StatefulSnippet}
import net.liftweb.util.Helpers._
import net.liftweb.util.Helpers

import scala.xml.NodeSeq


class UsuarioForm extends StatefulSnippet {

  private var imprimir: String = ""

  def dispatch = {
    case "listaUsuario" => listaUsuario
  }

  def listaUsuario = {

    var usuarioDao = new UsuarioDAO
    val lista = usuarioDao.findAll()

    ".linha *" #> lista.map( x =>
      ".id *" #> x.usuario_id &
      ".nome *" #> x.nome &
      ".email *" #> x.email &
      ".cargo *" #> x.cargo &
      ".observacao *" #> x.observacao &
      ".telefone *" #> x.telefone &
      ".permissao *" #> x.permissao &
      ".id_perfil *" #> x.id_perfil)
  }

}
