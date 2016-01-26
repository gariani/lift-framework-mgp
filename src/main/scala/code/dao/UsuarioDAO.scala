package code.dao

import code.model.Usuario
import code.lib.Settings
import net.liftweb.common.Full
import com.roundeights.hasher.Implicits._

class UsuarioDAO {

  def findByEmail(email: String): String = {
    Usuario.findByEmail(email) match {
      case Some(e) => e
      case None => ""
    }
  }

  def findByLogin(email: String, senha: String): Option[(String, String)] = {
    val s = senha.sha256.toString()
    Usuario.findByLogin(email, s)
  }

  def findAll() = {
    Usuario.findAll()
  }
}
