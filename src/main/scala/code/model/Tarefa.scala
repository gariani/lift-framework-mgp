package code.model


import java.sql.Time

import code.lib.Settings
import net.liftweb.common.{Full, Box}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import scalikejdbc._

/**
  * Created by daniel on 04/04/16.
  */

case class Tarefa(idTarefa: Long,
                  idProjeto: Option[Long],
                  idUsuarioResponsavel: Long,
                  nomeTarefa: String,
                  descricao: Option[String],
                  idTipoTarefa: Option[Long],
                  idStatusTarefa: Option[Long],
                  estimativa: Option[String],
                  dtInicioTarefa: Option[DateTime],
                  dtFinalTarefa: Option[DateTime],
                  dtEntregaTarefa: Option[DateTime],
                  idCreatedBy: Long,
                  createdAt: DateTime,
                  deletedAt: Option[DateTime]) {


  def save()(implicit session: DBSession = Tarefa.autoSession): Tarefa = Tarefa.save(this)(session)

  def destroy()(implicit session: DBSession = Tarefa.autoSession): Unit = Tarefa.destroy(idTarefa)(session)

  private val (t) = (Tarefa.t)

}

object Tarefa extends SQLSyntaxSupport[Tarefa] with Settings {

  override val tableName = "tarefa"

  override val columns = Seq("id_tarefa", "id_projeto", "id_usuario_responsavel", "nome_tarefa", "descricao", "id_tipo_tarefa", "id_status_tarefa", "estimativa",
    "dt_inicio_tarefa", "dt_final_tarefa", "dt_entrega_tarefa", "id_created_by", "created_at", "deleted_at")

  def apply(t: SyntaxProvider[Tarefa])(rs: WrappedResultSet): Tarefa = apply(t.resultName)(rs)

