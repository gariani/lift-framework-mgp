package code.model

import code.lib.Settings
import org.joda.time.DateTime
import scalikejdbc._
import sqls.count

case class Equipe(idEquipe: Long,
                  idLider: Option[Long],
                  nomeEquipe: String,
                  createdAt: DateTime,
                  deletedAt: Option[DateTime]) {

  def save()(implicit session: DBSession = Equipe.autoSession): Equipe = Equipe.save(this)(session)

  def destroy()(implicit session: DBSession = Equipe.autoSession): Unit = Equipe.destroy(idEquipe)(session)

  private val (e, u) = (Equipe.e, Usuario.u)
}

object Equipe extends SQLSyntaxSupport[Equipe] with Settings {

  override val tableName = "equipe"

  override val columns = Seq("id_equipe", "id_lider", "nome_equipe", "created_at", "deleted_at")

  def opt(p: SyntaxProvider[Equipe])(rs: WrappedResultSet) =
    rs.longOpt(p.resultName.idEquipe).map(_ => Equipe(p.resultName)(rs))

  def apply(p: SyntaxProvider[Equipe])(rs: WrappedResultSet): Equipe = apply(e.resultName)(rs)

  def apply(e: ResultName[Equipe])(rs: WrappedResultSet): Equipe = new Equipe(
    idEquipe = rs.get(e.idEquipe),
    idLider = rs.longOpt(e.idLider),
    nomeEquipe = rs.get(e.nomeEquipe),
    createdAt = rs.jodaDateTime(e.createdAt),
    deletedAt = rs.jodaDateTimeOpt(e.deletedAt)
  )

  val e = Equipe.syntax("e")

  private val u = Usuario.u

  def findAll()(implicit session: DBSession = autoSession) =
    sql"""select e.id_equipe, e.nome_equipe, u2.nome as nomeUsuario, count(u.id_usuario) as quantUsuario
          from equipe e
          left join usuario u on e.id_equipe  = u.id_equipe
          left join usuario u2 on e.id_lider = u2.id_usuario
      """
  .map { rs => (rs.int("id_equipe"), rs.string("nome_equipe"), rs.stringOpt("nomeUsuario"), rs.int("quantUsuario"))
  }.list().apply()

  def findEquipeQuantUsuario(idEquipe: Long)(implicit session: DBSession = autoSession) =
    sql"""select e.id_equipe, e.nome_equipe, u2.nome as nomeUsuario, count(u.id_usuario) as quantUsuario
          from equipe e
          left join usuario u on e.id_equipe  = u.id_equipe
          left join usuario u2 on e.id_lider = u2.id_usuario
          where e.id_equipe = ${idEquipe}
      """.map { rs => (rs.int("id_equipe"), rs.string("nome_equipe"), rs.stringOpt("nomeUsuario"), rs.int("quantUsuario"))
    }.single().apply()

  def findEquipeById(idEquipe: Long)(implicit sesession: DBSession = AutoSession): Option[Equipe] = withSQL {
    select.from(Equipe as e).where.eq(e.idEquipe, idEquipe)
  }.map(Equipe(e)).single().apply()

  def findEquipeByNome(nomeEquipe: String)(implicit sesession: DBSession = AutoSession): Option[Int] = withSQL {
    select(count(e.idEquipe)).from(Equipe as e).where.eq(e.nomeEquipe, nomeEquipe)
  }.map(rs => rs.int(1)).single().apply()

  def create(idLider: Option[Long], nomeEquipe: String, createdAt: DateTime, deletedAt: Option[DateTime])
            (implicit session: DBSession = AutoSession): Equipe = {

    val id = withSQL {
      insert.into(Equipe).namedValues(
        column.nomeEquipe -> nomeEquipe,
        column.idLider -> idLider,
        column.createdAt -> createdAt
      )
    }.updateAndReturnGeneratedKey.apply()

    Equipe(
      idEquipe = id,
      idLider = idLider,
      nomeEquipe = nomeEquipe,
      createdAt = createdAt,
      deletedAt = deletedAt
    )
  }

  def save(e: Equipe)(implicit session: DBSession = autoSession): Equipe = {
    withSQL {
      update(Equipe).set(
        Equipe.column.idEquipe -> e.idEquipe,
        Equipe.column.idLider -> e.idLider,
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
