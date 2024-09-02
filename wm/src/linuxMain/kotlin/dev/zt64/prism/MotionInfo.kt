package dev.zt64.prism

import xlib.XButtonEvent
import xlib.XWindowAttributes

internal data class MotionInfo(val start: XButtonEvent, val attrs: XWindowAttributes)