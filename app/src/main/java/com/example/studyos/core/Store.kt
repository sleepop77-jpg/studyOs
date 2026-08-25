package com.example.studyos.core

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow

data class Item(
    val id: String,
    val name: String,
    val desc: String,
    val category: String,
    val cost: Int
)

object Store {
    val ITEMS = listOf(
        Item("item_golden_desk", "Golden Desk Aesthetic", "Gold trimming for the mascot desk.", "Mascot", 150),
        Item("item_cyberpunk", "Cyberpunk StudyBuddy", "Neon visor and cyber headset.", "Mascot", 250),
        Item("item_night_owl_skin", "Night Owl Mascot Skin", "Sleepy nightcap with starry halo.", "Mascot", 180),
        Item("item_halo_scholar", "Scholar Halo", "A flying golden ring with orbiting sparks. Animated.", "Mascot", 300),
        Item("item_ninja_headband", "Ninja Headband", "Animated ninja headband with waving tails.", "Mascot", 250),
        Item("item_party_mode", "Party Mode", "Animated party hat with falling confetti.", "Mascot", 200),
        Item("item_aurora_dream", "Aurora Dream Theme", "Animated aurora launcher background.", "Theme", 250),
        Item("item_math_matrix", "Matrix Hacker Theme", "Emerald terminal aesthetic.", "Theme", 200),
        Item("item_spanish_fiesta", "Sol & Coral Palette", "Warm Andalusian sunset colors.", "Theme", 200)
    )
    val unlocked = MutableStateFlow<Set<String>>(emptySet())
    val equippedMascot = MutableStateFlow<String?>(null)
    val equippedTheme = MutableStateFlow<String?>(null)
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        if (::prefs.isInitialized) return
        prefs = context.getSharedPreferences("studyos_store", Context.MODE_PRIVATE)
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

    fun equip(category: String, id: String) {
        if (category == "Mascot") {
            equippedMascot.value = if (equippedMascot.value == id) null else id
            prefs.edit().putString("mascot", equippedMascot.value).apply()
        } else {
            equippedTheme.value = if (equippedTheme.value == id) null else id
            prefs.edit().putString("theme", equippedTheme.value).apply()
        }
    }

    fun unlockAll() {
        unlocked.value = ITEMS.map { it.id }.toSet()
        save()
    }

    private fun save() {
        if (!::prefs.isInitialized) return
        prefs.edit().putStringSet("unlocked", unlocked.value).apply()
    }
}
