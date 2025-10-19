# Timing Trials

This is an Android app that can be used to time sporting events, where each competitor starts at a fixed interval.
It was developed with cycling time trials in mind.

## Features

- Timing screen 
  - features easy way to note rider numbers as they pass the finish
  - notifies of rider start times
- Results generation and historic result database
  - supports personal records and course records
  - results can be exported as an image and CSV
- Event setup screen
  - designed streamline event setup
  - persistence of riders and courses minimises need to repeatedly enter these details


## To Build

```Powershell
./gradlew build
```

The APK should then be at [/app/build/outputs/apk/release/app-release.apk](/app/build/outputs/apk/release/app-release.apk)

## To Run Tests

```Powershell
./gradlew test
```

## Timing Screen

1. When a rider passes - press timer button to create an timestamp.

2. Make sure the timestamp is active (blue, not grey). 
    1. Now check the rider number. Press their number.

3. The timestamp should be "assigned" that number and change in appearance. Either "Rider finished" or "Rider passed" (if there are more than one lap). 
    1. You can long press an assigned event to unassign it again. 

4. Once all riders have finished or DNF/DNS (long press number to do this) you can go to the result view.

![timing screen](/doc/timing-screen.png "Timing Screen")

This workflow "create timestamp" then "assign number" designed to help when multiple riders pass at once.
Choosing the timestamp first allows you to get the exact time of passing correct. 
Then you can check their number carefully, as time is now not an issue,  and assign them to the correct timestamp.


## Technical Notes
Uses:
- AndroidX architecture components
- SQLight Database with Room, 
- LiveData and ViewModels for reactive UI, 
- Dagger 2 for dependency injection
- Material Design components for UI.

Special thanks to:
- Haruki Hasegawa for the awesome Advanced RecyclerView 
    - [https://github.com/h6ah4i/android-advancedrecyclerview](https://github.com/h6ah4i/android-advancedrecyclerview)

- Jake Wharton  for Android Three Ten APB Backport and Timber
    - [https://github.com/JakeWharton/ThreeTenABP](https://github.com/JakeWharton/ThreeTenABP)
    - [https://github.com/JakeWharton/timber](https://github.com/JakeWharton/timber)

- Open CSV
    - [https://opencsv.sourceforge.net/](https://opencsv.sourceforge.net/)

## Other Notes
This was written in order to learn Android dev, and to help in the 
real world where pen and paper is often used instead.