package code.model

import code.lib.Settings
import org.joda.time.DateTime
import scalikejdbc._

case class Equipe(idEquipe: Long,
                  nomeEquipe: String,
                  createdAt: DateTime,
                  deletedAt: Option[DateTime]) {

  def save()(implicit session: DBSession = Equipe.autoSession): Equipe = Equipe.save(this)(session)

  def destroy()(implicit session: DBSession = Equipe.autoSession): Unit = Equipe.destroy(idEquipe)(session)

  private val (e) = (Equipe.e)
}

object Equipe extends SQLSyntaxSupport[Equipe] with Settings {

  override val tableName = "Equipe"

  override val columns = Seq("id_Equipe", "nome_Equipe", "created_at", "deleted_at")

  def opt(p: SyntaxProvider[Equipe])(rs: WrappedResultSet) =
    rs.longOpt(p.resultName.idEquipe).map(_ => Equipe(p.resultName)(rs))

  def apply(p: SyntaxProvider[Equipe])(rs: WrappedResultSet): Equipe = apply(e.resultName)(rs)

  def apply(e: ResultName[Equipe])(rs: WrappedResultSet): Equipe = new Equipe(
    idEquipe = rs.get(e.idEquipe),
    nomeEquipe = rs.get(e.nomeEquipe),
    createdAt = rs.jodaDateTime(e.createdAt),
    deletedAt = rs.jodaDateTimeOpt(e.deletedAt)
  )

  val e = Equipe.syntax("e")

  def findAll()(implicit session: DBSession = autoSession): List[Equipe] = withSQL {
    select.from(Equipe as e).where.isNull(e.deletedAt)
  }.map(Equipe(e)).list().apply()

  def findEquipeById(idEquipe: Long)(implicit sesession: DBSession = AutoSession): Option[Equipe] = withSQL {
    select.from(Equipe as e).where.eq(e.idEquipe, idEquipe)
  }.map(Equipe(e)).single().apply()

  def create(idEquipe: Option[Long], nomeEquipe: String, createdAt: DateTime, deletedAt: Option[DateTime])
  (implicit session: DBSession = AutoSession): Equipe = {

    val id = withSQL {
      insert.into(Equipe).namedValues(
        column.idEquipe -> idEquipe,
        column.nomeEquipe -> nomeEquipe,
        column.createdAt -> createdAt
      )
    }.updateAndReturnGeneratedKey.apply()

    Equipe(
      idEquipe = id,
      nomeEquipe = nomeEquipe,
      createdAt = createdAt,
      deletedAt = deletedAt
    )
  }

  def save(e: Equipe)(implicit session: DBSession = autoSession): Equipe = {
    withSQL {
      update(Equipe).set(
        Equipe.column.idEquipe -> e.idEquipe,
        Equipe.column.nomeEquipe -> e.nomeEquipe,
        Equipe.column.createdAt -> e.createdAt,
        Equipe.column.deletedAt -> e.deletedAt).where.eq(Equipe.column.idEquipe, e.idEquipe)
    }.update().apply()
    e
  }


  def destroy(idEquipe: Long)(implicit session: DBSession = AutoSession): Unit = withSQL {
    delete.from(Equipe).where.eq(column.idEquipe, idEquipe)
  }.update.apply()

}
