package code.snippet

import java.sql.Time
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.logging.SimpleFormatter
import javax.mail.Session

import code.lib.FormDialog
import code.lib.Util._
import code.lib.session.SessionState
import code.model._
import net.liftmodules.validate.Validators._
import net.liftmodules.validate.global._
import net.liftmodules.widgets.bootstrap.{Bs3InfoDialogLg, Bs3InfoDialog, Bs3ConfirmDialogLg, Bs3ConfirmDialog}
import net.liftweb.common.{Box, Empty, Full, Logger}
import net.liftweb.http.Html5ElemAttr.Placeholder
import net.liftweb.http.js.JE._
import net.liftweb.http.js.JsCmds.{Script, Run, Alert, SetHtml}
import net.liftweb.http.js.jquery.{JqJE, JqJsCmds}
import net.liftweb.http.js.jquery.JqJsCmds.{DisplayMessage, ModalDialog}
import net.liftweb.http.js.{JE, JsCmd, JsCmds}
import net.liftweb.http.{RequestVar, S, StatefulSnippet, SHtml}
import net.liftweb.util
import net.liftweb.util.CssSelectorParser.Elem
import net.liftweb.util.Helpers
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, ISODateTimeFormat}
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
  private var statusTarefa: String = ""
  private var esforco: String = ""
  private var datap: String = ""

  def dispatch = {
    case "render" => render
    case "novaTarefa" => novaTarefa
    case "adicionarNovaTarefa" => adicionarNovaTarefa
    case "mostrarMinhasTarefas" => mostrarMinhasTarefas
    case "mostrarCrieiTarefas" => mostrarCrieiTarefas
    //case "mostrarSigoTarefas" => mostrarSigoTarefas
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

  private def salvarNovaTarefa(fechar: JsCmd): JsCmd = {
    if (nomeTarefa.isEmpty) {
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

  def retornarString(campo: Option[String]): String = {
    campo match {
      case Some(c) => c
      case None => ""
    }
  }

  private def retornarTemplateItemTareafa = {
    var temp: NodeSeq = NodeSeq.Empty
    temp = S.runTemplate("sistema" :: "tarefa" :: "tarefa-hidden" :: "_item" :: Nil).openOr(<div>
      {"Template não encontrado"}
    </div>)

    temp
  }

  def mostrarMinhasTarefas(node: NodeSeq): NodeSeq = {
    funcaoXEditable
    var temp = retornarTemplateItemTareafa
    var tarefas = Tarefa.findMeuDetalhe(SessionState.getLogin)
    println(tarefas)
    val cssSel =
      "#items" #> tarefas.map(t => {
        val guid = associatedGuid(t._1).get
        val idStatusTarefa = retornarIdStatusTarefa(t._3)
        val estimativa = retornarString(t._8)
        val dtDesejada = formatarData(t._10)
        val dtEntrega = formatarData(t._11)
        val nomeTarefa = t._12
        val nmCliente = retornarString(t._17)
        val nmProjeto = retornarString(t._16)
        val statusTarefa = retornarString(t._14)
        "#items [id]" #> (guid) &
          "#nomeTarefa" #> SHtml.a(Text(nomeTarefa), JsCmds.Noop, ("data-pk" -> t._1.toString)) &
          "#cliente_projeto" #> SHtml.a(Text(nmCliente + " -> " + nmProjeto), JsCmds.Noop, ("data-pk" -> t._1.toString)) &
          ".dtDesejada" #> SHtml.a(Text(dtDesejada), JsCmds.Noop, ("data-pk" -> t._1.toString)) &
          ".esforcoEstimado" #> SHtml.a(Text(estimativa), JsCmds.Noop, ("data-pk" -> t._1.toString)) &
          ".dtEntrega" #> SHtml.a(Text(dtEntrega), JsCmds.Noop, ("data-pk" -> t._1.toString)) &
          ".status" #> SHtml.a(Text(statusTarefa), JsCmds.Noop, ("data-pk" -> t._1.toString), ("value" -> idStatusTarefa))
      })
    cssSel.apply(temp)
  }

  def mostrarCrieiTarefas(node: NodeSeq): NodeSeq = {
    var temp = retornarTemplateItemTareafa
    var tarefas = Tarefa.findCrieiDetalhe(SessionState.getLogin)
    val cssSel =
      "#items" #> tarefas.map(t => {
        val guid = associatedGuid(t._1).get
        val idStatusTarefa = retornarIdStatusTarefa(t._3)
        val estimativa = retornarString(t._8)
        val dtDesejada = formatarData(t._10)
        val dtEntrega = formatarData(t._11)
        val nomeTarefa = t._12
        val nmCliente = retornarString(t._17)
        val nmProjeto = retornarString(t._16)
        val statusTarefa = retornarString(t._14)
        "#items [id]" #> (guid) &
          "#nomeTarefa" #> SHtml.a(Text(nomeTarefa), JsCmds.Noop, ("data-pk" -> t._1.toString)) &
          "#cliente_projeto" #> SHtml.a(Text(nmCliente + " -> " + nmProjeto), JsCmds.Noop, ("data-pk" -> t._1.toString)) &
          ".dtDesejada" #> SHtml.a(Text(dtDesejada), JsCmds.Noop, ("data-pk" -> t._1.toString)) &
          ".esforcoEstimado" #> SHtml.a(Text(estimativa), JsCmds.Noop, ("data-pk" -> t._1.toString)) &
          ".dtEntrega" #> SHtml.a(Text(dtEntrega), JsCmds.Noop, ("data-pk" -> t._1.toString)) &
          ".status" #> SHtml.a(Text(statusTarefa), JsCmds.Noop, ("data-pk" -> t._1.toString), ("value" -> idStatusTarefa))
      })
    cssSel.apply(temp)
  }

  /*def mostrarSigoTarefas(nodeSeq: NodeSeq): NodeSeq = {
    funcaoXEditable
    var temp = retornarTemplateItemTareafa
    var tarefas = Tarefa.findAllDetalhe()
    val cssSel =
      "#items" #> tarefas.map(t => {
        val guid = associatedGuid(t._1).get
        val idStatusTarefa = retornarIdStatusTarefa(t._3)
        val estimativa = retornarString(t._8)
        val dtDesejada = formatarData(t._10)
        val dtEntrega = formatarData(t._11)
        val nomeTarefa = t._12
        val nmCliente = retornarString(t._17)
        val nmProjeto = retornarString(t._16)
        val statusTarefa = retornarString(t._14)
        "#items [id]" #> (guid) &
          "#nomeTarefa" #> SHtml.a(Text(nomeTarefa), JsCmds.Noop, ("data-pk" -> t._1.toString)) &
          "#cliente_projeto" #> SHtml.a(Text(nmCliente + " -> " + nmProjeto), JsCmds.Noop, ("data-pk" -> t._1.toString)) &
          ".dtDesejada" #> SHtml.a(Text(dtDesejada), JsCmds.Noop, ("data-pk" -> t._1.toString)) &
          ".esforcoEstimado" #> SHtml.a(Text(estimativa), JsCmds.Noop, ("data-pk" -> t._1.toString)) &
          ".dtEntrega" #> SHtml.a(Text(dtEntrega), JsCmds.Noop, ("data-pk" -> t._1.toString)) &
          ".status" #> SHtml.a(Text(statusTarefa), JsCmds.Noop, ("data-pk" -> t._1.toString), ("value" -> idStatusTarefa))
      })
    cssSel.apply(temp)
  }*/

  def retornarIdStatusTarefa(id: Option[Long]): String = {
    id match {
      case Some(l) => l.toString
      case None => 0.toString
    }
  }

  def formatarData(data: Option[org.joda.time.DateTime]) = {

    val dt = data match {
      case Some(d) => d
      case None => ""
    }

    var outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm")
    var inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

    var inputText = dt.toString
    var date = inputFormat.parse(inputText)
    var outputText = outputFormat.format(date)
    outputText
  }

  def funcaoXEditable = {
    S.appendJs(Seq(JsRaw(
      """
        |$.fn.editable.defaults.mode = 'popup';
        |$('.dtDesejada').editable({
        | combodate: {
        |   minYear: 2016,
        |   maxYear: 2018
        |   },
        | type: 'combodate',
        | format: 'DD/MM/YYYY HH:mm',
        | template: 'DD / MM / YYYY     HH : mm',
        | emptytext: 'Não informado',
        | url: '/sistema/api/tarefa/dtDesejada',
        | title: 'Data desejada'
        |});
        | """.stripMargin).cmd,
      JsRaw(
        """
          |$('.esforcoEstimado')
          |.on('shown', function() { $(this).data('editable').input.$input.mask('999:99');})
          |.editable({
          | type: 'text',
          | title: 'Esforço',
          | emptytext: 'Não estimado',
          | tpl: "<input type='text' style='width: 100px'>",
          | url: '/sistema/api/tarefa/esforcoEstimado'
          |});
          | """.stripMargin).cmd,
      JsRaw(
        """
          |$(".dtEntrega").editable({
          | combodate: {
          |   minYear: 2016,
          |   maxYear: 2018
          |   },
          | type: 'combodate',
          | format: 'DD/MM/YYYY HH:mm',
          | template: 'DD / MM / YYYY     HH : mm',
          | emptytext: 'Não informado',
          | url: '/sistema/api/tarefa/dtEntrega',
          | title: 'Data de entrega'
      });""".stripMargin).cmd,
      JsRaw(
        """
          |$(".status").editable({
          | type: 'select',
          | emptytext: 'Sem status',
          | url: '/sistema/api/tarefa/statusTarefa',
          | title: 'Status da tafera',
          | source: %s
      });""".format(gerarListaStatus).stripMargin).cmd))
  }

  private def gerarListaStatus = {
    var total: String = "[ %s ]"
    var valor: String = "{\"value\": \"%d\", \"text\": \"%s\"}"
    val listaStatusTarefa = StatusTarefa.findListaStatus.map { case (i, s) => valor.format(i, s) }.mkString("[", ",", "]")
    listaStatusTarefa
  }

  private def retornarStatusTarefa(status: Option[String]) = {
    status match {
      case Some(s) => s
      case None => "Não informado"
    }
  }

  private def associatedGuid(l: Long): Option[String] = {
    val map = guidToIdRVTarefa.is;
    map.find(e => l == e._2) match {
      case Some(e) => Some(e._1)
      case None =>
        val guid = nextFuncName
        guidToIdRVTarefa.set(map + (guid -> l))
        Some(guid)
    }
  }

}
