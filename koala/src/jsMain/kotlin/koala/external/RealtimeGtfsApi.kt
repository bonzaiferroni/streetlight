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

package koala.external

// Ahoy! We've moved the enums to the top level to keep the ship tidy.

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

enum class DescriptorScheduleRelationship {
    SCHEDULED,
    ADDED,          // deprecated in proto; kept for completeness
    UNSCHEDULED,
    CANCELED,
    REPLACEMENT,
    DUPLICATED,
    DELETED,
    NEW,
}

enum class WheelchairBoarding {
    UNKNOWN,
    AVAILABLE,
    NOT_AVAILABLE,
}

/**
 * Proto2 → Kotlin model (external classes for the JS world).
 *
 * Notes:
 * - proto2 `required` → non-null Kotlin property.
 * - proto2 `optional` → nullable Kotlin property.
 * - proto2 `repeated` → List<T> or Array<T>.
 * - uint32/uint64 in protobuf are *unsigned*; here represented as Int/Long for practicality.
 */

/* ========== Top-level feed ========== */

external class FeedMessage<T>(
    val header: FeedHeader,
    val entity: Array<T>,
)

external class FeedHeader(
    val gtfsRealtimeVersion: String,
    val incrementality: Incrementality,
    val timestamp: Long,          // uint64
    val feedVersion: String?,
)

enum class Incrementality {
    FULL_DATASET,
    DIFFERENTIAL,
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

/* ========== TripUpdate ========== */

external class TripUpdate(
    val trip: TripDescriptor,
    val stopTimeUpdate: List<StopTimeUpdate>,
    val vehicle: VehicleDescriptor?,
    val timestamp: Long?,          // uint64
    val delay: Int?,
    val tripProperties: TripProperties?,
)

external class StopTimeEvent(
    val delay: Int?,
    val time: Long?,           // int64
    val uncertainty: Int?,
    val scheduledTime: Long?,  // int64
)

external class StopTimeUpdate(
    val stopSequence: Int?,    // uint32
    val stopId: String?,
    val arrival: StopTimeEvent?,
    val departure: StopTimeEvent?,
    val departureOccupancyStatus: OccupancyStatus?,
    val scheduleRelationship: ScheduleRelationship,
    val stopTimeProperties: StopTimeProperties?,
)

external class StopTimeProperties(
    val assignedStopId: String?,
    val stopHeadsign: String?,
    val pickupType: DropOffPickupType?,
    val dropOffType: DropOffPickupType?,
)

enum class DropOffPickupType {
    REGULAR,
    NONE,
    PHONE_AGENCY,
    COORDINATE_WITH_DRIVER,
}

enum class ScheduleRelationship {
    SCHEDULED,
    SKIPPED,
    NO_DATA,
    UNSCHEDULED,
}

external class TripProperties(
    val tripId: String?,
    val startDate: String?,
    val startTime: String?,
    val shapeId: String?,
    val tripHeadsign: String?,
    val tripShortName: String?,
)

/* ========== VehiclePosition ========== */

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
)

external class CarriageDetails(
    val id: String?,
    val label: String?,
    val occupancyStatus: OccupancyStatus,
    val occupancyPercentage: Int,
    val carriageSequence: Int?, // uint32
)

/* ========== Alert + supporting types ========== */

external class Alert(
    val activePeriod: List<TimeRange>,
    val informedEntity: List<EntitySelector>,
    val cause: Cause,
    val effect: Effect,
    val url: TranslatedString?,
    val headerText: TranslatedString?,
    val descriptionText: TranslatedString?,
    val ttsHeaderText: TranslatedString?,
    val ttsDescriptionText: TranslatedString?,
    val severityLevel: SeverityLevel,
    val image: TranslatedImage?,
    val imageAlternativeText: TranslatedString?,
    val causeDetail: TranslatedString?,
    val effectDetail: TranslatedString?,
)

external class TimeRange(
    val start: Long?,              // uint64
    val end: Long?,                // uint64
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
    val scheduleRelationship: DescriptorScheduleRelationship?,
    val modifiedTrip: ModifiedTripSelector?,
)

external class ModifiedTripSelector(
    val modificationsId: String?,
    val affectedTripId: String?,
    val startTime: String?,
    val startDate: String?,
)

external class VehicleDescriptor(
    val id: String?,
    val label: String?,
    val licensePlate: String?,
    val wheelchairAccessible: WheelchairAccessible,
)

enum class WheelchairAccessible {
    NO_VALUE,
    UNKNOWN,
    WHEELCHAIR_ACCESSIBLE,
    WHEELCHAIR_INACCESSIBLE,
}

external class EntitySelector(
    val agencyId: String?,
    val routeId: String?,
    val routeType: Int?,
    val trip: TripDescriptor?,
    val stopId: String?,
    val directionId: Int?,         // uint32
)

/* ========== Translated text / images ========== */

external class TranslatedString(
    val translation: List<Translation>,
)

external class Translation(
    val text: String,
    val language: String?,
)

external class TranslatedImage(
    val localizedImage: List<LocalizedImage>,
)

external class LocalizedImage(
    val url: String,
    val mediaType: String,
    val language: String?,
)

/* ========== Shape / Stop ========== */

external class Shape(
    val shapeId: String?,
    val encodedPolyline: String?,
)

external class Stop(
    val stopId: String?,
    val stopCode: TranslatedString?,
    val stopName: TranslatedString?,
    val ttsStopName: TranslatedString?,
    val stopDesc: TranslatedString?,
    val stopLat: Float?,
    val stopLon: Float?,
    val zoneId: String?,
    val stopUrl: TranslatedString?,
    val parentStation: String?,
    val stopTimezone: String?,
    val wheelchairBoarding: WheelchairBoarding,
    val levelId: String?,
    val platformCode: TranslatedString?,
)

/* ========== TripModifications + helpers ========== */

external class TripModifications(
    val selectedTrips: List<SelectedTrips>,
    val startTimes: List<String>,
    val serviceDates: List<String>,
    val modifications: List<Modification>,
)

external class Modification(
    val startStopSelector: StopSelector?,
    val endStopSelector: StopSelector?,
    val propagatedModificationDelay: Int,
    val replacementStops: List<ReplacementStop>,
    val serviceAlertId: String?,
    val lastModifiedTime: Long?, // uint64
)

external class SelectedTrips(
    val tripIds: List<String>,
    val shapeId: String?,
)

external class StopSelector(
    val stopSequence: Int?,        // uint32
    val stopId: String?,
)

external class ReplacementStop(
    val travelTimeToStop: Int?,
    val stopId: String?,
)
