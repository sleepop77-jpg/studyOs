package com.example.studyos.core

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow

data class Item(
    val id: String,
    val name: String,
    val description: String,
    val type: String,
    val cost: Int
)

object Store {
    val ITEMS = listOf(
        Item(id = "item_halo_scholar", name = "Scholar Halo", description = "Animated golden halo for the mascot.", type = "Mascot", cost = 120),
        Item(id = "item_ninja_headband", name = "Ninja Headband", description = "A disciplined study-warrior headband.", type = "Mascot", cost = 150),
        Item(id = "item_party_mode", name = "Party Mode", description = "Tiny celebration aura around the mascot.", type = "Mascot", cost = 180),
        Item(id = "item_cyberpunk", name = "Cyberpunk Visor", description = "Neon visor for late-night focus sessions.", type = "Mascot", cost = 220),
        Item(id = "item_night_owl_skin", name = "Night Owl Cap", description = "Sleepy focus cap with moonlight vibes.", type = "Mascot", cost = 240),
        Item(id = "item_golden_desk", name = "Golden Desk", description = "Premium golden study desk trim.", type = "Mascot", cost = 260),
        Item(id = "item_aurora_dream", name = "Aurora Dream", description = "Animated northern-light study background.", type = "Theme", cost = 260),
        Item(id = "item_math_matrix", name = "Math Matrix", description = "Dark green matrix-inspired theme.", type = "Theme", cost = 180),
        Item(id = "item_spanish_fiesta", name = "Sol & Coral Palette", description = "Warm Andalusian sunset colors.", type = "Theme", cost = 200),
        Item(id = "item_astral_crown", name = "Astral Crown", description = "Holographic crown with orbiting constellations.", type = "Mascot", cost = 450),
        Item(id = "item_dragon_aura", name = "Dragon Aura", description = "Animated ethereal dragon flames and floating embers.", type = "Mascot", cost = 500),
        Item(id = "item_neon_katana", name = "Neon Katana", description = "Cyberpunk glowing katana resting on the desk.", type = "Mascot", cost = 400),
        Item(id = "theme_void_nexus", name = "Void Nexus", description = "Deep space anomaly with swirling dark matter.", type = "Theme", cost = 400),
        Item(id = "theme_sakura_drift", name = "Sakura Drift", description = "Midnight cherry blossoms with falling petals.", type = "Theme", cost = 450),
        Item(id = "theme_crimson_focus", name = "Crimson Focus", description = "Classic black & red focus theme.", type = "Theme", cost = 200),
        Item(id = "item_free_neon_ring", name = "Neon Ring (FREE)", description = "Free animated neon ring around the mascot.", type = "Mascot", cost = 0),
        Item(id =  "item_free_orbit ", name =  "Golden Orbit (FREE) ", description =  "Free animated golden orbit around the mascot. ", type =  "Mascot ", cost = 0),
Item(id = "item_big_bull", name = "The Big Bull", description = "The legendary golden bull of Dalal Street. Horns of fortune, nose ring of commitment.", type = "Mascot", cost = 1500),
Item(id = "item_cosmic_scholar", name = "Cosmic Scholar Aura", description = "A rotating galaxy with orbiting planets, pulsing nebulae, and shooting stars. Intensifies during focus sessions.", type = "Mascot", cost = 800)
    )

    val unlocked = MutableStateFlow<Set<String>>(emptySet())
    val equippedMascot = MutableStateFlow<String?>(null)
    val equippedTheme = MutableStateFlow<String?>(null)

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        if (::prefs.isInitialized) return
        prefs = context.applicationContext.getSharedPreferences("studyos_store", Context.MODE_PRIVATE)
        unlocked.value = prefs.getStringSet("unlocked", emptySet()) ?: emptySet()
        equippedMascot.value = prefs.getString("mascot", null)
        equippedTheme.value = prefs.getString("theme", null)
    }

    fun buy(id: String): Boolean {
        val item = ITEMS.firstOrNull { it.id == id } ?: return false
        if (unlocked.value.contains(id)) return true
        if (!Economy.spend(item.cost)) return false
        unlocked.value = unlocked.value + id
        save()
        return true
    }

    fun equip(id: String) {
        val item = ITEMS.firstOrNull { it.id == id } ?: return
        if (!unlocked.value.contains(id)) return
        when (item.type) {
            "Mascot" -> equippedMascot.value = id
            "Theme" -> equippedTheme.value = id
        }
        save()
    }

    fun unlockAll() {
        unlocked.value = ITEMS.map { it.id }.toSet()
        save()
    }

    private fun save() {
        if (!::prefs.isInitialized) return
        prefs.edit()
            .putStringSet("unlocked", unlocked.value)
            .putString("mascot", equippedMascot.value)
            .putString("theme", equippedTheme.value)
            .apply()
    }
}