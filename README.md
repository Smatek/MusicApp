# MusicApp

## Description
An Android music player application built with Kotlin and Jetpack Compose that utilizes the Audius API to stream music. 
I created this project to learn how to use Cursor IDE.

## Features
*   Browse trending tracks from Audius
*   Browse trending playlists from Audius
*   Search for tracks
*   Stream music directly within the app - with prefetching to boost performance
*   Clean, intuitive UI built with Jetpack Compose following Material 3 guidelines.

## Tech Stack & Architecture
*   **Language:** Kotlin
*   **UI:** Jetpack Compose
*   **Architecture:** MVI (Model-View-Intent)
*   **Asynchronous Programming:** Kotlin Coroutines & StateFlow
*   **Dependency Injection:** Hilt
*   **Networking:** Retrofit (for Audius API)
*   **Database:** Room
*   **Testing:** JUnit, Mockk, Paparazzi (for screenshot testing)
*   **API:** Audius API

## Audius API Integration
This project uses the [Audius API](https://docs.audius.org/) to fetch music data and enable streaming features. No API key is required for basic usage.