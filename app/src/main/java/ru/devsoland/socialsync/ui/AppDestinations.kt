package ru.devsoland.socialsync.ui

object AppDestinations {
    const val WELCOME_ROUTE = "welcome"
    const val CONTACT_LIST_ROUTE = "contact_list"
    const val ADD_CONTACT_ROUTE = "add_contact"
    const val EVENTS_ROUTE = "events"
    const val PROFILE_ROUTE = "profile"

    // Маршрут для деталей события теперь включает contactId
    const val EVENT_DETAIL_CONTACT_ID_ARG = "contactId"
    const val EVENT_DETAIL_ROUTE_PATTERN = "event_detail/{$EVENT_DETAIL_CONTACT_ID_ARG}"

    // Вспомогательная функция для создания маршрута с аргументом
    fun eventDetailRoute(contactId: Long): String {
        return "event_detail/$contactId"
    }
}
