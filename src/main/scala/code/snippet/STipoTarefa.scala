package code.snippet

import java.sql.Time

import code.model.{TipoTarefa}
import code.lib.Util._
import net.liftmodules.widgets.bootstrap.{Bs3InfoDialog, Bs3ConfirmDialog}
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

object tipoTarefaSelecioada extends SessionVar[Option[Long]](None)

object exibirNovoTipoTarefa extends RequestVar[Option[Boolean]](Some(true))

object novoTipoTarefaVisivel extends RequestVar[Option[Boolean]](Some(true))

object editarPerfilTipoTarefa extends SessionVar[Option[String]](None)

object listTamplateRVTipoTarefa extends RequestVar[NodeSeq](Nil)

object guidToIdRVTipoTarefa extends RequestVar[Map[String, Long]](Map())

object tipoTarefaRV extends RequestVar[Option[TipoTarefa]](None)

object tipoTarefaExcluir extends SessionVar[List[(TipoTarefa, String)]](List())

class STipoTarefa extends StatefulSnippet with Logger {

  private val TEMPLATE_LIST_ID = "lista-tipo-tarefa";
  protected var idTipoTarefa: Long = 0
  protected var descricao: String = ""
  protected var estimativa: Option[Time] = None
  protected var min: String = ""
  protected var hora: String = ""
  protected var foraUso: Boolean = false;
  protected lazy val internvaloMinuto = intervaloMin
  protected lazy val internvaloHora = intervaloHora

  def dispatch = {
    case "render" => render
    case "lista" => lista
    case "addNovaTarefa" => addNovaTarefa
  }

  def addNovaTarefa = {
    "#descricao" #> SHtml.text(descricao, descricao = _) &
      "#hora" #> SHtml.ajaxSelect(internvaloHora, Full(hora), v => hora = v, "style" -> "width:70px;") &
      "#min" #> SHtml.ajaxSelect(internvaloMinuto, Full(min), v => min = v, "style" -> "width:70px;") &
      "#cancelar" #> SHtml.ajaxButton(Text("Cancelar"), () => cancelarNovoTipoTarefa) &
      "#adicionarBotao" #> SHtml.ajaxSubmit("Cadastrar novo", () => adicionarTipoTarefa)
  }

  protected def cancelarNovoTipoTarefa = {
    limparCampos
    exibirNovoTipoTarefa.is match {
      case Some(false) => exibirNovoTipoTarefa.set(Full(true))
        JsCmds.SetHtml("formNovoTipoTarefa", <div></div>) &
          JsCmds.JsShowId("adicionaNovoTipoTarefa")
      case _ => JsCmds.Noop
    }
  }

  protected def limparCampos = {
    descricao = ""
    hora = ""
    min = ""
    estimativa = Empty
  }

  protected def preencherCampos(tt: TipoTarefa) = {
    descricao = tt.nomeTipoTarefa
    hora = formataHora(tt.estimativa)
    min = formataMin(tt.estimativa)
  }

  protected def adicionarTipoTarefa: JsCmd = {
    if (descricao.isEmpty) {
      SetHtml("mensagem", mensagemErro(Mensagem.MIN.format(5, "Descrição")))
    }
    else {
      try {
        estimativa = formatarEstimativa(hora, min)
        val tt = TipoTarefa.create(descricao,
          estimativa,
          foraUso,
          DateTime.now)

        tipoTarefaRV.set(Some(tt))

        _ajaxRenderRow(tt, true, false) &
          SetHtml("mensageSucesso", mensagemSucesso(Mensagem.DADOS_SALVOS_SUCESSO)) &
          cancelarNovoTipoTarefa
      }
      catch {
        case e: Exception => SetHtml("mensagem", mensagemErro(Mensagem.ERRO_SALVAR_DADOS))
      }
    }
  }


