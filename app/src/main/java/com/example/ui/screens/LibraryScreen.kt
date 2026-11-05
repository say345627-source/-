package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AuthenticityGrade
import com.example.model.EvidenceType
import com.example.model.SunnahCategory
import com.example.model.SunnahTime
import com.example.ui.components.SunnahCard
import com.example.viewmodel.SunnahViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: SunnahViewModel,
    modifier: Modifier = Modifier
) {
    val sunanList by viewModel.filteredSunan.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val dailySunnah by viewModel.dailySunnah.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("library_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Hero Section with Daily Sunnah
        item {
            DailySunnahHeroCard(
                sunnah = dailySunnah,
                onToggleApplied = { sunnah -> viewModel.toggleAppliedToday(sunnah) }
            )
        }

        // Search Input
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = filterState.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_sunan_input"),
                    placeholder = {
                        Text(
                            text = "ابحث عن سنة، حديث، راوٍ، أو فكرة عملية...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "بحث",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (filterState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "مسح البحث"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }

        // Top Filter Bar Section (Multi-Filter Chips)
        item {
            FilterChipsSection(
                filterState = filterState,
                onSelectCategory = { viewModel.selectCategory(it) },
                onSelectTime = { viewModel.selectTimeCategory(it) },
                onSelectAuthenticity = { viewModel.selectAuthenticity(it) },
                onSelectEvidence = { viewModel.selectEvidenceType(it) },
                onResetFilters = { viewModel.resetFilters() },
                resultsCount = sunanList.size
            )
        }

        // Sunan List or Empty State
        if (sunanList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "لا توجد سنن مطابقة لهذه الفلاتر",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "جرب تعديل خيارات البحث أو إعادة ضبط الفلاتر",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = { viewModel.resetFilters() }) {
                            Text("إعادة ضبط الفلاتر")
                        }
                    }
                }
            }
        } else {
            items(
                items = sunanList,
                key = { it.id }
            ) { sunnah ->
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    SunnahCard(
                        sunnah = sunnah,
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onToggleApplied = { viewModel.toggleAppliedToday(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun DailySunnahHeroCard(
    sunnah: com.example.model.Sunnah?,
    onToggleApplied: (com.example.model.Sunnah) -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("daily_sunnah_hero_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "سُـنَّـةُ اليَــوْم",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                if (sunnah != null) {
                    Surface(
                        color = if (sunnah.isAppliedToday) Color(0xFF198754) else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (sunnah.isAppliedToday) "طُبِّقت اليوم ✓" else "لم تطبق بعد",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (sunnah.isAppliedToday) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (sunnah != null) {
                Text(
                    text = sunnah.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = sunnah.practicalIdea,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Short Hadith quote
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = sunnah.evidenceText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(10.dp),
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { onToggleApplied(sunnah) },
                        modifier = Modifier.testTag("apply_daily_sunnah_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (sunnah.isAppliedToday) "إلغاء التطبيق" else "سجلها كـ «طُبّقت اليوم»",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChipsSection(
    filterState: com.example.viewmodel.FilterState,
    onSelectCategory: (SunnahCategory) -> Unit,
    onSelectTime: (SunnahTime) -> Unit,
    onSelectAuthenticity: (AuthenticityGrade) -> Unit,
    onSelectEvidence: (EvidenceType) -> Unit,
    onResetFilters: () -> Unit,
    resultsCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Section 1: Time Chips (أوقات اليوم)
        Text(
            text = "الوقت والمناسبة:",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SunnahTime.values().forEach { time ->
                val selected = filterState.timeCategory == time
                FilterChip(
                    selected = selected,
                    onClick = { onSelectTime(time) },
                    label = { Text(time.titleAr) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Section 2: Topic / Category Chips (الموضوع والتصنيف)
        Text(
            text = "الموضوع والتصنيف:",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SunnahCategory.values().forEach { cat ->
                val selected = filterState.category == cat
                FilterChip(
                    selected = selected,
                    onClick = { onSelectCategory(cat) },
                    label = { Text(cat.titleAr) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Section 3: Authenticity & Evidence Type (درجة الصحة ونوع الدليل)
        Text(
            text = "درجة الصحة ونوع الدليل:",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AuthenticityGrade.values().forEach { auth ->
                val selected = filterState.authenticity == auth
                FilterChip(
                    selected = selected,
                    onClick = { onSelectAuthenticity(auth) },
                    label = { Text(auth.titleAr) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                        selectedLabelColor = MaterialTheme.colorScheme.onTertiary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            EvidenceType.values().filter { it != EvidenceType.ALL }.forEach { ev ->
                val selected = filterState.evidenceType == ev
                FilterChip(
                    selected = selected,
                    onClick = { onSelectEvidence(if (selected) EvidenceType.ALL else ev) },
                    label = { Text(ev.titleAr) },
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        // Header Summary Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "المعروض: $resultsCount سُنَّة موثقة",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (filterState.isFiltered) {
                TextButton(
                    onClick = onResetFilters,
                    modifier = Modifier.testTag("reset_filters_button")
                ) {
                    Text("إلغاء الفلاتر", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
