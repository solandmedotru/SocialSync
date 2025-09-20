package ru.devsoland.socialsync.ui.aigreeting

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.genai.Client
import com.google.genai.types.GenerateContentConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.devsoland.socialsync.BuildConfig
import ru.devsoland.socialsync.data.model.Contact
import ru.devsoland.socialsync.data.repository.SocialSyncRepository
import ru.devsoland.socialsync.ui.AppDestinations
import ru.devsoland.socialsync.util.AppConstants
import javax.inject.Inject

@HiltViewModel
class AiGreetingPromptViewModel @Inject constructor(
    private val repository: SocialSyncRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _contact = MutableStateFlow<Contact?>(null)
    private val _contactName = MutableStateFlow("")
    val contactName: StateFlow<String> = _contactName.asStateFlow()

    private val _fullPromptText = MutableStateFlow("")
    val fullPromptText: StateFlow<String> = _fullPromptText.asStateFlow()

    val availableKeywords: List<String> = AppConstants.MASTER_TAG_LIST
    private val _selectedKeywords = MutableStateFlow<Set<String>>(emptySet())
    val selectedKeywords: StateFlow<Set<String>> = _selectedKeywords.asStateFlow()

    val availableStyles: List<String> = listOf("Неформальное", "Официальное", "С юмором", "Душевное", "Короткое", "Строгое")
    private val _selectedStyle = MutableStateFlow(availableStyles.first())
    val selectedStyle: StateFlow<String> = _selectedStyle.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _generatedGreetings = MutableStateFlow<List<String>>(emptyList())
    val generatedGreetings: StateFlow<List<String>> = _generatedGreetings.asStateFlow()

    private val _selectedIndices = MutableStateFlow<Set<Int>>(emptySet())
    val selectedIndices: StateFlow<Set<Int>> = _selectedIndices.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _saveStatusMessage = MutableStateFlow<String?>(null)
    val saveStatusMessage: StateFlow<String?> = _saveStatusMessage.asStateFlow()

    private val geminiApiKey: String = BuildConfig.GEMINI_API_KEY

    private var genaiClient: Client? = null
    private val contactId: Long = savedStateHandle.get<Long>(AppDestinations.AI_GREETING_PROMPT_CONTACT_ID_ARG) ?: 0L
    private val eventId: Long = savedStateHandle.get<Long>(AppDestinations.AI_GREETING_PROMPT_EVENT_ID_ARG) ?: 0L

    private var initialPromptGenerated = false

    init {
        if (contactId != 0L) {
            loadContactDetailsAndInitializePrompt(contactId)
        }

        if (geminiApiKey == "YOUR_GEMINI_API_KEY_HERE_SHOULD_BE_SECURED" || geminiApiKey.isBlank()) {
            _errorMessage.value = "Ошибка: API ключ не установлен или не загружен из BuildConfig!"
        } else {
            try {
                genaiClient = Client.Builder().apiKey(geminiApiKey).build()
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка инициализации AI клиента: ${e.message}"
                e.printStackTrace()
            }
        }
        if (eventId == 0L) {
            println("ПРЕДУПРЕЖДЕНИЕ: eventId равен 0 в AiGreetingPromptViewModel. Сохранение будет невозможно.")
        }
    }

    private fun loadContactDetailsAndInitializePrompt(id: Long) {
        viewModelScope.launch {
            println("AI_DEBUG: loadContactDetailsAndInitializePrompt for contactId: $id")
            val contactData = repository.getContactById(id).first()
            _contact.value = contactData
            val currentContactName = contactData?.let { c -> listOfNotNull(c.firstName, c.lastName).joinToString(" ").trim() } ?: "человека"
            _contactName.value = currentContactName
            println("AI_DEBUG: Contact loaded: ${contactData?.firstName}, Tags from DB: ${contactData?.tags}")

            if (!initialPromptGenerated) {
                println("AI_DEBUG: initialPromptGenerated is false. Proceeding to build prompt.")
                val defaultStyle = _selectedStyle.value
                val existingContactTags = contactData?.tags ?: emptyList()
                
                val preselectedKeywordsSet = mutableSetOf<String>()
                existingContactTags.forEach { contactTag ->
                    val canonicalTag = AppConstants.MASTER_TAG_LIST.find { it.equals(contactTag, ignoreCase = true) }
                    println("AI_DEBUG: Checking contactTag '$contactTag', found canonical: '$canonicalTag'")
                    if (canonicalTag != null) {
                        preselectedKeywordsSet.add(canonicalTag)
                    }
                }
                println("AI_DEBUG: preselectedKeywordsSet: $preselectedKeywordsSet")
                _selectedKeywords.value = preselectedKeywordsSet

                val templateBuilder = StringBuilder()
                templateBuilder.append("Напиши поздравление с днем рождения для $currentContactName.")
                templateBuilder.append("\nСтиль поздравления: $defaultStyle.")
                preselectedKeywordsSet.forEach { keyword ->
                    templateBuilder.append("\nУчти также, что это мой $keyword.")
                }
                templateBuilder.append("\n\n[Добавь сюда свои пожелания, общие интересы, воспоминания или детали, которые должен учесть AI.]")
                templateBuilder.append("\n\nПредложи 1-2 развернутых варианта поздравления.")
                
                val finalTemplate = templateBuilder.toString()
                println("AI_DEBUG: Final template for fullPromptText: $finalTemplate")
                _fullPromptText.value = finalTemplate
                initialPromptGenerated = true
            } else {
                println("AI_DEBUG: initialPromptGenerated is true. Skipping prompt build.")
            }
        }
    }

    fun onFullPromptTextChanged(newText: String) {
        _fullPromptText.value = newText
    }

    fun onKeywordToggled(keyword: String) {
        val currentSelected = _selectedKeywords.value.toMutableSet()
        if (keyword in currentSelected) {
            currentSelected.remove(keyword)
        } else {
            currentSelected.add(keyword)
            _fullPromptText.value += "\nУчти также, что это мой $keyword."
        }
        _selectedKeywords.value = currentSelected
    }

    fun onStyleSelected(style: String) {
        _selectedStyle.value = style
        _fullPromptText.value += "\nСтиль поздравления: $style."
    }

    fun toggleSelection(index: Int) {
        _selectedIndices.update { if (index in it) it - index else it + index }
    }

    fun getSelectedGreetings(): List<String> {
        return _selectedIndices.value.mapNotNull { _generatedGreetings.value.getOrNull(it) }.toList()
    }

    fun saveSelectedGreetingsToEvent() {
        if (eventId == 0L) {
            _saveStatusMessage.value = "Ошибка: Не удалось определить событие для сохранения."
            return
        }
        val greetingsToSave = getSelectedGreetings()
        if (greetingsToSave.isEmpty()) {
            _saveStatusMessage.value = "Не выбрано ни одного поздравления для сохранения."
            return
        }
        viewModelScope.launch {
            try {
                repository.updateEventGeneratedGreetings(eventId, greetingsToSave)
                _saveStatusMessage.value = "Поздравления успешно сохранены!"
            } catch (e: Exception) {
                _saveStatusMessage.value = "Ошибка при сохранении поздравлений: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    fun clearSaveStatusMessage() {
        _saveStatusMessage.value = null
    }

    fun generateGreeting() {
        if (genaiClient == null) {
            _errorMessage.value = if (geminiApiKey == "YOUR_GEMINI_API_KEY_HERE_SHOULD_BE_SECURED" || geminiApiKey.isBlank()) "AI клиент: API ключ не установлен." else "AI клиент не инициализирован."
            return
        }
        val promptToSend = _fullPromptText.value.ifBlank {
            val currentContactNameFallback = _contactName.value.ifBlank { "человека" }
            "Напиши поздравление с днем рождения для $currentContactNameFallback. Стиль: ${_selectedStyle.value}. Предложи 1-2 варианта."
        }

        println("Финальный промпт для AI: $promptToSend")
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _generatedGreetings.value = emptyList()
            _selectedIndices.value = emptySet()
            try {
                val response = withContext(Dispatchers.IO) {
                    genaiClient!!.models.generateContent(AiGreetingPromptViewModel.GEMINI_MODEL_NAME, promptToSend, GenerateContentConfig.builder().candidateCount(1).build())
                }
                val generatedText = response.text()
                if (!generatedText.isNullOrBlank()) {
                    val cleanedText = generatedText
                        .replace(Regex("Вариант \\d+:", RegexOption.IGNORE_CASE), "")
                        .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
                        .replace(Regex("^\\s*[\\r\\n]+|[\\r\\n]+\\s*$"), "")

                    val greetings = cleanedText.split(Regex("\\n\\s*\\n"))
                        .map { it.trim() }
                        .filter { it.length > 15 }

                    if (greetings.isNotEmpty()) {
                        _generatedGreetings.value = greetings
                    } else {
                        _errorMessage.value = "AI вернул текст, но он оказался пустым после обработки."
                    }
                } else {
                    val errorDetails = mutableListOf<String>()
                    response.promptFeedback().ifPresent { feedback -> errorDetails.add("Prompt Feedback: $feedback") }
                    response.candidates().orElse(null)?.firstOrNull()?.finishReason()?.ifPresent { reason -> errorDetails.add("Finish Reason: $reason") }
                    _errorMessage.value = if (errorDetails.isNotEmpty()) "AI не вернул текст. Детали: ${errorDetails.joinToString("; ")}" else "AI не вернул текст (ответ пуст)."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка генерации: ${e.message ?: "Неизвестная ошибка"}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    companion object {
        private const val GEMINI_MODEL_NAME = "gemini-1.5-flash"
    }
}
