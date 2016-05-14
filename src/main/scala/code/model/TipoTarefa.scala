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
                      foraUso: Boolean,
                      createdAt: DateTime,
                      deletedAt: Option[DateTime] = None){

  def save()(implicit session: DBSession = TipoTarefa.autoSession): TipoTarefa = TipoTarefa.save(this)(session)

  def destroy()(implicit session: DBSession = TipoTarefa.autoSession): Unit = TipoTarefa.destroy(idTipoTarefa)(session)

  private val (tt) = (TipoTarefa.tt)

}

object TipoTarefa extends SQLSyntaxSupport[TipoTarefa] with Settings {

  override val tableName = "tipo_tarefa"

  override val columns = Seq("id_tipo_tarefa", "nome_tipo_tarefa", "estimativa", "fora_uso", "created_at", "deleted_at")

  def apply(tt: SyntaxProvider[TipoTarefa])(rs: WrappedResultSet): TipoTarefa = apply(tt.resultName)(rs)
  def apply(tt: ResultName[TipoTarefa])(rs: WrappedResultSet): TipoTarefa = new TipoTarefa(
    idTipoTarefa = rs.get(tt.idTipoTarefa),
    nomeTipoTarefa = rs.get(tt.nomeTipoTarefa),
    estimativa = rs.timeOpt(tt.estimativa),
    foraUso = rs.get(tt.foraUso),
    createdAt = rs.jodaDateTime(tt.createdAt),
    deletedAt = rs.jodaDateTimeOpt(tt.deletedAt)
  )

  val tt = TipoTarefa.syntax("tt")

  def findByIdTipoTarefa(idTipoTarefa: Long)(implicit session: DBSession = AutoSession): Option[TipoTarefa] = {
    withSQL{
      select.from(TipoTarefa as tt).where.eq(tt.idTipoTarefa, idTipoTarefa)
    }.map(TipoTarefa(tt)).single().apply()
  }

  def findAll()(implicit session: DBSession = AutoSession): List[TipoTarefa] = withSQL {
      select.from(TipoTarefa as tt).where.isNull(tt.deletedAt).orderBy(tt.idTipoTarefa)
  }.map(TipoTarefa(tt)).list().apply()

  def findAllTipoTarefaLista()(implicit session: DBSession = AutoSession): List[(Int, String)] = withSQL {
    select(tt.idTipoTarefa, tt.nomeTipoTarefa).from(TipoTarefa as tt).where.isNull(tt.deletedAt).orderBy(tt.idTipoTarefa)
  }.map{rs => (rs.int(1), rs.string(2))}.list().apply()


  def create(nomeTipoTarefa: String, estimativa: Option[Time], foraUso: Boolean, createdAt: DateTime)(implicit session: DBSession = AutoSession): TipoTarefa ={

    val id = withSQL {
      insert.into(TipoTarefa).namedValues(
        column.nomeTipoTarefa -> nomeTipoTarefa,
        column.estimativa -> estimativa,
        column.foraUso -> foraUso,
        column.createdAt -> DateTime.now
      )
    }.updateAndReturnGeneratedKey.apply()

    TipoTarefa(
      idTipoTarefa = id,
      nomeTipoTarefa = nomeTipoTarefa,
      estimativa = estimativa,
      foraUso = foraUso,
      createdAt = createdAt
    )

  }

  def save(tt: TipoTarefa)(implicit session: DBSession = AutoSession): TipoTarefa = {
    withSQL {
      update(TipoTarefa).set(
        TipoTarefa.column.nomeTipoTarefa -> tt.nomeTipoTarefa,
        TipoTarefa.column.estimativa -> tt.estimativa,
        TipoTarefa.column.foraUso -> tt.foraUso
      ).where.eq(TipoTarefa.column.idTipoTarefa, tt.idTipoTarefa).and.isNull(column.deletedAt)
    }.update.apply()
    tt
  }

  def foraUsoTipoTarefa(idTipoTarefa: Long, foraUso: Boolean)(implicit session: DBSession = AutoSession): Boolean = {
    withSQL {
      update(TipoTarefa).set(
        TipoTarefa.column.foraUso -> foraUso
      ).where.eq(TipoTarefa.column.idTipoTarefa, idTipoTarefa)
    }.update.apply()
    !foraUso
  }

  def getForaUso(idTipoTarefa: Long)(implicit session: DBSession = AutoSession): Option[Boolean]  = {
    withSQL {
      select(tt.foraUso).from(TipoTarefa as tt).where.eq(tt.idTipoTarefa, idTipoTarefa)
    }.map( rs => (rs.boolean("fora_uso"))).single().apply()

  }

  def destroy(idTipoTarefa: Long)(implicit session: DBSession = AutoSession): Unit = withSQL {
    delete.from(TipoTarefa).where.eq(column.idTipoTarefa, idTipoTarefa)
  }.update.apply()

}
