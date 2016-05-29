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
                  emDesenvolvimento: Boolean,
                  dtInicioTarefa: Option[DateTime],
                  dtFinalTarefa: Option[DateTime],
                  dtEntregaTarefa: Option[DateTime],
                  idCreatedBy: Long,
                  createdAt: DateTime,
                  deletedAt: Option[DateTime]) {


  def save()(implicit session: DBSession = Tarefa.autoSession): Tarefa = Tarefa.save(this)(session)

  def destroy()(implicit session: DBSession = Tarefa.autoSession): Unit = Tarefa.destroy(idTarefa)(session)

  private val (t, st, tt, u) = (Tarefa.t, StatusTarefa.st, TipoTarefa.tt, Usuario.u)

}

object Tarefa extends SQLSyntaxSupport[Tarefa] with Settings {

  override val tableName = "tarefa"

  override val columns = Seq("id_tarefa", "id_projeto", "id_usuario_responsavel", "nome_tarefa", "descricao",
    "id_tipo_tarefa", "id_status_tarefa", "estimativa", "em_desenvolvimento",
    "dt_inicio_tarefa", "dt_final_tarefa", "dt_entrega_tarefa",
    "id_created_by", "created_at", "deleted_at")

  def apply(t: SyntaxProvider[Tarefa])(rs: WrappedResultSet): Tarefa = apply(t)(rs)

  /*def apply(t: ResultName[Tarefa], tt: ResultName[TipoTarefa], st: ResultName[StatusTarefa], p: ResultName[Projeto], c: ResultName[Cliente], u: ResultName[Usuario])
           (rs: WrappedResultSet): Tarefa = {
    apply(t)(rs).copy(tipoTarefa = rs.longOpt(t.idTipoTarefa).map(_ => TipoTarefa(tt)(rs)), statusTarefa = rs.longOpt(t.idStatusTarefa).map(_ => StatusTarefa(st)(rs)),
      projeto = rs.longOpt(t.idProjeto).map(_ => Projeto(p)(rs)), cliente = rs.longOpt(t.idCliente).map(_ => Cliente(c)(rs)),
      usuario = rs.longOpt(t.idUsuario).map(_ => Usuario(u)(rs)))

  }*/

  /*
   def apply(m: ResultName[GroupMember], g: ResultName[Group])(rs: WrappedResultSet) =  {
    apply(m)(rs).copy(group = rs.longOpt(g.id).map(_ => Group(g)(rs)))
  }
   */

  def apply(t: ResultName[Tarefa], tt: ResultName[TipoTarefa], st: ResultName[StatusTarefa], p: ResultName[Projeto],
            c: ResultName[Cliente], u: ResultName[Usuario])(rs: WrappedResultSet): Tarefa = new Tarefa(
    idTarefa = rs.long(t.idTarefa),
    idProjeto = rs.longOpt(t.idProjeto),
    idUsuarioResponsavel = rs.long(t.idUsuarioResponsavel),
    nomeTarefa = rs.string(t.nomeTarefa),
    descricao = rs.stringOpt(t.descricao),
    idTipoTarefa = rs.longOpt(t.idTipoTarefa),
    idStatusTarefa = rs.longOpt(t.idStatusTarefa),
    estimativa = rs.stringOpt(t.estimativa),
    emDesenvolvimento = rs.boolean(t.emDesenvolvimento),
    dtInicioTarefa = rs.jodaDateTimeOpt(t.dtInicioTarefa),
    dtFinalTarefa = rs.jodaDateTimeOpt(t.dtFinalTarefa),
    dtEntregaTarefa = rs.jodaDateTimeOpt(t.dtEntregaTarefa),
    idCreatedBy = rs.long(t.idCreatedBy),
    createdAt = rs.jodaDateTime(t.createdAt),
    deletedAt = rs.jodaDateTimeOpt(t.deletedAt)
  )

  val (t, st, tt, u, p, c) = (Tarefa.syntax("t"), StatusTarefa.syntax("st"), TipoTarefa.syntax("tt"),
    Usuario.syntax("u"), Projeto.syntax("p"), Cliente.syntax("c"))

  def findByIdTarefa(idTarefa: Long)(implicit session: DBSession = AutoSession): Option[Tarefa] = {
    withSQL {
      select.from(Tarefa as t).where.eq(t.idTarefa, idTarefa)
    }.map(Tarefa(t)).single().apply()
  }

  def findAll()(implicit session: DBSession = AutoSession): List[Tarefa] = withSQL {
    select.from(Tarefa as t).orderBy(t.idTarefa)
  }.map(Tarefa(t)).list().apply()

  /*def findAllTarefaStatus()(implicit session: DBSession = AutoSession) = withSQL {
    select.from(Tarefa as t)
      .leftJoin(StatusTarefa as st).on(t.idStatusTarefa, st.idStatusTarefa)
      .leftJoin(TipoTarefa as tt).on(t.idTipoTarefa, tt.idTipoTarefa)
      .leftJoin(Usuario as u).on(t.idUsuarioResponsavel, u.idUsuario)
      .leftJoin(Projeto as p).on(t.idProjeto, p.idProjeto)
      .leftJoin(Cliente as c).on(p.idCliente, c.idCliente)
  }.one(Tarefa(t))
    .toManies(
      rs => StatusTarefa.opt(st)(rs),
      rs => TipoTarefa.opt(tt)(rs),
      rs => Usuario.opt(u)(rs),
      rs => Projeto.opt(p)(rs),
      rs => Cliente.opt(c)(rs))
    .map { (tarefa, status, tipoTarefa, usuario, projeto, cliente) =>
      tarefa.copy(status = status, tipoTarefa = tipoTarefa, usuario = usuario, projeto = projeto, cliente = cliente)
    }.list().apply()*/

