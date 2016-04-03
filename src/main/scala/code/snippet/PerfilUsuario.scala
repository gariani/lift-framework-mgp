package code.snippet

import code.dao.UsuarioDAO
import code.lib.Validador
import code.lib.session.SessionState
import code.model.Usuario
import net.liftweb.http.SHtml.{text}
import net.liftweb.http._
import net.liftweb.http.SHtml.ajaxSubmit
import net.liftweb.http.js.{JsCmds, JsCmd}
import net.liftweb.util.{Helpers}
import org.joda.time.DateTime
import scala.xml.{NodeSeq}
import net.liftweb._
import util.Helpers._


/**
  * Created by daniel on 15/01/16.
  */
class PerfilUsuario  extends StatefulSnippet {

  private var id_usuario: Long = 0
  private var nome: String = ""
  private var email: String = ""
  private var cargo: String = ""
  private var telefone: Long = 0
  private var observacao: String = ""
  private var teste: String = ""

  def carregarDados = {

    var usuarioDAO = new UsuarioDAO

    val usuario = usuarioDAO.findByEmail(SessionState.getLogin)

    usuario match {
      case Some(u) => {
        id_usuario = u.id_usuario
        nome = u.nome
        email = u.email
        telefone = u.telefone
        cargo = u.cargo match {
          case Some(c) => c.toString
          case None => ""
        }
        observacao = u.observacao match {
          case Some(o) => o.toString
          case None => ""
        }

      }
      case None => <div>Perfil não disponível</div>
    }

  }

  def dispatch = { case "render" => render }

  def render = {
      carregarDados
      "name=nome" #> (SHtml.text(nome, nome = _)) &
      "name=email" #> (SHtml.text(email, email = _)) &
      "name=telefone" #> SHtml.text(telefone.toString, s => Helpers.asLong(s).foreach( i => telefone = i)) &
      "name=cargo" #> SHtml.text(cargo, cargo = _) &
      "name=observacao" #> SHtml.textarea(observacao, observacao = _) &
      "type=submit" #> ajaxSubmit("Atualizar", () => atualizar)
  }

  def mensagemErro(msg: String): NodeSeq = {
    <div class="alert alert-danger alert-dismissible" role="alert">
      <button type="button" class="close" data-dismiss="alert" aria-label="Close">
        <span aria-hidden="true">
          &times;
        </span>
      </button>{msg}
    </div>
  }


  def atualizar: JsCmd = {

    S.clearCurrentNotices

    if (!Validador.validarMinTamanhoNome(nome)) {
      S.error("perfilError", mensagemErro("Nome deve conter no mínimo 4 caracteres."))
    }
    else if (!Validador.validarMaxTamanhoNome(nome)) {
      S.error("perfilError", mensagemErro("Nome muito grande, há mais de 100 caracteres."))
    }
    else if (!Validador.validarEmail(email)) {
      S.error("perfilError", mensagemErro("Email incorreto"))
    }
    else {
      salvar
    }

    JsCmds.Noop
  }

  def salvar() = {
    var u = new Usuario(id_usuario, email, nome, Some(cargo), Some(observacao), telefone, "", DateTime.now, Some(DateTime.now) )
    Usuario.save(u)

  }


}
