package code.snippet

import net.liftweb.http.{SHtml, StatefulSnippet}
import net.liftweb.util
import code.model.{Cliente}
import net.liftweb.http.js.JsCmds.{Alert}
import net.liftweb.http.js.jquery.JqJsCmds
import net.liftweb.http.js.jquery.JqJsCmds.ModalDialog
import net.liftweb.http.js.{JsCmds, JsCmd}
import net.liftweb.http._
import net.liftweb.util.Helpers._
import scala.xml.{NodeSeq, Text}

object novoClienteVisivel extends RequestVar[Option[Boolean]](Some(true))
object editarPerfilCliente extends SessionVar[Option[Long]](None)

private object listTamplateRVCliente extends RequestVar[NodeSeq](Nil)

private object guidToIdRVCliente extends RequestVar[Map[String, Long]](Map())
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

    private def _rowTemplate(cliente: List[Cliente]): NodeSeq = {
      val in = listTamplateRVCliente.is
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
      guidToIdRVCliente.set(guidToIdRVCliente.is - guid)
      Cliente.destroy(c.idCliente);
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
