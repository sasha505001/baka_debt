package com.example.sporttracker.data.model

enum class SupplementScheduleType {
    EVERY_DAY,        // каждый день
    EVERY_N_DAYS,     // каждые N дней
    WEEKDAYS_ONLY,    // будние дни
    WEEKENDS_ONLY,    // выходные
    SPECIFIC_WEEKDAYS,// по дням недели
    INTERVAL_HOURS    // через каждые N часов
}