# android-nd-location-reminder

Location Reminder app for the Android Developer Nano-Degree course. This is the 4th project
for the Udacity Android Kotlin Developer Nanodegree Program.

Android LocationReminder is a TODO list app with location reminders that reminds the
user to do something when the user is at a specific location. The app will require the
user to create an account and login to set and access reminders.

The app consists of following screens:

- welcome screen which guides the user to the login screen
- login screen to login or register using email or Google account
- main screen displaying a list of reminders
- create reminder screen to create a new reminder and allows to set a location on a Google maps
- details screen which shows a reminder's details. 

This app makes use of following Android features:

1. DI using Koin
2. Firebase authentication and FirebaseUI
3. Google maps
4. MVVM architecture
5. Android Architecture components such as ViewModels and LiveData 
6. Room database for persistence
7. Runtime permission handling
7. User notifications
8. Use of Espresso and Mockito to test the app including DAO (Data Access Object) and Repository
   classes and End-To-End testing for the Fragments navigation.


## Firebase API and Google maps API

Firebase API and Google maps API require API keys for access and use.
For privacy reasons, these keys are not included in the public code repository.

In order to build the project, you need to

1. add your your own `google-services.json` file to `app/` directory
2. provide your own Google maps API key as described below.

The build of this app is configured in a way that it uses a build configuration field for
providing the necessary API key in the source code. It requires the definition of a string
with name `GOOGLE_MAPS_API_KEY` in a local `gradle.properties` file (e.g. in `~/.gradle/gradle.properties`).
Alternatively, you can override the value of the defined `resValue` directly in `app/build.gradle`
(not recommended).


## Screenshots

<img src="https://raw.githubusercontent.com/jploz/android-nd-location-reminder/main/screenshots/Screenshot_060852042.png" width="480"/>
