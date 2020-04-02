package io.github.jlprat.akka.battleship

sealed trait Protocol

object Protocol {
    case object OK extends Protocol
    case object KO extends Protocol
    case object Hit extends Protocol
    case object Miss extends Protocol
    case object Sunk extends Protocol
}