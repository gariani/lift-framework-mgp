package code.snippet

import net.liftweb.common.Box
import net.liftweb.http.{LiftView, SHtml}
import net.liftweb.util.Helpers._

import scala.xml.{NodeSeq, _}

/**
  * Created by daniel on 29/02/16.
  */
class Teste /*CometActor*/ {

  var teste: String = "123"

  //override def defaultPrefix = Full("comet")

  def render = {
    "#teste" #> SHtml.text(teste, teste = _)
  }

  //Schedule.schedule(this, Teste, 1000L)

  def exemplo(xhtml: NodeSeq) = {
    Text("Teste 12312312")
  }

  /*override def lowPriority: PartialFunction[Any, Unit] = {
    case Teste => {
      partialUpdate(SetHtml("teste_id", <div>update: teste teste</div>))
      Schedule.schedule(this, teste, 1000L)
    }
  }*/

}

case object Teste
