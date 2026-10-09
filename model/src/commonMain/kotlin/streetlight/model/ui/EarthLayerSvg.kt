package streetlight.model.ui

import koala.SvgFile
import koala.SvgPack

/** An icon for each layer of the earth map. */
enum class EarthLayerSvg(val svg: SvgPack) {
    Events(SvgFile.Calendar),
    Galaxy(SvgFile.Planet),
    City(SvgFile.City),
    Locations(SvgFile.MapPin),
    Resources(SvgFile.Lifebuoy),
    Lights(SvgFile.Bulb),
    Stars(SvgFile.Star),
    Friends(SvgFile.Social),
    Transit(SvgFile.TransitBus),
    Streets(SvgFile.Road),
    Bikes(SvgFile.Bike),
    Traffic(SvgFile.TrafficLights),
    Parks(SvgFile.Trees),
    Terrain(SvgFile.Mountain),
    Satellite(SvgFile.Satellite),
    Weather(SvgFile.Cloud),
    Heatmap(SvgFile.ChartDots),
    Labels(SvgFile.Tag)
}
