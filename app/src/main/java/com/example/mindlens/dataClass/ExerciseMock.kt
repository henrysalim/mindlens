package com.example.mindlens.dataClass

// Data structure for storing exercise data
data class ExerciseMock(
    val id: String,
    val title: String,
    val duration: String,
    val videoId: String,
    val imageUrl: String,
    val description: String,
    val steps: List<String>
)

// List of exercises that users can do
val allExercises = listOf(
    ExerciseMock(
        id = "yoga_1",
        title = "Morning Sun Salutation",
        duration = "10 min",
        videoId = "ZP34IA0d8LI", // Valid ID
        imageUrl = "https://images.unsplash.com/photo-1544367563-12123d896889?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80",
        description = "Awaken your body with this flow to start your day with energy.",
        steps = listOf(
            "Stand tall",
            "Inhale and raise your arms",
            "Fold forward"
        )
    ),
    ExerciseMock(
        id = "yoga_2",
        title = "Yoga for Stress Relief",
        duration = "15 min",
        videoId = "hJbRpHZr_d0", // Valid ID
        imageUrl = "https://images.unsplash.com/photo-1506126613408-eca07ce68773?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80",
        description = "Release stress and muscle tension after a long day.",
        steps = listOf(
            "Child's Pose",
            "Cat-Cow Stretch",
            "Downward Dog"
        )
    ),
    // MEDITATION
    ExerciseMock(
        id = "med_1",
        title = "5-Minute Mindfulness",
        duration = "5 min",
        videoId = "HNab2YqCCiM", // Valid ID
        imageUrl = "https://images.unsplash.com/photo-1593811167562-9cef47bfc4d7?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80",
        description = "A quick meditation to restore focus and inner calm.",
        steps = listOf(
            "Sit comfortably",
            "Focus on your breath",
            "Let go of your thoughts"
        )
    ),
    ExerciseMock(
        id = "med_2",
        title = "Deep Sleep Music",
        duration = "20 min",
        videoId = "IVDuU3anYCI", // Valid ID
        imageUrl = "https://images.unsplash.com/photo-1515023115689-589c33041697?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80",
        description = "Relaxing music and visual guidance to help you sleep deeply.",
        steps = listOf(
            "Lie down",
            "Close your eyes",
            "Relax your muscles"
        )
    )
)
