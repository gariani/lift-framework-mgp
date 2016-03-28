package code.rest

import code.lib.session.SessionState
import net.liftweb.http.S
import net.liftweb.http.rest.RestHelper
import net.liftweb.json.JValue
import net.liftweb.json.JsonAST.{JValue, JString}

/**
  * Created by daniel on 15/03/16.
  */

object PerfilUsuarioRest extends RestHelper {
  serve {
    case "api" :: "perfil" :: id :: _ JsonGet _ =>  JString(id)
  }
}
