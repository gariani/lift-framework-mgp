package bootstrap.liftweb

import net.liftmodules.{FoBo}
import net.liftmodules.extras.{LiftExtras, Gravatar}
import net.liftweb._
import common._
import http._
import net.liftweb.util.{NamedPF}
import scala.xml.Text
import sitemap._
import Loc._

class Boot extends Loggable {

  def boot {

    LiftRules.addToPackages("code")

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))

    // Init Extras
    LiftExtras.init()
    LiftRules.addToPackages("net.liftmodules.extras")
    LiftExtras.errorTitle.default.set(Full(<em>Error!</em>))
    LiftExtras.warningTitle.default.set(Full(Text("Warning!")))
    LiftExtras.noticeTitle.default.set(Full(Text("Info!")))
    LiftExtras.successTitle.default.set(Full(Text("Success!")))
    LiftExtras.artifactName.default.set("extras-example-0.4.0")
    Gravatar.defaultImage.default.set("wavatar")

    FoBo.InitParam.JQuery = FoBo.JQuery214
    FoBo.InitParam.ToolKit = FoBo.Bootstrap320
    FoBo.InitParam.ToolKit = FoBo.FontAwesome430
    FoBo.init()

    LiftRules.setSiteMap(Site.siteMap)

    LiftRules.noticesToJsCmd

    LiftRules.uriNotFound.prepend(NamedPF("404handler"){
      case (req,failure) =>
        NotFoundAsTemplate(ParsePath(List("404"),"html",false,false))
    })

  }
}

object Site {


  val home = Menu.i("Home") / "sistema" / "index"
  val login = Menu("Login") / "index"
  val tarefas = Menu("Tarefas") / "sistema"/ "tarefa" / "tarefa"
  val perfil = Menu(Loc("perfil", Link(List("sistema", "usuario", "perfil", "perfil"), true, "/sistema/usuario/perfil/perfil"), S.loc("perfil", Text(""))))
  val projeto = Menu("Projeto") / "sistema"/ "projeto" / "index"

  var admin = Menu("Administrador") / "sistema" / "cliente" / "administrador" submenus(
      Menu.i("Usuários") / "sistema" / "usuario" / "configuracao" / "configuracao_usuario",
      Menu.i("Cliente->Projeto") / "sistema" / "cliente" / "cliente",
      Menu.i("Equipes") / "sistema" / "equipe" / "equipe",
      Menu.i("Tipos de Tarefas") / "sistema" / "tarefa" / "tipo_tarefa" / "tipo_tarefa" submenus (
        Menu.i("Editar Tipo de Tarefas") / "sistema" / "tarefa" / "tipo_tarefa" / "editar" >> Hidden
        ),
      Menu.i("Status de Tarefa") / "sistema" / "status_tarefa" / "status_tarefa"
    )

  def siteMap = SiteMap (
    home,    //>> If( () => SessionState.estaLogado, RedirectResponse("/")),
    login,
    tarefas, // If( () => SessionState.estaLogado, RedirectResponse("/")),
    perfil,  //>> If( () => SessionState.estaLogado, RedirectResponse("/")),
    projeto, // >> If( () => SessionState.estaLogado, RedirectResponse("/")),
    admin   //>> If( () => SessionState.estaLogado, RedirectResponse("/"))
  )
}
