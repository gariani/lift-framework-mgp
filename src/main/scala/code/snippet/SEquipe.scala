package code.snippet

import code.lib.Util._
import code.model.Equipe
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException
import net.liftmodules.widgets.bootstrap._
import net.liftweb.common.{Logger, Empty, Full}
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.http.{SHtml, StatefulSnippet}
import net.liftweb.util
import net.liftweb.http.js.jquery.JqJsCmds
import net.liftweb.http.js.jquery.JqJsCmds.{Unblock, ModalDialog}
import net.liftweb.http.js.{JsCmds, JsCmd}
import net.liftweb.http._
import net.liftweb.util.Helpers._
import org.joda.time.DateTime
import scala.xml.{NodeSeq, Text}

object novoEquipeVisivel extends RequestVar[Option[Boolean]](Some(true))

object editarPerfilEquipe extends SessionVar[Option[Long]](None)

private object listTamplateRVEquipe extends RequestVar[NodeSeq](Nil)

private object guidToIdRVEquipe extends RequestVar[Map[String, Long]](Map())

private object equipeRV extends RequestVar[Option[(Int, String, Option[String], Int)]](None)

object equipeExcluir extends SessionVar[List[(Equipe, String)]](List())

class SEquipe extends StatefulSnippet with Logger {

  private val TEMPLATE_LIST_ID = "lista-equipe";
  private var nome: String = ""
  private var projetos: String = ""
  private var equipe: List[Equipe] = List()
  private var count: Long = 0;
  private var nomeEquipe: String = ""
  private var idLider: Option[Long] = None

  def dispatch = {
    case "lista" => lista
    case "render" => adicionaNovoEquipe
    case "addNovoEquipe" => addNovoEquipe
  }

  def addNovoEquipe = {
    "#nome" #> SHtml.ajaxText(nomeEquipe, nomeEquipe = _) &
      "#cancelar" #> SHtml.ajaxButton(Text("Cancelar"), () => cancelarNovoUsuario) &
      "#adicionarNovo" #> SHtml.ajaxSubmit("Cadastrar", () => adicionarEquipe)
  }

  private def adicionarEquipe: JsCmd = {
    if (nomeEquipe.isEmpty) {
      SetHtml("mensagem", mensagemErro(Mensagem.INTERVALO.format(5, 200)))
    }
    else if (Equipe.findEquipeByNome(nomeEquipe).get > 0) {
      SetHtml("mensagem", mensagemErro(Mensagem.NOME_EXISTENTE))
    }
    else {
      val e = Equipe.create(idLider,
        nomeEquipe,
        DateTime.now,
        None)

      val eq = Equipe.findEquipeQuantUsuario(e.idEquipe)

      equipeRV.set(eq)
      _ajaxRenderRow(true, false) &
        SetHtml("mensageSucesso", mensagemSucesso(Mensagem.CADASTRO_SALVO_SUCESSO.format("Equipe"))) &
        cancelarNovoUsuario
    }
  }

  private def cancelarNovoUsuario = {
    limparCampos
    novoEquipeVisivel.is match {
      case Some(false) => novoEquipeVisivel.set(Full(true))
        JsCmds.SetHtml("formNovoEquipe", <div></div>) &
          JsCmds.JsShowId("adicionaNovoEquipe")
      case _ => novoEquipeVisivel.set(Empty)
        JsCmds.Noop
    }
  }


  private def limparCampos = {
    nome = ""
    projetos = ""
    nomeEquipe = ""
  }

  private def adicionarFormulario = {
    novoEquipeVisivel.is match {
      case Some(true) =>
        novoEquipeVisivel.set(Full(false))
        JsCmds.SetHtml("formNovoEquipe", formCadastroEquipe) &
          JsCmds.JsHideId("adicionaNovoEquipe") &
          SetHtml("mensageSucesso", Text(Mensagem.MSN_VAZIA))
      case _ => novoEquipeVisivel.set(Empty)
        JsCmds.Noop
    }
  }

  def adicionaNovoEquipe = {
    "#adicionaNovoEquipe" #> SHtml.ajaxButton(Text("Cadastrar novo"), () => adicionarFormulario)
  }

