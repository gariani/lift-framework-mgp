package code.model


import java.sql.Time

import code.lib.Settings
import org.joda.time.DateTime
import scalikejdbc._

/**
  * Created by daniel on 04/04/16.
  */

case class TipoTarefa(idTipoTarefa: Long,
                      nomeTipoTarefa: String,
                      estimativa: Option[Time],
                      createdAt: DateTime,
                      deletedAt: Option[DateTime] = None){

  def save()(implicit session: DBSession = TipoTarefa.autoSession): TipoTarefa = TipoTarefa.save(this)(session)

  def destroy()(implicit session: DBSession = TipoTarefa.autoSession): Unit = TipoTarefa.destroy(idTipoTarefa)(session)

  private val (tt) = (TipoTarefa.tt)

}

object TipoTarefa extends SQLSyntaxSupport[TipoTarefa] with Settings {

  override val tableName = "tipo_tarefa"

  override val columns = Seq("id_tipo_tarefa", "nome_tipo_tarefa", "estimativa", "created_at", "deleted_at")

  def apply(tt: SyntaxProvider[TipoTarefa])(rs: WrappedResultSet): TipoTarefa = apply(tt.resultName)(rs)
  def apply(tt: ResultName[TipoTarefa])(rs: WrappedResultSet): TipoTarefa = new TipoTarefa(
    idTipoTarefa = rs.get(tt.idTipoTarefa),
    nomeTipoTarefa = rs.get(tt.nomeTipoTarefa),
    estimativa = rs.timeOpt(tt.estimativa),
    createdAt = rs.get(tt.createdAt),
    deletedAt = rs.jodaDateTimeOpt(tt.deletedAt)
  )

  val tt = TipoTarefa.syntax("tt")

  def findByIdTipoTarefa(idTipoTarefa: Long)(implicit session: DBSession = autoSession): Option[TipoTarefa] = {
    withSQL{
      select.from(TipoTarefa as tt).orderBy(tt.idTipoTarefa)
    }.map(TipoTarefa(tt)).single().apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[TipoTarefa] = withSQL {
      select.from(TipoTarefa as tt).orderBy(tt.idTipoTarefa)
  }.map(TipoTarefa(tt)).list().apply()

  def create(nomeTipoTarefa: String, estimativa: Option[Time], createdAt: DateTime)(implicit session: DBSession = autoSession): TipoTarefa ={

    val id = withSQL {
      insert.into(TipoTarefa).namedValues(
        column.nomeTipoTarefa -> nomeTipoTarefa,
        column.estimativa -> estimativa,
        column.createdAt -> createdAt
      )
    }.updateAndReturnGeneratedKey.apply()

    TipoTarefa(
      idTipoTarefa = id,
      nomeTipoTarefa = nomeTipoTarefa,
      estimativa = estimativa,
      createdAt = createdAt
    )

  }

  def save(tipoTarefa: TipoTarefa)(implicit session: DBSession = autoSession): TipoTarefa = {
    withSQL {
      update(TipoTarefa).set(
        TipoTarefa.column.nomeTipoTarefa -> tipoTarefa.nomeTipoTarefa,
        TipoTarefa.column.estimativa -> tipoTarefa.estimativa
      ).where.eq(TipoTarefa.column.idTipoTarefa, tipoTarefa.idTipoTarefa).and.isNull(column.deletedAt)
    }.update.apply()
    tipoTarefa
  }

  def ativarDesativarTipoTarefa(idTipoTarefa: Long, deletedAt: Option[DateTime])(implicit session: DBSession = autoSession): Long = {
    withSQL {
      update(TipoTarefa).set(
        TipoTarefa.column.deletedAt -> deletedAt
      ).where.eq(TipoTarefa.column.idTipoTarefa, idTipoTarefa)
    }.update.apply()

    idTipoTarefa
  }

  def destroy(idTipoTarefa: Long)(implicit session: DBSession = autoSession): Unit = withSQL {
    delete.from(TipoTarefa).where.eq(column.idTipoTarefa, idTipoTarefa)
  }.update.apply()

}
