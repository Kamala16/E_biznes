package repositories

import models.Payment
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PaymentRepo @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit
    ec: ExecutionContext
) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class PaymentTable(tag: Tag)
      extends Table[Payment](tag, _tableName = "payment") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def * = (id, name) <> ((Payment.apply _).tupled, Payment.unapply)
  }

  val payment = TableQuery[PaymentTable]

  def create(name: String): Future[Payment] =
    db.run {
      (payment.map(c => (c.name))
        returning payment.map(_.id)
        into ((name, id) => Payment(id, name))) += (name)
    }

  def get(id: Int): Future[Option[Payment]] =
    db.run {
      payment.filter(_.id === id).result.headOption
    }

  def list(): Future[Seq[Payment]] =
    db.run {
      payment.result
    }

  def update(id: Int, new_payment: Payment): Future[Int] = {
    val updatePayment: Payment = new_payment.copy(id)
    db.run(payment.filter(_.id === id).update(updatePayment))
  }

  def delete(id: Int): Future[Int] = db.run(payment.filter(_.id === id).delete)
}
