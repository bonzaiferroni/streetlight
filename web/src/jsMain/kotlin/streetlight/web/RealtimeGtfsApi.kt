// Copyright 2015 The GTFS Specifications Authors.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// Protocol definition file for GTFS Realtime.
//
// GTFS Realtime lets transit agencies provide consumers with realtime
// information about disruptions to their service (stations closed, lines not
// operating, important delays etc), location of their vehicles and expected
// arrival times.
//
// This protocol is published at:
// https://github.com/google/transit/tree/master/gtfs-realtime

@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package streetlight.web

import streetlight.web.VehiclePosition.OccupancyStatus

/**
 * Proto2 → Kotlin model (plain data classes).
 *
 * Notes:
 * - proto2 `required` → non-null Kotlin property (no default unless obvious).
 * - proto2 `optional` → nullable Kotlin property (or default when proto has [default=...]).
 * - proto2 `repeated` → List<T> (default emptyList()).
 * - uint32/uint64 in ..streetlight.web.protobuf are *unsigned*; here represented as Int/Long for practicality.
 *   (If ye truly want UInt/ULong, swap types where marked.)
 */

/* ========== Top-level feed ========== */

data class FeedMessage(
    val header: FeedHeader,
    val entity: List<FeedEntity> = emptyList(),
)

data class FeedHeader(
    val gtfsRealtimeVersion: String,
    val incrementality: Incrementality = Incrementality.FULL_DATASET,
    val timestamp: Long? = null,          // uint64
    val feedVersion: String? = null,
) {
    enum class Incrementality {
        FULL_DATASET,
        DIFFERENTIAL,
    }
}

external class FeedEntity(
    val id: String,
    val isDeleted: Boolean,
    val tripUpdate: TripUpdate?,
    val vehicle: VehiclePosition?,
    val alert: Alert?,
    val shape: Shape?,
    val stop: Stop?,
    val tripModifications: TripModifications?,
)

/* ========== ..streetlight.web.TripUpdate ========== */

data class TripUpdate(
    val trip: TripDescriptor,
    val stopTimeUpdate: List<StopTimeUpdate> = emptyList(),
    val vehicle: VehicleDescriptor? = null,
    val timestamp: Long? = null,          // uint64
    val delay: Int? = null,
    val tripProperties: TripProperties? = null,
) {
    data class StopTimeEvent(
        val delay: Int? = null,
        val time: Long? = null,           // int64
        val uncertainty: Int? = null,
        val scheduledTime: Long? = null,  // int64
    )

    data class StopTimeUpdate(
        val stopSequence: Int? = null,    // uint32
        val stopId: String? = null,
        val arrival: StopTimeEvent? = null,
        val departure: StopTimeEvent? = null,
        val departureOccupancyStatus: VehiclePosition.OccupancyStatus? = null,
        val scheduleRelationship: ScheduleRelationship = ScheduleRelationship.SCHEDULED,
        val stopTimeProperties: StopTimeProperties? = null,
    ) {
        enum class ScheduleRelationship {
            SCHEDULED,
            SKIPPED,
            NO_DATA,
            UNSCHEDULED,
        }

        data class StopTimeProperties(
            val assignedStopId: String? = null,
            val stopHeadsign: String? = null,
            val pickupType: DropOffPickupType? = null,
            val dropOffType: DropOffPickupType? = null,
        ) {
            enum class DropOffPickupType {
                REGULAR,
                NONE,
                PHONE_AGENCY,
                COORDINATE_WITH_DRIVER,
            }
        }
    }

    data class TripProperties(
        val tripId: String? = null,
        val startDate: String? = null,
        val startTime: String? = null,
        val shapeId: String? = null,
        val tripHeadsign: String? = null,
        val tripShortName: String? = null,
    )
}

/* ========== ..streetlight.web.VehiclePosition ========== */

external class VehiclePosition(
    val trip: TripDescriptor?,
    val vehicle: VehicleDescriptor?,
    val position: Position?,
    val currentStopSequence: Int?, // uint32
    val stopId: String?,
    val currentStatus: VehicleStopStatus,
    val timestamp: Long?,          // uint64
    val congestionLevel: CongestionLevel?,
    val occupancyStatus: OccupancyStatus?,
    val occupancyPercentage: Int?, // uint32
    val multiCarriageDetails: List<CarriageDetails>,
) {
    enum class VehicleStopStatus {
        INCOMING_AT,
        STOPPED_AT,
        IN_TRANSIT_TO,
    }

    enum class CongestionLevel {
        UNKNOWN_CONGESTION_LEVEL,
        RUNNING_SMOOTHLY,
        STOP_AND_GO,
        CONGESTION,
        SEVERE_CONGESTION,
    }

    enum class OccupancyStatus {
        EMPTY,
        MANY_SEATS_AVAILABLE,
        FEW_SEATS_AVAILABLE,
        STANDING_ROOM_ONLY,
        CRUSHED_STANDING_ROOM_ONLY,
        FULL,
        NOT_ACCEPTING_PASSENGERS,
        NO_DATA_AVAILABLE,
        NOT_BOARDABLE,
    }
}

external class CarriageDetails(
    val id: String?,
    val label: String?,
    val occupancyStatus: OccupancyStatus,
    val occupancyPercentage: Int,
    val carriageSequence: Int?, // uint32
)

/* ========== ..streetlight.web.Alert + supporting types ========== */

