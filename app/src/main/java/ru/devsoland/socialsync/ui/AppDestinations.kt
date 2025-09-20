package ru.devsoland.socialsync.ui

object AppDestinations {
    const val WELCOME_ROUTE = "welcome"
    const val CONTACT_LIST_ROUTE = "contact_list"
    // const val ADD_CONTACT_ROUTE = "add_contact" // Старый маршрут для добавления, теперь объединен
    const val EVENTS_ROUTE = "events"
    const val PROFILE_ROUTE = "profile"

    // Event Detail Screen
    const val EVENT_DETAIL_CONTACT_ID_ARG = "contactId" // Аргумент остается тот же, но контекст другой
    const val EVENT_DETAIL_ROUTE_PATTERN = "event_detail/{$EVENT_DETAIL_CONTACT_ID_ARG}"

    fun eventDetailRoute(contactId: Long): String { // Or eventId: Long
        return "event_detail/$contactId"
    }

    // Add/Edit Contact Screen (объединенная логика)
    const val CONTACT_ID_ARG = "contactId" // Общий аргумент ID, переименован из EDIT_CONTACT_ID_ARG
    const val ADD_EDIT_CONTACT_ROUTE_BASE = "add_edit_contact"
    const val ADD_EDIT_CONTACT_ROUTE_PATTERN = "$ADD_EDIT_CONTACT_ROUTE_BASE/{$CONTACT_ID_ARG}"

    // Функция для навигации к экрану добавления/редактирования
    // contactId = 0L (или другое специальное значение, например, DEFAULT_CONTACT_ID) будет означать "новый контакт"
    fun addEditContactRoute(contactId: Long): String {
        return "$ADD_EDIT_CONTACT_ROUTE_BASE/$contactId"
    }
    const val DEFAULT_NEW_CONTACT_ID = 0L // Можно использовать это значение для ясности при навигации для нового контакта


    // AI Greeting Prompt Screen
    const val AI_GREETING_PROMPT_CONTACT_ID_ARG = "contactIdForGreeting"
    const val AI_GREETING_PROMPT_EVENT_ID_ARG = "eventIdForGreeting" // <-- НОВЫЙ АРГУМЕНТ
    const val AI_GREETING_PROMPT_ROUTE_PATTERN = "ai_greeting_prompt/{$AI_GREETING_PROMPT_CONTACT_ID_ARG}/{$AI_GREETING_PROMPT_EVENT_ID_ARG}" // <-- ОБНОВЛЕННЫЙ ШАБЛОН

    // Обновленная функция для создания пути, теперь принимает и eventId
    fun aiGreetingPromptRoute(contactId: Long, eventId: Long): String {
        return "ai_greeting_prompt/$contactId/$eventId"
    }
}
