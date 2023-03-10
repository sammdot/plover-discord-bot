package ca.sammdot.ploverbot

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.config.Configurator
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory
import org.apache.logging.log4j.core.layout.PatternLayout
import java.io.File
import kotlin.concurrent.thread

object PloverBot {
  init {
    val builder = ConfigurationBuilderFactory.newConfigurationBuilder()
    builder.add(
      builder.newAppender("stdout", "Console").add(
        builder.newLayout(PatternLayout::class.java.simpleName).addAttribute(
          "pattern", "%d{HH:mm:ss.SSS} [%logger/%level] %msg%n"
        )
      )
    )
    builder.add(
      builder.newRootLogger(Level.INFO).add(builder.newAppenderRef("stdout"))
    )
    Configurator.initialize(builder.build())
  }

  private var discord: Discord? = null
  private var config: Config? = null
  val LOGGER: Logger = LogManager.getLogger("PloverBot")

  private val configPath: String get() = File(".").canonicalPath

  init {
    LOGGER.info("Config path: $configPath")
  }

  val links = mutableMapOf<String, String>()
  val terms = mutableMapOf<String, String>()

  val theories = mutableSetOf<Theory>()
  val theoriesByName get() = theories.associateBy { it.shortName }
  private val defaultTheory get() = theoriesByName["plover"]!!
  private val channelTheories = mutableMapOf<MessageChannel, Theory>()

  fun theoryFor(channel: MessageChannel): Theory =
    channelTheories[channel] ?: defaultTheory

  private fun loadConfig() {
    LOGGER.info("Loading PloverBot config...")
    val file = File("plover-bot.json").reader(Charsets.UTF_8)
    config = Gson().fromJson(file, Config::class.java)
    LOGGER.info("Config: $config")
  }

  private fun loadLinks() {
    val file = File("links.json").reader(Charsets.UTF_8)
    Gson().fromJson<Map<String, String>>(
      file,
      object : TypeToken<Map<String, String>>() {}.type
    )?.let { l ->
      links.clear()
      links.putAll(l)
    }
  }

  private fun loadTerms() {
    val file = File("terms.json").reader(Charsets.UTF_8)
    Gson().fromJson<Map<String, String>>(
      file,
      object : TypeToken<Map<String, String>>() {}.type
    )?.let { l ->
      terms.clear()
      terms.putAll(l)
    }
  }

  fun reload() {
    loadLinks()
    loadTerms()
  }

  fun start() {
    loadConfig()
    loadLinks()
    loadTerms()

    config?.let {
      // Initialize the list of theories earlier so the Discord command
      // suggestions include all theories.
      theories.clear()
      it.theories.forEach { (name, fullName) ->
        val dict = Dictionary()
        dict.loadFrom("dictionaries/$name.json")
        theories.add(Theory(name, fullName, dict))
      }

      discord = Discord(it.token, it.server)

      channelTheories.clear()
      it.channelMap.forEach { (channelId, theoryName) ->
        val channel = discord?.channel(channelId)
        val theory = theoriesByName[theoryName]
        if (channel == null) {
          LOGGER.warn("Channel $channelId in theory mapping does not exist, ignoring")
          return
        }
        if (theory == null) {
          LOGGER.warn("Theory $theoryName in theory mapping does not exist, ignoring")
          return
        }
        channelTheories[channel] = theory
      }
    }

    Runtime.getRuntime()
      .addShutdownHook(thread(start = false) { discord?.shutdown() })
  }
}

fun main() {
  PloverBot.start()
}