data class Alert(
    val activePeriod: List<TimeRange> = emptyList(),
    val informedEntity: List<EntitySelector> = emptyList(),
    val cause: Cause = Cause.UNKNOWN_CAUSE,
    val effect: Effect = Effect.UNKNOWN_EFFECT,
    val url: TranslatedString? = null,
    val headerText: TranslatedString? = null,
    val descriptionText: TranslatedString? = null,
    val ttsHeaderText: TranslatedString? = null,
    val ttsDescriptionText: TranslatedString? = null,
    val severityLevel: SeverityLevel = SeverityLevel.UNKNOWN_SEVERITY,
    val image: TranslatedImage? = null,
    val imageAlternativeText: TranslatedString? = null,
    val causeDetail: TranslatedString? = null,
    val effectDetail: TranslatedString? = null,
) {
    enum class Cause {
        UNKNOWN_CAUSE,
        OTHER_CAUSE,
        TECHNICAL_PROBLEM,
        STRIKE,
        DEMONSTRATION,
        ACCIDENT,
        HOLIDAY,
        WEATHER,
        MAINTENANCE,
        CONSTRUCTION,
        POLICE_ACTIVITY,
        MEDICAL_EMERGENCY,
    }

    enum class Effect {
        NO_SERVICE,
        REDUCED_SERVICE,
        SIGNIFICANT_DELAYS,
        DETOUR,
        ADDITIONAL_SERVICE,
        MODIFIED_SERVICE,
        OTHER_EFFECT,
        UNKNOWN_EFFECT,
        STOP_MOVED,
        NO_EFFECT,
        ACCESSIBILITY_ISSUE,
    }

    enum class SeverityLevel {
        UNKNOWN_SEVERITY,
        INFO,
        WARNING,
        SEVERE,
    }
}

data class TimeRange(
    val start: Long? = null,              // uint64
    val end: Long? = null,                // uint64
)

external class Position(
    val latitude: Float,
    val longitude: Float,
    val bearing: Float?,
    val odometer: Double?,
    val speed: Float?,
)

/* ========== Descriptors / selectors ========== */

external class TripDescriptor(
    val tripId: String?,
    val routeId: String?,
    val directionId: Int?,
    val startTime: String?,
    val startDate: String?,
    val scheduleRelationship: ScheduleRelationship?,
    val modifiedTrip: ModifiedTripSelector?,
) {
    enum class ScheduleRelationship {
        SCHEDULED,
        ADDED,          // deprecated in proto; kept for completeness
        UNSCHEDULED,
        CANCELED,
        REPLACEMENT,
        DUPLICATED,
        DELETED,
        NEW,
    }
}

external class ModifiedTripSelector(
    val modificationsId: String?,
    val affectedTripId: String?,
    val startTime: String?,
    val startDate: String?,
)

data class VehicleDescriptor(
    val id: String? = null,
    val label: String? = null,
    val licensePlate: String? = null,
    val wheelchairAccessible: WheelchairAccessible = WheelchairAccessible.NO_VALUE,
) {
    enum class WheelchairAccessible {
        NO_VALUE,
        UNKNOWN,
        WHEELCHAIR_ACCESSIBLE,
        WHEELCHAIR_INACCESSIBLE,
    }
}

data class EntitySelector(
    val agencyId: String? = null,
    val routeId: String? = null,
    val routeType: Int? = null,
    val trip: TripDescriptor? = null,
    val stopId: String? = null,
    val directionId: Int? = null,         // uint32
)

/* ========== Translated text / images ========== */

data class TranslatedString(
    val translation: List<Translation> = emptyList(),
) {
    data class Translation(
        val text: String,
        val language: String? = null,
    )
}

data class TranslatedImage(
    val localizedImage: List<LocalizedImage> = emptyList(),
) {
    data class LocalizedImage(
        val url: String,
        val mediaType: String,
        val language: String? = null,
    )
}

/* ========== ..streetlight.web.Shape / ..streetlight.web.Stop ========== */

data class Shape(
    val shapeId: String? = null,
    val encodedPolyline: String? = null,
)

data class Stop(
    val stopId: String? = null,
    val stopCode: TranslatedString? = null,
    val stopName: TranslatedString? = null,
    val ttsStopName: TranslatedString? = null,
    val stopDesc: TranslatedString? = null,
    val stopLat: Float? = null,
    val stopLon: Float? = null,
    val zoneId: String? = null,
    val stopUrl: TranslatedString? = null,
    val parentStation: String? = null,
    val stopTimezone: String? = null,
    val wheelchairBoarding: WheelchairBoarding = WheelchairBoarding.UNKNOWN,
    val levelId: String? = null,
    val platformCode: TranslatedString? = null,
) {
    enum class WheelchairBoarding {
        UNKNOWN,
        AVAILABLE,
        NOT_AVAILABLE,
    }
}

/* ========== ..streetlight.web.TripModifications + helpers ========== */

data class TripModifications(
    val selectedTrips: List<SelectedTrips> = emptyList(),
    val startTimes: List<String> = emptyList(),
    val serviceDates: List<String> = emptyList(),
    val modifications: List<Modification> = emptyList(),
) {
    data class Modification(
        val startStopSelector: StopSelector? = null,
        val endStopSelector: StopSelector? = null,
        val propagatedModificationDelay: Int = 0,
        val replacementStops: List<ReplacementStop> = emptyList(),
        val serviceAlertId: String? = null,
        val lastModifiedTime: Long? = null, // uint64
    )

    data class SelectedTrips(
        val tripIds: List<String> = emptyList(),
        val shapeId: String? = null,
    )
}

data class StopSelector(
    val stopSequence: Int? = null,        // uint32
    val stopId: String? = null,
)

data class ReplacementStop(
    val travelTimeToStop: Int? = null,
    val stopId: String? = null,
)
