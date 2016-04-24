package code.snippet

import java.sql.Time
import java.text.SimpleDateFormat
import code.model.TipoTarefa
import code.lib.Util._
import net.liftweb.common.{Full, Empty, Box, Logger}
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.http.js.{JsCmd, JsCmds}
import net.liftweb.http._
import net.liftweb._
import net.liftweb.util.Helpers
import util.Helpers._
import scala.xml.{NodeSeq, Text, Group}
import org.joda.time.DateTime

/**
  * Created by daniel on 03/04/16.
  */

object tipoTarefaSelecioada extends SessionVar[Option[Long]](None)

object exibirNovoTipoTarefa extends RequestVar[Box[Boolean]](Full(true))

class STipoTarefa extends StatefulSnippet with Logger {

  def dispatch = {
    case "render" => render
  }

  def render = {
    "#descricaoTipoTarefa" #> listaTipoTarefa &
      "#adicionaNovoTipoTarefa" #> SHtml.ajaxButton(Text("Cadastrar novo"), () => adicionarFormulario)
  }

  def listaTipoTarefa = {
    val lista = TipoTarefa.findAll()
    ".listaTipoTarefa" #> lista.map(tt =>
      "#row [id]" #> Helpers.nextFuncName &
        ".id *" #> tt.idTipoTarefa &
        ".descricao *" #> tt.nomeTipoTarefa &
        ".estimativa *" #> retornarEstimativa(tt.estimativa) &
        "#foraUso *" #> SHtml.ajaxCheckbox(retornaForaUso(tt.idTipoTarefa), (v) => setForaUso(tt, v)) &
        "#editar" #> SHtml.ajaxButton(Text("Editar"), () => editar(tt.idTipoTarefa)) &
        "#deletar" #> SHtml.ajaxButton(Text("Deletar"), () => deletar(tt.idTipoTarefa), "class" -> "button delete"))

  }

  protected def setForaUso(tipoTarefa: TipoTarefa, v: Boolean) = {
    TipoTarefa.foraUsoTipoTarefa(tipoTarefa.idTipoTarefa, v)
    JsCmds.Noop
  }

  protected def retornaForaUso(idTipoTarefa: Long): Boolean = {
    TipoTarefa.getForaUso(idTipoTarefa) match {
      case Some(f) => f
      case None => false
    }
  }

  private def editar(idTipoTarefa: Long) = {
    TipoTarefa.findByIdTipoTarefa(idTipoTarefa) match {
      case Some(tt) => S.redirectTo("/sistema/tarefa/tipo_tarefa/editar", () => definirTipoTarefaEdicao(tt.idTipoTarefa))
      case None => SetHtml("mensagem", mensagemErro(MensagemUsuario.NAO_ENCONTRADO))
    }
  }

  protected def deletar(idTipoTarefa: Long) = {
    TipoTarefa.destroy(idTipoTarefa)
    JsCmds.Noop
  }


  private def definirTipoTarefaEdicao(idTipoTarefa: Long) = {
    tipoTarefaSelecioada.set(Some(idTipoTarefa))
  }

  private def adicionarFormulario = {
    exibirNovoTipoTarefa.is match {
      case Full(true) =>
        exibirNovoTipoTarefa.set(Full(false))
        JsCmds.SetHtml("formNovoTipoTarefa", formCadstroTipoTarefa) &
          JsCmds.JsHideId("adicionaNovoTipoTarefa")
      case _ => JsCmds.Noop
    }
  }

  private val formCadstroTipoTarefa: NodeSeq =
    <div class="lift:SFormularioCadastroTipoTarefa">
      <div class="col-md-4">
        <div class="panel panel-default">
          <div class="panel-heading">
            <i class="fa fa-bell fa-fw"></i>
            Cadastrar novo tipo de tarefa
          </div>
          <div class="panel-body">
            <div id="mensagem"></div>
            <form id="tipoTarefa" class="lift:form.ajax" method="post">
              <fieldset style="margin-bottom:5px;">
                <div>
                  <label for="descricao">* Descrição</label> <br/>
                  <input class="form-control" type="text" id="descricao" name="descricao"/>
                  <div class="form-group">
                    <label>Estimativa</label>
                    <div class="form-group input-group">
                      <span class="form-control" data-inline="true" id="hora" name="hora"></span>
                      <span class="form-control" data-inline="true" id="min" name="min"></span>
                    </div>
                  </div>
                </div>
              </fieldset>
              <div>
                <button type="submit" id="adicionarBotao"
                        class="btn btn-primary">
                  <i class="glyphicon glyphicon-ok"></i>
                </button>
                <button type="button" id="cancelar" value="Cancelar" name="cancelar" class="btn btn-danger">
                  <span class="glyphicon glyphicon-remove-sign"></span>
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
}
