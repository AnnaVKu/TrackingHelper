# About
Repository TrackingHelper contains two apps. First of them tracking locations. Second of them showing locations.
# Description
TrackingHelper contains two apps and use MVVM architecture pattern.
1) trackingLocationsApp tracking lacations every 30 sec and send them into firebase.
2) showLocationsApp showing route: 
 - User can chose a current day for showing route;
 - user can find own location.
# Instructions:
Before running the app in packege showLocationsApp/trackingLocationsApp in AndroidManifest.xml -> meta-data ->  android:value="YOUR_API_KEY"
- api key from Google Maps SDK.
# Technologies:
Kotlin, View Binding, Firebase (Auth (email and password), RealTimeDatabase), Dagger2, RxJava3, Google Maps SDK.
