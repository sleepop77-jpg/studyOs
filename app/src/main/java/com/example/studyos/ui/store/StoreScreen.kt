package com.example.studyos.ui.store

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyos.core.Economy
import com.example.studyos.core.Store
import com.example.studyos.ui.common.SIcons
import com.example.studyos.ui.common.homeBrush
import com.example.studyos.ui.launcher.AnimatedMascotPreview
import com.example.studyos.ui.launcher.AnimatedSkins
import com.example.studyos.ui.launcher.AnimatedThemePreview
import com.example.studyos.ui.theme.AccentTeal
import com.example.studyos.ui.theme.FameGold
import com.example.studyos.ui.theme.OnSurfaceDark
import com.example.studyos.ui.theme.SuccessGreen
import kotlinx.coroutines.launch

@Composable
fun StoreScreen(back: () -> Unit) {
    val fame by Economy.fame.collectAsState()
    val unlocked by Store.unlocked.collectAsState()
    val eqMascot by Store.equippedMascot.collectAsState(initial = null)
    val eqTheme by Store.equippedTheme.collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().background(homeBrush())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp, start = 4.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = back) { Icon(SIcons.Back, contentDescription = "Back", tint = Color.White) }
            Text("Fame Store", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp, modifier = Modifier.weight(1f))
            Box(modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(FameGold).padding(horizontal = 10.dp, vertical = 4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(SIcons.Star, contentDescription = null, tint = OnSurfaceDark, modifier = Modifier.size(14.dp))
                    Text("$fame", color = OnSurfaceDark, fontWeight = FontWeight.Black, fontSize = 13.sp)
                }
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(Store.ITEMS, key = { it.id }) { item ->
                val isUnlocked = unlocked.contains(item.id)
                val isEquipped = (item.type == "Mascot" && eqMascot == item.id) || (item.type == "Theme" && eqTheme == item.id)

                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFE8706C).copy(alpha = 0.25f)), contentAlignment = Alignment.Center) {
                            when {
                                item.type == "Mascot" && item.id in AnimatedSkins.MASCOT_SET -> AnimatedMascotPreview(item.id, size = 56.dp)
                                item.type == "Theme" -> AnimatedThemePreview(item.id, size = 56.dp)
                                else -> Icon(SIcons.Bag, contentDescription = null, tint = Color(0xFFF5A623), modifier = Modifier.size(24.dp))
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            Text(item.description, fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                        }

                        when {
                            !isUnlocked -> Button(
                                onClick = { scope.launch { Store.buy(item.id) } },
                                enabled = fame >= item.cost,
                                colors = ButtonDefaults.buttonColors(containerColor = FameGold),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text("${item.cost}", color = OnSurfaceDark, fontWeight = FontWeight.Black, fontSize = 13.sp) }
                            else -> Button(
                                onClick = { Store.equip(item.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isEquipped) SuccessGreen else AccentTeal),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isEquipped) Icon(SIcons.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Text(if (isEquipped) "On" else "Equip", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.size(40.dp)) }
        }
    }
}
