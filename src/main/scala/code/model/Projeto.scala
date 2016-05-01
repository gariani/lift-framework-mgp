package code.model

import code.lib.Settings
import org.joda.time.DateTime
import scalikejdbc._
import net.liftweb.common._

case class Projeto(idProjeto: Long,
                   idCliente: Option[Long],
                   idEquipe: Option[Long],
                   nomeProjeto: String,
                   descricaoProjeto: String,
                   dtInicioProjeto: Option[DateTime],
                   dtFinalProjeto: Option[DateTime],
                   createdAt: DateTime,
                   deletedAt: Option[DateTime]) {

  def save()(implicit session: DBSession = Projeto.autoSession): Projeto = Projeto.save(this)(session)

  def destroy()(implicit session: DBSession = Projeto.autoSession): Unit = Projeto.destroy(idProjeto)(session)

  private val (p) = (Projeto.p)
}

object Projeto extends SQLSyntaxSupport[Projeto] with Settings {

  override val tableName = "projeto"

  override val columns = Seq("id_projeto", "id_cliente", "id_equipe", "nome_projeto", "descricao_projeto", "dt_inicio_projeto", "dt_final_projeto", "created_at", "deleted_at")

  def opt(p: SyntaxProvider[Projeto])(rs: WrappedResultSet) =
    rs.longOpt(p.resultName.idProjeto).map(_ => Projeto(p.resultName)(rs))

  def apply(p: SyntaxProvider[Projeto])(rs: WrappedResultSet): Projeto = apply(p.resultName)(rs)

  def apply(c: ResultName[Projeto])(rs: WrappedResultSet): Projeto = new Projeto(
    idProjeto = rs.get(c.idProjeto),
    idCliente = rs.longOpt(c.idCliente),
    idEquipe = rs.longOpt(c.idEquipe),
    nomeProjeto = rs.get(c.nomeProjeto),
    descricaoProjeto = rs.get(c.descricaoProjeto),
    dtInicioProjeto = rs.jodaDateTimeOpt(c.dtInicioProjeto),
    dtFinalProjeto = rs.jodaDateTimeOpt(c.dtFinalProjeto),
    createdAt = rs.jodaDateTime(c.createdAt),
    deletedAt = rs.jodaDateTimeOpt(c.deletedAt)
  )

  val p = Projeto.syntax("p")

  def findAll()(implicit session: DBSession = autoSession): List[Projeto] = withSQL {
    select.from(Projeto as p).where.isNull(p.deletedAt)
  }.map(Projeto(p)).list().apply()

  def findProjetoById(idProjeto: Long)(implicit sesession: DBSession = AutoSession): Option[Projeto] = withSQL {
    select.from(Projeto as p).where.eq(p.idProjeto, idProjeto)
  }.map(Projeto(p)).single().apply()

  def create(idCliente: Option[Long], idEquipe: Option[Long], nomeProjeto: String, descricaoProjeto: String,
             dtInicioProjeto: Option[DateTime], dtFinalProjeto: Option[DateTime], createdAt: DateTime, deletedAt: Option[DateTime])
            (implicit session: DBSession = AutoSession): Projeto = {

    val id = withSQL {
      insert.into(Projeto).namedValues(
        column.nomeProjeto -> nomeProjeto,
        column.descricaoProjeto -> descricaoProjeto,
        column.dtInicioProjeto -> dtInicioProjeto,
        column.dtFinalProjeto -> dtFinalProjeto,
        column.nomeProjeto -> nomeProjeto,
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
      deletedAt = deletedAt
    )
  }

  def save(p: Projeto)(implicit session: DBSession = autoSession): Projeto = {
    withSQL {
      update(Projeto).set(
        Projeto.column.idProjeto -> p.idProjeto,
        Projeto.column.nomeProjeto -> p.nomeProjeto,
        Projeto.column.descricaoProjeto -> p.nomeProjeto,
        Projeto.column.dtInicioProjeto -> p.dtInicioProjeto,
        Projeto.column.dtFinalProjeto -> p.dtFinalProjeto,
        Projeto.column.createdAt -> p.createdAt,
        Projeto.column.deletedAt -> p.deletedAt).where.eq(Projeto.column.idProjeto, p.idProjeto)
    }.update().apply()
    p
  }

  def destroy(idProjeto: Long)(implicit session: DBSession = AutoSession): Unit = withSQL {
    update(Projeto).set(column.deletedAt -> DateTime.now).where.eq(column.idProjeto, idProjeto)
  }.update.apply()

}