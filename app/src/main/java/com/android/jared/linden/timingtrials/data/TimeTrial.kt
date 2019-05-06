package com.android.jared.linden.timingtrials.data

import androidx.room.*
import com.android.jared.linden.timingtrials.domain.RiderAssignmentResult
import com.android.jared.linden.timingtrials.ui.RiderStatus
import org.threeten.bp.*
import org.threeten.bp.temporal.ChronoUnit


//Todo: Use proper relational mapping, for now we simply store as JSON

@Entity(tableName = "timetrial_table", indices = [Index("id")])
data class TimeTrialHeader(var ttName: String,
                           var course: Course? = null,
                           var laps: Int = 1,
                           var interval:Int = 60,
                           var startTime: Instant,
                           var isSetup: Boolean = false,
                           var isFinished: Boolean = false,
                           @PrimaryKey(autoGenerate = true) var id: Long? = null) {

    companion object {
        fun createBlank(): TimeTrialHeader {
            val instant = Instant.now().truncatedTo(ChronoUnit.MINUTES).plus(15, ChronoUnit.MINUTES)
            return TimeTrialHeader(ttName = "", course = null, laps = 1, startTime = instant, isSetup = false, isFinished = false)
        }


    }

}


data class TimeTrial(
        @Embedded val timeTrialHeader: TimeTrialHeader,
        @Relation(parentColumn = "id", entityColumn = "timeTrialId", entity = TimeTrialRider::class)
        var riderList: List<TimeTrialRider> = listOf(),
        @Relation(parentColumn = "id", entityColumn = "timeTrialId", entity = TimeTrialEvent::class)
        var eventList: List<TimeTrialEvent> = listOf()
){

    fun getRiderStatus(riderId: Long): RiderStatus{

        val status = RiderStatus.NOT_STARTED
        val riderEvents = eventList.filter { it.riderId == riderId }


        if(riderEvents.any { it.eventType == EventType.RIDER_STARTED }){
                val num = riderEvents.filter { it.eventType == EventType.RIDER_PASSED }.count()
            if(num> 0){
                return if(num == timeTrialHeader.laps)  RiderStatus.FINISHED else RiderStatus.RIDING
            }
            return RiderStatus.RIDING
        }
        return status
    }

    fun assignRiderToEvent(riderId: Long, eventTimestamp: Long): RiderAssignmentResult{
        val event = eventList.find { it.timeStamp == eventTimestamp }
        val timeTrialRider = riderList.find { r -> r.rider.id == riderId }

        if(event != null && timeTrialRider!=null && event.eventType == EventType.RIDER_PASSED){
            if(event.timeStamp <= timeTrialRider.startTime) return RiderAssignmentResult(false, "Rider must have started")
           return when(getRiderStatus(riderId)){
                RiderStatus.NOT_STARTED -> RiderAssignmentResult(false, "This rider has not started")
                RiderStatus.FINISHED -> RiderAssignmentResult(false, "Rider has already finished")
                RiderStatus.RIDING -> {
                    event.riderId = riderId
                    RiderAssignmentResult(true, "Success")
                }
            }
        }else{
            return RiderAssignmentResult(false, "Error")
        }
    }

    fun addRidersAsTimeTrialRiders(riders: List<Rider>){
        riderList = riders.mapIndexed { index, rider -> TimeTrialRider(rider, timeTrialHeader.id, index + 1, (60 + index * timeTrialHeader.interval).toLong()) }
    }

    companion object {
        fun createBlank(): TimeTrial {
            return TimeTrial(TimeTrialHeader.createBlank())
        }
    }
}



