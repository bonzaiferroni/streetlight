package streetlight.model.data

import koala.SvgFile
import koala.SvgPack

/** An icon for each kind of vehicle. */
enum class VehicleSvg(val svg: SvgPack) {
    Bus(SvgFile.TransitBus),
    Shuttle(SvgFile.BusStop),
    LightRail(SvgFile.Tram),
    Train(SvgFile.Locomotive),
    Streetcar(SvgFile.Tram),
    Subway(SvgFile.Locomotive),
    Ferry(SvgFile.Ship),
    Boat(SvgFile.Sailboat),
    Bike(SvgFile.Bike),
    Scooter(SvgFile.Scooter),
    Motorcycle(SvgFile.Motorbike),
    Car(SvgFile.Car),
    Taxi(SvgFile.Car),
    Suv(SvgFile.CarSuv),
    Truck(SvgFile.Truck),
    Camper(SvgFile.Caravan),
    Plane(SvgFile.Plane),
    Helicopter(SvgFile.Helicopter),
    Walk(SvgFile.Walk),
    Wheelchair(SvgFile.Wheelchair),
    Skateboard(SvgFile.Skateboard)
}
