package code.model

import code.lib.Settings
import org.joda.time.DateTime
import scalikejdbc._

case class Projeto(idProjeto: Long,
                   idCliente: Option[Long],
                   idEquipe: Option[Long],
                   nomeProjeto: String,
                   descricaoProjeto: Option[String],
                   dtInicioProjeto: Option[DateTime],
                   dtFinalProjeto: Option[DateTime],
                   createdAt: DateTime,
                   deletedAt: Option[DateTime]) {

  def save()(implicit session: DBSession = Projeto.autoSession): Projeto = Projeto.save(this)(session)

  def criarMinimoProjeto()(implicit session: DBSession = Projeto.autoSession): Projeto = Projeto.criarMinimoProjeto(this)(session)

  def destroy()(implicit session: DBSession = Projeto.autoSession): Unit = Projeto.destroy(idProjeto)(session)

  private val (p) = (Projeto.p)
}

object Projeto extends SQLSyntaxSupport[Projeto] with Settings {

  override val tableName = "projeto"

  override val columns = Seq("id_projeto", "id_cliente", "id_equipe", "nome_projeto", "descricao_projeto", "dt_inicio_projeto", "dt_final_projeto", "created_at", "deleted_at")

  def opt(p: SyntaxProvider[Projeto])(rs: WrappedResultSet) =
    rs.longOpt(p.resultName.idProjeto).map(_ => Projeto(p.resultName)(rs))

  def apply(p: SyntaxProvider[Projeto])(rs: WrappedResultSet): Projeto = apply(p.resultName)(rs)

  def apply(p: ResultName[Projeto])(rs: WrappedResultSet): Projeto = new Projeto(
    idProjeto = rs.get(p.idProjeto),
    idCliente = rs.longOpt(p.idCliente),
    idEquipe = rs.longOpt(p.idEquipe),
    nomeProjeto = rs.get(p.nomeProjeto),
    descricaoProjeto = rs.stringOpt(p.descricaoProjeto),
    dtInicioProjeto = rs.jodaDateTimeOpt(p.dtInicioProjeto),
    dtFinalProjeto = rs.jodaDateTimeOpt(p.dtFinalProjeto),
    createdAt = rs.jodaDateTime(p.createdAt),
    deletedAt = rs.jodaDateTimeOpt(p.deletedAt)
  )

  val p = Projeto.syntax("p")

  def findAll()(implicit session: DBSession = autoSession): List[Projeto] = withSQL {
    select.from(Projeto as p).where.isNull(p.deletedAt)
  }.map(Projeto(p)).list().apply()

  def findAllProjetoCliente()(implicit session: DBSession = autoSession) = withSQL {
    select(p.idProjeto, p.nomeProjeto).from(Projeto as p).where.isNull(p.deletedAt)
  }.map { rs => (rs.int(1), rs.string(2)) }.list().apply()


  def findProjetoById(idProjeto: Long)(implicit sesession: DBSession = AutoSession): Option[Projeto] = withSQL {
    select.from(Projeto as p).where.eq(p.idProjeto, idProjeto)
  }.map(Projeto(p)).single().apply()

  def create(idCliente: Option[Long], idEquipe: Option[Long], nomeProjeto: String, descricaoProjeto: Option[String],
             dtInicioProjeto: Option[DateTime], dtFinalProjeto: Option[DateTime], createdAt: DateTime)
            (implicit session: DBSession = AutoSession): Projeto = {

    val id = withSQL {
      insert.into(Projeto).namedValues(
        column.idCliente -> idCliente,
        column.idEquipe -> idEquipe,
        column.nomeProjeto -> nomeProjeto,
        column.descricaoProjeto -> descricaoProjeto,
        column.dtInicioProjeto -> dtInicioProjeto,
        column.dtFinalProjeto -> dtFinalProjeto,
        column.createdAt -> createdAt
      )
    }.updateAndReturnGeneratedKey.apply()

    Projeto(
      idProjeto = id,
      idCliente = idCliente,
      idEquipe = idEquipe,
      nomeProjeto = nomeProjeto,
      descricaoProjeto = descricaoProjeto,
      dtInicioProjeto = dtInicioProjeto,
      dtFinalProjeto = dtFinalProjeto,
      createdAt = createdAt,
      deletedAt = None
    )
  }

  def save(p: Projeto)(implicit session: DBSession = autoSession): Projeto = {
    withSQL {
      update(Projeto).set(
        Projeto.column.idProjeto -> p.idProjeto,
        Projeto.column.nomeProjeto -> p.nomeProjeto,
        Projeto.column.descricaoProjeto -> p.descricaoProjeto,
        Projeto.column.dtInicioProjeto -> p.dtInicioProjeto,
        Projeto.column.dtFinalProjeto -> p.dtFinalProjeto,
        Projeto.column.createdAt -> p.createdAt,
        Projeto.column.deletedAt -> p.deletedAt).where.eq(Projeto.column.idProjeto, p.idProjeto)
    }.update().apply()
    p
  }

  def alterarNomeProjetoDescricao(idProjeto: Long, nmProjeto: String, descricao: String)(implicit session: DBSession = autoSession) = {
    withSQL {
      update(Projeto).set(
        Projeto.column.idProjeto -> idProjeto,
        Projeto.column.nomeProjeto -> nmProjeto,
        Projeto.column.descricaoProjeto -> descricao
      ).where.eq(Projeto.column.idProjeto, idProjeto)
    }.update().apply()
  }

  def retornarDataInicioFimProjeto(idCliente: Long, idProjeto: Long)(implicit session: DBSession = AutoSession) = {
    sql"""
      select count(t.id_tarefa), min(t.dt_inicio_tarefa), max(t.dt_final_tarefa)
      from tarefa t
      join projeto p on t.id_projeto = p.id_projeto
      join cliente c on p.id_cliente = c.id_cliente
      where c.id_cliente = ${idCliente} and p.id_projeto = ${idProjeto}
      """.map(rs => (rs.int(1), rs.jodaDateTimeOpt(2), rs.jodaDateTimeOpt(3))).single().apply()
  }

  def criarMinimoProjeto(p: Projeto)
                        (implicit session: DBSession = AutoSession): Projeto = {
    withSQL {
      update(Projeto).set(
        Projeto.column.nomeProjeto -> p.nomeProjeto,
        Projeto.column.descricaoProjeto -> p.descricaoProjeto
      ).where.eq(Projeto.column.idProjeto, p.idProjeto).and.eq(Projeto.column.idCliente, p.idCliente)
    }.update().apply()
    p
  }

  def destroy(idProjeto: Long)(implicit session: DBSession = AutoSession): Unit = withSQL {
    delete.from(Projeto).where.eq(column.idProjeto, idProjeto)
  }.update.apply()

}
