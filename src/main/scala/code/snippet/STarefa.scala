package code.snippet

import java.text.SimpleDateFormat
import java.util.logging.SimpleFormatter
import javax.mail.Session

import code.lib.FormDialog
import code.lib.Util._
import code.lib.session.SessionState
import code.model.{TipoTarefa, Usuario, Tarefa, Cliente}
import net.liftmodules.validate.Validators._
import net.liftmodules.validate.global._
import net.liftmodules.widgets.bootstrap.{Bs3InfoDialogLg, Bs3InfoDialog, Bs3ConfirmDialogLg, Bs3ConfirmDialog}
import net.liftweb.common.{Box, Empty, Full, Logger}
import net.liftweb.http.Html5ElemAttr.Placeholder
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmds.{Run, Alert, SetHtml}
import net.liftweb.http.js.jquery.JqJsCmds.{DisplayMessage, ModalDialog}
import net.liftweb.http.js.{JE, JsCmd, JsCmds}
import net.liftweb.http.{RequestVar, S, StatefulSnippet, SHtml}
import net.liftweb.util
import net.liftweb.util.CssSelectorParser.Elem
import net.liftweb.util.Helpers
import org.joda.time.DateTime
import util.Helpers._

import scala.xml.{UnprefixedAttribute, Null, Text, NodeSeq}


object itemTarefa extends RequestVar[NodeSeq](Nil)

object guidToIdRVTarefa extends RequestVar[Map[String, Long]](Map())

class STarefa extends StatefulSnippet with Logger {

  private val dataFormato = new SimpleDateFormat("dd/MM/yy")
  private val dataPadrao = (dataFormato format (now))

  private var nomeTarefa: String = ""
  private var idUsuarioResponsavel: Long = 0
  private var idCreatedBy: Long = 0
  private var descricao: String = ""
  private var idCliente: Long = 0
  private var idTipoTarefa: Long = 0
  private var idStatusTarefa: String = ""
  //private var esforco: Option[DateTime] = None
  private var esforco: String = ""

  def dispatch = {
    case "render" => render
    case "novaTarefa" => novaTarefa
    case "adicionarNovaTarefa" => adicionarNovaTarefa
    case "mostrarTarefa" => mostrarTarefa
  }

  def adicionarNovaTarefa(node: NodeSeq): NodeSeq = {

    def _template: NodeSeq = bind("item",
      S.runTemplate("sistema" :: "tarefa" :: "tarefa-hidden" :: "_modal_nova_tarefa" :: Nil) openOr
        <div>
          {"Cannot find template"}
        </div>,
      "name" -> "")

    val dialog = new FormDialog(true, "Cadastrar Tarefa") {
      override def getFormContent = _template

      override def confirmDialog: NodeSeq = SHtml.ajaxSubmit("Salvar",
        () => {
          salvarNovaTarefa(this.closeCmd)
        }) ++ super.confirmDialog
    }

    dialog.a("Nova Tarefa", "fa fa-caret-tasks")
  }


  def render(node: NodeSeq): NodeSeq = {
    node
  }

