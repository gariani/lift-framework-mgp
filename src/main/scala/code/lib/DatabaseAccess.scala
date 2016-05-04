package code.lib

import javax.sql.DataSource
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari._
import scalikejdbc.{DataSourceConnectionPool, ConnectionPool}
import scalikejdbc._

object DataSourceHi {

  private var url: String = "jdbc:mysql://localhost:3306/mgp"
  private var user: String = "root"
  private var password: String = "root"

  private[this] lazy val instance: DataSource = {
    val ds = new HikariConfig()
    ds.setDriverClassName("com.mysql.jdbc.Driver")
    ds.setJdbcUrl(url)
    ds.setPoolName("mggPool")
    ds.setMaximumPoolSize(10)
    ds.addDataSourceProperty("user", user)
    ds.addDataSourceProperty("password", password)
    ds.setConnectionTimeout(1000)
    ds.setMaxLifetime(180000)
    new HikariDataSource(ds)
  }

  def apply(): javax.sql.DataSource = instance

}

object InicializarConfiguracao {

  val init = {
    ConnectionPool.singleton(new DataSourceConnectionPool(DataSourceHi()))
  }
}

trait Settings {
  InicializarConfiguracao.init
}