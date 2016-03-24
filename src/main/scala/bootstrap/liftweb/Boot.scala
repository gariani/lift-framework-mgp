package bootstrap.liftweb

import code.rest.MyRest
import net.liftweb._
import common._
import http._
import net.liftweb.sitemap._
import Loc._

/*object DbxToken extends
  SessionVar[Option[omniauth.AuthInfo]](None)*/

class Boot extends Loggable {

  def boot {
    //omniauth.Omniauth.init
    //LiftRules.dispatch.append(MyRest)

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

   /* DbxToken.get.map{auth =>
      val client = new DbxClient(
        new DbxRequestConfig(
          "Teste",
          S.locale.toString),
        auth.token)
    }

    val getDbxToken = EarlyResponse(() => {
      omniauth.Omniauth.currentAuth.map { a =>
        DbxToken(Full(a))
      }
      S.redirectTo("/")
    })*/

    def siteMap = List (
      Menu("Login") / "index",
      Menu.i("Home") / "sistema" / "index",
      Menu("Perfil") / "sistema"/ "usuario" / "perfil"
      /*Menu(Loc(
        "Gmail Authenticated",
        List("/sistema/index"),
        S.?("dropbox"),
        getDbxToken))*/

    )// ++ omniauth.Omniauth.sitemap

    LiftRules.setSiteMap(Site.siteMap)
  }
}

object Site {

  val login = Menu("Login") / "index"
  val home = Menu.i("Home") / "sistema" / "index" //>> If( () => SessionState.estaLogado, RedirectResponse("/"))
  val perfil = Menu("Perfil") / "sistema"/ "usuario" / "perfil" //>> If( () => SessionState.estaLogado, RedirectResponse("/"))

  def siteMap = SiteMap (
    login,
    home,
    perfil
  )
}
