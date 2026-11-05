package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.repository.SunnahRepository
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

private const val TAG = "SunanAiAssistant"

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

sealed interface AiUiState {
    data object Idle : AiUiState
    data object Loading : AiUiState
    data class Streaming(val partialResponse: String) : AiUiState
    data class Success(val response: String) : AiUiState
    data class Error(val message: String) : AiUiState
}

class AiAssistantViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val repository = SunnahRepository(database)

    // Lazy initialization of Firebase AI generative model as mandated by firebase-ai-logic-android
    private val model by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    private val _uiState = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val uiState: StateFlow<AiUiState> = _uiState.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "السلام عليكم ورحمة الله وبركاته 🌿\nأنا مساعدك الذكي في تطبيق «سُنَن». اسألني عن السنن النبوية في أي موقف أو وقت (مثلاً: سنن النوم، الصلاة، الجمعة، أو آداب التعامل) وسأجيبك بأدلة موثقة من الأحاديث النبوية الصحيحة مع التخريج والراوي.",
                isUser = false
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    fun askQuestion(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return

        // Append user message
        val userMsg = ChatMessage(text = trimmed, isUser = true)
        _messages.value = _messages.value + userMsg

        viewModelScope.launch {
            _uiState.value = AiUiState.Loading
            try {
                // Grounding step: fetch matching authentic Sunan from our local verified database
                val knowledgeBase = repository.getKnowledgeBaseForQuery(trimmed)

                val prompt = buildString {
                    append("أنت المساعد الذكي لتطبيق «سُنَن» المتخصص في السنن النبوية الشريفة وأفكار تطبيقها العملية.\n")
                    append("الضوابط الصارمة المنهجية التي يجب أن تلتزم بها حرفياً:\n")
                    append("1. استند فقط إلى الأحاديث الصحيحة والحسنة المقبولة. يُحظر تماماً اختلاق أي حديث أو نسبة كلام إلى النبي ﷺ لم يثبت.\n")
                    append("2. اذكر مع كل سنة نص الحديث أو معناه، وراوي الحديث، والمصدر (مثل صحيح البخاري، صحيح مسلم، سنن أبي داود، الترمذي، إلخ)، ودرجة الحديث.\n")
                    append("3. قدم فكرة عملية مبسطة تناسب واقع المسلم اليوم لتطبيق السنة بسهولة.\n")
                    append("4. إذا لم تجد دليلاً ثابتاً في المسألة أو لم ترد سنة مخصوصة فيها، قل بوضوح وأمانة علمية: «لم يرد في ذلك سنة مخصوصة ثابتة» أو «لا أعلم دليلاً في ذلك».\n")
                    append("5. لا تفتِ في المسائل الفقهية الخلافية أو النوازل؛ بل وجه السائل للرجوع لأهل العلم المعتبرين.\n")
                    append("6. تحدث بلغة عربية فصيحة، راقية، مريحة، وأنيقة.\n\n")
                    append("--- قاعدة البيانات الموثقة المسترجعة (RAG) ---\n")
                    append(knowledgeBase)
                    append("\n--- سؤال المستخدم ---\n")
                    append(trimmed)
                }

                val accumulated = StringBuilder()
                withTimeout(60_000L) {
                    model.generateContentStream(prompt).collect { chunk ->
                        val chunkText = chunk.text.orEmpty()
                        accumulated.append(chunkText)
                        _uiState.value = AiUiState.Streaming(accumulated.toString())
                    }
                }

                val finalText = accumulated.toString().trim()
                if (finalText.isNotEmpty()) {
                    _uiState.value = AiUiState.Success(finalText)
                    _messages.value = _messages.value + ChatMessage(text = finalText, isUser = false)
                } else {
                    _uiState.value = AiUiState.Error("لم يتم استلام رد من النموذج.")
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "AI Assistant timed out", e)
                val errMsg = "استغرقت المعالجة وقتاً طويلاً. يرجى المحاولة مرة أخرى."
                _uiState.value = AiUiState.Error(errMsg)
                _messages.value = _messages.value + ChatMessage(text = "⚠️ $errMsg", isUser = false)
            } catch (e: CancellationException) {
                // Preserve cooperative cancellation
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI Assistant error", e)
                val errMsg = e.localizedMessage ?: "حدث خطأ غير متوقع أثناء الاتصال بالمساعد الذكي."
                _uiState.value = AiUiState.Error(errMsg)
                _messages.value = _messages.value + ChatMessage(
                    text = "عذراً، حدث تعثر أثناء جلب الإجابة: $errMsg\nيمكنك البحث مباشرة في مكتبة السنن المحلية.",
                    isUser = false
                )
            }
        }
    }

    fun clearHistory() {
        _messages.value = listOf(
            ChatMessage(
                text = "تم بدء محادثة جديدة. اسألني عن أي سنة نبوية مع أدلتها وتخريجها 🌿",
                isUser = false
            )
        )
        _uiState.value = AiUiState.Idle
    }
}
