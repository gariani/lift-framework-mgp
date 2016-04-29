package code.snippet

import net.liftweb.http.{SHtml, StatefulSnippet}
import net.liftweb.util
import util.Helpers._
import code.model.{Cliente, Usuario}
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

object novoClienteVisivel extends RequestVar[Option[Boolean]](Some(true))
object editarPerfilCliente extends SessionVar[Option[Long]](None)
private object listTamplateRV extends RequestVar[NodeSeq](Nil)
private object guidToIdRV extends RequestVar[Map[String, Long]](Map())
private object clienteRV extends RequestVar[Option[Cliente]](None)

class SCliente extends StatefulSnippet {

    private val TEMPLATE_LIST_ID = "lista";
    private var nome: String = ""
    private var projetos: String = ""

    def dispatch = {
      case "lista" => lista
    }

    private def limparCampos = {
      nome = ""
      projetos = ""
    }

    private def editar(id: Long) = {
      editarPerfilCliente.set(Some(id))
      S.redirectTo("/sistema/cliente/cliente")
    }

    def lista(in: NodeSeq): NodeSeq = {
      val c = Cliente.findAll()
      listTamplateRV(in)
      _rowTemplate(c);
    }

    private def associatedGuid(l: Long): Option[String] = {
      val map = guidToIdRV.is;
      map.find(e => l == e._2) match {
        case Some(e) => Some(e._1)
        case None =>
          val guid = nextFuncName
          guidToIdRV.set(map + (guid -> l))
          Some(guid)
      }
    }

    private def _rowTemplate(cliente: List[Cliente]): NodeSeq = {
      val in = listTamplateRV.is
      val cssSel =
        "#row" #> cliente.map(c => {
          val guid = associatedGuid(c.idCliente).get
          "#row [id]" #> (guid) &
            ".lista [class]" #> "gradeA" &
            cellSelector("id") #> Text(c.idCliente.toString) &
            cellSelector("nome") #> Text(c.nomeCliente) &
            //cellSelector("projetos") #> Text(c.projetos) &
            "#editar [onclick]" #> SHtml.ajaxInvoke(() => editar(c.idCliente)) &
            "#deletar [onclick]" #> {
              SHtml.ajaxInvoke(() => {
                S.runTemplate(List("/sistema/templates-hidden/_confirmar_exclusao"))
              }.map(ns => ModalDialog(ns)) openOr Alert("Erro ao excluir!")
              )}
        })
      cssSel.apply(in)
    }

    private def cellSelector(p: String): String = {
      "#" + p + " *"
    }

    private def _ajaxDelete(c: Cliente, guid: String): JsCmd = {
      guidToIdRV.set(guidToIdRV.is - guid)
      Usuario.destroy(c.idCliente);
      JsCmds.Replace(guid, NodeSeq.Empty)
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

}
