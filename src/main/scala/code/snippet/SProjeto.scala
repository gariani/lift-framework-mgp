package code.snippet

import code.lib.Util._
import code.model.Projeto
import net.liftmodules.widgets.bootstrap.Bs3ConfirmDialog
import net.liftweb.common.Full
import net.liftweb.http.js.{JsCmd, JsCmds}
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.http.{S, SHtml, StatefulSnippet}
import net.liftweb.util
import org.joda.time.DateTime
import util.Helpers._

import scala.xml.{Text, NodeSeq}

/**
  * Created by daniel on 27/03/16.
  */

object SProjeto extends StatefulSnippet {

  private var nomeProjeto: String = ""
  private var descricao: String = ""
  private var idProjeto: Long = 0
  private var idCliente: Option[Long] = Some(0)
  private var idEquipe: Option[Long] = Some(0)
  private var dtInicioProjeto: Option[DateTime] = Some(DateTime.now)
  private var dtFinalProjeto: Option[DateTime] = Some(DateTime.now)
  private var createdAt: DateTime = DateTime.now
  private var deletedAt: Option[DateTime] = Some(DateTime.now)
  private var projeto: Option[Projeto] = None


  def dispatch = {
    case "render" => render
  }

  def render = "#teste" #> Text("")
}
