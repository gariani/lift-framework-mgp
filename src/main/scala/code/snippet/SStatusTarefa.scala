package code.snippet

import java.sql.Time

import code.model.{StatusTarefa}
import code.lib.Util._
import net.liftmodules.widgets.bootstrap.Bs3ConfirmDialog
import net.liftweb.common.{Empty, Full, Box, Logger}
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmds.{Script, Alert, SetHtml}
import net.liftweb.http.js.jquery.JqJsCmds
import net.liftweb.http.js.jquery.JqJsCmds.{Unblock, ModalDialog}
import net.liftweb.http.js.{JsCmd, JsCmds}
import net.liftweb.http._
import net.liftweb._
import org.joda.time.DateTime
import util.Helpers._
import scala.xml.{NodeSeq, Text}

/**
  * Created by daniel on 03/04/16.
  */

object statusTarefaSelecioada extends SessionVar[Option[Long]](None)

object exibirNovoStatusTarefa extends RequestVar[Option[Boolean]](Some(true))

object novoStatusTarefaVisivel extends RequestVar[Option[Boolean]](Some(true))

object listTamplateRVStatusTarefa extends RequestVar[NodeSeq](Nil)

object guidToIdRVStatusTarefa extends RequestVar[Map[String, Long]](Map())

object statusTarefaRV extends RequestVar[Option[StatusTarefa]](None)

object statusTarefaExcluir extends SessionVar[List[(StatusTarefa, String)]](List())

class SStatusTarefa extends StatefulSnippet with Logger {

  private val TEMPLATE_LIST_ID = "lista-status-tarefa";
  protected var idStatusTarefa: Long = 0
  protected var descricao: String = ""

  def dispatch = {
    case "render" => render
    case "lista" => lista
    case "addNovoStausTarefa" => addNovoStausTarefa
  }

  def addNovoStausTarefa = {
    limparCampos
    "#descricao" #> SHtml.text(descricao, descricao = _) &
      "#cancelar" #> SHtml.ajaxButton(Text("Cancelar"), () => cancelarNovoStatusTarefa) &
      "#adicionarBotao" #> SHtml.ajaxSubmit("Cadastrar novo", () => adicionarStatusTarefa)
  }

  protected def cancelarNovoStatusTarefa = {
    limparCampos
    exibirNovoStatusTarefa.is match {
      case Some(false) => exibirNovoStatusTarefa.set(Full(true))
        JsCmds.SetHtml("formNovoStatusTarefa", <div></div>) &
          JsCmds.JsShowId("adicionaNovoStatusTarefa")
      case _ => JsCmds.Noop
    }
  }

  protected def limparCampos = {
    descricao = ""
  }

  private def validarDescricao(d: String): Boolean = {
    if (d.isEmpty) {
      SetHtml("mensagem", mensagemErro(Mensagem.MIN.format(5, "Descrição")))
      false
    }
    else if (d.length >= 50) {
      SetHtml("mensagem", mensagemErro(Mensagem.MAX.format(50)))
      false
    }
    else {
      true
    }
  }

  protected def adicionarStatusTarefa: JsCmd = {
    if (validarDescricao(descricao)) {
      try {
        val st = StatusTarefa.create(descricao,
          DateTime.now)

        statusTarefaRV.set(Some(st))
        _ajaxRenderRow(st, true, false) &
          SetHtml("mensagem", mensagemSucesso(Mensagem.DADOS_SALVOS_SUCESSO)) &
          cancelarNovoStatusTarefa
      }
      catch {
        case e: Exception => SetHtml("mensagem", mensagemErro(Mensagem.ERRO_SALVAR_DADOS))
      }
    }
    else {
      SetHtml("mensagem", mensagemErro(Mensagem.ERRO_SALVAR_DADOS))
    }
  }


  def render = {
    "#adicionaNovoStatusTarefa" #> SHtml.ajaxButton(Text("Cadastrar novo"), () => adicionarFormulario)
  }

  protected def deletar(idStatusTarefa: Long) = {
    StatusTarefa.destroy(idStatusTarefa)
    JsCmds.Noop
  }

  private def adicionarFormulario = {
    exibirNovoStatusTarefa.is match {
      case Some(true) =>
        exibirNovoStatusTarefa.set(Full(false))
        JsCmds.SetHtml("formNovoStatusTarefa", formCadstroStatusTarefa) &
          JsCmds.JsHideId("adicionaNovoStatusTarefa") &
          SetHtml("mensagemSucesso", Text(Mensagem.MSN_VAZIA))
      case _ => JsCmds.Noop
    }
  }

  def lista(in: NodeSeq): NodeSeq = {
    val statusTarefa = StatusTarefa.findAll()
    listTamplateRVStatusTarefa(in)
    _rowTemplate(statusTarefa);
  }

