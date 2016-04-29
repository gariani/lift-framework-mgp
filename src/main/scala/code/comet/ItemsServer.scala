package code.comet


import java.text.SimpleDateFormat
import java.util
import java.util.{Date, Calendar}
import code.lib.DependencyFactory
import code.model.Usuario
import net.liftweb.common.{Box, Full}
import net.liftweb.http.js.JE.Str
import net.liftweb.http.js.jquery.JqJsCmds.AppendHtml
import net.liftweb.http.js.{JsCmds, JsCmd}
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.http._
import net.liftweb.actor._
import net.liftweb.util._
import scala.xml.Text

/**
  * Created by daniel on 24/04/16.
  */

case class Item(id: Integer, nome: String, data: Box[String])
case class Excluir(idUsuario: Long)

object ItemsServer extends LiftActor with ListenerManager {

  private var items = List[Usuario]()//Usuario.findAll()

  private def excluir(idUsuario: Long) = {
    Usuario.destroy(idUsuario)
  }

  def createUpdate = items

  override def lowPriority = {
    case Excluir(id) => {
      excluir(id);
      novaLista;
      updateListeners()
    }
  }

  def novaLista = {
    items = Usuario.findAll
  }

   def hora = {
     var format = new SimpleDateFormat("hh:mm:ss")
     format.format(Calendar.getInstance.getTime)
   }
}



//case object Message
