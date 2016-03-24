package code.lib

import code.dao.UsuarioDAO

/**
  * Created by daniel on 05/01/16.
  */
object Validador {

  private val emailRegex = """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r
  private val TAM_MIN_NOME = 3
  private val TAM_MAX_NOME = 100

  def isValidoEmail(email: String): Boolean = {
    val usuarioDao = new UsuarioDAO
    !usuarioDao.findByEmail(email).isEmpty
  }

  def isValidoLogin(email: Option[String], senha: Option[String]): Boolean = {
    val usuarioDAO = new UsuarioDAO
    usuarioDAO.findByLogin(email, senha)
  }

  def validarEmail(e: String): Boolean = e match {
    case null => false
    case e if emailRegex.findFirstMatchIn(e).isDefined => true
    case e if e.trim.isEmpty => false
  }

  def validarMinTamanhoNome(n: String): Boolean = n match {
    case null => false
    case n if n.trim.isEmpty => false
    case n if (n.length < TAM_MIN_NOME) => false
    case n if (n.length >= TAM_MIN_NOME) => true
  }

  def validarMaxTamanhoNome(n: String): Boolean = n match {
    case null => false
    case n if n.trim.isEmpty => false
    case n if (n.length > TAM_MAX_NOME) => false
    case n if (n.length <= TAM_MAX_NOME) => true
  }
}
