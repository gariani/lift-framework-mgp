package code.snippet

import code.comet.{Excluir, ItemsServer}
import code.model.Usuario
import code.view.{DataTableParams, DataTableObjectSource, DataTable}
import net.liftweb.common.Box
import net.liftweb.http.js.{JE, JsCmd, JsCmds}
import net.liftweb.http._
import net.liftweb.util.Helpers._
import code.util.{Droppable, Draggable, DragDrop}
import code.model._
import scala.xml.NodeSeq

import scala.xml.{NodeSeq, _}

class Teste extends StatefulSnippet {

  def dispatch = {
    case "render" => render
  }

  def render = {

    var titleText = ""
    var bodyText = ""

    "#titleText" #> SHtml.ajaxText(titleText, (a: String) => {
      titleText = a;
      JsCmds.Noop
    }) &
      "#bodyText" #> SHtml.ajaxTextarea(bodyText, (a: String) => {
        bodyText = a;
        JsCmds.Noop
      }) &
      "#submit [onclick]" #> SHtml.ajaxCall(JE.JsRaw("CKEDITOR.instances.bodyText.getData()"),
        (x: String) => {
          bodyText = x;
          println(
            "titleText=" + titleText + "\n" +
              "bodyText=" + bodyText + "\n"
          );
        }
      )
  }
}