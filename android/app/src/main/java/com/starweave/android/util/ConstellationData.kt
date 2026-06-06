package com.starweave.android.util

/**
 * Real star positions (RA in hours, Dec in degrees) for named bright stars,
 * and constellation connection data matching the web version.
 */
object ConstellationData {
    data class BrightStar(
        val name: String,
        val raHours: Float,   // Right Ascension in hours (0-24)
        val decDeg: Float,    // Declination in degrees (-90 to +90)
        val magnitude: Float  // Brightness (lower = brighter)
    )

    // 12 named bright stars with real astronomical coordinates
    val BRIGHT_STARS = listOf(
        BrightStar("Sirius", 6.75f, -16.72f, -1.46f),
        BrightStar("Canopus", 6.4f, -52.7f, -0.74f),
        BrightStar("Arcturus", 14.26f, 19.18f, -0.05f),
        BrightStar("Vega", 18.62f, 38.78f, 0.03f),
        BrightStar("Capella", 5.28f, 46.0f, 0.08f),
        BrightStar("Rigel", 5.24f, -8.2f, 0.13f),
        BrightStar("Procyon", 7.65f, 5.22f, 0.34f),
        BrightStar("Betelgeuse", 5.92f, 7.41f, 0.42f),
        BrightStar("Altair", 19.85f, 8.87f, 0.77f),
        BrightStar("Deneb", 20.69f, 45.28f, 1.25f),
        BrightStar("Spica", 13.42f, -11.16f, 0.97f),
        BrightStar("Antares", 16.49f, -26.43f, 1.04f)
    )

    // Orion constellation connections (indices into BRIGHT_STARS)
    // Betelgeuse=7, Rigel=6 (wait, let me re-check indices)
    // Actually, Orion uses specific star positions relative to the constellation shape
    // We store pairs of (starIndex1, starIndex2) - these connect the constellation outline
    data class Constellation(val name: String, val connections: List<Pair<Int, Int>>)

    // For the background field, we generate constellation star positions randomly
    // but store connection patterns here
    val ORION_CONNECTIONS = listOf(
        0 to 1, 1 to 2, 2 to 3, 3 to 4, 4 to 5, 5 to 6, 6 to 0
    )

    val URSA_MAJOR_CONNECTIONS = listOf(
        0 to 1, 1 to 2, 2 to 3, 3 to 4, 4 to 5, 5 to 6
    )

    // Spectral color distribution (matches web version)
    data class SpectralType(val color: Int, val weight: Float)

    val SPECTRAL_TYPES = listOf(
        SpectralType(0xFFAACCFF.toInt(), 0.10f),  // O/B - blue
        SpectralType(0xFFBBDDFF.toInt(), 0.15f),  // A - blue-white
        SpectralType(0xFFFFFFFF.toInt(), 0.20f),  // F - white
        SpectralType(0xFFFFF4E8.toInt(), 0.25f),  // G - yellow-white
        SpectralType(0xFFFFE4B5.toInt(), 0.15f),  // K - yellow
        SpectralType(0xFFFFCC99.toInt(), 0.10f),  // K - orange
        SpectralType(0xFFFFB088.toInt(), 0.05f),  // M - red
    )

    // Healing tags (matches web version)
    val HEALING_TAGS = listOf(
        "会好的" to "一切都会好起来的，给自己一点时间，让星光治愈你的心。",
        "加油" to "你比你想象中更坚强，每一个努力的你都在闪闪发光。",
        "抱抱你" to "虽然隔着星河，但请收下这个温暖的拥抱。",
        "放下了" to "有些事，放下了才能重新出发。你已经做得很好了。",
        "想开点" to "换个角度看看，也许没有想象中那么糟糕。",
        "慢慢来" to "放慢脚步也没关系，星河一直在那里等你。",
        "我懂" to "你的感受是真实的，有人和你一样在经历着。",
        "没关系" to "犯错也没关系，成长本来就是磕磕绊绊的过程。"
    )

    val HEALING_EMOJIS = mapOf(
        "会好的" to "🌱", "加油" to "💪", "抱抱你" to "🤗",
        "放下了" to "🍃", "想开点" to "☀️", "慢慢来" to "🐢",
        "我懂" to "💜", "没关系" to "🌈"
    )
}
