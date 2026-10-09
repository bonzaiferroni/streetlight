package streetlight.model.data

import koala.SvgFile
import koala.SvgPack

/** An icon for each kind of button action. */
enum class ButtonSvg(val svg: SvgPack) {
    // Edit
    Save(SvgFile.DeviceFloppy),
    Cancel(SvgFile.X),
    Close(SvgFile.X),
    Delete(SvgFile.Trash),
    Edit(SvgFile.Pencil),
    Add(SvgFile.Plus),
    Remove(SvgFile.Minus),
    Duplicate(SvgFile.CopyPlus),
    Copy(SvgFile.Copy),
    Paste(SvgFile.Clipboard),
    Cut(SvgFile.Cut),
    Undo(SvgFile.ArrowBackUp),
    Redo(SvgFile.ArrowForwardUp),
    Clear(SvgFile.Eraser),
    Reset(SvgFile.Restore),
    Apply(SvgFile.Check),
    Confirm(SvgFile.CircleCheck),
    Submit(SvgFile.Send),
    Retry(SvgFile.Reload),
    Refresh(SvgFile.Refresh),
    Sync(SvgFile.RefreshDot),

    // Publish
    Publish(SvgFile.WorldUpload),
    Unpublish(SvgFile.WorldOff),
    Draft(SvgFile.Pencil),
    Archive(SvgFile.Archive),
    Restore(SvgFile.Restore),
    Approve(SvgFile.ThumbUp),
    Reject(SvgFile.ThumbDown),
    Report(SvgFile.Flag),
    Block(SvgFile.Ban),

    // Navigate
    Back(SvgFile.ArrowLeft),
    Forward(SvgFile.ArrowRight),
    Previous(SvgFile.ChevronLeft),
    Next(SvgFile.ChevronRight),
    ShowMore(SvgFile.ChevronDown),
    ShowLess(SvgFile.ChevronUp),
    Expand(SvgFile.ArrowsMaximize),
    Collapse(SvgFile.ArrowsMinimize),
    Open(SvgFile.ExternalLink),
    Home(SvgFile.Home),
    Menu(SvgFile.Menu),
    More(SvgFile.Dots),
    Settings(SvgFile.Gear),
    Help(SvgFile.Question),
    Info(SvgFile.Info),

    // Find
    Search(SvgFile.Search),
    Filter(SvgFile.Filter),
    Sort(SvgFile.ArrowsSort),
    List(SvgFile.List),
    Grid(SvgFile.LayoutGrid),
    Map(SvgFile.Map),
    Locate(SvgFile.CurrentLocation),
    Directions(SvgFile.Directions),
    ZoomIn(SvgFile.ZoomIn),
    ZoomOut(SvgFile.ZoomOut),
    Fullscreen(SvgFile.Maximize),
    ExitFullscreen(SvgFile.Minimize),

    // Arrange
    Move(SvgFile.ArrowsMove),
    DragHandle(SvgFile.GripVertical),
    Reorder(SvgFile.ArrowsUpDown),
    Pin(SvgFile.Pin),
    Unpin(SvgFile.PinnedOff),
    Merge(SvgFile.GitMerge),
    Split(SvgFile.ArrowsSplit),

    // Files
    Upload(SvgFile.Upload),
    Download(SvgFile.Download),
    Import(SvgFile.FileImport),
    Export(SvgFile.FileExport),
    Attach(SvgFile.Paperclip),
    Print(SvgFile.Printer),
    Link(SvgFile.Link),
    Unlink(SvgFile.Unlink),
    Camera(SvgFile.Camera),
    UploadImage(SvgFile.PhotoUp),
    Crop(SvgFile.Crop),
    Rotate(SvgFile.RotateClockwise),
    Flip(SvgFile.FlipHorizontal),
    Scan(SvgFile.Scan),
    QrCode(SvgFile.Qrcode),

    // Account
    Login(SvgFile.Login),
    Logout(SvgFile.Logout),
    SignUp(SvgFile.UserPlus),
    Profile(SvgFile.User),
    Lock(SvgFile.Lock),
    Unlock(SvgFile.LockOpen),
    Hide(SvgFile.EyeOff),
    Show(SvgFile.Eye),

    // Social
    Like(SvgFile.Heart),
    Unlike(SvgFile.HeartOff),
    Star(SvgFile.Star),
    Bookmark(SvgFile.Bookmark),
    Follow(SvgFile.UserHeart),
    Unfollow(SvgFile.UserMinus),
    Subscribe(SvgFile.Bell),
    Unsubscribe(SvgFile.BellOff),
    Notifications(SvgFile.BellRinging),
    Share(SvgFile.Share),
    Comment(SvgFile.Message),
    Reply(SvgFile.MessageReply),
    Message(SvgFile.MessageCircle),
    Mute(SvgFile.VolumeOff),
    Unmute(SvgFile.Volume),
    Invite(SvgFile.MailPlus),
    Join(SvgFile.DoorEnter),
    Leave(SvgFile.DoorExit),
    Contact(SvgFile.Mail),
    Call(SvgFile.Phone),
    Feedback(SvgFile.MessageDots),

    // Events
    Rsvp(SvgFile.CalendarCheck),
    AddToCalendar(SvgFile.CalendarPlus),
    Today(SvgFile.Calendar),
    Tickets(SvgFile.Ticket),
    Buy(SvgFile.ShoppingCart),
    Checkout(SvgFile.CreditCard),
    Donate(SvgFile.HeartDollar),

    // Media
    Play(SvgFile.PlayerPlay),
    Pause(SvgFile.PlayerPause),
    Stop(SvgFile.PlayerStop),
    Skip(SvgFile.PlayerSkipForward),
    Record(SvgFile.PlayerRecord),
    Microphone(SvgFile.Microphone),

    // Text
    Bold(SvgFile.Bold),
    Italic(SvgFile.Italic),
    Underline(SvgFile.Underline),
    Code(SvgFile.Code),
    Translate(SvgFile.Language),

    // Display
    Theme(SvgFile.Palette),
    DarkMode(SvgFile.Moon),
    LightMode(SvgFile.Sun),
    Generate(SvgFile.Magic)
}
