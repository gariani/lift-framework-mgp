package code.snippet

import java.sql.Time

import code.lib.Util._
import code.model.TipoTarefa
import net.liftweb.common.{Logger, Full, Empty}
import net.liftweb.http.{StatefulSnippet, SHtml}
import net.liftweb.util
import net.liftweb.util.Helpers
import net.liftweb.http.js.{JsCmd, JsCmds}
import org.joda.time.DateTime
import util.Helpers._
import scala.xml.Text
import net.liftweb.http.js.JsCmds.SetHtml

/**
  * Created by daniel on 24/04/16.
  */
class SFormularioCadastroTipoTarefa extends StatefulSnippet with Logger {

  protected var idTipoTarefa: Long = 0
  protected var descricao: String = ""
  protected var estimativa: Option[Time] = Empty
  protected var min: String = ""
  protected var hora: String = ""
  protected var foraUso: Boolean = false;
  protected lazy val internvaloMinuto = intervaloMin
  protected lazy val internvaloHora = intervaloHora

  def formularioTipoTarefa = {
    "#descricao" #> SHtml.text(descricao, descricao = _) &
      "#hora" #> SHtml.ajaxSelect(internvaloHora, Full(hora), v => hora = v, "style" -> "width:70px;") &
      "#min" #> SHtml.ajaxSelect(internvaloMinuto, Full(min), v => min = v, "style" -> "width:70px;") &
      "#cancelar" #> SHtml.ajaxButton(Text("Cancelar"), () => cancelarNovoTipoTarefa) &
    "#adicionarBotao" #> SHtml.ajaxSubmit("Cadastrar novo", () => criar)
  }

  def dispatch = {
    case "render" => formularioTipoTarefa
  }

  protected def cancelarNovoTipoTarefa = {
    limparCampos
    exibirNovoTipoTarefa.is match {
      case Full(false) => exibirNovoTipoTarefa.set(Full(true))
        JsCmds.SetHtml("formNovoTipoTarefa", <div></div>) &
          JsCmds.JsShowId("adicionaNovoTipoTarefa")
      case _ => JsCmds.Noop
    }
  }

  protected def limparCampos = {
    descricao = ""
    estimativa = Empty
  }

  protected def preencherCampos(tt: TipoTarefa) = {
    descricao = tt.nomeTipoTarefa
    hora = formataHora(tt.estimativa)
    min = formataMin(tt.estimativa)
  }

  protected def criar: JsCmd = {
    if (descricao.isEmpty) {
      SetHtml("mensagem", mensagemErro(MensagemUsuario.MIN.format(5, "Descrição")))
    }
    else {
      try {
        TipoTarefa.create(descricao,
          estimativa,
          foraUso,
          DateTime.now)
        SetHtml("mensagem", mensagemSucesso(MensagemUsuario.DADOS_SALVOS_SUCESSO))
      }
      catch {
        case e: Exception => SetHtml("mensagem", mensagemErro(MensagemUsuario.ERRO_SALVAR_DADOS))
      }
    }
  }

}
