package streetlight.model.data

import koala.SvgFile
import koala.SvgPack

/** An icon for each kind of resource. */
enum class ResourceSvg(val svg: SvgPack) {
    // Food
    FoodAssistance(SvgFile.Basket),
    FoodPantry(SvgFile.PaperBag),
    FoodBank(SvgFile.Package),
    SoupKitchen(SvgFile.Soup),
    FreeMeal(SvgFile.ToolsKitchen),
    Groceries(SvgFile.ShoppingCart),
    FoodStamps(SvgFile.CreditCard),
    InfantFormula(SvgFile.BabyBottle),
    SchoolMeals(SvgFile.Apple),
    MealDelivery(SvgFile.TruckDelivery),
    CommunityFridge(SvgFile.Fridge),
    CommunityGarden(SvgFile.Seedling),

    // Water
    Water(SvgFile.Droplet),
    DrinkingFountain(SvgFile.GlassFull),
    WaterRefill(SvgFile.Bottle),

    // Shelter & Housing
    Shelter(SvgFile.Home),
    EmergencyShelter(SvgFile.HomeExclamation),
    FamilyShelter(SvgFile.HomeHeart),
    YouthShelter(SvgFile.MoodKid),
    DomesticViolenceShelter(SvgFile.ShieldHeart),
    WarmingCenter(SvgFile.Fire),
    CoolingCenter(SvgFile.Snowflake),
    TransitionalHousing(SvgFile.Building),
    AffordableHousing(SvgFile.BuildingCommunity),
    HousingAssistance(SvgFile.Key),
    RentAssistance(SvgFile.Receipt),
    UtilityAssistance(SvgFile.Bulb),
    EvictionPrevention(SvgFile.HomeShield),
    SafeParking(SvgFile.Parking),
    Campsite(SvgFile.Tent),

    // Health
    Medical(SvgFile.FirstAidKit),
    Clinic(SvgFile.Stethoscope),
    UrgentCare(SvgFile.Ambulance),
    Hospital(SvgFile.BuildingHospital),
    Dental(SvgFile.Dental),
    Vision(SvgFile.Eyeglass),
    Pharmacy(SvgFile.Pill),
    Prescriptions(SvgFile.Prescription),
    Vaccination(SvgFile.Vaccine),
    DiseaseTesting(SvgFile.TestPipe),
    PrenatalCare(SvgFile.BabyCarriage),
    ReproductiveHealth(SvgFile.HeartPlus),
    Disability(SvgFile.Wheelchair),

    // Mental Health & Recovery
    MentalHealth(SvgFile.Brain),
    Counseling(SvgFile.MessageHeart),
    CrisisLine(SvgFile.PhoneCall),
    SuicidePrevention(SvgFile.Lifebuoy),
    SupportGroup(SvgFile.UsersGroup),
    PeerSupport(SvgFile.Friends),
    AddictionRecovery(SvgFile.StairsUp),
    HarmReduction(SvgFile.ShieldPlus),
    NaloxoneKit(SvgFile.MedicineSyrup),
    SyringeServices(SvgFile.VaccineBottle),
    NeedleDisposal(SvgFile.TrashX),

    // Hygiene
    Bathroom(SvgFile.ToiletPaper),
    Shower(SvgFile.Bath),
    Laundry(SvgFile.WashMachine),
    HygieneKit(SvgFile.WashHand),
    Haircut(SvgFile.Scissors),
    MenstrualProducts(SvgFile.DropletHeart),
    Diapers(SvgFile.BabyBottle),

    // Belongings
    Storage(SvgFile.Box),
    Lockers(SvgFile.Lock),
    Clothing(SvgFile.Shirt),
    Shoes(SvgFile.Shoe),
    WinterGear(SvgFile.Jacket),
    Blankets(SvgFile.Bed),
    Furniture(SvgFile.Armchair),
    HouseholdGoods(SvgFile.Bucket),
    FreeStore(SvgFile.Tag),
    DonationCenter(SvgFile.Gift),

    // Connection
    Mail(SvgFile.Mail),
    Phone(SvgFile.Phone),
    Charging(SvgFile.BatteryCharging),
    Wifi(SvgFile.Wifi),
    Computers(SvgFile.DeviceDesktop),
    Printing(SvgFile.Printer),

    // Documents & Legal
    IdServices(SvgFile.Id),
    BirthCertificate(SvgFile.Certificate),
    Documents(SvgFile.FileText),
    LegalAid(SvgFile.Scale),
    Immigration(SvgFile.World),
    Translation(SvgFile.Language),
    TaxHelp(SvgFile.ReceiptTax),
    BenefitsEnrollment(SvgFile.ClipboardCheck),

    // Money & Work
    CashAssistance(SvgFile.Cash),
    Banking(SvgFile.BuildingBank),
    FinancialCoaching(SvgFile.Coin),
    Employment(SvgFile.Briefcase),
    JobTraining(SvgFile.Tools),
    ResumeHelp(SvgFile.FileCv),
    DayLabor(SvgFile.Hammer),

    // Education & Family
    Education(SvgFile.School),
    Ged(SvgFile.Certificate),
    EnglishClasses(SvgFile.AlphabetLatin),
    Library(SvgFile.Books),
    Tutoring(SvgFile.Pencil),
    Childcare(SvgFile.HorseToy),
    Afterschool(SvgFile.Backpack),
    YouthProgram(SvgFile.MoodKid),
    SeniorServices(SvgFile.Old),

    // Services
    CaseManagement(SvgFile.ClipboardList),
    Outreach(SvgFile.Walk),
    DropInCenter(SvgFile.Door),
    DayCenter(SvgFile.Sun),
    ReEntry(SvgFile.DoorEnter),
    VeteranServices(SvgFile.Medal),
    WomensServices(SvgFile.GenderFemale),
    MensServices(SvgFile.GenderMale),
    LgbtqSupport(SvgFile.Rainbow),
    Advocacy(SvgFile.Speakerphone),
    SafeSpace(SvgFile.ShieldCheck),
    DisasterRelief(SvgFile.Storm),

    // Transportation
    Transportation(SvgFile.TransitBus),
    BusPass(SvgFile.Ticket),
    BikeRepair(SvgFile.Bike),
    Rides(SvgFile.Car),

    // Pets
    PetCare(SvgFile.Paw),
    PetFood(SvgFile.Bone),
    Veterinary(SvgFile.Dog),

    // Community
    Spiritual(SvgFile.Pray),
    Recreation(SvgFile.BallFootball),
    ArtProgram(SvgFile.Palette),
    GoodVibes(SvgFile.MoodHappy)
}