  def apply(t: ResultName[Tarefa])(rs: WrappedResultSet): Tarefa = new Tarefa(
    idTarefa = rs.long(t.idTarefa),
    idProjeto = rs.longOpt(t.idProjeto),
    idUsuarioResponsavel = rs.long(t.idUsuarioResponsavel),
    nomeTarefa = rs.string(t.nomeTarefa),
    descricao = rs.stringOpt(t.descricao),
    idTipoTarefa = rs.longOpt(t.idTipoTarefa),
    idStatusTarefa = rs.longOpt(t.idStatusTarefa),
    estimativa = rs.stringOpt(t.estimativa),
    dtInicioTarefa = rs.jodaDateTimeOpt(t.dtInicioTarefa),
    dtFinalTarefa = rs.jodaDateTimeOpt(t.dtFinalTarefa),
    dtEntregaTarefa = rs.jodaDateTimeOpt(t.dtEntregaTarefa),
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

  def findAllDetalhe()(implicit session: DBSession = AutoSession) =
    sql"""select
       t.id_tarefa,
       t.id_projeto,
       t.id_status_tarefa,
       t.id_usuario_responsavel,
       t.id_created_by,
       t.descricao,
       t.id_tipo_tarefa,
       t.estimativa,
       t.dt_inicio_tarefa,
       t.dt_final_tarefa,
       t.dt_entrega_tarefa,
       t.nome_tarefa,
       t.created_at,
       s.nome_status_tarefa,
       u.nome as nome_usuario,
       p.nome_projeto,
       c.nome_cliente
       from tarefa t
       left join tipo_tarefa tt on t.id_tipo_tarefa = tt.id_tipo_tarefa
       left join status_tarefa s on t.id_status_tarefa = s.id_status_tarefa
       left join usuario u on t.id_usuario_responsavel = u.id_usuario
       left join projeto p on p.id_projeto = t.id_projeto
       left join cliente c on c.id_cliente = p.id_cliente
       where t.deleted_at is null
      """
      .map { rs => (rs.long("id_tarefa"),
        rs.longOpt("id_projeto"),
        rs.longOpt("id_status_tarefa"),
        rs.longOpt("id_usuario_responsavel"),
        rs.longOpt("id_created_by"),
        rs.stringOpt("descricao"),
        rs.stringOpt("id_tipo_tarefa"),
        rs.stringOpt("estimativa"),
        rs.jodaDateTimeOpt("dt_inicio_tarefa"),
        rs.jodaDateTimeOpt("dt_final_tarefa"),
        rs.jodaDateTimeOpt("dt_entrega_tarefa"),
        rs.string("nome_tarefa"),
        rs.stringOpt("created_at"),
        rs.stringOpt("nome_status_tarefa"),
        rs.stringOpt("nome_usuario"),
        rs.stringOpt("nome_projeto"),
        rs.stringOpt("nome_cliente"))
      }.list().apply()

  def create(idProjeto: Option[Long],
             idUsuarioResponsavel: Long,
             nomeTarefa: String,
             descricao: Option[String],
             idTipoTarefa: Option[Long],
             idStatusTarefa: Option[Long],
             estimativa: Option[String],
             dtInicioTarefa: Option[DateTime],
             dtFinalTarefa: Option[DateTime],
             dtEntregaTarefa: Option[DateTime],
             idCreatedBy: Long,
             createdAt: DateTime)
            (implicit session: DBSession = AutoSession): Tarefa = {

    val id = withSQL {
      insert.into(Tarefa).namedValues(
        column.idProjeto -> idProjeto,
        column.idUsuarioResponsavel -> idUsuarioResponsavel,
        column.nomeTarefa -> nomeTarefa,
        column.descricao -> descricao,
        column.idTipoTarefa -> idTipoTarefa,
        column.idStatusTarefa -> idStatusTarefa,
        column.estimativa -> estimativa,
        column.dtInicioTarefa -> dtInicioTarefa,
        column.dtFinalTarefa -> dtFinalTarefa,
        column.dtEntregaTarefa -> dtEntregaTarefa,
        column.idCreatedBy -> idCreatedBy,
        column.createdAt -> createdAt
      )
    }.updateAndReturnGeneratedKey.apply()

    Tarefa(
      idTarefa = id,
      idProjeto = idProjeto,
      idUsuarioResponsavel = idUsuarioResponsavel,
      nomeTarefa = nomeTarefa,
      descricao = descricao,
      idTipoTarefa = idTipoTarefa,
      idStatusTarefa = idStatusTarefa,
      estimativa = estimativa,
      dtInicioTarefa = dtInicioTarefa,
      dtFinalTarefa = dtFinalTarefa,
      dtEntregaTarefa = dtEntregaTarefa,
      idCreatedBy = idCreatedBy,
      createdAt = createdAt,
      None
    )
  }

  def saveDescricaoTarefa(name: Box[String], primary: Box[String], value: Box[String]) = {
    var id = primary match {
      case Full(p) => p.toLong
    }
    var descricao = value match {
      case Full(v) => v
    }
    val tup: Seq[((SQLSyntax, String))] = Seq(column.descricao -> descricao)
    saveTarefa(id, tup)
  }

  def salvarDataDesejada(name: Box[String], primary: Box[String], value: Box[String]) = {
    var id = primary match {
      case Full(p) => p.toLong
    }
    var dtFinalTarefa = value match {
      case Full(v) => v
    }

    var uiDateFormat = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm");
    var dtFinal = uiDateFormat.parseDateTime(dtFinalTarefa)

    val tup: Seq[((SQLSyntax, DateTime))] = Seq(column.dtFinalTarefa -> dtFinal)
    saveDataTarefa(id, tup)
  }

  def salvarDataEntrega(name: Box[String], primary: Box[String], value: Box[String]) = {
    var id = primary match {
      case Full(p) => p.toLong
    }
    var dtEntregaTarefa = value match {
      case Full(v) => v
    }

    var uiDateFormat = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm");
    var dtEntrega = uiDateFormat.parseDateTime(dtEntregaTarefa)

    val tup: Seq[((SQLSyntax, DateTime))] = Seq(column.dtEntregaTarefa -> dtEntrega)
    saveDataTarefa(id, tup)
  }

  def salvarEstimativa(name: Box[String], primary: Box[String], value: Box[String]) = {
    var id = primary match {
      case Full(p) => p.toLong
    }
    var estimativa = value match {
      case Full(v) => v
    }
    val tup: Seq[((SQLSyntax, String))] = Seq(column.estimativa -> estimativa)
    saveTarefa(id, tup)
  }

  def salvarStatus(name: Box[String], primary: Box[String], value: Box[String]) = {
    var id = primary match {
      case Full(p) => p.toLong
    }
    var id_status_tarefa = value match {
      case Full(v) => v
    }
    val tup: Seq[((SQLSyntax, String))] = Seq(column.idStatusTarefa -> id_status_tarefa)
    saveTarefa(id, tup)
  }

  def saveDataTarefa(id: Long, tup: Seq[((SQLSyntax, DateTime))])(implicit session: DBSession = AutoSession) = withSQL {
    update(Tarefa).set(tup: _*).where.eq(column.c("id_tarefa"), id)
  }.update.apply()

  def saveTarefa(id: Long, tup: Seq[((SQLSyntax, String))])(implicit session: DBSession = AutoSession) = withSQL {
    update(Tarefa).set(tup: _*).where.eq(column.c("id_tarefa"), id)
  }.update.apply()


  def save(t: Tarefa)(implicit session: DBSession = AutoSession): Tarefa = {
    withSQL {
      update(Tarefa).set(
        Tarefa.column.idTarefa -> t.idTarefa,
        Tarefa.column.idProjeto -> t.idProjeto,
        Tarefa.column.idUsuarioResponsavel -> t.idUsuarioResponsavel,
        Tarefa.column.nomeTarefa -> t.nomeTarefa,
        Tarefa.column.descricao -> t.descricao,
        Tarefa.column.estimativa -> t.estimativa,
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
