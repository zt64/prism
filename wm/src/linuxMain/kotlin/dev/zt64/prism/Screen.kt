package dev.zt64.prism
// 4     WINDOW                          root
// 4     COLORMAP                        default-colormap
// 4     CARD32                          white-pixel
// 4     CARD32                          black-pixel
// 4     SETofEVENT                      current-input-masks
// 2     CARD16                          width-in-pixels
// 2     CARD16                          height-in-pixels
// 2     CARD16                          width-in-millimeters
// 2     CARD16                          height-in-millimeters
// 2     CARD16                          min-installed-maps
// 2     CARD16                          max-installed-maps
// 4     VISUALID                        root-visual
// 1                                     backing-stores
// 0     Never
// 1     WhenMapped
// 2     Always
// 1     BOOL                            save-unders
// 1     CARD8                           root-depth
// 1     CARD8                           number of DEPTHs in allowed-depths
// n     LISTofDEPTH                     allowed-depths (n is always a multiple of 4)

data class Screen(
//    val display: Display
    val root: Window,
    val defaultColormap: Colormap,
    val whitePixel: Int,
    val blackPixel: Int,
    val currentInputMasks: Int,
    val widthInPixels: Int,
    val heightInPixels: Int,
    val widthInMillimeters: Int,
    val heightInMillimeters: Int,
    val minInstalledMaps: Int,
    val maxInstalledMaps: Int,
    val rootVisual: VisualId,
    val backingStores: BackingStore,
    val saveUnders: Boolean,
    val rootDepth: Int,
    val allowedDepths: List<Depth>
)

typealias Colormap = Int

data class Depth(val depth: Int, val visuals: List<Visual>)

data class Visual(
    val visualId: VisualId,
    val type: VisualType,
    val bitsPerRgbValue: Int,
    val colormapEntries: Int,
    val redMask: Int,
    val greenMask: Int,
    val blueMask: Int
)

enum class VisualType {
    STATIC_GRAY,
    GRAY_SCALE,
    STATIC_COLOR,
    PSEUDO_COLOR,
    TRUE_COLOR,
    DIRECT_COLOR
}

typealias VisualId = Int

enum class BackingStore {
    NEVER,
    WHEN_MAPPED,
    ALWAYS
}

enum class BitGravity {
    FORGET,
    STATIC,
    NORTH_WEST,
    NORTH,
    NORTH_EAST,
    WEST,
    CENTER,
    EAST,
    SOUTH_WEST,
    SOUTH,
    SOUTH_EAST
}

enum class WinGravity {
    UNMAP,
    STATIC,
    NORTH_WEST,
    NORTH,
    NORTH_EAST,
    WEST,
    CENTER,
    EAST,
    SOUTH_WEST,
    SOUTH,
    SOUTH_EAST
}