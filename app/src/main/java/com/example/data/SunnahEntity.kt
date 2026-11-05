package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.AuthenticityGrade
import com.example.model.EvidenceType
import com.example.model.Sunnah
import com.example.model.SunnahCategory
import com.example.model.SunnahTime

@Entity(tableName = "sunan")
data class SunnahEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val category: String,
    val timeCategory: String,
    val practicalIdea: String,
    val evidenceText: String,
    val evidenceType: String,
    val narrator: String,
    val source: String,
    val authenticity: String,
    val benefit: String,
    val isFavorite: Boolean = false,
    val lastAppliedDate: String = "", // e.g. "2026-11-05"
    val timesAppliedTotal: Int = 0
) {
    fun toDomain(todayDate: String): Sunnah {
        return Sunnah(
            id = id,
            title = title,
            category = runCatching { SunnahCategory.valueOf(category) }.getOrDefault(SunnahCategory.DAILY),
            timeCategory = runCatching { SunnahTime.valueOf(timeCategory) }.getOrDefault(SunnahTime.GENERAL),
            practicalIdea = practicalIdea,
            evidenceText = evidenceText,
            evidenceType = runCatching { EvidenceType.valueOf(evidenceType) }.getOrDefault(EvidenceType.HADITH),
            narrator = narrator,
            source = source,
            authenticity = runCatching { AuthenticityGrade.valueOf(authenticity) }.getOrDefault(AuthenticityGrade.SAHIH),
            benefit = benefit,
            isFavorite = isFavorite,
            isAppliedToday = lastAppliedDate == todayDate,
            timesAppliedTotal = timesAppliedTotal
        )
    }

    companion object {
        fun fromDomain(sunnah: Sunnah, todayDate: String): SunnahEntity {
            return SunnahEntity(
                id = sunnah.id,
                title = sunnah.title,
                category = sunnah.category.name,
                timeCategory = sunnah.timeCategory.name,
                practicalIdea = sunnah.practicalIdea,
                evidenceText = sunnah.evidenceText,
                evidenceType = sunnah.evidenceType.name,
                narrator = sunnah.narrator,
                source = sunnah.source,
                authenticity = sunnah.authenticity.name,
                benefit = sunnah.benefit,
                isFavorite = sunnah.isFavorite,
                lastAppliedDate = if (sunnah.isAppliedToday) todayDate else "",
                timesAppliedTotal = sunnah.timesAppliedTotal
            )
        }
    }
}
