# iOS Clock Clone

**iOS Clock Clone** is a mobile application that replicates the iOS Clock app. With this app, users can select cities from a list and display their respective local times.

## Features

- Choose cities from a list and view their local time.
- Synchronize time with an NTP server for accurate timekeeping.

## Technologies Used

- Developed in Android using Kotlin.
- Utilizes RecyclerView for displaying the list of cities.
- Syncs time with an NTP server using the TrueTime library.
- Serializes/deserializes city list with Gson and SharedPreferences.

## Installation

1. Clone this repository.
2. Open the project in Android Studio.
3. Build and run the project on an Android device or emulator.

## How to Use

1. Launch the application on your Android device.
2. Tap the "Add" button to select cities.
3. Choose cities from the list provided in the fragment.
4. The selected cities and their local times will be displayed on the main screen.

## Known Issues

- Animation inconsistency when opening the fragment at start.
- Clock update could skip which seems to be network related.
- Not possible to properly scroll in long list of cities.

## Future Improvements

- Resolve animation inconsistencies in the fragment.
- Implement custom fonts for text elements.
- Add additional features, such as alarm and timer.