  private def editar(id: Long) = {
    editarPerfilEquipe.set(Some(id))
    S.redirectTo("/sistema/equipe/editar")
  }

  def lista(in: NodeSeq): NodeSeq = {
    val e = Equipe.findAll()
    listTamplateRVEquipe(in)
    _rowTemplate(e);
  }

  private def associatedGuid(l: Long): Option[String] = {
    val map = guidToIdRVEquipe.is;
    map.find(e => l == e._2) match {
      case Some(e) => Some(e._1)
      case None =>
        val guid = nextFuncName
        guidToIdRVEquipe.set(map + (guid -> l))
        Some(guid)
    }
  }

  private def _rowTemplate(equipe: List[(Int, String, Option[String], Int)]): NodeSeq = {
    val in = listTamplateRVEquipe.is
    val cssSel =
      "#row" #> equipe.map(e => {
        val guid = associatedGuid(e._1).get
        "#row [id]" #> (guid) &
          ".listaEquipe [class]" #> "gradeA" &
          cellSelector("nome") #> Text(e._2) &
          cellSelector("membros") #> Text(e._4.toString) &
          cellSelector("lider") #> Text(e._3.getOrElse("")) &
          "#editar [onclick]" #> SHtml.ajaxInvoke(() => editar(e._1)) &
          "#deletar" #> SHtml.ajaxButton(Text("Excluir"), () => notificarExcluirEquipe(e._1, guid))
      })
    cssSel.apply(in)
  }

  private def notificarExcluirEquipe(idEquipe: Long, guid: String) = {
    val node = S.runTemplate("sistema" :: "equipe" :: "equipe-hidden" :: "_modal_excluir" :: Nil)
    node match {
      case Full(nd) => Bs3ConfirmDialog("Excluir Equipe", nd, () => excluirEquipe(idEquipe, guid), () => JsCmds.Noop)
      case _ => Bs3ConfirmDialog("Erro ao carregar tela", NodeSeq.Empty, () => JsCmds.Noop, () => JsCmds.Noop)
    }
  }

  private def erroExcluirEquipe(m: String): JsCmd = {
    val node = S.runTemplate("sistema" :: "equipe" :: "equipe-hidden" :: "_modal_aviso" :: Nil)
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

  private def _ajaxDelete(idEquipe: Long, guid: String): JsCmd = {
    guidToIdRVEquipe.set(guidToIdRVEquipe.is - guid)
    Equipe.destroy(idEquipe);
    JsCmds.Replace(guid, NodeSeq.Empty)
  }

  private def excluirEquipe(idEquipe: Long, guid: String) = {
    try {
      _ajaxDelete(idEquipe, guid)
    }
    catch {
      case e: Exception => {erroExcluirEquipe("Erro ao excluir equipe: Existe projetos relacionados.") }
      case _: Throwable => {erroExcluirEquipe("Erro ao excluir equipe: Existe projetos relacionados. Mensagem original: ") }
    }
  }

  private def _ajaxRenderRow(isNew: Boolean, selected: Boolean): JsCmd = {
    val templateRowRoot = TEMPLATE_LIST_ID;
    var xml: NodeSeq = NodeSeq.Empty
    var idEquipe: Long = -1

    equipeRV.is match {
      case Some(eRV) => xml = _rowTemplate(List(eRV)); idEquipe = eRV._1
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
        val guid = associatedGuid(idEquipe).get
        op = Some(JsCmds.Replace(guid, ajaxRow));
      }
    }
    op.get
  }

  private val formCadastroEquipe: NodeSeq =
    <div class="lift:SEquipe.addNovoEquipe">
      <div class="col-md-4">
        <div class="panel panel-default">
          <div class="panel-heading">
            <i class="fa fa-bell fa-fw"></i>
            Cadastrar novo equipe
          </div>
          <div class="panel-body">
            <div id="mensagem"></div>
            <form class="lift:form.ajax" method="post">
              <fieldset style="margin-bottom:5px;">
                <div>
                  <label>* Nome Equipe</label> <br/>
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
