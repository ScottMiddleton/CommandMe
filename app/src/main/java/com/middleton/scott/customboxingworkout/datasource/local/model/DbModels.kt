package com.middleton.scott.customboxingworkout.datasource.local.model

import androidx.room.Entity
import java.util.*

@Entity()
data class Workout constructor(
    var name: String = "",
    var preparation_time_secs: Int = 10,
    var numberOfRounds: Int = 5,
    var work_time_secs: Int = 180,
    var rest_time_secs: Int = 60,
    var intensity: Int = 5,
    var dateCreated: Date = Date()
) : BaseDbModel()

@Entity()
data class Combination constructor(
    var name: String,
    var allocatedTimeSecs: Int,
    var file_name: String
) : BaseDbModel()

@Entity()
data class CombinationFrequency constructor(
    var workout_id: Long,
    var combination_id: Long,
    var frequency: Long
) : BaseDbModel()

