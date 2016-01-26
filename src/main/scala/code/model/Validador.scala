package code.model

import code.dao.UsuarioDAO

/**
  * Created by daniel on 05/01/16.
  */
object Validador {

  def isValidEmail(email: String) = {
    val usuarioDao = new UsuarioDAO
    !usuarioDao.findByEmail(email).isEmpty
  }

  def isValidLogin(email: String, senha: String): Boolean = {
    val usuarioDAO = new UsuarioDAO
    !usuarioDAO.findByLogin(email, senha).isEmpty
  }

}
