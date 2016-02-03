package code.snippet

import code.dao.UsuarioDAO
import code.lib.session.SessionState
import code.model.Usuario
import net.liftweb.common._
import net.liftweb.http.SHtml.{text}
import net.liftweb.http
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.http.{RequestVar, SHtml}
import net.liftweb.http.SHtml.ajaxSubmit
import net.liftweb.http.js.{JE, JsCmd}
import net.liftweb.util.Helpers._
import scala.xml.{Text, NodeSeq}
import net.liftweb._
import http._
import util.Helpers._
import js._
import JsCmds._
import SHtml._


/**
  * Created by daniel on 15/01/16.
  */
class PerfilUsuario  extends StatefulSnippet {

  private var id: String = "1"
  private var nome: String = "daniel"
  private var email: String = "teste@teste.com.br"
  private var cargo: String = "cargo"
  private var observacao: String = "obs"
  private var usuario: Usuario = _

  def carregarDados = {

    var usuarioDAO = new UsuarioDAO
    usuario = usuarioDAO.findUser(SessionState.getLogin).head

    nome = usuario.nome
    email = usuario.email

    cargo = usuario.cargo match {
      case Some(c) => c.toString
      case None => ""
    }

    observacao = usuario.observacao match {
      case Some(o) => o.toString
      case None => ""
    }
  }

  def dispatch = {
    case "render" => render
  }

  def render = {
    carregarDados

    "name=id" #> SHtml.text(id, id = _) &
      "name=nome" #> SHtml.text(nome, nome = _) &
      "name=email" #> SHtml.text(email, email = _) &
      "name=cargo" #> SHtml.text(cargo, cargo = _) &
      "name=observacao" #> SHtml.text(observacao, observacao = _) &
      "type=submit" #> ajaxSubmit("Alterar", alterar)
  }

  private def alterar() = {
    //var us: Usuario = new Usuario
    var usuarioDAO = new UsuarioDAO
    //usuarioDAO.save(usuario)

  }


}
