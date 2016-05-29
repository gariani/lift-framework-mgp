package code.model

import code.lib.Settings
import jdk.nashorn.internal.runtime.JSType
import org.joda.time.DateTime
import scalikejdbc._
import sqls.count

case class Cliente(idCliente: Long,
                   nomeCliente: String,
                   createdAt: DateTime,
                   deletedAt: Option[DateTime],
                   projetos: Seq[Projeto] = Nil) {

  def save()(implicit session: DBSession = Cliente.autoSession): Cliente = Cliente.save(this)(session)

  def destroy()(implicit session: DBSession = Cliente.autoSession): Unit = Cliente.destroy(idCliente)(session)

  private val (c, p) = (Cliente.c, Projeto.p)
}

object Cliente extends SQLSyntaxSupport[Cliente] with Settings {

  override val tableName = "cliente"

  override val columns = Seq("id_cliente", "nome_cliente", "created_at", "deleted_at")

  def opt(p: SyntaxProvider[Cliente])(rs: WrappedResultSet) =
    rs.longOpt(p.resultName.idCliente).map(_ => Cliente(p.resultName)(rs))

  def apply(c: SyntaxProvider[Cliente])(rs: WrappedResultSet): Cliente = apply(c.resultName)(rs)

  def apply(c: ResultName[Cliente])(rs: WrappedResultSet): Cliente = new Cliente(
    idCliente = rs.get(c.idCliente),
    nomeCliente = rs.get(c.nomeCliente),
    createdAt = rs.jodaDateTime(c.createdAt),
    deletedAt = rs.jodaDateTimeOpt(c.deletedAt)
  )

  val c = Cliente.syntax("c")

  private val (p) = (Projeto.p)

  def findAllClienteLista()(implicit session: DBSession = AutoSession) = {
    withSQL {
      select(c.idCliente, c.nomeCliente).from(Cliente as c).where.isNull(c.deletedAt)
    }.map { rs => (rs.int(1), rs.string(2)) }.list().apply()
  }

  def findClienteById(idCliente: Long)(implicit sesession: DBSession = AutoSession): Option[Cliente] =
    withSQL {
      select.from(Cliente as c)
        .leftJoin(Projeto as p)
        .on(c.idCliente, p.idCliente).where.eq(c.idCliente, idCliente)
    }
      .one(Cliente(c))
      .toMany(Projeto.opt(p)).map { (cliente, projetos) => cliente.copy(projetos = projetos)
    }.single().apply()

  def findAll()(implicit session: DBSession = AutoSession) = {
    withSQL {
      select.from(Cliente as c)
        .leftJoin(Projeto as p)
        .on(c.idCliente, p.idCliente).where.isNull(c.deletedAt)
    }
      .one(Cliente(c))
      .toMany(Projeto.opt(p)).map { (cliente, projetos) => cliente.copy(projetos = projetos) }

  }.list.apply()

  def findPrimeiraUltimaTarefa(idCliente: Long)(implicit session: DBSession = AutoSession) =
    sql"""
        select min(t.dt_inicio_tarefa), max(t.dt_final_tarefa)
        from tarefa t
        join projeto p on t.id_projeto = p.id_projeto
        join cliente c on p.id_cliente = c.id_cliente
        where c.id_cliente = ${idCliente}
      """.map { rs => (rs.longOpt(1), rs.longOpt(2)) }.list().apply()

  def findClienteByNome(nomeCliente: String)(implicit sesession: DBSession = AutoSession): Option[Int] = withSQL {
    select(count(c.idCliente)).from(Cliente as c)
      .where.eq(c.nomeCliente, nomeCliente)
  }.map(rs => (rs.int(1))).single().apply()

  def create(nomeCliente: String, createdAt: DateTime = DateTime.now)(implicit session: DBSession = AutoSession): Cliente = {

    val id = withSQL {
      insert.into(Cliente).namedValues(
        column.nomeCliente -> nomeCliente,
        column.createdAt -> createdAt
      )
    }.updateAndReturnGeneratedKey.apply()

    Cliente(
      idCliente = id,
      nomeCliente = nomeCliente,
      createdAt = createdAt,
      None
    )
  }

  def save(c: Cliente)(implicit session: DBSession = AutoSession): Cliente = {
    withSQL {
      update(Cliente).set(
        Cliente.column.nomeCliente -> c.nomeCliente,
        Cliente.column.deletedAt -> c.deletedAt).where.eq(Cliente.column.idCliente, c.idCliente)
    }.update().apply()
    c
  }

  def destroy(idCliente: Long)(implicit session: DBSession = AutoSession): Unit = withSQL {
    delete.from(Cliente).where.eq(column.idCliente, idCliente)
  }.update.apply()

}
