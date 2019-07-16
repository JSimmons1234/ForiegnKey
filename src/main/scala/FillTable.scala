import slick.jdbc.MySQLProfile.api._
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

import TableSetup._
import Run._

object FillTable {

  val dropTable = DBIO.seq(suppliers.schema.dropIfExists, coffees.schema.dropIfExists)
  val setupTable = DBIO.seq(suppliers.schema.createIfNotExists,coffees.schema.createIfNotExists)
//  val dropTable = DBIO.seq(suppliers.schema.dropIfExists)
//  val dropTable2 = DBIO.seq(coffees.schema.dropIfExists)
//  val setupTable = DBIO.seq(suppliers.schema.createIfNotExists)
//  val setupTable2 = DBIO.seq(coffees.schema.createIfNotExists)


  def dropDB() = {
    //do a drop followed by initialisePeople
    val dropFuture = Future{
      db.run(DBIO.seq(dropTable))

    }
    //Attempt to drop the table, Await does not block here
    Await.result(dropFuture, Duration.Inf).andThen{
      case Success(_) =>  initialisePeople()
      case Failure(error) => println("Dropping the table failed due to: " + error.getMessage)
        initialisePeople()
    }
  }

  def initialisePeople() = {
    //initialise people
    val setupFuture =  Future {
      db.run(DBIO.seq(setupTable))
    }
    //once our DB has finished initializing we are ready to roll, Await does not block
    Await.result(setupFuture, Duration.Inf).andThen{
      case Success(_) => println("good to here")
      case Failure(error) => println("Initialising the table failed due to: " + error.getMessage)
    }
  }

  def addToTable() {
    val insertPeople = Future {
      val query = DBIO.seq(
        suppliers ++= Seq (
          (101, "Acme, Inc.", "99 Market Street", "Groundsville", "CA", "95199"),
          (49, "Superior Coffee", "1 Party Place", "Mendocino", "CA", "95460"),
          (150, "The High Ground", "100 Coffee Lane", "Meadows", "CA", "93966")
        ),
        coffees ++= Seq(
          ("Colombian", 101, 7.99, 0, 0),
          ("French_Roast", 49, 8.99, 0, 0),
          ("Espresso", 150, 9.99, 0, 0),
          ("Colombian_Decaf", 101, 8.99, 0, 0),
          ("French_Roast_Decaf", 49, 9.99, 0, 0)
        )
      )
      db.run(query)
    }
    Await.result(insertPeople, Duration.Inf).andThen {
      case Success(_) => getData()
      case Failure(error) => println("Welp! Something went wrong! " + error.getMessage)
    }
  }


  def getData(): Unit = {
    println("Coffees:")
    db.run(coffees.result).map(_.foreach {
      case (name, supID, price, sales, total) =>
        println("  " + name + "\t" + supID + "\t" + price + "\t" + sales + "\t" + total)

    })
      val q1 = for (c <- coffees)
      yield LiteralColumn("  ") ++ c.name ++ "\t" ++ c.supID.asColumnOf[String] ++
        "\t" ++ c.price.asColumnOf[String] ++ "\t" ++ c.sales.asColumnOf[String] ++
        "\t" ++ c.total.asColumnOf[String]

    db.stream(q1.result).foreach(println)

    val q2 = for {
      c <- coffees if c.price < 9.0
      s <- suppliers if s.id === c.supID
    } yield (c.name, s.name)

    db.stream(q2.result).foreach(println)
  }
}





