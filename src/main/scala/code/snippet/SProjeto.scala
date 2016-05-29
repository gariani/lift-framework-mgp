package code.snippet

import java.text.SimpleDateFormat

import code.lib.{FormDialog, JQueryDialog}
import code.lib.Util._
import code.model.{Tarefa, Cliente, Projeto}
import net.liftmodules.widgets.bootstrap.Bs3ConfirmDialog
import net.liftweb.common.{Empty, Full}
import net.liftweb.http.js.JE.Call
import net.liftweb.http.js.{JsCmd, JsCmds}
import net.liftweb.http.js.JsCmds.{Alert, SetHtml}
import net.liftweb.http.{SessionVar, S, SHtml, StatefulSnippet}
import net.liftweb.util
import org.joda.time.DateTime
import util.Helpers._

import scala.xml.{Text, NodeSeq}

/**
  * Created by daniel on 27/03/16.
  */


object guidIdClienteProjeto extends SessionVar[List[(String, Option[Projeto])]](List())

object getProjeto extends SessionVar[Option[Projeto]](None)

object guidToIdRVProjeto extends SessionVar[Option[Long]](None)

class SProjeto extends StatefulSnippet {

  private var nomeProjeto: String = ""
  private var nomeCliente: String = ""
  private var descricao: String = ""
  private var quantTarefaEntregue: Option[Double] = None
  private var quantDesenvolvimento: Option[Double] = None
  private var quantNaFila: Option[Double] = None
  private var idProjeto: Long = 0
  private var idCliente: Option[Long] = None
  private var idEquipe: Option[Long] = None
  private var dtInicioProjeto: Option[DateTime] = Some(DateTime.now)
  private var dtFinalProjeto: Option[DateTime] = Some(DateTime.now)
  private var createdAt: DateTime = DateTime.now
  private var deletedAt: Option[DateTime] = Some(DateTime.now)
  private var projeto: Option[Projeto] = None
  private var nmClienteSelecionado: String = ""

  def dispatch = {
    case "render" => render
    case "listaProjetos" => listaProjetos
    case "mostrarProjetoSelecionado" => mostrarProjetoSelecionado
    case "progressoTarefa" => progressoTarefa
    case "progressoHoras" => progressoHoras
    case "formularioNovoProjeto" => formularioNovoProjeto
    case "botaoNovoProjeto" => botaoNovoProjeto
    case "formularioEditarProjeto" => formularioEditarProjeto
    case "editarDescricaoProjeto" => editarDescricaoProjeto
    case "editarNomeProjeto" => editarNomeProjeto
    case "adicionarNovoProjetoMenu" => adicionarNovoProjetoMenu
  }

  private def retornarTemplateItemProjeto = {
    var temp: NodeSeq = NodeSeq.Empty
    temp = S.runTemplate("sistema" :: "projeto" :: "projeto-hidden" :: "_abas" :: Nil).openOr(<div>
      {"Template não encontrado"}
    </div>)

    temp
  }

  private def retornarTemplate(nomeTemplate: String) = {
    var temp: NodeSeq = NodeSeq.Empty
    temp = S.runTemplate("sistema" :: "projeto" :: "projeto-hidden" :: nomeTemplate :: Nil).openOr(<div>
      {"Template não encontrado"}
    </div>)
    temp
  }

