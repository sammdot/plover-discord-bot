package ca.sammdot.ploverbot

import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy

class Discord(botToken: String, serverId: Long) {
  private val jda = JDABuilder.createDefault(botToken)
    .enableIntents(
      GatewayIntent.GUILD_MESSAGES,
      GatewayIntent.GUILD_WEBHOOKS,
      GatewayIntent.GUILD_MEMBERS,
      GatewayIntent.MESSAGE_CONTENT
    )
    .setStatus(OnlineStatus.ONLINE)
    .setMemberCachePolicy(MemberCachePolicy.ALL)
    .build()

  init {
    PloverBot.LOGGER.info("Starting PloverBot...")
    jda.awaitReady()
  }

  val server = jda.getGuildById(serverId)
    ?: run {
      PloverBot.LOGGER.error("Unable to find Discord server with ID $serverId")
      throw IllegalArgumentException("Unable to find Discord server")
    }

  fun channel(id: Long): MessageChannel? =
    server.getChannelById(TextChannel::class.java, id)

  init {
    PloverBot.LOGGER.debug("Server: $server")
    server.loadMembers()
    jda.presence.activity = Activity.playing("Plover")
    PloverBot.LOGGER.info("PloverBot is connected!")
    jda.addEventListener(DiscordListener(this))
  }

  fun shutdown() {
    PloverBot.LOGGER.info("Shutting down PloverBot...")
    jda.presence.activity = null
    jda.shutdown()
  }
}
