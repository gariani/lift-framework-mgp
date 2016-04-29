package code.snippet

import net.liftweb.http.SHtml
import net.liftweb.http.js.JsCmds.Alert
import net.liftweb.http.js.jquery.JqJsCmds.Unblock
import net.liftweb.util.Helpers._

import scala.xml.NodeSeq

/**
  * Created by daniel on 24/04/16.
  */
class ConfirmarExcluir {

  def confirmacao =
  "#sim" #> ((b: NodeSeq) => SHtml.ajaxButton(b, () => Unblock & Alert("Dado excluído"))) &
    "#nao" #> ((b: NodeSeq) => <button onclick={Unblock.toJsCmd}> {b} </button>)
}
