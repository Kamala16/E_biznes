package repositories

import models.Delivery
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeliveryRepo @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]


  import dbConfig._
  import profile.api._

  class DeliveryTable(tag: Tag) extends Table[Delivery](tag, _tableName = "Delivery") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("type")
    def * = (id, name) <> (Delivery.tupled, Delivery.unapply)
  }

  val delivery = TableQuery[DeliveryTable]

  def create(userId: Int, name: String): Future[Delivery] = db.run {
    (delivery.map(c => (c.name))
      returning delivery.map(_.id)
      into ((name, id) => Delivery(id, name))
      ) +=(name)
  }

  def get(id: Int): Future[Option[Delivery]] = db.run {
    delivery.filter(_.id === id).result.headOption
  }

  def list(): Future[Seq[Delivery]] = db.run {
    delivery.result
  }

  def update(id: Int, new_carts: Delivery): Future[Int] = {
    val updateCart: Delivery = new_carts.copy(id)
    db.run(delivery.filter(_.id === id).update(updateCart))
  }

  def delete(id: Int): Future[Int] = db.run(delivery.filter(_.id === id).delete)
}
