package com.example.mindlens.dataClass

// data structure for storing exercise data
data class ExerciseMock(
    val id: String,
    val title: String,
    val duration: String,
    val videoId: String,
    val imageUrl: String,
    val description: String,
    val steps: List<String>
)

// list of exercises that user can do
val allExercises = listOf(
    ExerciseMock(
        id = "yoga_1",
        title = "Morning Sun Salutation",
        duration = "10 min",
        videoId = "ZP34IA0d8LI", // Valid ID
        imageUrl = "https://images.unsplash.com/photo-1544367563-12123d896889?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80",
        description = "Awaken your body with this flow to start your day with energy.",
        steps = listOf("Stand tall", "Inhale arms up", "Fold forward")
    ),
    ExerciseMock(
        id = "yoga_2",
        title = "Yoga for Stress Relief",
        duration = "15 min",
        videoId = "hJbRpHZr_d0", // Valid ID
        imageUrl = "https://images.unsplash.com/photo-1506126613408-eca07ce68773?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80",
        description = "Hilangkan stres dan ketegangan otot setelah seharian bekerja.",
        steps = listOf("Child's pose", "Cat-Cow stretch", "Downward Dog")
    ),
    // MEDITATION
    ExerciseMock(
        id = "med_1",
        title = "5 Min Mindfulness",
        duration = "5 min",
        videoId = "HNab2YqCCiM", // Valid ID
        imageUrl = "https://images.unsplash.com/photo-1593811167562-9cef47bfc4d7?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80",
        description = "Meditasi cepat untuk mengembalikan fokus dan ketenangan.",
        steps = listOf("Sit comfortably", "Focus on breath", "Let go of thoughts")
    ),
    ExerciseMock(
        id = "med_2",
        title = "Deep Sleep Music",
        duration = "20 min",
        videoId = "IVDuU3anYCI", // Valid ID
        imageUrl = "https://images.unsplash.com/photo-1515023115689-589c33041697?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80",
        description = "Musik relaksasi dan panduan visual untuk tidur nyenyak.",
        steps = listOf("Lie down", "Close eyes", "Relax muscles")
    )
)