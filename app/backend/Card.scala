package backend

import scala.collection.mutable.{Buffer, Set}

import org.apache.poi.ss.usermodel.{ DataFormatter, WorkbookFactory, Row }
import java.io.File

// heart, diamond, spades, clubs
sealed abstract class Shape(val country: Char)

class Card(country: Char, val value: Int) extends Shape(country) {
  override def equals(obj: Any): Boolean = obj match {
    case card: Card => this.country == card.country && this.value == card.value
    case _ => false
  }
}

object PokerGame {

  def ranking(cards: Vector[Card]): Int = {
    val groupedByCountry = cards.groupBy(_.country)
    val groupedByValue = cards.groupBy(_.value)
    cards.sortBy(_.value) match {
      case straitFlush if (
        groupedByCountry.exists(_._2.size >= 5)
        && groupedByCountry.maxBy(_._2.size)._2.sortBy(_.value).zipWithIndex.groupBy(c => c._1.value - c._2).exists(_._2.size >= 5)) => 8
      case four if (groupedByValue.exists(_._2.size == 4)) => 7
      case fullHouse if (groupedByValue.exists(_._2.size == 3) && groupedByValue.exists(_._2.size == 2)) => 6
      case flush if (groupedByCountry.exists(_._2.size == 5)) => 5
      case strait if (groupedByValue.keys.zipWithIndex.groupBy(c => c._1 - c._2).exists(_._2.size >= 5)) => 4 //bugi jos monta samaa valueta, fixattu?!?!?
      case three if ((groupedByValue.exists(_._2.size == 3))) => 3
      case twoPairs if (groupedByValue.filter(_._2.size == 2).count(_._1 % 1 == 0) == 2) => 2
      case two if (groupedByValue.exists(_._2.size == 2)) => 1
      case highCard => 0
    }
  }

