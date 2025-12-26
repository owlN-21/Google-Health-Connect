package com.example.healthconnect

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.metadata.Device
import java.time.Instant
import java.time.Duration
import java.time.ZoneOffset

import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.records.metadata.Metadata.Companion.manualEntry
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.LocalDate
import java.time.ZoneId


class HealthManager(
    private val client: HealthConnectClient
) {
    suspend fun insertSteps(){
        val endTime = Instant.now()
        val startTime = endTime.minus(Duration.ofMinutes(15))

        val stepsRecord = StepsRecord(
            count = 120,
            startTime = startTime,
            endTime = endTime,
            startZoneOffset = ZoneOffset.UTC,
            endZoneOffset = ZoneOffset.UTC,
            metadata = Metadata.autoRecorded(
                device = Device(type = Device.TYPE_WATCH)
            )
        )

        client.insertRecords(listOf(stepsRecord))
    }

    suspend fun insertStepsForDate(
        count: Long,
        date: LocalDate
    ) {
        val zone = ZoneId.systemDefault()
        val startTime = date.atStartOfDay(zone).toInstant()
        val endTime = startTime.plus(Duration.ofMinutes(30))


        val record = StepsRecord(
            count = count,
            startTime = startTime,
            endTime = endTime,
            startZoneOffset = ZoneOffset.UTC,
            endZoneOffset = ZoneOffset.UTC,
            metadata = Metadata.manualEntry()
        )

        client.insertRecords(listOf(record))
    }


    suspend fun insertHeartRate() {
        val now = Instant.now()

        val record = HeartRateRecord(
            startTime = now.minusSeconds(60),
            endTime = now,
            samples = listOf(
                HeartRateRecord.Sample(
                    time = now.minusSeconds(30),
                    beatsPerMinute = 78
                )
            ),
            startZoneOffset = ZoneOffset.UTC,
            endZoneOffset = ZoneOffset.UTC,
            metadata = Metadata.autoRecorded(
                device = Device(type = Device.TYPE_WATCH)
            )
        )

        client.insertRecords(listOf(record))
    }

    suspend fun insertSleepSession() {
        val now = Instant.now()

        val record = SleepSessionRecord(
            startTime = now.minusSeconds(8 * 60 * 60),
            endTime = now,
            startZoneOffset = ZoneOffset.UTC,
            endZoneOffset = ZoneOffset.UTC,
            metadata = Metadata.manualEntry() // введены пользователем вручную

        )

        client.insertRecords(listOf(record))
    }


    /**
     * Возвращает общее количество шагов за указанный период.
     *
     * Использует агрегацию Health Connect, без доступа к отдельным записям.
     *
     * @param startTime начало периода
     * @param endTime конец периода
     * @return общее количество шагов или null, если данных нет
     */
    suspend fun aggregateSteps(
        startTime: Instant,
        endTime: Instant
    ): Long? {
        val response = client.aggregate(
            AggregateRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
        )
        return response[StepsRecord.COUNT_TOTAL]
    }


    /**
     * Возвращает список записей пульса за указанный период.
     *
     * Данные возвращаются в сыром виде, включая все измерения и метаданные.
     *
     * @param startTime начало периода
     * @param endTime конец периода
     * @return список HeartRateRecord (может быть пустым)
     */
    suspend fun readHeartRate(
        startTime: Instant,
        endTime: Instant
    ): List<HeartRateRecord> {
        val response = client.readRecords(
            ReadRecordsRequest(
                HeartRateRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
        )
        return response.records
    }

    suspend fun readSleepSessions(
        startTime: Instant,
        endTime: Instant
    ): List<SleepSessionRecord> {

        val response = client.readRecords(
            ReadRecordsRequest(
                SleepSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
        )

        return response.records
    }



}