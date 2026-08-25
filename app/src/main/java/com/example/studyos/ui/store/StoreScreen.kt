package com.example.studyos.ui.store

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.studyos.ui.launcher.AnimatedMascotPreview
import com.example.studyos.ui.launcher.AnimatedSkins
import com.example.studyos.ui.launcher.AnimatedThemePreview
import com.example.studyos.ui.theme.AccentTeal
import com.example.studyos.ui.theme.FameGold
import com.example.studyos.ui.theme.OnSurfaceDark
import com.example.studyos.ui.theme.OnSurfaceMuted
import com.example.studyos.ui.theme.SuccessGreen
import com.example.studyos.ui.theme.SurfaceCream
import kotlinx.coroutines.launch

@Composable
fun StoreScreen(back: () -> Unit) {
    val fame by Economy.fame.collectAsState()
    val unlocked by Store.unlocked.collectAsState()
    val eqMascot by Store.equippedMascot.collectAsState(initial = null)
    val eqTheme by Store.equippedTheme.collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.studyos.ui.common.homeBrush())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp, start = 8.dp, end = 20.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = back,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
            ) {
                Icon(SIcons.Back, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(20.dp))
            }

            Text(
                "Fame Store",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                letterSpacing = 0.5.sp,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(FameGold)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(SIcons.Star, contentDescription = null, tint = OnSurfaceDark, modifier = Modifier.size(16.dp))
                    Text("$fame", color = OnSurfaceDark, fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 0.5.sp)
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(Store.ITEMS, key = { it.id }) { item ->
                val isUnlocked = unlocked.contains(item.id)
                val isEquipped = (item.type == "Mascot" && eqMascot == item.id) || (item.type == "Theme" && eqTheme == item.id)

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                item.type == "Mascot" && item.id in AnimatedSkins.MASCOT_SET -> AnimatedMascotPreview(item.id, size = 60.dp)
                                item.type == "Theme" -> AnimatedThemePreview(item.id, size = 60.dp)
                                else -> Icon(SIcons.Bag, contentDescription = null, tint = Color(0xFFF5A623), modifier = Modifier.size(26.dp))
                            }
                        }

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                item.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White,
                                letterSpacing = 0.3.sp
                            )
                            Text(
                                item.description,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.6f),
                                lineHeight = 14.sp,
                                letterSpacing = 0.2.sp
                            )
                        }

                        when {
                            !isUnlocked -> Button(
                                onClick = { scope.launch { Store.buy(item.id) } },
                                enabled = fame >= item.cost,
                                colors = ButtonDefaults.buttonColors(containerColor = FameGold),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Text(
                                    "${item.cost}",
                                    color = OnSurfaceDark,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            else -> Button(
                                onClick = { Store.equip(item.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isEquipped) SuccessGreen else AccentTeal),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(38.dp)
                            ) {
                                if (isEquipped) Icon(SIcons.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Text(
                                    if (isEquipped) "Equipped" else "Equip",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.size(48.dp)) }
        }
    }
}
