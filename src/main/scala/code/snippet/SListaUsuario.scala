package code.snippet

import code.model.Usuario
import net.liftweb.http.js.JsCmds.{Alert, SetHtml}
import net.liftweb.http.js.jquery.JqJsCmds
import net.liftweb.http.js.jquery.JqJsCmds.ModalDialog
import net.liftweb.util.Helpers
import net.liftweb.common._
import net.liftweb.http.js.{JsCmds, JsCmd}
import net.liftweb.http._
import net.liftweb.util.Helpers._
import org.joda.time.DateTime
import code.lib.Util._
import scala.xml.{NodeSeq, Text}
import net.liftweb.util.Helpers.strToCssBindPromoter

object novoUsuarioVisivel extends RequestVar[Option[Boolean]](Some(true))
object editarPerfilUsuario extends SessionVar[Option[String]](None)
object listTamplateRVUsuario extends RequestVar[NodeSeq](Nil)
object guidToIdRVUsuario extends RequestVar[Map[String, Long]](Map())
object usuarioRV extends RequestVar[Option[Usuario]](None)
private object formTemplateUsuarioRV extends RequestVar[NodeSeq](Nil)

class SListaUsuario extends StatefulSnippet with Logger {

  private val TEMPLATE_LIST_ID = "lista-usuarios";


  def dispatch = {
    case "render" => adicionaNovoUsuario
    case "lista" => lista
  }

  private def adicionarFormulario = {
    novoUsuarioVisivel.is match {
      case Some(true) =>
        novoUsuarioVisivel.set(Full(false))
        JsCmds.SetHtml("formNovoUsuario", formCadstroUsuario) &
          JsCmds.JsHideId("adicionaNovoUsuario") &
          SetHtml("mensageSucesso", Text(Mensagem.MSN_VAZIA))
      case _ => novoUsuarioVisivel.set(Empty)
        JsCmds.Noop
    }
  }

  def adicionaNovoUsuario = {
    "#adicionaNovoUsuario" #> SHtml.ajaxButton(Text("Cadastrar novo"), () => adicionarFormulario)
  }

  private def editar(email: String) = {
    editarPerfilUsuario.set(Some(email))
    S.redirectTo("/sistema/usuario/perfil/perfil")
  }

  def lista(in: NodeSeq): NodeSeq = {
    val products = Usuario.findAll()
    listTamplateRVUsuario(in)
    _rowTemplate(products);
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

  private def _rowTemplate(usuarios: List[Usuario]): NodeSeq = {
    val in = listTamplateRVUsuario.is
    val cssSel =
      "#row" #> usuarios.map(u => {
        val guid = associatedGuid(u.idUsuario).get
        "#row [id]" #> (guid) &
          ".listaUsuario [class]" #> "gradeA" &
          cellSelector("id") #> Text(u.idUsuario.toString) &
          cellSelector("nome") #> Text(u.nome) &
          cellSelector("email") #> Text(u.email) &
          "#editar [onclick]" #> SHtml.ajaxInvoke(() => editar(u.email)) &
          "#deletar [onclick]" #> {
            SHtml.ajaxInvoke(() => {
              S.runTemplate(List("/sistema/templates-hidden/_confirmar_exclusao"))
            }.map(ns => ModalDialog(ns)) openOr Alert("Erro ao excluir!")
            )
          }
      })
    cssSel.apply(in)
  }

  private def cellSelector(p: String): String = {
    "#" + p + " *"
  }

  private def _ajaxDelete(p: Usuario, guid: String): JsCmd = {
    guidToIdRVUsuario.set(guidToIdRVUsuario.is - guid)
    Usuario.destroy(p.idUsuario);
    JsCmds.Replace(guid, NodeSeq.Empty)
  }

  private def _ajaxRenderRow(u: Usuario, isNew: Boolean, selected: Boolean): JsCmd = {
    val templateRowRoot = TEMPLATE_LIST_ID;
    var xml: NodeSeq = NodeSeq.Empty
    var idUsuario: Long = -1

    usuarioRV.is match {
      case Some(uRV) => xml = _rowTemplate(List(uRV)); idUsuario = uRV.idUsuario
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

  private val formCadstroUsuario: NodeSeq =
    <div class="lift:SFormularioCadastroUsuario">
      <div class="col-md-4">
        <div class="panel panel-default">
          <div class="panel-heading">
            <i class="fa fa-bell fa-fw"></i>
            Cadastrar novo usuário
          </div>
          <div class="panel-body">
            <div id="mensagem"></div>
            <form class="lift:form.ajax" method="post">
              <fieldset style="margin-bottom:5px;">
                <div>
                  <label>* Nome</label> <br/>
                  <input class="form-control" type="text" id="nome" name="nome"/>
                </div>
                <div class="form-group">
                  <label>* E-mail</label>
                  <div class="form-group input-group">
                    <span class="input-group-addon">@</span>
                    <input class="form-control" type="text" id="email" name="email"/>
                  </div>
                </div>
                <label>* Senha</label> <br/>
                <input class="form-control" type="password" id="senha" name="senha"/>
              </fieldset>
              <div>
                <input type="submit" id="adicionarNovo" value="Cadastrar" name="adicionarNovo" class="btn btn-primary">
                  <span class="glyphicon glyphicon-ok"></span>
                </input>
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
