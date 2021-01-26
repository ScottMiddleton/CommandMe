package com.middleton.scott.cmboxing.datasource.local.model

import androidx.room.*
import com.middleton.scott.cmboxing.datasource.local.enums.CommandFrequencyType

@Entity(
    tableName = "structured_commands"
)

data class StructuredCommandCrossRef constructor(
    var workout_id: Long,
    val command_id: Long,
    val round: Int,
    var time_allocated_secs: Int,
    var position_index: Int = -1
) : BaseDbModel()

class StructuredWorkoutWithCommands {
    @Embedded
    var workout: Workout? = null

    @Relation(
        parentColumn = "_id",
        entityColumn = "_id",
        entity = Command::class,
        associateBy = Junction(
            value = StructuredCommandCrossRef::class,
            parentColumn = "workout_id",
            entityColumn = "command_id"
        )
    )
    lateinit var commands: List<Command>
}