  //1: eka voittaa, 2: toka voittaa, 3: tasapeli
  def rankTie(eval: Int, first: Vector[Card], second: Vector[Card]): Int = {
    val firstGroupedByCountry = first.groupBy(_.country)
    val secGroupedByCountry = second.groupBy(_.country)
    val firstGroupedByValue = first.groupBy(_.value)
    val secGroupedByValue = second.groupBy(_.value)

    eval match {
      case 0 =>
        val sortZip = first.sortBy(_.value).zip(second.sortBy(_.value))
        sortZip.indexWhere(t => t._1 != t._2) match {
        case v if (v <= 4 && v != -1) => if (sortZip(v)._1.value > sortZip(v)._2.value) 1 else 2
        case _ => 3
      }

      case 1 =>
        val boolean = firstGroupedByValue.find(_._2.size == 2).get._1 > secGroupedByValue.find(_._2.size == 2).get._1
        val voolean = firstGroupedByValue.find(_._2.size == 2).get._1 < secGroupedByValue.find(_._2.size == 2).get._1
        if (boolean) 1 else if (voolean) 2
        else {
          val parValue = firstGroupedByValue.find(_._2.size == 2).get._1

          val sortZip = first.sortBy(_.value).filterNot(_.value == parValue).zip(second.sortBy(_.value).filterNot(_.value == parValue))
          sortZip.indexWhere(t => t._1 != t._2) match {
            case v if (v <= 2 && v != -1) => if (sortZip(v)._1.value > sortZip(v)._2.value) 1 else 2
            case _ => 3
          }
        }

      case 2 =>
        val firstPairs = firstGroupedByValue.filter(_._2.size == 2).keys.toVector.sorted
        val secPairs = secGroupedByValue.filter(_._2.size == 2).keys.toVector.sorted
        val sortZip = firstPairs.zip(secPairs)
        sortZip.indexWhere(t => t._1 != t._2) match {
            case v if (v == 0) => if (sortZip(v)._1 > sortZip(v)._2) 1 else 2
            case _ => {
              val fHigh = first.map(_.value).filterNot(firstPairs.contains(_)).max
              val sHigh = second.map(_.value).filterNot(firstPairs.contains(_)).max
              if (fHigh > sHigh) 1 else if (fHigh < sHigh) 2 else 3
            }
          }
      case 3 =>
        val fHand = firstGroupedByValue.find(_._2.size == 3).get._1
        val sHand = secGroupedByValue.find(_._2.size == 3).get._1
        if (fHand > sHand) 1 else if (sHand > fHand) 2 else {
          val sortZip = first.filterNot(_.value == fHand).sortBy(_.value).zip(second.filterNot(_.value == fHand).sortBy(_.value))
          sortZip.indexWhere(t => t._1 != t._2) match {
            case v if (v <= 1 && v != -1) => if (sortZip(v)._1.value > sortZip(v)._2.value) 1 else 2
            case _ => 3
          }
        }
      case 4 =>
        val fHand = firstGroupedByValue.keys.zipWithIndex.groupBy(c => c._1 - c._2).maxBy(_._2.size)._2.maxBy(_._1)._1
        val sHand = secGroupedByValue.keys.zipWithIndex.groupBy(c => c._1 - c._2).maxBy(_._2.size)._2.maxBy(_._1)._1
        if (fHand > sHand) 1 else if (fHand < sHand) 2 else 3
      case 5 =>
        val fHand = firstGroupedByCountry.maxBy(_._2.size)._2.sortBy(_.value)
        val sHand = secGroupedByCountry.maxBy(_._2.size)._2.sortBy(_.value)
        val zipSort = fHand.zip(sHand)
        zipSort.indexWhere(t => t._1 != t._2) match {
            case v if (v <= 4 && v != -1) => if (zipSort(v)._1.value > zipSort(v)._2.value) 1 else 2
            case _ => 3
          }
      case 6 =>
        val fHand = Vector(firstGroupedByValue.filter(_._2.size == 3).keys.max, firstGroupedByValue.filter(c => (c._2.size == 3 || c._2.size == 2) && c._1 != firstGroupedByValue.filter(_._2.size == 3).keys.max).keys.max)
        val sHand = Vector(secGroupedByValue.filter(_._2.size == 3).keys.max, secGroupedByValue.filter(c => (c._2.size == 3 || c._2.size == 2) && c._1 != secGroupedByValue.filter(_._2.size == 3).keys.max).keys.max)
        fHand.zip(sHand).indexWhere(t => t._1 != t._2) match {
            case v if (v != -1) => if (fHand(v) > sHand(v)) 1 else 2
            case _ => 3
        }
      case 7 =>
        val fHand: Int = firstGroupedByValue.maxBy(_._2.size)._1
        val sHand: Int = secGroupedByCountry.maxBy(_._2.size)._1
        if (fHand > sHand) 1 else if (fHand < sHand) 2 else {
          val fHigh = first.filterNot(_.value == fHand).map(_.value).max
          val sHigh = second.filterNot(_.value == fHand).map(_.value).max
          if (fHigh > sHigh) 1 else if (fHigh < sHigh) 2 else 3
        }
      case 8 =>
        val fHand: Int = firstGroupedByCountry.maxBy(_._2.size)._2.sortBy(_.value).zipWithIndex.groupBy(c => c._1.value - c._2).toVector.maxBy(_._2.size)._2.map(_._1.value).max
        val sHand: Int = secGroupedByCountry.maxBy(_._2.size)._2.sortBy(_.value).zipWithIndex.groupBy(c => c._1.value - c._2).toVector.maxBy(_._2.size)._2.map(_._1.value).max
        if (fHand > sHand) 1 else if (fHand < sHand) 2 else 3
    }
  }

  private val countries = Vector('h','d','s','c')
  private val allCards: Buffer[Card] = Buffer()

  for (c <- countries;
       n <- 2 to 14) {
    allCards += new Card(c, n)
  }

  def getCard(string: String) = {
    val country = string(0).toLower
    val value: Int = string.tail.toUpperCase match {
      case "T" => 10
      case "J" => 11
      case "Q" => 12
      case "K" => 13
      case "A" => 14
      case e => e.toInt
    }
    new Card(country, value)
  }

