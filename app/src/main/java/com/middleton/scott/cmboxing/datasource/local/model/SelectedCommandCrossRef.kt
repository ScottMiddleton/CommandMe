package com.middleton.scott.cmboxing.datasource.local.model

import androidx.room.*
import com.middleton.scott.cmboxing.datasource.local.enums.CommandFrequencyType

@Entity(
    tableName = "workout_commands", primaryKeys = ["workout_id", "command_id"], foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["_id"],
            childColumns = ["workout_id"],
            onDelete = ForeignKey.CASCADE
        ), ForeignKey(
            entity = Command::class,
            parentColumns = ["_id"],
            childColumns = ["command_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)

data class SelectedCommandCrossRef constructor(
    @ColumnInfo
    var workout_id: Long,
    @ColumnInfo
    val command_id: Long,
    @ColumnInfo
    var frequency: CommandFrequencyType = CommandFrequencyType.AVERAGE
) : BaseDbModel()

class WorkoutWithCommands {
    @Embedded
    var workout: Workout? = null

    @Relation(
        parentColumn = "_id",
        entityColumn = "_id",
        entity = Command::class,
        associateBy = Junction(
            value = SelectedCommandCrossRef::class,
            parentColumn = "workout_id",
            entityColumn = "command_id"
        )
    )
    lateinit var commands: List<Command>
}