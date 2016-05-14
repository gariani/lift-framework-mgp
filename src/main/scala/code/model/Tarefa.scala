package code.model


import java.sql.Time

import code.lib.Settings
import org.joda.time.DateTime
import scalikejdbc._

/**
  * Created by daniel on 04/04/16.
  */

case class Tarefa(idTarefa: Long,
                  idUsuarioResponsavel: Long,
                  nomeTarefa: String,
                  descricao: Option[String],
                  idTipoTarefa: Option[Long],
                  idStatusTarefa: Option[Long],
                  esforco: Option[DateTime],
                  dtInicioTarefa: Option[DateTime],
                  dtFinalTarefa: Option[DateTime],
                  idCreatedBy: Long,
                  createdAt: DateTime,
                  deletedAt: Option[DateTime]) {


  def save()(implicit session: DBSession = Tarefa.autoSession): Tarefa = Tarefa.save(this)(session)

  def destroy()(implicit session: DBSession = Tarefa.autoSession): Unit = Tarefa.destroy(idTarefa)(session)

  private val (t) = (Tarefa.t)

}

object Tarefa extends SQLSyntaxSupport[Tarefa] with Settings {

  override val tableName = "tarefa"

  override val columns = Seq("id_tarefa", "id_usuario_responsavel", "nome_tarefa", "descricao", "id_tipo_tarefa", "id_status_tarefa", "esforco",
    "dt_inicio_tarefa", "dt_final_tarefa", "id_created_by", "created_at", "deleted_at")

  def apply(t: SyntaxProvider[Tarefa])(rs: WrappedResultSet): Tarefa = apply(t.resultName)(rs)

  def apply(t: ResultName[Tarefa])(rs: WrappedResultSet): Tarefa = new Tarefa(
    idTarefa = rs.long(t.idTarefa),
    idUsuarioResponsavel = rs.long(t.idUsuarioResponsavel),
    nomeTarefa = rs.string(t.nomeTarefa),
    descricao = rs.stringOpt(t.descricao),
    idTipoTarefa = rs.longOpt(t.idTipoTarefa),
    idStatusTarefa = rs.longOpt(t.idStatusTarefa),
    esforco = rs.jodaDateTimeOpt(t.esforco),
    dtInicioTarefa = rs.jodaDateTimeOpt(t.dtInicioTarefa),
    dtFinalTarefa = rs.jodaDateTimeOpt(t.dtFinalTarefa),
    idCreatedBy = rs.long(t.idCreatedBy),
    createdAt = rs.jodaDateTime(t.createdAt),
    deletedAt = rs.jodaDateTimeOpt(t.deletedAt)
  )

  val t = Tarefa.syntax("t")

  def findByIdTarefa(idTarefa: Long)(implicit session: DBSession = AutoSession): Option[Tarefa] = {
    withSQL {
      select.from(Tarefa as t).where.eq(t.idTarefa, idTarefa)
    }.map(Tarefa(t)).single().apply()
  }

  def findAll()(implicit session: DBSession = AutoSession): List[Tarefa] = withSQL {
    select.from(Tarefa as t).orderBy(t.idTarefa)
  }.map(Tarefa(t)).list().apply()

  def create(idUsuarioResponsavel: Long,
             nomeTarefa: String,
             descricao: Option[String],
             idTipoTarefa: Option[Long],
             idStatusTarefa: Option[Long],
             esforco: Option[DateTime],
             dtInicioTarefa: Option[DateTime],
             dtFinalTarefa: Option[DateTime],
             idCreatedBy: Long,
             createdAt: DateTime)
            (implicit session: DBSession = AutoSession): Tarefa = {

    val id = withSQL {
      insert.into(Tarefa).namedValues(
        column.idUsuarioResponsavel -> idUsuarioResponsavel,
        column.nomeTarefa -> nomeTarefa,
        column.descricao -> descricao,
        column.idTipoTarefa -> idTipoTarefa,
        column.idStatusTarefa -> idStatusTarefa,
        column.esforco -> esforco,
        column.dtInicioTarefa -> dtInicioTarefa,
        column.dtFinalTarefa -> dtFinalTarefa,
        column.idCreatedBy -> idCreatedBy,
        column.createdAt -> createdAt
      )
    }.updateAndReturnGeneratedKey.apply()

    Tarefa(
      idTarefa = id,
      idUsuarioResponsavel = idUsuarioResponsavel,
      nomeTarefa = nomeTarefa,
      descricao = descricao,
      idTipoTarefa = idTipoTarefa,
      idStatusTarefa = idStatusTarefa,
      esforco = esforco,
      dtInicioTarefa = dtInicioTarefa,
      dtFinalTarefa = dtFinalTarefa,
      idCreatedBy = idCreatedBy,
      createdAt = createdAt,
      None
    )
  }

  def save(t: Tarefa)(implicit session: DBSession = AutoSession): Tarefa = {
    withSQL {
      update(Tarefa).set(
        Tarefa.column.idTarefa -> t.idTarefa,
        Tarefa.column.idUsuarioResponsavel -> t.idUsuarioResponsavel,
        Tarefa.column.nomeTarefa -> t.nomeTarefa,
        Tarefa.column.descricao -> t.descricao,
        Tarefa.column.esforco -> t.esforco,
        Tarefa.column.idStatusTarefa -> t.idStatusTarefa,
        Tarefa.column.dtInicioTarefa -> t.dtInicioTarefa,
        Tarefa.column.dtFinalTarefa -> t.dtFinalTarefa,
        Tarefa.column.idCreatedBy -> t.idCreatedBy
      ).where.eq(Tarefa.column.idTarefa, t.idTarefa).and.isNull(column.deletedAt)
    }.update.apply()
    t
  }

  def destroy(idTarefa: Long)(implicit session: DBSession = AutoSession): Unit = withSQL {
    delete.from(Tarefa).where.eq(column.idTarefa, idTarefa)
  }.update.apply()

}
