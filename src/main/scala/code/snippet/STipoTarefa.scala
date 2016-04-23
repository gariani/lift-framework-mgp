package code.snippet

import java.sql.Time
import java.text.SimpleDateFormat
import java.util.Calendar

import code.dao.TipoTarefaDAO
import code.model.TipoTarefa
import net.liftweb.common.{Full, Empty, Box}
import net.liftweb.http.js.JE.ValById
import net.liftweb.http.js.{JsCmd, JsCmds}
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.http.js.jquery.JqJsCmds
import net.liftweb.http.{RequestVar, StatefulSnippet, SHtml}
import net.liftweb._
import org.joda.time.DateTime
import util.Helpers._
import scala.xml.{NodeSeq, Text, Group}

/**
  * Created by daniel on 03/04/16.
  */

object tipoTarefaSelecioada extends RequestVar[Option[TipoTarefa]](Empty)
object exibirNovoTipoTarefa extends RequestVar[Option[Boolean]](Some(false))

class STipoTarefa extends StatefulSnippet {

  private var descricao: Option[String] = None
  private var estimativa: Option[String] = None
  private var flForaUso: Boolean = false;

  def dispatch = {
    case "render" => render
  }

  def render = {
    "#descricaoTipoTarefa" #> listaTipoTarefa
  }

  def formatarEstimativa(e: Time): String = {
    val formatar = new SimpleDateFormat("HH:mm")
    val hora = formatar.format(e)
    hora
  }

  def retornarEstimativa(estima: Option[Time]) = {
    estima match {
      case Some(e) => formatarEstimativa(e)
      case None => ""
    }
  }

  def desativarNovoTipoTarefa = {

    JsCmds.Noop
  }

  def ativarNovoTipoTarefa = {
    JsCmds.Noop
  }

  def criarNovoTipoTarefa: JsCmd = {
    exibirNovoTipoTarefa.get match {
      case Some(true) => desativarNovoTipoTarefa
      case Some(false) => ativarNovoTipoTarefa
      case _ => JsCmds.Noop
    }
  }


  def listaTipoTarefa = {

    var tipoTarefaDao = new TipoTarefaDAO
    val lista = tipoTarefaDao.findAll()
    "#adicionaNovoTipoTarefa" #> SHtml.ajaxButton(Text("Cadastrar novo"), () => criarNovoTipoTarefa) &
    ".listaTipoTarefa *" #> lista.map(tt =>
      ".id *" #> tt.idTipoTarefa &
        ".descricao *" #> tt.nomeTipoTarefa &
        ".estimativa *" #> retornarEstimativa(tt.estimativa) &
        "#foraUso *" #> SHtml.ajaxCheckbox(retornaForaUso(tt.idTipoTarefa), (v) => setForaUso(tt, v)) &
        "#editar [onclick]" #> SHtml.ajaxInvoke(() => editar(tt.idTipoTarefa)) &
        "#deletar [onclick]" #> SHtml.ajaxInvoke(() => deletar(tt.idTipoTarefa)))
  }

  private def editar(idTipoTarefa: Long) = {

  }

  private def retornaForaUso(idTipoTarefa: Long): Boolean = {
    TipoTarefa.getForaUso(idTipoTarefa) match {
      case Some(f) => f
      case None => false
    }
  }

  private def setForaUso(tipoTarefa: TipoTarefa, v: Boolean) = {
    flForaUso = TipoTarefa.foraUsoTipoTarefa(tipoTarefa.idTipoTarefa, v)
    JsCmds.Noop
  }

  private def deletar(idTipoTarefa: Long) = {
    var tipoTarefaDAO = new TipoTarefaDAO
    tipoTarefaDAO.delete(idTipoTarefa)
    JsCmds.Noop
  }

  private val formCadstroTipoTarefa: NodeSeq =
    <div class="col-md-4">
      <div class="panel panel-default">
        <div class="panel-heading">
          <i class="fa fa-bell fa-fw"></i>
          Cadastrar novo usuário
        </div>
        <div class="panel-body">
          <div id="alertaMensagem"></div>
    <div class="lift:EditarTipoTarefa">
      <div id="mensagem"></div>
      <form id="tipoTarefa" class="lift:form.ajax">
        <fieldset style="margin-bottom:5px;">
          <div class="lift:EditarTipoTarefa">
            <div class="row">
              <div class="col-lg-3">
                <label for="descricao">* Descrição</label> <br/>
                <input class="form-control" type="text" id="descricao" name="descricao"/>
              </div>
              <div class="col-lg-3">
                <label>Estimativa</label>
                  <div class="input-group">
                    <span class="form-control" data-inline="true" id="hora" name="hora"></span>
                    <span class="form-control" data-inline="true" id="min" name="min"></span>
                  </div>
              </div>
              <br/>
            </div>
              </div>
              </fieldset>
              <div class="row">
                <div class="col-md-3">
                  <button type="submit" name="adicionarNovo" class="btn btn-primary">
                    <i
                    class="glyphicon glyphicon-ok"></i>
                    Salvar
                  </button>
                  <a id="cancelar" class="btn btn-info" href="#">
                    <i class="fa fa-trash-o fa-lg"></i>
                  </a>
                </div>
              </div>
            </form>
          </div>
          </div>
      </div>
    </div>


}
