package streetlight.model.data

import koala.SvgFile
import koala.SvgPack

/** An icon for each status of an event. */
enum class EventStatusSvg(val svg: SvgPack) {
    Draft(SvgFile.Pencil),
    Pending(SvgFile.Hourglass),
    Upcoming(SvgFile.Calendar),
    StartingSoon(SvgFile.Clock),
    Live(SvgFile.Broadcast),
    OnBreak(SvgFile.PlayerPause),
    Delayed(SvgFile.ClockPause),
    Finished(SvgFile.CircleCheck),
    Canceled(SvgFile.CalendarX),
    Postponed(SvgFile.CalendarPause),
    Rescheduled(SvgFile.CalendarRepeat),
    Moved(SvgFile.MapPinShare),
    SoldOut(SvgFile.TicketOff),
    Waitlist(SvgFile.ListNumbers),
    Tentative(SvgFile.CalendarQuestion)
}
