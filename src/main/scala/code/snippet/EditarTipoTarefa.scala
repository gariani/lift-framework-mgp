package code.snippet

import java.sql.Time
import java.text.SimpleDateFormat

import code.lib.Util._
import code.model.TipoTarefa
import net.liftweb.common.{Logger, Full, Empty, Box}
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.http.{S, SHtml, StatefulSnippet}
import net.liftweb.http.js.{JsCmd, JsCmds}
import net.liftweb.util
import util.Helpers._
import org.joda.time.DateTime
import scala.xml.{NodeSeq, Text}

/**
  * Created by daniel on 09/04/16.
  */
class EditarTipoTarefa extends StatefulSnippet with Logger {


  private var idTipoTarefa: Long = 0
  private var descricao: String = ""
  private var estimativa: Option[Time] = Empty
  private var foraUso: Boolean = false;
  private var hora: String = "00"
  private var min: String = "00"
  private lazy val internvaloMinuto = intervaloMin
  private lazy val internvaloHora = intervaloHora


  iniciaValores


  def dispatch = {
    case "render" => render
  }

  def render = {
    "#descricao" #> SHtml.text(descricao, descricao = _) &
      "#hora" #> SHtml.ajaxSelect(internvaloHora, Full(hora), v => hora = v, "style" -> "width:70px;") &
      "#min" #> SHtml.ajaxSelect(internvaloMinuto, Full(min), v => min = v, "style" -> "width:70px;") &
      "#cancelar" #> link("/sistema/tarefa/tipo_tarefa/tipo_tarefa", () => JsCmds.Noop, Text("Voltar")) &
      "type=submit" #> SHtml.ajaxOnSubmit(() => salvar)
  }

  private def salvar: JsCmd = {
    if (descricao.isEmpty) {
      SetHtml("mensagem", mensagemErro(MensagemUsuario.REQUERIDO.format("Descrição")))
    }
    else {
      estimativa = formatarEstimativa(hora, min)
      val tipoTarefa = new TipoTarefa(idTipoTarefa,
        descricao,
        estimativa,
        foraUso,
        DateTime.now,
        None)

      try {
        TipoTarefa.save(tipoTarefa)
        SetHtml("mensagem", mensagemSucesso(MensagemUsuario.DADOS_SALVOS_SUCESSO))
      }
      catch {
        case e: Exception => SetHtml("mensagem", mensagemErro(MensagemUsuario.ERRO_SALVAR_DADOS))
      }
    }
  }


  private def iniciaValores = {

    tipoTarefaSelecioada.get match {
      case Some(id) => {
        val tipoTarefa = TipoTarefa.findByIdTipoTarefa(id)
        tipoTarefa match {
          case Some(tt) => idTipoTarefa = tt.idTipoTarefa
            descricao = tt.nomeTipoTarefa
            tt.estimativa match {
              case Some(e) => hora = formataHora(tt.estimativa)
                min = formataMin(tt.estimativa)
              case None => Empty
            }
          case None => descricao = ""
            estimativa = Empty
        }
      }
      case None => {
        descricao = ""
        estimativa = Empty
      }
    }
  }


}
