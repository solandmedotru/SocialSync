package ru.devsoland.socialsync.ui.aigreeting

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.devsoland.socialsync.data.model.Contact
import ru.devsoland.socialsync.data.repository.SocialSyncRepository
import ru.devsoland.socialsync.ui.AppDestinations
import javax.inject.Inject

import com.google.genai.Client
import com.google.genai.types.GenerateContentConfig
import ru.devsoland.socialsync.BuildConfig

@HiltViewModel
class AiGreetingPromptViewModel @Inject constructor(
    private val repository: SocialSyncRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _contact = MutableStateFlow<Contact?>(null)
    private val _contactName = MutableStateFlow("")
    val contactName: StateFlow<String> = _contactName.asStateFlow()

    val availableStyles: List<String> = listOf("Неформальное", "Официальное", "С юмором", "Душевное", "Короткое", "Строгое")
    private val _selectedStyle = MutableStateFlow(availableStyles.first())
    val selectedStyle: StateFlow<String> = _selectedStyle.asStateFlow()

    val availableRelationships: List<String> = listOf("Супруг(а)", "Друг", "Коллега", "Родственник", "Лучший друг", "Знакомый")
    private val _selectedRelationship = MutableStateFlow(availableRelationships.first())
    val selectedRelationship: StateFlow<String> = _selectedRelationship.asStateFlow()

    private val _userKeywords = MutableStateFlow("")
    val userKeywords: StateFlow<String> = _userKeywords.asStateFlow()

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

    // Используем ключ из BuildConfig для безопасности
    private val geminiApiKey: String = BuildConfig.GEMINI_API_KEY
    private val GEMINI_MODEL_NAME = "gemini-1.5-flash"

    private var genaiClient: Client? = null
    private val contactId: Long = savedStateHandle.get<Long>(AppDestinations.AI_GREETING_PROMPT_CONTACT_ID_ARG) ?: 0L
    private val eventId: Long = savedStateHandle.get<Long>(AppDestinations.AI_GREETING_PROMPT_EVENT_ID_ARG) ?: 0L

    init {
        if (contactId != 0L) loadContactDetails(contactId)
        
        // Проверяем, был ли ключ успешно загружен из BuildConfig
        if (geminiApiKey == "YOUR_GEMINI_API_KEY_HERE_SHOULD_BE_SECURED" || geminiApiKey.isBlank()) {
            _errorMessage.value = "Ошибка: API ключ не установлен или не загружен из BuildConfig!"
            println("ОШИБКА: API_KEY не установлен или не валиден в AiGreetingPromptViewModel (значение по умолчанию из BuildConfig)")
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

    private fun loadContactDetails(id: Long) {
        viewModelScope.launch {
            repository.getContactById(id).collect {
                _contact.value = it
                _contactName.value = it?.let { contact -> listOfNotNull(contact.firstName, contact.lastName).joinToString(" ").trim() } ?: "Контакт не найден"
            }
        }
    }

    fun onStyleSelected(style: String) { _selectedStyle.value = style }
    fun onRelationshipSelected(relationship: String) { _selectedRelationship.value = relationship }
    fun onUserKeywordsChanged(keywords: String) { _userKeywords.value = keywords }

    fun toggleSelection(index: Int) {
        _selectedIndices.update { if (index in it) it - index else it + index }
    }

    fun getSelectedGreetings(): List<String> {
        return _selectedIndices.value.mapNotNull { _generatedGreetings.value.getOrNull(it) }.toList()
    }

    fun saveSelectedGreetingsToEvent() {
        if (eventId == 0L) {
            _saveStatusMessage.value = "Ошибка: Не удалось определить событие для сохранения."
            println("AiGreetingPromptViewModel: Попытка сохранения при eventId = 0")
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
                println("AiGreetingPromptViewModel: Поздравления для eventId $eventId сохранены: $greetingsToSave")
            } catch (e: Exception) {
                _saveStatusMessage.value = "Ошибка при сохранении поздравлений: ${e.message}"
                println("AiGreetingPromptViewModel: Ошибка сохранения поздравлений для eventId $eventId: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun clearSaveStatusMessage() {
        _saveStatusMessage.value = null
    }

    fun generateGreeting() {
        if (genaiClient == null) {
            _errorMessage.value = if (geminiApiKey == "YOUR_GEMINI_API_KEY_HERE_SHOULD_BE_SECURED" || geminiApiKey.isBlank()) "AI клиент: API ключ не установлен (проверьте BuildConfig)." else "AI клиент не инициализирован."
            return
        }
        val currentContactName = _contactName.value.ifBlank { "человека" }
        val prompt = buildString {
            append("Напиши текст поздравления с днем рождения для $currentContactName. ")
            if (_selectedRelationship.value.isNotBlank()) append("Мои отношения с этим человеком: ${_selectedRelationship.value}. ")
            append("Тональность поздравления: ${_selectedStyle.value}. ")
            if (_userKeywords.value.isNotBlank()) append("Учти также следующие детали, ключевые слова или особые пожелания: ${_userKeywords.value}. ")
            append("Предложи 1-2 развернутых варианта текста поздравления. Каждый вариант должен быть законченным поздравлением. Раздели варианты поздравления двойным переносом строки.")
        }
        println("Сформированный промпт для AI: $prompt")
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _generatedGreetings.value = emptyList()
            _selectedIndices.value = emptySet()
            try {
                val response = withContext(Dispatchers.IO) {
                    genaiClient!!.models.generateContent(GEMINI_MODEL_NAME, prompt, GenerateContentConfig.builder().candidateCount(1).build())
                }
                println("AI_GREETING_DEBUG: Запрос к AI завершен. Ответ: $response")
                val generatedText = response.text()
                println("AI_GREETING_DEBUG: Извлеченный текст: $generatedText")
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
                        println("AI_GREETING_DEBUG: Сгенерированные поздравления: $greetings")
                    } else {
                        _errorMessage.value = "AI вернул текст, но он оказался пустым после обработки."
                        println("AI_GREETING_DEBUG: Текст от AI пуст после обработки. Исходный: '$generatedText', Очищенный: '$cleanedText'")
                    }
                } else {
                    println("AI_GREETING_DEBUG: Текст от AI null или пустой.")
                    val errorDetails = mutableListOf<String>()
                    response.promptFeedback().ifPresent { feedback ->
                        println("AI_GREETING_DEBUG: Prompt Feedback: $feedback")
                        errorDetails.add("Prompt Feedback: ${feedback.toString()}")
                    }
                    response.candidates().orElse(null)?.firstOrNull()?.let { candidate ->
                        candidate.finishReason().ifPresent { reason ->
                             println("AI_GREETING_DEBUG: Finish Reason: $reason")
                             errorDetails.add("Candidate Finish Reason: ${reason.toString()}")
                        }
                    }
                    _errorMessage.value = if (errorDetails.isNotEmpty()) "AI не вернул текст. Детали: ${errorDetails.joinToString("; ")}" else "AI не вернул текст поздравления (ответ пуст или null)."
                     println("AI_GREETING_DEBUG: _errorMessage.value = ${_errorMessage.value}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка генерации: ${e.message ?: "Неизвестная ошибка"}"
                println("AI_GREETING_DEBUG: ИСКЛЮЧЕНИЕ: ${e.javaClass.simpleName} - ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
                println("AI_GREETING_DEBUG: isLoading = false")
            }
        }
    }
}
