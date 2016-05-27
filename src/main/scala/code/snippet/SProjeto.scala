package code.snippet

import code.lib.Util._
import code.model.{Cliente, Projeto}
import net.liftmodules.widgets.bootstrap.Bs3ConfirmDialog
import net.liftweb.common.Full
import net.liftweb.http.js.{JsCmd, JsCmds}
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.http.{SessionVar, S, SHtml, StatefulSnippet}
import net.liftweb.util
import org.joda.time.DateTime
import util.Helpers._

import scala.xml.{Text, NodeSeq}

/**
  * Created by daniel on 27/03/16.
  */

object guidIdClienteProjeto extends SessionVar[Option[Projeto]](None)

class SProjeto extends StatefulSnippet {

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
    case "listaProjetos" => listaProjetos
  }

  private def retornarTemplateItemTareafa = {
    var temp: NodeSeq = NodeSeq.Empty
    temp = S.runTemplate("sistema" :: "projeto" :: "projeto-hidden" :: "_abas" :: Nil).openOr(<div>
      {"Template não encontrado"}
    </div>)

    temp
  }

  def listaProjetos(nodeSeq: NodeSeq): NodeSeq = {
    val temp = retornarTemplateItemTareafa
    val clientes = Cliente.findAll
    val cli = clientes.asInstanceOf[List[Cliente]]
    val cssSel =
      "#items" #> cli.map(c => {
        val guid = associatedGuid(c.idCliente).get
        "#idcliente [id]" #> (guid) &
        ".cliente" #> SHtml.a(Text(c.nomeCliente), JsCmds.Noop, ("data-toggle" -> "collapse"), ("href" -> retornarGuid(guid))) &
          ".projetos" #> {
            "#projetos [id]" #> (guid) &
            ".list-group-item *" #> (c.projetos.map(p => {
                SHtml.a(Text(p.nomeProjeto), informarIdProjeto(p))
            }))
          }
      })
    cssSel.apply(temp)
  }

  private def informarIdProjeto(projeto: Projeto) = {
    guidIdClienteProjeto.set(Some(projeto))
    JsCmds.RedirectTo("/sistema/projeto/projeto_individual")
  }

  def retornarIdNodo(guid: String) = {
    "" + guid
  }

  private def retornarGuid(guid: String): String = {
    "#" + guid
  }

  private def associatedGuid(l: Long): Option[String] = {
    val map = guidToIdRVUsuario.is;
    map.find(e => l == e._2) match {
      case Some(e) => Some(e._1)
      case None =>
        val guid = nextFuncName
        guidToIdRVUsuario.set(map + (guid -> l))
        Some(guid)
    }
  }


  def render = "#teste" #> Text("")
}
