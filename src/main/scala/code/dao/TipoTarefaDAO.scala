package code.dao

import code.model.{TipoTarefa, Usuario}

class TipoTarefaDAO {

  def findAll() = {
    TipoTarefa.findAll()
  }

  def save(tipoTarefa: TipoTarefa) = {
    TipoTarefa.save(tipoTarefa)
  }

  def delete(idTipoTarefa: Long) =
  {
    TipoTarefa.destroy(idTipoTarefa)
  }

}
