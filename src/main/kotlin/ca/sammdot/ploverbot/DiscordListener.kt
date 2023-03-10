package ca.sammdot.ploverbot

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData

class DiscordListener(discord: Discord) : ListenerAdapter() {
  init {
    discord.server.updateCommands().addCommands(
      Commands.slash(
        "links", "Lists all available links that can be sent with /link.",
      ),
      Commands.slash(
        "link",
        "Sends a link to a commonly-referenced Open Steno community resource.",
      ).addOptions(
        OptionData(
          OptionType.STRING,
          "name",
          "The name of the link. See /links for all available links.",
          true,
        ).setAutoComplete(true)
      ),

      Commands.slash(
        "terms", "Lists all available terms that can be shown with /define.",
      ),
      Commands.slash(
        "define",
        "Shows the definition of a steno term.",
      ).addOptions(
        OptionData(
          OptionType.STRING,
          "term",
          "The term to define. See /define for all available terms.",
          true,
        ).setAutoComplete(true)
      ),

      Commands.slash(
        "theories",
        "Lists all steno theories with available dictionaries.",
      ),
      Commands.slash(
        "lookup",
        "Looks up a steno outline given a text translation.",
      ).addOption(
        OptionType.STRING,
        "translation",
        "The text the steno outline should translate to.",
        true,
      ).addOptions(
        OptionData(
          OptionType.STRING,
          "theory",
          "Which theory's steno dictionary to use for lookup.",
        ).addChoices(
          PloverBot.theories.sortedBy { it.shortName }.map {
            Choice(it.name, it.shortName)
          }
        )
      ).addOption(
        OptionType.BOOLEAN,
        "broadcast",
        "Whether to send the output of this command to the whole channel.",
      ),
      Commands.slash(
        "write",
        "Shows the text translation of a steno outline.",
      ).addOption(
        OptionType.STRING,
        "outline",
        "The steno outline to look up.",
        true,
      ).addOptions(
        OptionData(
          OptionType.STRING,
          "theory",
          "Which theory's steno dictionary to use for lookup.",
        ).addChoices(
          PloverBot.theories.map {
            Choice(it.name, it.shortName)
          }
        )
      ).addOption(
        OptionType.BOOLEAN,
        "broadcast",
        "Whether to send the output of this command to the whole channel.",
      ),

      Commands.slash(
        "jenpls",
        "Asks Jen to respond sooner.",
      ),

      Commands.slash(
        "reload",
        "Reloads the database of links and terms.",
      )
        .setDefaultPermissions(
          DefaultMemberPermissions.enabledFor(
            Permission.MANAGE_CHANNEL,
            Permission.MODERATE_MEMBERS
          )
        ),
    ).queue()
  }

  private val handler = CommandHandler(discord)

  override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
    val handler = when (event.name) {
      "links" -> handler::links
      "link" -> handler::link
      "terms" -> handler::terms
      "define" -> handler::define
      "theories" -> handler::theories
      "lookup" -> handler::lookup
      "write" -> handler::write
      "reload" -> handler::reload
      "jenpls" -> handler::jenpls

      else -> { _ ->
        event.reply("That command has not been implemented!")
          .setEphemeral(true).queue()
      }
    }

    handler(event)
  }

  override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
    val choices = mutableListOf<String>()
    if (event.name == "link" && event.focusedOption.name == "name") {
      choices.addAll(PloverBot.links.keys.filter { it.contains(event.focusedOption.value) })
    } else if (event.name == "define" && event.focusedOption.name == "term") {
      choices.addAll(PloverBot.terms.keys.filter { it.contains(event.focusedOption.value) })
    }

    if (choices.size <= 25) {
      event.replyChoices(choices.map { Choice(it, it) }).queue()
    } else {
      event.replyChoices().queue()
    }
  }
}