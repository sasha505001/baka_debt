package com.example.sporttracker.data.model

enum class SupplementScheduleType {
    EVERY_DAY,        // каждый день
    EVERY_N_DAYS,     // каждые N дней
    WEEKDAYS_ONLY,    // только будние дни
    WEEKENDS_ONLY,    // только выходные
    SPECIFIC_WEEKDAYS // по конкретным дням недели
}
