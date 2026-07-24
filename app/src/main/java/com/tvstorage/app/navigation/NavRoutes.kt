package com.tvstorage.app.navigation

object NavRoutes {
    const val HOME = "home"
    const val ADD_TV = "add_tv"
    const val EDIT_TV = "edit_tv/{tvId}"
    const val DETAILS = "details/{tvId}"
    const val SETTINGS = "settings"
    const val ARCHIVE = "archive"

    fun editTv(tvId: Long) = "edit_tv/$tvId"
    fun details(tvId: Long) = "details/$tvId"
}