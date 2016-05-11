package code.snippet

import java.sql.SQLIntegrityConstraintViolationException

import code.lib.Util._
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException
import net.liftmodules.widgets.bootstrap._
import net.liftweb.common.{Logger, Empty, Full}
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.http.{SHtml, StatefulSnippet}
import net.liftweb.util
import code.model.{Cliente}
import net.liftweb.http.js.jquery.JqJsCmds
import net.liftweb.http.js.jquery.JqJsCmds.{Unblock, ModalDialog}
import net.liftweb.http.js.{JsCmds, JsCmd}
import net.liftweb.http._
import net.liftweb.util.Helpers._
import org.joda.time.DateTime
import scala.xml.{NodeSeq, Text}

object novoClienteVisivel extends RequestVar[Option[Boolean]](Some(true))

object editarPerfilCliente extends SessionVar[Option[Long]](None)

private object listTamplateRVCliente extends RequestVar[NodeSeq](Nil)

private object guidToIdRVCliente extends RequestVar[Map[String, Long]](Map())

private object clienteRV extends RequestVar[Option[Cliente]](None)

object clienteExcluir extends SessionVar[List[(Cliente, String)]](List())

class SCliente extends StatefulSnippet {

  private val TEMPLATE_LIST_ID = "lista-cliente";
  private var nome: String = ""
  private var projetos: String = ""
  private var cliente: List[Cliente] = List()
  private var count: Long = 0;
  private var nomeCliente: String = ""

  def dispatch = {
    case "lista" => lista
    case "render" => adicionaNovoCliente
    case "addNovoCliente" => addNovoCliente
  }

  def addNovoCliente = {
    "#nome" #> SHtml.ajaxText(nomeCliente, nomeCliente = _) &
      "#cancelar" #> SHtml.ajaxButton(Text("Cancelar"), () => cancelarNovoCliente) &
      "#adicionarNovo" #> SHtml.ajaxSubmit("Cadastrar", () => adicionarCliente)
  }

  def validarNomeCliente(nomeCliente: String): Boolean = {
    Cliente.findClienteByNome(nomeCliente) match {
      case Some(a) if (a <= 0) => false
      case _ => true
    }
  }

  private def adicionarCliente: JsCmd = {
    if (validarNomeCliente(nomeCliente)) {
      SetHtml("mensagem", mensagemErro(Mensagem.NOME_EXISTENTE))
    }
    else if (nomeCliente.isEmpty) {
      SetHtml("mensagem", mensagemErro(Mensagem.INTERVALO.format(5, 50)))
    }
    else {
      val data  = DateTime.now
      val c = Cliente.create(
        nomeCliente,
        data)

      clienteRV.set(Some(c))
      _ajaxRenderRow(c, true, false) &
      SetHtml("mensageSucesso", mensagemSucesso(Mensagem.CADASTRO_SALVO_SUCESSO.format("Cliente"))) &
        cancelarNovoCliente
    }
  }

  private def cancelarNovoCliente = {
    limparCampos
    novoClienteVisivel.is match {
      case Some(false) => novoClienteVisivel.set(Full(true))
        JsCmds.SetHtml("formNovoCliente", <div></div>) &
          JsCmds.JsShowId("adicionaNovoCliente")
      case _ => novoClienteVisivel.set(Empty)
        JsCmds.Noop
    }
  }


  private def limparCampos = {
    nome = ""
    projetos = ""
    nomeCliente = ""
  }

  private def adicionarFormulario = {
    novoClienteVisivel.is match {
      case Some(true) =>
        novoClienteVisivel.set(Full(false))
        JsCmds.SetHtml("formNovoCliente", formCadastroCliente) &
          JsCmds.JsHideId("adicionaNovoCliente") &
          SetHtml("mensageSucesso", Text(Mensagem.MSN_VAZIA))
      case _ => novoClienteVisivel.set(Empty)
        JsCmds.Noop
    }
  }

  def adicionaNovoCliente = {
    "#adicionaNovoCliente" #> SHtml.ajaxButton(Text("Cadastrar novo"), () => adicionarFormulario)
  }

  private def editar(id: Long) = {
    editarPerfilCliente.set(Some(id))
    S.redirectTo("/sistema/cliente/editar")
  }

