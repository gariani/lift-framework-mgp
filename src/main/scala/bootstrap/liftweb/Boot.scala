package bootstrap.liftweb

import code.model.Usuario
import net.liftmodules.FoBo
import net.liftweb._
import net.liftweb.http.auth.{HttpBasicAuthentication, userRoles, AuthRole}
import util._
import common._
import http._
import sitemap._
import Loc._
import scala.xml._
import code.lib.session.SessionState

class Boot extends Loggable{

  def boot {

    FoBo.InitParam.JQuery=FoBo.JQuery1113
    FoBo.InitParam.ToolKit=FoBo.Bootstrap320
    FoBo.InitParam.ToolKit = FoBo.AngularJS141
    FoBo.init()

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

    net.liftmodules.ng.AngularJsRest.init()
    net.liftmodules.ng.Angular.init()

    LiftRules.setSiteMapFunc(() => Site.siteMap)

  }
}

object Site {

  val login = Menu("Login") / "index"
  val home = Menu.i("Home") / "sistema" / "index" //>> If( () => SessionState.estaLogado, RedirectResponse("/"))
  //val teste = Menu(Loc("Static", Link(List("static"), true, "/static/index"), S.loc("StaticContent" , scala.xml.Text("Static Content")),LocGroup("lg2","topRight"))) //>> If( () => SessionState.estaLogado, RedirectResponse("/"))
  val perfil = Menu("Perfil") / "sistema"/ "usuario" / "perfil" //>> If( () => SessionState.estaLogado, RedirectResponse("/"))

  def siteMap = SiteMap (
    login,
    home,
    perfil >> LocGroup("perfil")
    //teste >> LocGroup("teste")
  )
}