package bootstrap.liftweb

import code.lib.session.SessionState
import code.rest.PerfilUsuarioRest
import net.liftmodules.extras.{LiftExtras, Gravatar}
import net.liftweb._
import common._
import http._
import net.liftweb.sitemap._
import Loc._
import net.liftweb.util.NamedPF
import scala.xml.Text

class Boot extends Loggable {

  def boot {

    LiftRules.dispatch.append(PerfilUsuarioRest)

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

    LiftRules.setSiteMap(Site.siteMap)

    LiftRules.uriNotFound.prepend(NamedPF("404handler"){
      case (req,failure) =>
        NotFoundAsTemplate(ParsePath(List("404"),"html",false,false))
    })

  }
}

object Site {

  val login = Menu("Login") / "index"
  val home = Menu.i("Home") / "sistema" / "index" >> If( () => SessionState.estaLogado, RedirectResponse("/"))
  val perfil = Menu("Perfil") / "sistema"/ "usuario" / "perfil" >> If( () => SessionState.estaLogado, RedirectResponse("/"))
  val projeto = Menu("Projeto") / "sistema"/ "projeto" / "index" >> If( () => SessionState.estaLogado, RedirectResponse("/"))
  val cliente = Menu("Projeto") / "sistema"/ "cliente" / "index" >> If( () => SessionState.estaLogado, RedirectResponse("/"))

  def siteMap = SiteMap (
    login,
    home,
    perfil,
    projeto,
    cliente
  )
}
