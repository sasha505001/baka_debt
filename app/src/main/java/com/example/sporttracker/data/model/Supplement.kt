package com.example.sporttracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.sporttracker.data.model.SupplementScheduleType

@Entity(tableName = "supplements")
data class Supplement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val dosage: String,
    val time: String, // формат "HH:mm"
    val scheduleType: SupplementScheduleType,
    val intervalDays: Int? = null,      // если scheduleType == EVERY_N_DAYS
    val weekdays: String? = null,       // строка вида "1,3,5" для ПН, СР, ПТ
    val notes: String? = null
)
