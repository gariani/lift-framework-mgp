package code.rest

import net.liftweb.http.S
import net.liftweb.http.rest.RestHelper
import net.liftweb.json.JValue
import net.liftweb.json.JsonAST.JString

/**
  * Created by daniel on 15/03/16.
  */
object MyRest extends RestHelper {

  serve {
    case "sistema" :: "usuario" :: "perfil" :: email :: _ JsonGet _ =>  JString(email)
  }



}
