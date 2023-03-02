package controllers

import backend.{Card, PokerGame}

import javax.inject._
import play.api.mvc._
import play.api.libs.json._

class CardOddsController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  case class Odds(win: Int, tie: Int, lose: Int)

  def getAll: Action[AnyContent] = Action {
    NoContent
  }

  def getOdds(cards: String): Action[AnyContent] = {
    println(cards)
    val odds = PokerGame.countOdds(cards.grouped(2).map(s => PokerGame.getCard(s)).toVector)
    println(odds)
    val oddsObject = new Odds(odds._1, odds._2, odds._3)
    implicit val JSONOdds = Json.format[Odds]
    Action {
      Ok(Json.toJson(oddsObject))
    }
  }
}