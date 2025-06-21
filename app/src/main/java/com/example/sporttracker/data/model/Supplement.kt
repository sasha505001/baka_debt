package com.example.sporttracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "supplements")
data class Supplement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val notes: String?,

    val scheduleType: SupplementScheduleType,

    val intervalHours: Int?,         // для INTERVAL_HOURS
    val intervalDays: Int?,          // для EVERY_N_DAYS
    val startDate: String?,          // ISO-дата начала (2025-06-21)

    val scheduleMapJson: String?,    // JSON: {"08:00": "2 капсулы"}
    val weekdays: String?,           // строка: "1,3,5" для SPECIFIC_WEEKDAYS
    val specificDates: String?,      // JSON-массив: ["2025-06-21", "2025-06-28"]

    val createdAt: Long = System.currentTimeMillis()
)
