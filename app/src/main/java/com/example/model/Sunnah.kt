package com.example.model

enum class SunnahCategory(val titleAr: String, val iconName: String) {
    ALL("الكل", "All"),
    DAILY("يومية وعامة", "WbSunny"),
    PRAYER("الصلاة والأذان", "Mosque"),
    FOOD_DRINK("الطعام والشراب", "Restaurant"),
    SLEEP("النوم والاستيقاظ", "Bedtime"),
    MORNING_EVENING("أذكار اليوم", "AutoAwesome"),
    MANNERS("الأخلاق والتعامل", "Favorite"),
    TRAVEL("السفر والخروج", "Flight"),
    PURIFICATION("الوضوء والطهارة", "WaterDrop"),
    FRIDAY("يوم الجمعة", "CalendarToday")
}

enum class SunnahTime(val titleAr: String) {
    ALL("كل الأوقات"),
    MORNING("الصباح"),
    EVENING("المساء"),
    SLEEP("عند النوم"),
    PRAYER("أوقات الصلاة"),
    GENERAL("عام طوال اليوم")
}

enum class EvidenceType(val titleAr: String) {
    ALL("كل الأدلة"),
    HADITH("حديث نبوي"),
    QURAN("آية قرآنية")
}

enum class AuthenticityGrade(val titleAr: String, val colorCode: Long) {
    ALL("جميع الدرجات", 0xFF6C757D),
    MUTTAFAAQ_ALAYH("متفق عليه (البخاري ومسلم)", 0xFF6F42C1),
    SAHIH("صحيح", 0xFF198754),
    HASAN("حسن", 0xFF0D6EFD)
}

data class Sunnah(
    val id: Int,
    val title: String,
    val category: SunnahCategory,
    val timeCategory: SunnahTime,
    val practicalIdea: String,
    val evidenceText: String,
    val evidenceType: EvidenceType,
    val narrator: String,
    val source: String,
    val authenticity: AuthenticityGrade,
    val benefit: String,
    val isFavorite: Boolean = false,
    val isAppliedToday: Boolean = false,
    val timesAppliedTotal: Int = 0
)