  private def associatedGuid(l: Long): Option[String] = {
    val map = guidToIdRVStatusTarefa.is;
    map.find(e => l == e._2) match {
      case Some(e) => Some(e._1)
      case None =>
        val guid = nextFuncName
        guidToIdRVStatusTarefa.set(map + (guid -> l))
        Some(guid)
    }
  }

  private def _rowTemplate(statusTarefa: List[StatusTarefa]): NodeSeq = {
    val in = listTamplateRVStatusTarefa.is
    val cssSel =
      "#row" #> statusTarefa.map(st => {
        val guid = associatedGuid(st.idStatusTarefa).get
        "#row [id]" #> (guid) &
          ".listaStatusTarefa [class]" #> "gradeA" &
          cellSelector("id") #> Text(st.idStatusTarefa.toString) &
          cellSelector("descricao") #> SHtml.ajaxEditable(Text(st.nomeStatusTarefa),
            SHtml.text(st.nomeStatusTarefa, descricao = _, "class" -> "form-control"),
            () => salvarAlteracaoDescricao(st, descricao)) &
          "#deletar" #> SHtml.ajaxButton(Text("Excluir"), () => notificarExcluirProjeto(st, guid))
      })
    cssSel.apply(in)
  }

  private def salvarAlteracaoDescricao(st: StatusTarefa, d: String) = {
    if (validarDescricao(descricao)) {
      val status = StatusTarefa(st.idStatusTarefa, d, DateTime.now, None).save
      statusTarefaRV(Some(status))
      _ajaxRenderRow(st, false, true) &
        SetHtml("mensagem", mensagemSucesso(Mensagem.DADOS_SALVOS_SUCESSO))
    }
    else {
      SetHtml("mensagem", mensagemErro(Mensagem.ERRO_SALVAR_DADOS))
    }
  }

  private def notificarExcluirProjeto(st: StatusTarefa, guid: String) = {
    val node = S.runTemplate("sistema" :: "cliente" :: "cliente-hidden" :: "_modal_excluir" :: Nil)
    node match {
      case Full(nd) => Bs3ConfirmDialog("Excluir Cliente", nd, () => excluirStatusTarefa(st, guid), () => JsCmds.Noop)
      case _ => Bs3ConfirmDialog("Erro ao carregar tela", NodeSeq.Empty, () => JsCmds.Noop, () => JsCmds.Noop)
    }
  }

  private def cellSelector(p: String): String = {
    "#" + p + " *"
  }


  private def excluirStatusTarefa(st: StatusTarefa, guid: String) = {
    _ajaxDelete(st, guid)
  }

  private def _ajaxDelete(st: StatusTarefa, guid: String): JsCmd = {
    guidToIdRVStatusTarefa.set(guidToIdRVStatusTarefa.is - guid)
    StatusTarefa.destroy(st.idStatusTarefa);
    JsCmds.Replace(guid, NodeSeq.Empty)
  }

  private def _ajaxRenderRow(st: StatusTarefa, isNew: Boolean, selected: Boolean): JsCmd = {
    val templateRowRoot = TEMPLATE_LIST_ID;
    var xml: NodeSeq = NodeSeq.Empty
    var idStatusTarefa: Long = -1

    statusTarefaRV.is match {
      case Some(stRV) => xml = _rowTemplate(List(stRV)); idStatusTarefa = stRV.idStatusTarefa
      case _ => xml = NodeSeq.Empty
    }

    var op: Option[JsCmd] = None;
    for {
      elem <- xml \\ "_"
      tr <- (elem \ "tr") if (elem \ "@id").text == templateRowRoot
    } yield {
      val ajaxRow = if (selected) ("td [class+]" #> "selected-row").apply(tr) else tr
      if (isNew) {
        op = Some(JqJsCmds.AppendHtml(templateRowRoot, ajaxRow));
      } else {
        val guid = associatedGuid(idStatusTarefa).get
        op = Some(JsCmds.Replace(guid, ajaxRow));
      }
    }
    op.get
  }

  private val formCadstroStatusTarefa: NodeSeq =
    <div class="lift:SStatusTarefa.addNovoStausTarefa">
      <div class="col-md-4">
        <div class="panel panel-default">
          <div class="panel-heading">
            <i class="fa fa-bell fa-fw"></i>
            Cadastrar novo status de tarefa
          </div>
          <div class="panel-body">
            <div id="mensagem"></div>
            <form id="statusTarefa" class="lift:form.ajax" method="post">
              <fieldset style="margin-bottom:5px;">
                <div>
                  <label for="descricao">* Descrição</label> <br/>
                  <input class="form-control" type="text" id="descricao" name="descricao"/>
                </div>
              </fieldset>
              <div>
                <button type="submit" id="adicionarBotao"
                        class="btn btn-default">
                  <i class="glyphicon glyphicon-ok"></i>
                </button>
                <button type="button" id="cancelar" value="Cancelar" name="cancelar" class="btn btn-default">
                  <span class="glyphicon glyphicon-remove-sign"></span>
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
}