  def findMeuDetalhe(email: String)(implicit session: DBSession = AutoSession) =
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
       left join projeto p on t.id_projeto = p.id_projeto
       left join cliente c on c.id_cliente = p.id_cliente
       where t.deleted_at is null and t.id_usuario_responsavel = (select u2.id_usuario from usuario as u2 where u2.email = ${email})
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

  def findCrieiDetalhe(email: String)(implicit session: DBSession = AutoSession) =
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
       left join projeto p on t.id_projeto = p.id_projeto
       left join cliente c on c.id_cliente = p.id_cliente
       where t.deleted_at is null and
       t.id_created_by = (select u2.id_usuario from usuario as u2 where u2.email = ${email})
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

  def findTarefaDetalhe(idTarefa: Long)(implicit session: DBSession = AutoSession) =
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
       tt.nome_tipo_tarefa,
       s.nome_status_tarefa,
       u.nome as nome_responsavel,
       u2.nome as nome_criador,
       p.nome_projeto,
       c.nome_cliente
       from tarefa t
       left join tipo_tarefa tt on t.id_tipo_tarefa = tt.id_tipo_tarefa
       left join status_tarefa s on t.id_status_tarefa = s.id_status_tarefa
       left join usuario u on t.id_usuario_responsavel = u.id_usuario
       left join usuario u2 on t.id_created_by= u2.id_usuario
       left join projeto p on t.id_projeto = p.id_projeto
       left join cliente c on c.id_cliente = p.id_cliente
       where t.deleted_at is null and
       t.id_tarefa = ${idTarefa}
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
        rs.jodaDateTimeOpt("created_at"),
        rs.stringOpt("nome_tipo_tarefa"),
        rs.stringOpt("nome_status_tarefa"),
        rs.stringOpt("nome_responsavel"),
        rs.stringOpt("nome_criador"),
        rs.stringOpt("nome_projeto"),
        rs.stringOpt("nome_cliente"))
      }.single().apply()

  /*def findAllDetalheTeste()(implicit session: DBSession = AutoSession) =
    sql"""
          select
          ${t.result.*},
          ${tt.result.*},
          ${st.result.*},
          ${u.result.*},
          ${p.result.*},
          ${c.result.*}
          from ${Tarefa.as(t)}
          left join ${TipoTarefa.as(tt)} on ${t.idTipoTarefa} = ${tt.idTipoTarefa}
          left join ${StatusTarefa.as(st)} on ${t.idStatusTarefa} = ${st.idStatusTarefa}
          left join ${Usuario.as(u)} on ${t.idUsuarioResponsavel} = ${u.idUsuario}
          left join ${Projeto.as(p)} on ${t.idProjeto} = ${p.idProjeto}
          left join ${Cliente.as(c)} on ${c.idCliente} = ${p.idCliente}
          where ${t.result.deletedAt} is null
          """
      .map(Tarefa(t.resultName, st.resultName, u.resultName, p.resultName, c.resultName))
      .list().apply()*/


  def create(idProjeto: Option[Long],
             idUsuarioResponsavel: Long,
             nomeTarefa: String,
             descricao: Option[String],
             idTipoTarefa: Option[Long],
             idStatusTarefa: Option[Long],
             estimativa: Option[String],
             emDesenvolvimento: Boolean,
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
        column.emDesenvolvimento -> emDesenvolvimento,
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
      emDesenvolvimento = emDesenvolvimento,
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

  def retornarTarefasEntregues(idCliente: Long, idProjeto: Long)(implicit session: DBSession = AutoSession) = {
    sql"""
       select count(t.id_tarefa)
       from projeto p
         left join tarefa t on t.id_projeto = p.id_projeto and t.dt_entrega_tarefa is not null
         left join cliente c on p.id_cliente = c.id_cliente
       where c.id_cliente = ${idCliente} and p.id_projeto = ${idProjeto}
      """.map(rs => (rs.double(1))).single().apply()
  }

  def retornarTarefasNaoEntregues(idCliente: Long, idProjeto: Long)(implicit session: DBSession = AutoSession) = {
    sql"""
       select count(t.id_tarefa)
       from projeto p
         left join tarefa t on t.id_projeto = p.id_projeto and t.dt_entrega_tarefa is null
         left join cliente c on p.id_cliente = c.id_cliente
      where c.id_cliente = ${idCliente} and p.id_projeto = ${idProjeto}
      """.map(rs => (rs.double(1))).single().apply()
  }

  def retornarTarefasEmDesenlvimento(idCliente: Long, idProjeto: Long)(implicit session: DBSession = AutoSession) = {
    sql"""
       select count(t.id_tarefa)
       from projeto p
         left join tarefa t on t.id_projeto = p.id_projeto
          and t.em_desenvolvimento = true
          and t.dt_entrega_tarefa is NULL
          and t.dt_final_tarefa is NULL
        left join cliente c on p.id_cliente = c.id_cliente
      where c.id_cliente = ${idCliente} and p.id_projeto = ${idProjeto}
      """.map(rs => (rs.double(1))).single().apply()
  }

  def retornarTotalTarefas(idCliente: Long, idProjeto: Long)(implicit session: DBSession = AutoSession) = {
    sql"""
      select count(t.id_tarefa)
      from projeto p
      left join tarefa t on t.id_projeto = p.id_projeto
      left join cliente c on p.id_cliente = c.id_cliente
      where c.id_cliente = ${idCliente} and p.id_projeto = ${idProjeto}
      """.map(rs => (rs.int(1))).single().apply()
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
