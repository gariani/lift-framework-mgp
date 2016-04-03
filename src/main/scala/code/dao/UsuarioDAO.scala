package code.dao

import code.model.Usuario
import com.roundeights.hasher.Implicits._
import net.liftweb.common._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

class UsuarioDAO {

  def findByEmail(email: String): Option[Usuario] = {
    Usuario.findByEmail(email)
  }

  def findByLogin(email: Option[String], senha: Option[String]): Boolean = {
    val s = senha.get.sha256.toString()
    val e = email.getOrElse("-1")
    Usuario.findByLogin(e, s) match {
      case Some(l) => true
      case None => false
    }
  }

  def findAllUsuarios() = {
    Usuario.findAllUsuarios()
  }

  def save(usuario: Usuario) = {
    Usuario.save(usuario)
  }

}
