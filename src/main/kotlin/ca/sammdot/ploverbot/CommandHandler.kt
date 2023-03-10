package ca.sammdot.ploverbot

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction
import net.dv8tion.jda.api.utils.MarkdownSanitizer

class CommandHandler(private val discord: Discord) {
  private val server get() = discord.server

  private val SlashCommandInteractionEvent.doBroadcast
    get() =
      getOption("broadcast")?.asBoolean ?: false

  private fun ReplyCallbackAction.setEphemeralIf(event: SlashCommandInteractionEvent)
    = setEphemeral(!event.doBroadcast)

  private val Int.entries: String get() {
    val word = if (this == 1) "entry" else "entries"
    val num = String.format("%,d", this)
    return "$num $word"
  }

  private val Int.outlines: String get() =
    if (this == 1) "**1** outline"
    else "**$this** outlines"

  fun links(event: SlashCommandInteractionEvent) {
    val linkList = PloverBot.links.keys.sorted()
      .joinToString(", ") { "`$it`" }
    event.reply(
      if (linkList.isEmpty()) "No links registered."
      else "Available links:\n$linkList"
    )
      .setEphemeral(true).queue()
  }

  fun link(event: SlashCommandInteractionEvent) {
    val name = event.getOption("name")?.asString
    PloverBot.links[name]?.let {
      event.reply(it).queue()
    } ?: run {
      event.reply("Could not find a link named `$name`.")
        .setEphemeral(true).queue()
    }
  }

  fun terms(event: SlashCommandInteractionEvent) {
    val termList = PloverBot.terms.keys.sorted().joinToString(", ")
    event.reply(
      if (termList.isEmpty()) "No terms registered."
      else "Available terms:\n$termList"
    )
      .setEphemeral(true).queue()
  }

  fun define(event: SlashCommandInteractionEvent) {
    val word = event.getOption("term")?.asString
    PloverBot.terms[word]?.let {
      event.reply("**$word**: $it").queue()
    } ?: run {
      event.reply("Could not find the term `$word`.")
        .setEphemeral(true).queue()
    }
  }

  fun theories(event: SlashCommandInteractionEvent) {
    val theoryList = PloverBot.theories.sortedBy { it.name.lowercase() }
      .map { "    •  ${it.name} (`${it.shortName}` - ${it.entryCount.entries})" }
      .joinToString("\n")
    event.reply(
      if (theoryList.isEmpty()) "No theories registered."
      else "Available theories:\n$theoryList"
    )
      .setEphemeral(true).queue()
  }

  companion object {
    fun escape(str: String) = MarkdownSanitizer.escape(str)
  }

  fun lookup(event: SlashCommandInteractionEvent) {
    val theory = event.getOption("theory")?.asString
      ?.let { PloverBot.theoriesByName[it] }
      ?: run { PloverBot.theoryFor(event.messageChannel) }

    event.getOption("translation")?.asString?.let { tl ->
      val outlines = theory.outlinesForTranslation(tl)
      val outlineList = outlines.sorted()
        .map { "    •  ${it.replace("*", "\\*")}" }
        .joinToString("\n")
      if (outlines.isEmpty()) {
        event.reply("Could not find outlines for '${escape(tl)}' in ${theory.name}.")
          .setEphemeral(true).queue()
      } else {
        event.reply("Found ${outlines.size.outlines} for '${escape(tl)}' in ${theory.name}:\n$outlineList")
          .setEphemeralIf(event).queue()
      }
    } ?: run {
      event.reply("Translation is required").setEphemeral(true).queue()
    }
  }

  fun write(event: SlashCommandInteractionEvent) {
    val theory = event.getOption("theory")?.asString
      ?.let { PloverBot.theoriesByName[it] }
      ?: run { PloverBot.theoryFor(event.messageChannel) }

    event.getOption("outline")?.asString?.let { ol ->
      theory.translationForOutline(ol)?.let { tl ->
        event.reply("Found a translation for ${escape(ol)} in ${theory.name}:\n${escape(tl)}")
          .setEphemeralIf(event).queue()
      } ?: run {
        event.reply("Could not find translation for outline ${escape(ol)}.")
          .setEphemeral(true).queue()
      }
    } ?: run {
      event.reply("Outline is required").setEphemeral(true).queue()
    }
  }

  fun jenpls(event: SlashCommandInteractionEvent) {
    event.reply("<:jenpls:985021533850316870>").queue()
  }

  fun reload(event: SlashCommandInteractionEvent) {
    event.reply("Reloading PloverBot...").setEphemeral(true).queue()
    PloverBot.reload()
  }
}
