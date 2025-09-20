package ru.devsoland.socialsync.ui

object AppDestinations {
    const val WELCOME_ROUTE = "welcome"
    const val CONTACT_LIST_ROUTE = "contact_list"
    const val ADD_CONTACT_ROUTE = "add_contact"
    const val EVENTS_ROUTE = "events"
    const val PROFILE_ROUTE = "profile"

    // Event Detail Screen
    const val EVENT_DETAIL_CONTACT_ID_ARG = "contactId" // Уже используется для деталей события
    const val EVENT_DETAIL_ROUTE_PATTERN = "event_detail/{$EVENT_DETAIL_CONTACT_ID_ARG}"

    fun eventDetailRoute(contactId: Long): String {
        return "event_detail/$contactId"
    }

    // Edit Contact Screen
    const val EDIT_CONTACT_ID_ARG = "contactIdToEdit" // Используем другое имя для ясности, хотя значение то же
    const val EDIT_CONTACT_ROUTE_PATTERN = "edit_contact/{$EDIT_CONTACT_ID_ARG}"

    fun editContactRoute(contactId: Long): String {
        return "edit_contact/$contactId"
    }
}
