package code.snippet

import code.lib.Util._
import code.model.Cliente
import net.liftweb.common.{Empty, Full}
import net.liftweb.http.{StatefulSnippet, SHtml}
import net.liftweb.http.js.{JsCmds, JsCmd}
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.util.Helpers
import org.joda.time.DateTime
import Helpers._
import scala.xml.Text

/**
  * Created by daniel on 01/05/16.
  */
class SFormularioCadastroCliente extends StatefulSnippet {

  private var nomeCliente: String = ""

  private def limparCampos = {
    nomeCliente = ""
  }

  def dispatch = {
    case "render" => formularioCliente
  }

  def formularioCliente = {
    "#nome" #> SHtml.ajaxText(nomeCliente, nomeCliente = _) &
    "#cancelar" #> SHtml.ajaxButton(Text("Cancelar"), () => cancelarNovoUsuario) &
      "#adicionarNovo" #> SHtml.ajaxSubmit("Cadastrar", () => adicionarUsuario)
  }

  def validarNomeCliente(nomeCliente: String): Boolean = {
    Cliente.findClienteByNome(nomeCliente) match {
      case Some(a) if (a <= 0) => false
      case _ => true
    }
  }

  private def adicionarUsuario: JsCmd = {
    if (validarNomeCliente(nomeCliente)) {
      SetHtml("mensagem", mensagemErro(Mensagem.NOME_EXISTENTE))
    }
    else if (nomeCliente.isEmpty) {
      SetHtml("mensagem", mensagemErro(Mensagem.INTERVALO.format(5, 50)))
    }
    else {
      val data  = DateTime.now
      val c = Cliente.create(
        nomeCliente,
        data)

      clienteRV.set(Full(c))

      SetHtml("mensageSucesso", mensagemSucesso(Mensagem.CADASTRO_SALVO_SUCESSO.format("Cliente"))) &
        cancelarNovoUsuario
    }
  }

  private def cancelarNovoUsuario = {
    limparCampos
    novoClienteVisivel.is match {
      case Some(false) => novoClienteVisivel.set(Full(true))
        JsCmds.SetHtml("formNovoCliente", <div></div>) &
          JsCmds.JsShowId("adicionaNovoCliente")
      case _ => novoClienteVisivel.set(Empty)
        JsCmds.Noop
    }
  }

}
