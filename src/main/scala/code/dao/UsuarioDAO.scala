package code.dao

import code.model.Usuario
import com.roundeights.hasher.Implicits._
import net.liftweb.common._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

case class UsuarioPerfil(id_usuario: String, email: String, nome: String, cargo: String,
                   observacao: String, telefone: Long, senha: String){
  def toJson = {
    ("id_usuario" -> id_usuario) ~ ("email" -> email) ~ ("nome" -> nome) ~ ("cargo" -> cargo) ~
      ("observacao" -> observacao) ~ ("telefone" -> telefone) ~ ("senha" -> senha)
  }
}

class UsuarioDAO {

  def findByEmail(email: String): String = {
    Usuario.findByEmail(email) match {
      case Some(e) => e
      case None => ""
    }
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

  def findUser(email: String): Option[Usuario] = {
    Usuario.findUser(email)
  }

  def save(usuario: Usuario) = {
    Usuario.save(usuario)
  }

}
