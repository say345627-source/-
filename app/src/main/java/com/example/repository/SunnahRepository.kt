package com.example.repository

import com.example.data.AppDatabase
import com.example.data.SunnahDefaultData
import com.example.model.AuthenticityGrade
import com.example.model.EvidenceType
import com.example.model.Sunnah
import com.example.model.SunnahCategory
import com.example.model.SunnahTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SunnahRepository(private val database: AppDatabase) {
    private val dao = database.sunnahDao()

    fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
        if (dao.getCount() == 0) {
            dao.insertAll(SunnahDefaultData.initialSunan)
        }
    }

    fun getAllSunan(): Flow<List<Sunnah>> {
        val today = getTodayDate()
        return dao.getAllSunan().map { list ->
            list.map { it.toDomain(today) }
        }
    }

    fun getFavorites(): Flow<List<Sunnah>> {
        val today = getTodayDate()
        return dao.getFavorites().map { list ->
            list.map { it.toDomain(today) }
        }
    }

    fun getAppliedToday(): Flow<List<Sunnah>> {
        val today = getTodayDate()
        return dao.getAppliedToday(today).map { list ->
            list.map { it.toDomain(today) }
        }
    }

    suspend fun toggleFavorite(id: Int, current: Boolean) = withContext(Dispatchers.IO) {
        dao.setFavorite(id, !current)
    }

    suspend fun toggleAppliedToday(id: Int, currentlyApplied: Boolean) = withContext(Dispatchers.IO) {
        val today = getTodayDate()
        if (currentlyApplied) {
            dao.unmarkAppliedToday(id)
        } else {
            dao.markAppliedToday(id, today)
        }
    }

    suspend fun getDailySunnah(): Sunnah? = withContext(Dispatchers.IO) {
        ensureSeeded()
        val today = getTodayDate()
        val all = SunnahDefaultData.initialSunan
        if (all.isEmpty()) return@withContext null
        // Pick consistent sunnah based on day of year hash
        val dayHash = Math.abs(today.hashCode())
        val selectedId = (dayHash % all.size) + 1
        val entity = dao.getSunnahById(selectedId) ?: all[selectedId - 1]
        entity.toDomain(today)
    }

    /**
     * RAG Knowledge Base retriever for AI assistant grounding.
     * Extracts relevant authentic Sunan matching keywords or returns a comprehensive authentic summary.
     */
    suspend fun getKnowledgeBaseForQuery(query: String): String = withContext(Dispatchers.IO) {
        ensureSeeded()
        val today = getTodayDate()
        val allEntities = SunnahDefaultData.initialSunan
        val cleanQuery = query.trim().lowercase(Locale.getDefault())

        // Keyword matching
        val matched = allEntities.filter { entity ->
            entity.title.contains(cleanQuery, ignoreCase = true) ||
            entity.practicalIdea.contains(cleanQuery, ignoreCase = true) ||
            entity.evidenceText.contains(cleanQuery, ignoreCase = true) ||
            entity.category.contains(cleanQuery, ignoreCase = true) ||
            entity.timeCategory.contains(cleanQuery, ignoreCase = true)
        }

        val candidates = if (matched.isNotEmpty()) matched.take(8) else allEntities.take(12)

        buildString {
            append("قاعدة بيانات السنن النبوية الموثقة المتاحة:\n\n")
            candidates.forEachIndexed { index, item ->
                append("${index + 1}. السنة: ${item.title}\n")
                append("   - التصنيف: ${item.category} | الوقت: ${item.timeCategory}\n")
                append("   - الفكرة العملية: ${item.practicalIdea}\n")
                append("   - الدليل: ${item.evidenceText}\n")
                append("   - الراوي: ${item.narrator} | المصدر: ${item.source}\n")
                append("   - الدرجة: ${item.authenticity}\n")
                append("   - الفضل: ${item.benefit}\n\n")
            }
        }
    }
}
