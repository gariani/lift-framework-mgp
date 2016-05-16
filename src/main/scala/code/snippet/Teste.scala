package code.snippet


import code.lib.{FormDialog, JQueryDialog}
import net.liftweb._
import net.liftweb.json.JValue
import json._
import net.liftweb.http.{SHtml, TemplateFinder}
import scala.xml._
import net.liftweb.json.JsonDSL._
import net.liftweb._
import net.liftweb.http.{S, SHtml, StatefulSnippet}
import net.liftweb.http.js.{JsCmds, JsCmd}
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE._
import net.liftweb.util.Helpers._

import net.liftmodules.extras._



class Teste {

  def dialog(node: NodeSeq): NodeSeq = {
    FormDialog("/sistema/temp", true, "Cadastrar Tarefa").button("show dialog")
  }

  def edit(node: NodeSeq): NodeSeq = {

    def _template: NodeSeq = bind("item",
      S.runTemplate("sistema" :: "tarefa" :: "tarefa-hidden" :: "_modal_nova_tarefa" :: Nil) openOr
        <div>{"Cannot find template"}</div>,
      "name" -> "")

    val dialog = new FormDialog(true, "Cadastrar Tarefa") {
      override def getFormContent = _template
      override def confirmDialog: NodeSeq = SHtml.ajaxSubmit("save",
        () => {println("");this.closeCmd}) ++ super.confirmDialog
    }

    dialog.button("Nova Tarefa")
  }

}