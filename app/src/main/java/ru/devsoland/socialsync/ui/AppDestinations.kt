package ru.devsoland.socialsync.ui

object AppDestinations {
    const val WELCOME_ROUTE = "welcome"
    const val CONTACT_LIST_ROUTE = "contact_list"
    const val ADD_CONTACT_ROUTE = "add_contact"
    const val EVENTS_ROUTE = "events"
    const val PROFILE_ROUTE = "profile"

    // Event Detail Screen
    const val EVENT_DETAIL_CONTACT_ID_ARG = "contactId" // This might need to become eventId if this screen is for a specific event's details
    const val EVENT_DETAIL_ROUTE_PATTERN = "event_detail/{$EVENT_DETAIL_CONTACT_ID_ARG}"

    fun eventDetailRoute(contactId: Long): String { // Or eventId: Long
        return "event_detail/$contactId"
    }

    // Edit Contact Screen
    const val EDIT_CONTACT_ID_ARG = "contactIdToEdit"
    const val EDIT_CONTACT_ROUTE_PATTERN = "edit_contact/{$EDIT_CONTACT_ID_ARG}"

    fun editContactRoute(contactId: Long): String {
        return "edit_contact/$contactId"
    }

    // AI Greeting Prompt Screen
    const val AI_GREETING_PROMPT_CONTACT_ID_ARG = "contactIdForGreeting"
    const val AI_GREETING_PROMPT_EVENT_ID_ARG = "eventIdForGreeting" // <-- НОВЫЙ АРГУМЕНТ
    const val AI_GREETING_PROMPT_ROUTE_PATTERN = "ai_greeting_prompt/{$AI_GREETING_PROMPT_CONTACT_ID_ARG}/{$AI_GREETING_PROMPT_EVENT_ID_ARG}" // <-- ОБНОВЛЕННЫЙ ШАБЛОН

    // Обновленная функция для создания пути, теперь принимает и eventId
    fun aiGreetingPromptRoute(contactId: Long, eventId: Long): String {
        return "ai_greeting_prompt/$contactId/$eventId"
    }
}
