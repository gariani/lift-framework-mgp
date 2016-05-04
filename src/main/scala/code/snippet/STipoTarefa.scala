package code.snippet

import code.model.{TipoTarefa}
import code.lib.Util._
import net.liftweb.common.{Full, Box, Logger}
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmds.{Alert, SetHtml}
import net.liftweb.http.js.jquery.JqJsCmds
import net.liftweb.http.js.jquery.JqJsCmds.{Unblock, ModalDialog}
import net.liftweb.http.js.{JsCmd, JsCmds}
import net.liftweb.http._
import net.liftweb._
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

  def dispatch = {
    case "render" => render
    case "lista" => lista
    case "confirmacao" => confirmacao
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
          "#deletar" #> SHtml.ajaxButton(Text("Excluir"), () => ModalDialog(configurarExclusao(tt, guid)))
      })
    cssSel.apply(in)
  }

  private def excluir = {

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
    var idUsuario: Long = -1

    tipoTarefaRV.is match {
      case Some(ttRV) => xml = _rowTemplate(List(ttRV)); idUsuario = ttRV.idTipoTarefa
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
        val guid = associatedGuid(idUsuario).get
        op = Some(JsCmds.Replace(guid, ajaxRow));
      }
    }
    op.get
  }

  def confirmacao =
    "#sim" #> SHtml.ajaxButton(Text("Sim"), () => informarConfirmacao(true)) &
      "#nao" #> SHtml.ajaxButton(Text("Não"), () => informarConfirmacao(false))

  private def informarConfirmacao(b: Boolean) = {
    if(b) {
      tipoTarefaExcluir.is match {
        case List((tt, guid)) => _ajaxDelete(tt, guid)
        case _ => JsCmds.Noop
      }
    }

    Unblock
  }

  private def configurarExclusao(tt: TipoTarefa, guid: String) = {
    val itemList: List[(TipoTarefa, String)] = List((tt, guid))
    tipoTarefaExcluir.set(itemList)
    <div>
      <h4>
        Deseja excluir o tipo de tarefa?
        <br/>
        <div class="lift:STipoTarefa.confirmacao">
          <div id="sim">Sim</div>
          <div id="nao">Cancelar exclusão</div>
        </div>
      </h4>
    </div>
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
