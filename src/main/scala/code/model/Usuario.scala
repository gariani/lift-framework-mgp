package code.model

import java.sql.Date

import code.lib.Settings
import org.joda.time.DateTime
import scalikejdbc._

case class Usuario(idUsuario: Long,
                   idEquipe: Option[Long],
                   email: String,
                   nome: String,
                   cargo: Option[String] = None,
                   observacao: Option[String] = None,
                   telefone: Option[String],
                   senha: String,
                   inicioEmpresa: Option[DateTime],
                   nascimento: Option[DateTime],
                   sexo: Option[Int],
                   estadoCivil: Option[Int],
                   createdAt: DateTime,
                   deletedAt: Option[DateTime] = None) {
  def save()(implicit session: DBSession = Usuario.autoSession): Usuario = Usuario.save(this)(session)

  def destroy()(implicit session: DBSession = Usuario.autoSession): Unit = Usuario.destroy(idUsuario)(session)

  private val (u) = (Usuario.u)
  private val (u2) = (Usuario.u2)
}

object Usuario extends SQLSyntaxSupport[Usuario] with Settings {

  override val tableName = "usuario"

  override val columns = Seq("id_usuario", "id_equipe", "email", "nome", "cargo", "observacao", "telefone", "senha", "inicio_empresa", "nascimento", "sexo", "estado_civil", "created_at", "deleted_at")

  def opt(p: SyntaxProvider[Usuario])(rs: WrappedResultSet) =
    rs.longOpt(p.idUsuario).map(_ => Usuario(p)(rs))

  def apply(u: SyntaxProvider[Usuario])(rs: WrappedResultSet): Usuario = apply(u.resultName)(rs)

  def apply(u: ResultName[Usuario])(rs: WrappedResultSet): Usuario = new Usuario(
    idUsuario = rs.get(u.idUsuario),
    idEquipe = rs.longOpt(u.idEquipe),
    email = rs.get(u.email),
    nome = rs.get(u.nome),
    cargo = rs.stringOpt(u.cargo),
    observacao = rs.stringOpt(u.observacao),
    telefone = rs.stringOpt(u.telefone),
    senha = rs.string(u.senha),
    inicioEmpresa = rs.jodaDateTimeOpt(u.inicioEmpresa),
    nascimento = rs.jodaDateTimeOpt(u.nascimento),
    sexo = rs.intOpt(u.sexo),
    estadoCivil = rs.intOpt(u.estadoCivil),
    createdAt = rs.get(u.createdAt),
    deletedAt = rs.jodaDateTimeOpt(u.deletedAt)
  )

  val u = Usuario.syntax("u")
  val u2 = Usuario.syntax("u2")

  private val isNotDeleted = sqls.isNull(u.deletedAt)

  def isExistsEmail(email: String)(implicit session: DBSession = AutoSession): Option[String] = withSQL {
    select(u.email).from(Usuario as u).where.eq(u.email, email).and.isNull(u.deletedAt)
  }.map(_.string(u.email)).single().apply()

  def findIdByEmail(email: String)(implicit session: DBSession = AutoSession): Option[Long] = withSQL {
    select(u.idUsuario).from(Usuario as u).where.eq(u.email, email).and.isNull(u.deletedAt)
  }.map{ rs => (rs.long(1))}.single().apply()

  def findByEmail(email: String)(implicit session: DBSession = AutoSession): Option[Usuario] = withSQL {
    select.from(Usuario as u).where.eq(u.email, email).and.isNull(u.deletedAt)
  }.map(Usuario(u)).single().apply()

  def findByLogin(email: String, senha: String)(implicit session: DBSession = AutoSession): Option[String] = withSQL {
    select(u.email).from(Usuario as u).where.eq(u.email, email).and.eq(u.senha, senha).and.isNull(u.deletedAt)
  }.map(_.string(u.email)).single().apply()

  def findById(idUsuario: Long)(implicit session: DBSession = AutoSession): Option[Usuario] = withSQL {
    select.from(Usuario as u).where.eq(u.idUsuario, idUsuario).and.isNull(u.deletedAt)
  }.map(Usuario(u)).single().apply()

