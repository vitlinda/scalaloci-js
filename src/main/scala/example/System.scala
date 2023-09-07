package example

import loci.language._
import rescala.default._
import loci.language.transmitter.rescala._
import loci.communicator.tcp._
import loci.communicator.ws.webnative
import loci.communicator.ws.jetty

import scala.scalajs.js.annotation.JSExportTopLevel
import loci.platform
import loci.serializer.upickle._
import loci.contexts.Pooled.Implicits.global

import org.eclipse.jetty.server.{Server, ServerConnector}
import org.eclipse.jetty.servlet.ServletContextHandler

import scala.scalajs.js.Dynamic.{global => jsGlobal}

@multitier object PingPong {
  @peer type Node
  @peer type Pinger <: Node { type Tie <: Single[Ponger] }
  @peer type Ponger <: Node { type Tie <: Single[Pinger] }

  val ping: Evt[String] on Pinger = Evt[String]()
  val pong: Evt[String] on Ponger = Evt[String]()

  on[Ponger] {
    ping.asLocal observe {
      case "ping" => println("ping received"); pong.fire("pong")
      case _ => println("error"); multitier.terminate()
    }
  }

  on[Pinger] {
    pong.asLocal observe {
      case "pong" => println("pong received"); ping.fire("ping")
      case _ => println("error"); multitier.terminate()
    }
  }

  def main(): Unit on Node = on[Pinger] {
    println("pinger")
    ping.fire("ping")
  } and on[Ponger] {
    println("ponger")
    pong.fire("pong")
  }
}

object Pinger extends App {
  platform(platform.jvm) {
    val port = 8080

    val server = new Server()

    val connector = new ServerConnector(server)
    connector.setPort(port)

    val context = new ServletContextHandler(ServletContextHandler.SESSIONS)
    server.setHandler(context)
    server.addConnector(connector)

    val runtime = multitier start new Instance[PingPong.Pinger](
      listen[PingPong.Ponger] { jetty.WS(context, "/ws/*") })
    runtime.terminated foreach { _ =>
      server.stop()
    }

    server.start()
  }
}

object Ponger {
@JSExportTopLevel("main")
  def main(args: Array[String]): Unit = {
    platform(platform.js) {
      multitier start new Instance[PingPong.Ponger](
        connect[PingPong.Pinger] {
          webnative.WS("ws://192.168.252.34:8080/ws/") // use the device ip for the smartphone
//          webnative.WS("ws://10.0.2.2:8080/ws/") use this for the Android Studio emulator
        })
    }
  }
}