  def lista(in: NodeSeq): NodeSeq = {
    val c = Cliente.findAll()
    listTamplateRVCliente(in)
    _rowTemplate(c);
  }

  private def associatedGuid(l: Long): Option[String] = {
    val map = guidToIdRVCliente.is;
    map.find(e => l == e._2) match {
      case Some(e) => Some(e._1)
      case None =>
        val guid = nextFuncName
        guidToIdRVCliente.set(map + (guid -> l))
        Some(guid)
    }
  }

  private def _rowTemplate(cliente: List[Any]): NodeSeq = {
    val in = listTamplateRVCliente.is
    val cli = cliente.asInstanceOf[List[Cliente]]
    val cssSel =
      "#row" #> cli.map(c => {
        val guid = associatedGuid(c.idCliente).get
        "#row [id]" #> (guid) &
          ".listaCliente [class]" #> "gradeA" &
          cellSelector("id") #> Text(c.idCliente.toString) &
          cellSelector("nome") #> Text(c.nomeCliente) &
          cellSelector("projetos") #> Text(c.projetos.size.toString) &
          "#editar [onclick]" #> SHtml.ajaxInvoke(() => editar(c.idCliente)) &
          "#deletar" #> SHtml.ajaxButton(Text("Excluir"), () => notificarExcluirProjeto(c, guid))
      })
    cssSel.apply(in)
  }

  private def notificarExcluirProjeto(c: Cliente, guid: String) = {
    val node = S.runTemplate("sistema" :: "cliente" :: "cliente-hidden" :: "_modal_excluir" :: Nil)
    node match {
      case Full(nd) => Bs3ConfirmDialog("Excluir Cliente", nd, () => excluirCliente(c, guid), () => JsCmds.Noop)
      case _ => Bs3ConfirmDialog("Erro ao carregar tela", NodeSeq.Empty, () => JsCmds.Noop, () => JsCmds.Noop)
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

  private def cellSelector(p: String): String = {
    "#" + p + " *"
  }

  private def _ajaxDelete(c: Cliente, guid: String): JsCmd = {
    guidToIdRVCliente.set(guidToIdRVCliente.is - guid)
    Cliente.destroy(c.idCliente);
    JsCmds.Replace(guid, NodeSeq.Empty)
  }

  private def excluirCliente(c: Cliente, guid: String) = {
    try {
      _ajaxDelete(c, guid)
    }
    catch {
      case e: Exception => {erroExcluirProjeto("Erro ao excluir cliente: Existe projetos relacionados.") }
      case _: Throwable => {erroExcluirProjeto("Erro ao excluir cliente: Existe projetos relacionados. Mensagem original: ") }
    }
  }

  private def _ajaxRenderRow(c: Cliente, isNew: Boolean, selected: Boolean): JsCmd = {
    val templateRowRoot = TEMPLATE_LIST_ID;
    var xml: NodeSeq = NodeSeq.Empty
    var idCliente: Long = -1

    clienteRV.is match {
      case Some(cRV) => xml = _rowTemplate(List(cRV)); idCliente = cRV.idCliente
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
        val guid = associatedGuid(idCliente).get
        op = Some(JsCmds.Replace(guid, ajaxRow));
      }
    }
    op.get
  }

  private val formCadastroCliente: NodeSeq =
    <div class="lift:SCliente.addNovoCliente">
      <div class="col-md-4">
        <div class="panel panel-default">
          <div class="panel-heading">
            <i class="fa fa-bell fa-fw"></i>
            Cadastrar novo cliente
          </div>
          <div class="panel-body">
            <div id="mensagem"></div>
            <form class="lift:form.ajax" method="post">
              <fieldset style="margin-bottom:5px;">
                <div>
                  <label>* Nome Cliente</label> <br/>
                  <input class="form-control" type="text" id="nome" name="nome"/>
                </div>
              </fieldset>
              <input type="submit" id="adicionarNovo" value="Cadastrar" name="adicionarNovo" class="btn btn-default">
                <span class="glyphicon glyphicon-ok"></span>
              </input>
              <button type="button" id="cancelar" value="Cancelar" name="cancelar" class="btn btn-default">
                <span class="glyphicon glyphicon-remove-sign"></span>
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>

}
