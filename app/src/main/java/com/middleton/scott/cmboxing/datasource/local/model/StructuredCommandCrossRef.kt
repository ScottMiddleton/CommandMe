package com.middleton.scott.cmboxing.datasource.local.model

import androidx.room.*
import com.middleton.scott.cmboxing.datasource.local.enums.CommandFrequencyType

@Entity(
    tableName = "structured_commands", primaryKeys = ["workout_id", "command_id"], foreignKeys = [
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

data class StructuredCommandCrossRef constructor(
    @ColumnInfo
    var workout_id: Long,
    @ColumnInfo
    val command_id: Long,
    @ColumnInfo
    val round: Int,
    @ColumnInfo
    val time_allocated_secs: Int,
    @ColumnInfo
    val position_index: Int
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