  def render = {
    "#adicionaNovoTipoTarefa" #> SHtml.ajaxButton(Text("Cadastrar novo"), () => adicionarFormulario)
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
      case None => SetHtml("mensagem", mensagemErro(Mensagem.NAO_ENCONTRADO))
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
      case Some(true) =>
        exibirNovoTipoTarefa.set(Full(false))
        JsCmds.SetHtml("formNovoTipoTarefa", formCadstroTipoTarefa) &
          JsCmds.JsHideId("adicionaNovoTipoTarefa") &
          SetHtml("mensageSucesso", Text(Mensagem.MSN_VAZIA))
      case _ => JsCmds.Noop
    }
  }

  def lista(in: NodeSeq): NodeSeq = {
    val tipoTarefa = TipoTarefa.findAll()
    listTamplateRVUsuario(in)
    _rowTemplate(tipoTarefa);
  }

  private def associatedGuid(l: Long): Option[String] = {
    val map = guidToIdRVTipoTarefa.is;
    map.find(e => l == e._2) match {
      case Some(e) => Some(e._1)
      case None =>
        val guid = nextFuncName
        guidToIdRVTipoTarefa.set(map + (guid -> l))
        Some(guid)
    }
  }

  private def _rowTemplate(tipoTarefa: List[TipoTarefa]): NodeSeq = {
    val in = listTamplateRVUsuario.is
    val cssSel =
      "#row" #> tipoTarefa.map(tt => {
        val guid = associatedGuid(tt.idTipoTarefa).get
        "#row [id]" #> (guid) &
          ".listaUsuario [class]" #> "gradeA" &
          cellSelector("id") #> Text(tt.idTipoTarefa.toString) &
          cellSelector("descricao") #> Text(tt.nomeTipoTarefa) &
          cellSelector("estimativa") #> retornarEstimativa(tt.estimativa) &
          cellSelector("fora") #> SHtml.ajaxCheckbox(retornaForaUso(tt.idTipoTarefa), (v) => setForaUso(tt, v)) &
          "#editar [onclick]" #> SHtml.ajaxInvoke(() => editar(tt.idTipoTarefa)) &
          "#deletar" #> SHtml.ajaxButton(Text("Excluir"), () => notificarExcluirProjeto(tt, guid))
      })
    cssSel.apply(in)
  }

  private def cellSelector(p: String): String = {
    "#" + p + " *"
  }

  private def _ajaxDelete(tt: TipoTarefa, guid: String): JsCmd = {
    guidToIdRVTipoTarefa.set(guidToIdRVTipoTarefa.is - guid)
    TipoTarefa.destroy(tt.idTipoTarefa);
    JsCmds.Replace(guid, NodeSeq.Empty)
  }


  private def _ajaxRenderRow(tt: TipoTarefa, isNew: Boolean, selected: Boolean): JsCmd = {
    val templateRowRoot = TEMPLATE_LIST_ID;
    var xml: NodeSeq = NodeSeq.Empty
    var idTipoTarefa: Long = -1

    tipoTarefaRV.is match {
      case Some(ttRV) => xml = _rowTemplate(List(ttRV)); idTipoTarefa = ttRV.idTipoTarefa
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
        val guid = associatedGuid(idTipoTarefa).get
        op = Some(JsCmds.Replace(guid, ajaxRow));
      }
    }
    op.get
  }

  private def notificarExcluirProjeto(tt: TipoTarefa, guid: String) = {
    val node = S.runTemplate("sistema" :: "tarefa" :: "tipo_tarefa" :: "tipo-tarefa-hidden" :: "_modal_excluir" :: Nil)
    node match {
      case Full(nd) => Bs3ConfirmDialog("Excluir tipo de tarefa", nd, () => excluirTipoTarefa(tt, guid), () => JsCmds.Noop)
      case _ => Bs3ConfirmDialog("Erro ao carregar tela", NodeSeq.Empty, () => JsCmds.Noop, () => JsCmds.Noop)
    }
  }

  private def excluirTipoTarefa(tt: TipoTarefa, guid: String) = {
    try {
      _ajaxDelete(tt, guid)
    }
    catch {
      case e: Exception => {
        erroExcluirProjeto("Erro ao excluir tipo de tarefa: Existe projetos relacionados.")
      }
      case _: Throwable => {
        erroExcluirProjeto("Erro ao excluir tipo de tarefa: Existe projetos relacionados. Mensagem original: ")
      }
    }
  }

  private def erroExcluirProjeto(m: String): JsCmd = {
    val node = S.runTemplate("sistema" :: "cliente" :: "cliente-hidden" :: "_modal_aviso" :: Nil)
    node match {
      case Full(nd) => val b = new Bs3InfoDialog(m, nd)
        b
      case _ => val b = new Bs3InfoDialog(m, NodeSeq.Empty)
        b
    }
  }

  private val formCadstroTipoTarefa: NodeSeq =
    <div class="lift:STipoTarefa.addNovaTarefa">
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