  def listaProjetos(nodeSeq: NodeSeq): NodeSeq = {
    val temp = retornarTemplateItemProjeto
    val clientes = Cliente.findAll
    val cli = clientes.asInstanceOf[List[Cliente]]
    val cssSel =
      "#items" #> cli.map(c => {
        val guid = associatedGuid(c.idCliente).get
        "#idcliente [id]" #> (guid) &
          ".cliente" #> SHtml.a(Text(c.nomeCliente), JsCmds.Noop, ("data-toggle" -> "collapse"), ("href" -> retornarGuid(guid))) &
          ".projetos" #> {
            "#projetos [id]" #> (guid) &
              ".list-group-item *" #> (c.projetos.map(p => {
                SHtml.a(() => informarIdProjeto(c.nomeCliente, p), Text(p.nomeProjeto))
              }))
          }
      })
    cssSel.apply(temp)
  }

  private def informarIdProjeto(nmCliente: String, projeto: Projeto) = {
    guidIdClienteProjeto.set(List((nmCliente, Some(projeto))))
    JsCmds.RedirectTo("/sistema/projeto/projeto")
  }

  def retornarIdNodo(guid: String) = {
    "" + guid
  }

  private def retornarGuid(guid: String): String = {
    "#" + guid
  }

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

  def mostrarProjetoSelecionado(nodeSeq: NodeSeq): NodeSeq = {
    val lista = guidIdClienteProjeto.is
    val cssSel = "#projeto" #> {
      lista.map(rs => {
        "#nomeCliente" #> Text(rs._1.toUpperCase()) &
          "#detalhesDoProjeto" #> rs._2.map { p =>
            getProjeto.set(Some(p))
            val desc = retornarDescricaoProjeto(p.descricaoProjeto)
            val idCliente = retornarIdCliente(p.idCliente)
            idProjeto = p.idProjeto
            val dadosProjeto = Projeto.retornarDataInicioFimProjeto(idCliente, idProjeto)
            "#nomeProjeto" #> SHtml.a(() => exibirModalEditarProjeto, Text(p.nomeProjeto.toUpperCase)) &
              "#descricaoProjeto" #> Text(desc) &
              "#dadosProjeto" #> dadosProjeto.map { dp =>
                val dataInicio = converterData(dp._2)
                val dataFinal = converterData(dp._3)
                "#dataPrimeiraTarefa" #> Text(dataInicio) &
                  "#dataUltimaTarefa" #> Text(dataFinal)
              }
          }
      })
    }
    cssSel.apply(nodeSeq)
  }

  def retornarFormDialogComplementoTarefa = {
    val dialog = new FormDialog(true, "Editar Projeto") {
      override def getFormContent = retornarTemplate("_modal_editar_projeto")

      override def confirmDialog: NodeSeq = SHtml.ajaxSubmit("Salvar",
        () => {
          salvarAlteracaoProjeto(this.closeCmd)
        }) ++ super.confirmDialog
    }
    dialog
  }

  def editarNomeProjeto(node: NodeSeq): NodeSeq = {
    var nomeProjeto: String = ""
    var titulo: String = ""
    getProjeto.is match {
      case Some(p) => p.nomeProjeto match {
        case d => nomeProjeto = d
        case _ => nomeProjeto= ""
      }
    }
    val dialog = retornarFormDialogComplementoTarefa
    dialog.a(nomeProjeto, "")
  }

  def formularioEditarProjeto = {
    val projeto = getProjeto.is match {
      case Some(p) =>
        nomeProjeto = p.nomeProjeto
        descricao = p.descricaoProjeto match {
          case Some(d) => d
          case None => ""
        }
      case None =>
        nomeProjeto = ""
        descricao = ""
        idProjeto = -1
    }
    "#nomeProjeto" #> SHtml.ajaxText(nomeProjeto, (s) => {
      nomeProjeto = s; JsCmds.Noop
    }) &
      "#descricao" #> SHtml.textarea(descricao, (s) => descricao = s, "class" -> "form-control")
  }

  def editarDescricaoProjeto(node: NodeSeq): NodeSeq = {

    var desc: String = ""
    var titulo: String = ""
    getProjeto.is match {
      case Some(p) => p.descricaoProjeto match {
        case Some(d) => desc = d
        case None => desc = ""
      }
    }
    if (!desc.isEmpty) {
      titulo = "editar"
    } else {
      titulo = "adicionar descricao"
    }
    val dialog = retornarFormDialogComplementoTarefa

    dialog.a(titulo, "")
  }

  def salvarAlteracaoProjeto(fecharModal: JsCmd) = {
    if (nomeProjeto.isEmpty) {
      SetHtml("mensagem", mensagemErro(Mensagem.MIN))
    }
    else {
      Projeto.alterarNomeProjetoDescricao(idProjeto, nomeProjeto, descricao)
    }

    fecharModal & atualizarNomeDescricaoProjeto
  }

  def atualizarNomeDescricaoProjeto = {
    SetHtml("nomeProjeto", Text(nomeProjeto))
    SetHtml("descricaoProjeto", Text(descricao))
  }

  def progressoTarefa(nodeSeq: NodeSeq): NodeSeq = {
    val temp = retornarTemplate("_progressoTarefas")
    val valorPorcentagem = calcularPorcentagem
    val valor = valorPorcentagem.toArray

    val totalEntregue = valor(0)._1
    val totalEmDesenvolvimento = valor(0)._2
    val totalNafila = valor(0)._3

    val entregue = quantTarefaEntregue match {
      case Some(e) if (e > 0) => "Entregue (%d)".format(e.toInt)
      case _ => ""
    }

    val emDesenvolvimento = quantDesenvolvimento match {
      case Some(d) if (d > 0) => "Desenvolvimento (%d)".format(d.toInt)
      case _ => ""
    }

    val nafila = quantNaFila match {
      case Some(f) if (f > 0) => "Na fila (%d)".format(f.toInt)
      case _ => ""
    }

    val cssSel =
      "#progresso" #> {
        "#sucessoEntregue [style]" #> "width: %d%s".format(totalEntregue, "%") &
          "#progressoEntregue" #> Text(entregue) &
          "#warningDesenvolvimento [style]" #> "width: %d%s".format(totalEmDesenvolvimento, "%") &
          "#progressoDesenvolvimento" #> Text(emDesenvolvimento) &
          "#dangerNaFila [style]" #> "width: %d%s".format(totalNafila, "%") &
          "#progressoNaFila" #> Text(nafila)
      }
    cssSel.apply(temp)
  }


  def progressoHoras(nodeSeq: NodeSeq): NodeSeq = {
    val temp = retornarTemplate("_progressoHoras")
    val valorPorcentagem = calcularPorcentagem
    val valor = valorPorcentagem.toArray

    val totalEntregue = valor(0)._1
    val totalEmDesenvolvimento = valor(0)._2
    val totalNafila = valor(0)._3

    val entregue = "Entregue (%d)".format(
      quantTarefaEntregue match {
        case Some(e) => e.toInt
        case None => ""
      })

    val emDesenvolvimento = "Desenvolvimento (%d)".format(
      quantDesenvolvimento match {
        case Some(e) => e.toInt
        case None => ""
      })

    val nafila = "Na fila (%d)".format(
      quantNaFila match {
        case Some(e) => e.toInt
        case None => ""
      })

    val cssSel =
      "#progresso" #> {
        "#sucessoEntregue [style]" #> "width: %d%s".format(totalEntregue, "%") &
          "#progressoEntregue" #> Text(entregue) &
          "#warningDesenvolvimento [style]" #> "width: %d%s".format(totalEmDesenvolvimento, "%") &
          "#progressoDesenvolvimento" #> Text(emDesenvolvimento) &
          "#dangerNaFila [style]" #> "width: %d%s".format(totalNafila, "%") &
          "#progressoNaFila" #> Text(nafila)
      }
    cssSel.apply(temp)
  }

  private def calcularPorcentagem = {
    val objeto = guidIdClienteProjeto.is
    var listaAndamentoProjeto: List[(Option[Double], Option[Double], Option[Double])] = List()
    var totalTarefas: Option[Int] = None
    var retorno: List[(Int, Int, Int)] = List()
    val projeto = objeto.map { case (s, o) =>
      o match {
        case Some(p) =>
          val idCliente = p.idCliente match {
            case Some(c) => c
            case none => -1
          }
          quantTarefaEntregue = Tarefa.retornarTarefasEntregues(idCliente, p.idProjeto)
          quantDesenvolvimento = Tarefa.retornarTarefasEmDesenlvimento(idCliente, p.idProjeto)
          quantNaFila = Tarefa.retornarTarefasNaoEntregues(idCliente, p.idProjeto)
          listaAndamentoProjeto = List((quantTarefaEntregue, quantDesenvolvimento, quantNaFila))
          totalTarefas = Tarefa.retornarTotalTarefas(idCliente, p.idProjeto)
          retorno = totalTarefas match {
            case Some(tot) if (tot > 0) => listaAndamentoProjeto.map { case (a, b, c) =>
              val a1 = a match {
                case Some(v) => ((v / tot) * 100).toInt
                case None => 0
              }
              val b1 = b match {
                case Some(v) => ((v / tot) * 100).toInt
                case None => 0
              }
              val c1 = c match {
                case Some(v) => ((v / tot) * 100).toInt
                case None => 0
              }
              (a1, b1, c1)
            }
            case _ => List((0, 0, 0))
          }
      }
    case _ => None
    }
    retorno
  }

  private def retornarIdCliente(idCliente: Option[Long]): Long = {
    val idCli = idCliente match {
      case Some(id) => id
      case None => -1
    }
    idCli
  }

  private def retornarDescricaoProjeto(descricao: Option[String]): String = {
    val desc = descricao match {
      case Some(d) => d
      case None => ""
    }
    desc
  }

  private def converterData(dt: Option[DateTime]) = {
    val d = dt match {
      case Some(x) => x
      case None => DateTime.now
    }
    val outputFormat = new SimpleDateFormat("MMM dd yyyy")
    val inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val inputText = d.toString
    val date = inputFormat.parse(inputText)
    val outputText = outputFormat.format(date)
    outputText.toUpperCase()
  }

  private def exibirModalEditarProjeto = {
    JsCmds.Noop
  }

  def render = "#teste" #> Text("")

  def botaoNovoProjeto(nodeSeq: NodeSeq): NodeSeq = {
    val dialog = carregarModalNovoProjeto
    dialog.button("Novo Projeto")
  }

  def adicionarNovoProjetoMenu(nodeSeq: NodeSeq): NodeSeq = {
    val dialog = carregarModalNovoProjeto
    dialog.a("Novo Projeto",  "fa fa-caret-tasks")
  }

  def carregarModalNovoProjeto = {
    val dialog = new FormDialog(true, "Cadastrar Projeto") {
      override def getFormContent = retornarTemplate("_modal_novo_projeto")

      override def confirmDialog: NodeSeq = SHtml.ajaxSubmit("Salvar",
        () => {
          salvarNovoProjeto(this.closeCmd)
        }) ++ super.confirmDialog
    }
    dialog
  }

  def clienteValido: Boolean = {
    idCliente match {
      case None => false
      case Some(c) if (c > 0) => true
    }
  }

  def salvarNovoProjeto(fecharDialog: JsCmd) = {
    if (idProjeto.toString.isEmpty) {
      SetHtml("mensagem", mensagemErro(Mensagem.MIN))
    }
    else if (!clienteValido) {
      SetHtml("mensagem", mensagemErro(Mensagem.MIN))
    }
    else {
      val p = Projeto.create(
        idCliente,
        None,
        nomeProjeto,
        None,
        None,
        None,
        DateTime.now)
      informarIdProjeto(nmClienteSelecionado, p)
      fecharDialog & JsCmds.RedirectTo("/sistema/projeto/projeto")
    }
  }

  def formularioNovoProjeto = {
    "#nomeNovoProjeto" #> SHtml.ajaxText(nomeProjeto, (s) => {
      nomeProjeto = s;
      JsCmds.Noop
    }) &
      "#nomeClienteProjeto" #> SHtml.ajaxSelect(listaClientes, Empty, (s) => idCliente = Some(s.toLong), ("class" -> "form-control"))
  }

  def listaClientes = {
    val vazio = Map("-1" -> "").toList.map { case (s, c) => (s, c) }
    val cliente = Cliente.findAllClienteLista()
    val cli = cliente.map { case (i, c) => (i.toString, c) }
    vazio ++ cli
  }

}