  def findAll()(implicit session: DBSession = AutoSession): List[Usuario] = withSQL {
    select.from(Usuario as u).where.isNull(u.deletedAt)
  }.map(Usuario(u)).list().apply()

  def findAllUsuariosLivres(implicit session: DBSession = AutoSession): List[(Int, String)] = withSQL {
    select(u.idUsuario, u.nome).from(Usuario as u).where.isNull(u.deletedAt).and.isNull(u.idEquipe)
  }.map{ rs => (rs.int(1), rs.string(2)) }.list().apply()

  def findAllUsuariosLivresPorEquipe(implicit session: DBSession = AutoSession): List[(Int, String)] = withSQL {
    select(u.idUsuario, u.nome).from(Usuario as u).where.isNull(u.deletedAt).and.isNotNull(u.idEquipe)
  }.map{ rs => (rs.int(1), rs.string(2)) }.list().apply()

  def findByEquipe(idEquipe: Long)(implicit session: DBSession = AutoSession): List[Usuario] = withSQL {
    select.from(Usuario as u).where.eq(u.idEquipe, idEquipe).and.isNull(u.deletedAt)
  }.map(Usuario(u)).list().apply()

  def updateEquipe(idUsuario: Long, idEquipe: Option[Long])(implicit session: DBSession = AutoSession) = {
    withSQL {
      update(Usuario).set(
        Usuario.column.idEquipe -> idEquipe
      ).where.eq(Usuario.column.idUsuario, idUsuario).and.isNull(column.deletedAt)
    }.update.apply()
  }


  def save(usuario: Usuario)(implicit session: DBSession = AutoSession): Usuario = {
    withSQL {
      update(Usuario).set(
        Usuario.column.idEquipe -> usuario.idEquipe,
        Usuario.column.email -> usuario.email,
        Usuario.column.nome -> usuario.nome,
        Usuario.column.cargo -> usuario.cargo,
        Usuario.column.observacao -> usuario.observacao,
        Usuario.column.senha -> usuario.senha,
        Usuario.column.telefone -> usuario.telefone,
        Usuario.column.inicioEmpresa -> usuario.inicioEmpresa,
        Usuario.column.nascimento -> usuario.nascimento,
        Usuario.column.sexo -> usuario.sexo,
        Usuario.column.estadoCivil -> usuario.estadoCivil
      ).where.eq(Usuario.column.idUsuario, usuario.idUsuario).and.isNull(column.deletedAt)
    }.update.apply()
    usuario
  }

  def create(idEquipe: Option[Long], email: String, nome: String, cargo: Option[String] = None, observacao: Option[String] = None,
             telefone: Option[String] = None, senha: String, inicioEmpresa: Option[DateTime] = None, nascimento: Option[DateTime]= None,
             sexo: Option[Int] = None, estadoCivil: Option[Int] = None, createdAt: DateTime = DateTime.now)
            (implicit session: DBSession = autoSession): Usuario = {

    val id = withSQL {
      insert.into(Usuario).namedValues(
        column.idEquipe -> idEquipe,
        column.email -> email,
        column.nome -> nome,
        column.cargo -> cargo,
        column.observacao -> observacao,
        column.telefone -> telefone,
        column.senha -> senha,
        column.inicioEmpresa -> inicioEmpresa,
        column.nascimento -> nascimento,
        column.sexo -> sexo,
        column.estadoCivil -> estadoCivil,
        column.createdAt -> createdAt
      )
    }.updateAndReturnGeneratedKey.apply()

    Usuario(
      idUsuario = id,
      idEquipe = idEquipe,
      email = email,
      nome = nome,
      cargo = cargo,
      observacao = observacao,
      telefone = telefone,
      senha = senha,
      inicioEmpresa = inicioEmpresa,
      nascimento = nascimento,
      sexo = sexo,
      estadoCivil = estadoCivil,
      createdAt = createdAt
    )
  }

  def destroy(idUsuario: Long)(implicit session: DBSession = AutoSession): Unit = withSQL {
    update(Usuario).set(column.deletedAt -> DateTime.now).where.eq(column.idUsuario, idUsuario)
  }.update.apply()
}