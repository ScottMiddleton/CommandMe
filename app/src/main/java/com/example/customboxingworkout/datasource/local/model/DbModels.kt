package com.example.customboxingworkout.datasource.local.model

import androidx.room.*

@Entity()
data class Workout constructor(
    var name: String,
    var numberOfRounds: Int,
    var round_duration_secs: Int,
    var rest_duration_secs: Int
) : BaseDbModel()

@Entity()
data class Exercise constructor(
    var name: String,
    var allocatedTimeSecs: Int,
    var file_name: String
) : BaseDbModel()

