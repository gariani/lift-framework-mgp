package code.model


import java.sql.Time

import code.lib.Settings
import org.joda.time.DateTime
import scalikejdbc._

/**
  * Created by daniel on 04/04/16.
  */

case class StatusTarefa(idStatusTarefa: Long,
                        nomeStatusTarefa: String,
                        createdAt: DateTime,
                        deletedAt: Option[DateTime] = None) {

  def save()(implicit session: DBSession = StatusTarefa.autoSession): StatusTarefa = StatusTarefa.save(this)(session)

  def destroy()(implicit session: DBSession = StatusTarefa.autoSession): Unit = StatusTarefa.destroy(idStatusTarefa)(session)

  private val (st) = (StatusTarefa.st)
}

object StatusTarefa extends SQLSyntaxSupport[StatusTarefa] with Settings {

  override val tableName = "status_tarefa"

  override val columns = Seq("id_status_tarefa", "nome_status_tarefa", "created_at", "deleted_at")

  def opt(st: SyntaxProvider[StatusTarefa])(rs: WrappedResultSet) =
    rs.longOpt(st.idStatusTarefa).map(_ => StatusTarefa(st)(rs))

  def apply(st: SyntaxProvider[StatusTarefa])(rs: WrappedResultSet): StatusTarefa = apply(st.resultName)(rs)

  def apply(st: ResultName[StatusTarefa])(rs: WrappedResultSet): StatusTarefa = new StatusTarefa(
    idStatusTarefa = rs.get(st.idStatusTarefa),
    nomeStatusTarefa = rs.get(st.nomeStatusTarefa),
    createdAt = rs.jodaDateTime(st.createdAt),
    deletedAt = rs.jodaDateTimeOpt(st.deletedAt)
  )

  val st = StatusTarefa.syntax("st")

  def findByIdStatusTarefa(idStatusTarefa: Long)(implicit session: DBSession = AutoSession): Option[StatusTarefa] = {
    withSQL {
      select.from(StatusTarefa as st).where.eq(st.idStatusTarefa, idStatusTarefa)
    }.map(StatusTarefa(st)).single().apply()
  }

  def findAll()(implicit session: DBSession = AutoSession): List[StatusTarefa] = withSQL {
    select.from(StatusTarefa as st).orderBy(st.idStatusTarefa)
  }.map(StatusTarefa(st)).list().apply()

  def create(nomeStatusTarefa: String, createdAt: DateTime)
            (implicit session: DBSession = AutoSession): StatusTarefa = {

    val id = withSQL {
      insert.into(StatusTarefa).namedValues(
        column.nomeStatusTarefa -> nomeStatusTarefa,
        column.createdAt -> DateTime.now
      )
    }.updateAndReturnGeneratedKey.apply()

    StatusTarefa(
      idStatusTarefa = id,
      nomeStatusTarefa = nomeStatusTarefa,
      createdAt = createdAt
    )

  }

  def findListaStatus(implicit session: DBSession = AutoSession) = withSQL {
    select(st.idStatusTarefa, st.nomeStatusTarefa).from(StatusTarefa as st).where.isNull(st.deletedAt)
  }.map(rs => (rs.int(1), rs.string(2))).list().apply()

  def save(st: StatusTarefa)(implicit session: DBSession = AutoSession): StatusTarefa = {
    withSQL {
      update(StatusTarefa).set(
        StatusTarefa.column.nomeStatusTarefa -> st.nomeStatusTarefa
      ).where.eq(StatusTarefa.column.idStatusTarefa, st.idStatusTarefa).and.isNull(column.deletedAt)
    }.update.apply()
    st
  }

  def destroy(idStatusTarefa: Long)(implicit session: DBSession = AutoSession): Unit = withSQL {
    delete.from(StatusTarefa).where.eq(column.idStatusTarefa, idStatusTarefa)
  }.update.apply()

}
