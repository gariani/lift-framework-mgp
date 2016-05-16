package code.lib

import _root_.java.util.{UUID}
import _root_.scala.xml.{NodeSeq, Text}

import net.liftweb.common.{Box, Full}
import net.liftweb.http.{S, SHtml}
import net.liftweb.util.{Helpers}
import net.liftweb.http.js.{JsCmd, JsCmds}
import net.liftweb.http.js.JsCmds.{Alert, After, Noop}
import net.liftweb.http.js.jquery.{JqJsCmds}
import net.liftweb.http.js.jquery.JqJsCmds.{ModalDialog, Unblock}

import Helpers._

object FormDialog {
  def apply(template: String, modal: Boolean, title: String) = new FormDialog(modal, title) {
    override def getFormContent = S.runTemplate(List(template)) openOr Text("Cannot find template")
  }

  def apply(node: NodeSeq, modal: Boolean, title: String) = new FormDialog(modal, title) {
    override def getFormContent = node
  }
}

/**
  *
  *
  */
class FormDialog(modal: Boolean, title: String) {
  lazy val dialogId: String = "dialog_box_%s".format(UUID.randomUUID)

  protected def createDialog(node: NodeSeq): JsCmd = new JQueryDialog(node, title) {
    override def elementId = dialogId
    override def options = "modal:%s".format(modal) :: super.options
  }

  def button(title: String): NodeSeq = {
    SHtml.ajaxButton(title,
      () => createDialog(_bindTemplate(getFormContent)))
  }

  def a(title: String, opt: String): NodeSeq = {
    SHtml.a(() => createDialog(_bindTemplate(getFormContent)), Text(title), "class" -> opt)
  }

  /**
    *
    */
  protected def submitCmds: JsCmd = Noop

  protected def getFormContent: NodeSeq = Text("No content defined")

  protected def _bindTemplate(node: NodeSeq): NodeSeq = {
    SHtml.ajaxForm(<div>
      <div class={"dialog_content"}>{node}</div>
      <div class={"dialog_actions"}>
        {confirmDialog}
      </div>
    </div>,
      submitCmds)
  }

  protected def closeButton: NodeSeq = {
    <button onclick={closeCmd.toJsCmd}>{"Fechar"}</button>
  }

  protected def confirmDialog: NodeSeq = closeButton
  protected def closeCmd: JsCmd = JQueryDialog.close(dialogId)
}