  private def countFiveCardOdds(hand: Vector[Card], table: Vector[Card]): (Int, Int, Int) = {

    val vector = hand ++ table

    var lost = 0
    var tied = 0
    var won = 0

    for (turn <- allCards.filterNot(c => vector.contains(c));
         river <-  allCards.filterNot(c => (vector :+ turn).contains(c))) {

      for (otherOne <- allCards.filterNot(c => (vector :+ turn :+ river).contains(c));
           otherTwo <- allCards.filterNot(c => (vector :+ turn :+ river :+ otherOne).contains(c))) {

          val me = PokerGame.ranking(vector :+ turn :+ river)
          val you = PokerGame.ranking(table :+ otherOne :+ otherTwo :+ river :+ turn)

          val number = (won+tied+lost)
          if (number % 10000 == 0) println(number.toDouble/(44*47*46*45) * 100)

          if (me > you) won += 1
          else if (me == you) {
            PokerGame.rankTie(me, vector :+ turn :+ river, table :+ otherOne :+ otherTwo :+ turn :+ river) match {
              case 1 => won += 1
              case 2 => lost += 1
              case 3 => tied += 1
            }
          }
          else if (me < you) {
            lost += 1
          }
      }
    }

    (won, tied, lost)
  }

    private def countSixCardOdds(hand: Vector[Card], table: Vector[Card]): (Int, Int, Int) = {

    val vector = hand ++ table

    var lost = 0
    var tied = 0
    var won = 0

    for (river <-  allCards.filterNot(c => vector.contains(c))) {

      for (otherOne <- allCards.filterNot(c => (vector :+ river).contains(c));
            otherTwo <- allCards.filterNot(c => (vector :+ river :+ otherOne).contains(c))) {

          val me = PokerGame.ranking(vector :+ river)
          val you = PokerGame.ranking(table :+ otherOne :+ otherTwo :+ river)

          if (me > you) won += 1
          else if (me == you) {
            PokerGame.rankTie(me, vector :+ river, table :+ otherOne :+ otherTwo :+ river) match {
              case 1 => won += 1
              case 2 => lost += 1
              case 3 => tied += 1
            }
          }
          else if (me < you) lost += 1
        }
    }

    (won, tied, lost)
  }

  private def countSevenCardOdds(hand: Vector[Card], table: Vector[Card]): (Int, Int, Int) = {

    val vector = hand ++ table

    var lost = 0
    var tied = 0
    var won = 0

    for (otherOne <- allCards.filterNot(c => (vector).contains(c));
         otherTwo <- allCards.filterNot(c => (vector :+ otherOne).contains(c))) {

        val me = PokerGame.ranking(vector)
        val you = PokerGame.ranking(table :+ otherOne :+ otherTwo)

        if (me > you) won += 1
        else if (me == you) {
            PokerGame.rankTie(me, vector, table :+ otherOne :+ otherTwo) match {
              case 1 => won += 1
              case 2 => lost += 1
              case 3 => tied += 1
            }
          }
        else if (me < you) lost += 1
      }

    (won, tied, lost)
  }

  def countTwoCardsOdds(cards: Vector[Card]): (Int, Int, Int) = {
    val file = new File(getClass.getClassLoader.getResource("pokerodds.xlsx").getPath)
    val workbook = WorkbookFactory.create(file)
    val sheet = workbook.getSheetAt(0)

    if (cards(0).country == cards(1).country) {
      val row = sheet.getRow(15-cards(0).value)
      val cell = row.getCell(15-cards(1).value)
      val value = cell.getStringCellValue.filterNot('%' == _).toDouble

      val options = (50*49*48*47*46)

      ((value/100*options).toInt, 0, ((100-value)/100*options).toInt)
    } else {
      val row = sheet.getRow(29-cards(0).value)
      val cell = row.getCell(15-cards(1).value)
      val value = cell.getStringCellValue.filterNot('%' == _).toDouble

      val options = (50*49*48*47*46)

      ((value/100*options).toInt, 0, ((100-value)/100*options).toInt)
    }
  }

  def countOdds(cards: Vector[Card]): (Int, Int, Int) = {
    cards.size match {
      case 2 => countTwoCardsOdds(cards)
      case 5 => countFiveCardOdds(cards.take(2), cards.drop(2))
      case 6 => countSixCardOdds(cards.take(2), cards.drop(2))
      case 7 => countSevenCardOdds(cards.take(2), cards.drop(2))
    }
  }
}