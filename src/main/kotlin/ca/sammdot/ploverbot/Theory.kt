package ca.sammdot.ploverbot

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileNotFoundException

class Dictionary {
  private val entriesByOutline = mutableMapOf<String, String>()
  private val entriesByTranslation = mutableMapOf<String, List<String>>()

  val entryCount: Int get() = entriesByOutline.size

  fun loadFrom(path: String) {
    try {
      val file = File(path).reader(Charsets.UTF_8)
      Gson().fromJson<Map<String, String>>(
        file,
        object : TypeToken<Map<String, String>>() {}.type
      )?.let { e ->
        entriesByOutline.clear()
        entriesByOutline.putAll(e)
        entriesByTranslation.clear()
        entriesByTranslation.putAll(
          e.map { Pair(it.key, it.value) }
            .groupBy { it.second }
            .map { it.key to it.value.map { it.first } })
      } ?: run {
        PloverBot.LOGGER.error("File $path does not exist or can not be parsed correctly; ignoring")
      }
    } catch (e: FileNotFoundException) {
      PloverBot.LOGGER.error("File $path does not exist; ignoring")
    }
  }

  fun translationForOutline(outline: String): String? {
    return entriesByOutline[outline]
  }

  fun outlinesForTranslation(translation: String): List<String> {
    return entriesByTranslation[translation] ?: emptyList()
  }
}

data class Theory(
  val shortName: String,
  val name: String,
  val dictionary: Dictionary
) {
  val entryCount: Int get() = dictionary.entryCount

  fun translationForOutline(outline: String) =
    dictionary.translationForOutline(outline)

  fun outlinesForTranslation(translation: String) =
    dictionary.outlinesForTranslation(translation)
}
