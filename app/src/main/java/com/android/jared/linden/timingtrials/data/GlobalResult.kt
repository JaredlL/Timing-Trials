package com.android.jared.linden.timingtrials.data

import androidx.room.*
import org.threeten.bp.OffsetDateTime
import java.lang.Exception


//@Entity(tableName = "globalresult_table",
//        indices = [Index("riderId"),Index("courseId"), Index("timeTrialId")],
//        foreignKeys = [
//            ForeignKey(entity = Rider::class, parentColumns = arrayOf("id"), childColumns = arrayOf("riderId"), onDelete = ForeignKey.CASCADE, deferred = false),
//            ForeignKey(entity = Course::class, parentColumns = arrayOf("id"), childColumns = arrayOf("courseId"), onDelete = ForeignKey.CASCADE, deferred = false),
//            ForeignKey(entity = TimeTrialHeader::class, parentColumns = arrayOf("id"), childColumns = arrayOf("timeTrialId"), onDelete = ForeignKey.CASCADE, deferred = false)
//        ])
//
//data class GlobalResult(val riderId: Long,
//                        val courseId: Long,
//                        val riderClub: String,
//                        val category: String,
//                        val gender: Gender,
//                        val laps: Int,
//                        val resultTime: Long,
//                        val splits: List<Long>,
//                        val dateSet: OffsetDateTime?,
//                        val timeTrialId: Long?,
//                        val notes:String,
//                        @PrimaryKey(autoGenerate = true) val resultId: Long? = null){
//
//    companion object{
//        fun fromiResult(res: IResult): GlobalResult{
//
//            val riderId = res.rider.id
//            val courseId = res.course.id
//
//            if(riderId !=null && courseId!= null && res.timeTrial?.id != null){
//                return GlobalResult(
//                        riderId,
//                        courseId,
//                        res.riderClub,
//                        res.category,
//                        res.gender,
//                        res.laps,
//                        res.resultTime,
//                        res.splits,
//                        res.dateSet,
//                        res.timeTrial?.id,
//                        res.notes,
//                        null
//                )
//            }else{
//                throw Exception("Error creating new result - Rider or Course id was null")
//
//            }
//
//        }
//    }
//
//}


//data class FilledResult(@Embedded val globalResult:GlobalResult,
//                        @Relation(parentColumn = "riderId", entityColumn = "id", entity = Rider::class)
//                        override val rider: Rider,
//                        @Relation(parentColumn = "courseId", entityColumn = "id", entity = Course::class)
//                        override val course: Course,
//                        @Relation(parentColumn = "timeTrialId", entityColumn = "id", entity = TimeTrialHeader::class)
//                        override val timeTrial: TimeTrialHeader?
//                        ) : IResult{
//
//
//    override val riderClub: String
//        get() = globalResult.riderClub
//
//    override val category: String
//        get() = globalResult.category
//
//    override val gender: Gender
//        get() = globalResult.gender
//
//    override val laps: Int
//        get() = globalResult.laps
//
//    override val resultTime: Long
//        get() = globalResult.resultTime
//
//    override val splits: List<Long>
//        get() = globalResult.splits
//
//    override val dateSet: OffsetDateTime?
//        get() = globalResult.dateSet
//
//    override val notes: String
//        get() = globalResult.notes
//
//    override val resultId: Long?
//        get() = globalResult.resultId
//
//    companion object{
//        fun fromRiderCourseTT(rider: Rider,
//                              course: Course,
//                              resultTime: Long,
//                              splits: List<Long>,
//                              timeTrial: TimeTrialHeader,
//                              notes:String):FilledResult{
//            if (rider.id != null && course.id != null){
//                val global = GlobalResult(rider.id, course.id, rider.club, rider.category?:"", rider.gender, timeTrial.laps, resultTime, splits, timeTrial.startTime, timeTrial.id, notes)
//                return FilledResult(global, rider, course, timeTrial)
//            }else{
//                throw Exception("Error creating new result - Rider or Course id was null")
//            }
//
//        }
//
//        fun fromRiderCourse(rider: Rider,
//                              course: Course,
//                              resultTime: Long,
//                              splits: List<Long>,
//                              dateSet: OffsetDateTime?,
//                              timeTrial: TimeTrialHeader,
//                              notes:String):FilledResult{
//            if (rider.id != null && course.id != null){
//                val global = GlobalResult(rider.id, course.id, rider.club, rider.category?:"",rider.gender,  timeTrial.laps, resultTime, splits, timeTrial.startTime, timeTrial.id, notes)
//                return FilledResult(global, rider, course, timeTrial)
//            }else{
//                throw Exception("Error creating new result - Rider or Course id was null")
//            }
//
//        }
//    }
//
//}

interface IResult{

    val rider:Rider
    val course: Course
    val riderClub: String
    val category: String
    val gender: Gender
    val laps: Int
    val resultTime: Long?
    val splits: List<Long>
    val dateSet: OffsetDateTime?
    val timeTrial: TimeTrialHeader?
    val notes:String

}