  def novaTarefa = {
    "#nomeTarefa" #> (SHtml.ajaxText(nomeTarefa, (s) => nomeTarefa = s, "class" -> "form-control", "placeholder" -> "Título da tarefa...") >> ValidateRequired(() => nomeTarefa)) &
      "#responsavel" #> SHtml.ajaxSelect(selecionarUsuarios, Full("Selecione usuário responsável..."), (s) => idUsuarioResponsavel = s.toLong, "class" -> "form-control",
        "placeholder" -> "Selecione usuário responsável...") &
      "#tipoTarefa" #> SHtml.ajaxSelect(selecionarTipoTarefa, Full("Selecione tipo de tarefa..."), (s) => idTipoTarefa = s.toLong, "class" -> "form-control",
        "data-placeholder" -> "Selecione tipo de tarefa...") &
      "#cliente_projeto" #> SHtml.ajaxSelect(selecionarClienteProjeto, Empty, (s) => idCliente = s.toLong, "class" -> "form-control",
        "data-placeholder" -> "Selecione o cliente...") &
      "#descricao" #> SHtml.ajaxTextarea(descricao, (d) => descricao = d, "class" -> "form-control", "placeholder" -> "descreva a tarefa...")
  }

  /*def editarDetalheTarefa = {

  }*/

  private def salvarNovaTarefa(fechar: JsCmd): JsCmd = {
    if (nomeTarefa.isEmpty) {
      //SetHtml("mensagem", mensagemErro(Mensagem.MIN))
      S.error("teste")
    }
    else if (idUsuarioResponsavel.toString.isEmpty) {
      SetHtml("mensagem", mensagemErro(Mensagem.MIN))
    }
    else if (idTipoTarefa.toString.isEmpty) {
      SetHtml("mensagem", mensagemErro(Mensagem.MIN))
    }
    else if (idCliente.toString.isEmpty) {
      SetHtml("mensagem", mensagemErro(Mensagem.MIN))
    }
    else {
      Usuario.findIdByEmail(SessionState.getLogin) match {
        case None => Alert("Erro")
        case Some(u) => idCreatedBy = u
          Tarefa.create(
            None,
            idUsuarioResponsavel,
            nomeTarefa,
            Some(descricao),
            Some(idTipoTarefa),
            None,
            None,
            None,
            None,
            idCreatedBy,
            DateTime.now)

          fechar
      }
    }
  }

  private def selecionarUsuarios = {
    var lista = Map("" -> "").toList.map { case (s1, s2) => (s1, s2) }
    lista ++ Usuario.findAllUsuariosLivresPorEquipe().map { case (i, u) => (i.toString, u) }
  }

  private def selecionarTipoTarefa = {
    var lista = Map("" -> "").toList.map { case (s1, s2) => (s1, s2) }
    lista ++ TipoTarefa.findAllTipoTarefaLista().map { case (i, tt) => (i.toString, tt) }
  }

  private def selecionarClienteProjeto = {
    var lista = Map("" -> "").toList.map { case (s1, s2) => (s1, s2) }
    lista ++ Cliente.findAllClienteLista().map { case (i, c) => (i.toString, c) }
  }

  //private def pegarTemplate:

  def mostrarTarefa(nodeSeq: NodeSeq): NodeSeq = {
    S.appendJs(dataDesejada)
    var temp: NodeSeq = NodeSeq.Empty
    temp = S.runTemplate("sistema" :: "tarefa" :: "tarefa-hidden" :: "_item" :: Nil).openOr(<div>
      {"Cannot find template"}
    </div>)
    var tarefas = Tarefa.findAll()
    val cssSel =
      "#items" #> tarefas.map(t => {
        val guid = associatedGuid(t.idTarefa).get
        "#items [id]" #> (guid) &
          "#nomeTarefa" #> SHtml.link("#", () => JsCmds.Noop, Text(t.nomeTarefa)) &
          "#dataDesejada" #> SHtml.ajaxText("", (s) => JsCmds.Noop, "type" -> "date") &
          "#esforcoEstimado" #> SHtml.ajaxText("", (s) => JsCmds.Noop, "type" -> "text", "style" -> "width=10px") &
          "#dtEntrega" #> SHtml.ajaxText("", (s) => Alert(s), "type" -> "date") &
          "#status" #> SHtml.ajaxEditable(Text("Status :" + retornarEstimativa(t.esforco)),
            SHtml.text(retornarEstimativa(t.esforco), esforco = _),
            () => JsCmds.Noop)
      })
    cssSel.apply(temp)
  }

  val dataDesejada =
    Run("$('#dataDesejada').datepicker({dateFormat:'yy-MM-dd'});" +
      "$('#dtEntrega').datepicker({dateFormat:'yy-MM-dd'});" +
      "$('#esforcoEstimado').datetimepicker({format: 'HH:mm'});")

  private def associatedGuid(l: Long): Option[String] = {
    val map = guidToIdRVUsuario.is;
    map.find(e => l == e._2) match {
      case Some(e) => Some(e._1)
      case None =>
        val guid = nextFuncName
        guidToIdRVUsuario.set(map + (guid -> l))
        Some(guid)
    }
  }

}
