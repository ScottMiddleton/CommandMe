package com.middleton.scott.customboxingworkout.datasource.local.model

import androidx.room.*
import java.util.*

@Entity()
data class Workout constructor(
    var name: String = "",
    var preparation_time_secs: Int = 10,
    var numberOfRounds: Int = 5,
    var round_duration_secs: Int = 180,
    var rest_duration_secs: Int = 60,
    var intensity: Int = 50,
    var dateCreated: Date = Date()
) : BaseDbModel()

@Entity()
data class Exercise constructor(
    var name: String,
    var allocatedTimeSecs: Int,
    var file_name: String
) : BaseDbModel()

