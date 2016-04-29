package code.snippet

import code.comet.{Excluir, ItemsServer}
import code.model.Usuario
import net.liftweb.common.Box
import net.liftweb.http.js.{JsCmd, JsCmds}
import net.liftweb.http.{RequestVar, LiftView, SHtml}
import net.liftweb.util.Helpers._

import scala.xml.{NodeSeq, _}

/**
  * Created by daniel on 29/02/16.
  */

//private object listTamplateRV extends RequestVar[NodeSeq](Nil)

//private object guidToIdRV extends RequestVar[Map[String, Long]](Map())

/*class Teste {

  def render = {
      "#editar" #> SHtml.onSubmit((s) => editar(s))
  }

  def editar(s: String) = {
    print("teste edicao: " + s)
    //ItemsServer ! Excluir(s)
  }

  def deletar = {
    print("teste deletear")
    JsCmds.Noop
  }

  def list(in:NodeSeq) : NodeSeq = {
    val products = Usuario.findAll()
    listTamplateRV(in)
    _rowTemplate(products);
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

  private def _rowTemplate(products: List[Usuario]): NodeSeq = {
    val in = listTamplateRV.is
    val cssSel =
      ".row" #> products.map(p => {
        val guid = associatedGuid(p.idUsuario).get
        ".row [id]" #> (guid) &
          cellSelector("id") #> Text(p.nome) &
          cellSelector("nome") #> Text(p.email) &
          cellSelector("email") #> {
            SHtml.a(() => {
              _ajaxDelete(p, guid)
            }, Text("Excluir"))
          }
      })
    cssSel.apply(in)
  }

  private def cellSelector(p: String): String = {
    "#" + p + " *"
  }

  private def _ajaxDelete(p: Usuario, guid: String): JsCmd = {
    guidToIdRV.set(guidToIdRV.is - guid)
    Usuario.destroy(p.idUsuario);
    JsCmds.Replace(guid, NodeSeq.Empty)
  }

}

*/