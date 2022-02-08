import xlib.XButtonEvent
import xlib.XWindowAttributes

data class MotionInfo(val start: XButtonEvent, val attrs: XWindowAttributes)