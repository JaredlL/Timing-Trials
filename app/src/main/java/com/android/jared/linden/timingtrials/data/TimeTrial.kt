package com.android.jared.linden.timingtrials.data

import androidx.room.*
import com.android.jared.linden.timingtrials.domain.RiderAssignmentResult
import com.android.jared.linden.timingtrials.ui.RiderStatus
import org.threeten.bp.*
import org.threeten.bp.temporal.ChronoUnit


@Entity(tableName = "timetrial_table", indices = [Index("id")])
data class TimeTrialHeader(val ttName: String,
                           val course: Course? = null,
                           val laps: Int = 1,
                           val interval:Int = 60,
                           val startTime: OffsetDateTime,
                           val isSetup: Boolean = false,
                           val isFinished: Boolean = false,
                           @PrimaryKey(autoGenerate = true) val id: Long? = null) {

    companion object {
        fun createBlank(): TimeTrialHeader {
            val instant = Instant.now().truncatedTo(ChronoUnit.MINUTES).plus(15, ChronoUnit.MINUTES)
            return TimeTrialHeader(ttName = "", course = null, laps = 1, startTime = OffsetDateTime.ofInstant(instant, ZoneId.systemDefault()), isSetup = false, isFinished = false)
        }


    }

}


data class TimeTrial(
        @Embedded val timeTrialHeader: TimeTrialHeader,
        @Relation(parentColumn = "id", entityColumn = "timeTrialId", entity = TimeTrialRider::class)
        val riderList: List<TimeTrialRider> = listOf(),
        @Relation(parentColumn = "id", entityColumn = "timeTrialId", entity = TimeTrialEvent::class)
        val eventList: List<TimeTrialEvent> = listOf()
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

    fun getUnfinishedRiders(): List<TimeTrialRider>{
        return riderList.filter { r-> getRiderStatus(r.rider.id?:0) != RiderStatus.FINISHED }
    }

    fun addRidersAsTimeTrialRiders(riders: List<Rider>): TimeTrial{
        return this.copy(riderList =  riders.mapIndexed { index, rider -> TimeTrialRider(rider, timeTrialHeader.id, index + 1, (60 + index * timeTrialHeader.interval).toLong()) })
    }

    fun getDepartedRiders(): List<TimeTrialRider> {
        return eventList.filter { it.eventType == EventType.RIDER_STARTED}.mapNotNull { event-> riderList.firstOrNull { rn -> rn.rider.id == event.riderId }  }
    }

    fun getFinishedRiders(): List<TimeTrialRider>{
       return eventList.filter { it.eventType == EventType.RIDER_PASSED }.groupBy { it.riderId }.filter { it.value.count() == timeTrialHeader.laps }.keys.mapNotNull { riderList.find { r-> r.rider.id == it } }
    }



    companion object {
        fun createBlank(): TimeTrial {
            return TimeTrial(TimeTrialHeader.createBlank())
        }
    }
}



