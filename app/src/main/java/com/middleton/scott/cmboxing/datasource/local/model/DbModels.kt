package com.middleton.scott.cmboxing.datasource.local.model

import androidx.room.Entity
import java.util.*

@Entity()
data class BoxingWorkout constructor(
    var name: String = "",
    var preparation_time_secs: Int = 10,
    var numberOfRounds: Int = 5,
    var work_time_secs: Int = 180,
    var rest_time_secs: Int = 60,
    var intensity: Int = 5,
    var dateCreated: Date = Date()
) : BaseDbModel()

@Entity()
data class Command constructor(
    var name: String,
    var timeToCompleteMillis: Long,
    var file_name: String
) : BaseDbModel()

@Entity()
data class User constructor(
    var email: String = "",
    var first: String = "",
    var last: String = ""
    ) : BaseDbModel()
