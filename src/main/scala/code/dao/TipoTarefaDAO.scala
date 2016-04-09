package code.dao

import code.model.{TipoTarefa, Usuario}

class TipoTarefaDAO {

  def findAll() = {
    TipoTarefa.findAll()
  }

  def save(usuario: Usuario) = {
    Usuario.save(usuario)
  }

}
