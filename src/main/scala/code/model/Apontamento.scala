package code.model

import java.sql.Time

import code.lib.Settings
import org.joda.time.DateTime
import scalikejdbc._

/**
  * Created by daniel on 04/04/16.
  */

case class Apontamento(idApontamento: Long,
                       idTarefa: Long,
                       idUsuario: Long,
                       tempoInicial: DateTime,
                       tempoFinal: Option[DateTime],
                       createdAt: DateTime,
                       deletedAt: Option[DateTime] = None) {

  def save()(implicit session: DBSession = Apontamento.autoSession): Apontamento = Apontamento.save(this)(session)

  def destroy()(implicit session: DBSession = Apontamento.autoSession): Unit = Apontamento.destroy(idApontamento)(session)

  private val (a) = (Apontamento.a)

}

object Apontamento extends SQLSyntaxSupport[Apontamento] with Settings {

  override val tableName = "apontamento"

  override val columns = Seq("id_apontamento", "id_tarefa", "id_usuario", "tempo_inicial", "tempo_final", "created_at", "deleted_at")

  def opt(a: SyntaxProvider[Apontamento])(rs: WrappedResultSet) =
    rs.longOpt(a.idApontamento).map(_ => Apontamento(a.resultName)(rs))

  def apply(a: SyntaxProvider[Apontamento])(rs: WrappedResultSet): Apontamento = apply(a.resultName)(rs)

  def apply(a: ResultName[Apontamento])(rs: WrappedResultSet): Apontamento = new Apontamento(
    idApontamento = rs.get(a.idApontamento),
    idTarefa = rs.get(a.idTarefa),
    idUsuario = rs.get(a.idUsuario),
    tempoInicial = rs.get(a.tempoInicial),
    tempoFinal = rs.jodaDateTimeOpt(a.tempoFinal),
    createdAt = rs.jodaDateTime(a.createdAt),
    deletedAt = rs.jodaDateTimeOpt(a.deletedAt)
  )

  val a = Apontamento.syntax("a")

  def findByUsuarioTarefaApontamento(idTarefa: Long, idUsuario: Long)(implicit session: DBSession = AutoSession): Option[Apontamento] = {
    withSQL {
      select.from(Apontamento as a).where.eq(a.idTarefa, idTarefa).and.eq(a.idUsuario, idUsuario)
    }.map(Apontamento(a)).single().apply()
  }

  def create(idTarefa: Long, idUsuario: Long, tempoInicial: DateTime, createdAt: DateTime)(implicit session: DBSession = AutoSession): Apontamento = {

    val id = withSQL {
      insert.into(Apontamento).namedValues(
        column.idUsuario -> idUsuario,
        column.idTarefa -> idTarefa,
        column.tempoInicial -> tempoInicial,
        column.createdAt -> DateTime.now
      )
    }.updateAndReturnGeneratedKey.apply()

    Apontamento(
      idApontamento = id,
      idTarefa = idTarefa,
      idUsuario = idUsuario,
      tempoInicial = tempoInicial,
      tempoFinal = None,
      createdAt = createdAt
    )
  }

  def save(a: Apontamento)(implicit session: DBSession = AutoSession): Apontamento = {
    withSQL {
      update(Apontamento).set(
        Apontamento.column.idUsuario -> a.idUsuario,
        Apontamento.column.idTarefa -> a.idTarefa,
        Apontamento.column.tempoInicial -> a.tempoInicial
      ).where.eq(Apontamento.column.idApontamento, a.idApontamento).and.isNull(column.deletedAt)
    }.update.apply()
    a
  }

  def destroy(idApontamento: Long)(implicit session: DBSession = AutoSession): Unit = withSQL {
    delete.from(Apontamento).where.eq(column.idApontamento, idApontamento)
  }.update.apply()

}
