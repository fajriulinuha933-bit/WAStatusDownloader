# WA Status Downloader (Android)

Prototype Android app for copying WhatsApp Status media that the user has already viewed, with user-selected source folder and one-tap saving to public Gallery folders.


## Features
- Choose a WhatsApp `.Statuses` source folder using Android's folder picker (SAF).
- Browse image/video statuses in a two-column grid.
- Filter images and videos.
- One-tap copy to `Pictures/WA Status Downloader` or `Movies/WA Status Downloader`.
- Persist folder permission between launches.

## Build APK
### Android Studio
1. Install Android Studio and Android SDK Platform 35.
2. Open this project folder (`WAStatusDownloader`).
3. Allow Gradle sync and dependency downloads.
4. Select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. The debug APK appears at `app/build/outputs/apk/debug/app-debug.apk`.

### GitHub Actions (automated cloud build)
A workflow is included at `.github/workflows/android-apk.yml`.
1. Upload this project to a GitHub repository.
2. Open the repository's **Actions** tab and select **Build WA Status Downloader APK**.
3. Click **Run workflow** (workflow_dispatch).
4. When finished, download the artifact named `WAStatusDownloader-debug-apk`.


## First run
1. Tap **Pilih folder .Statuses WhatsApp**.
2. Select the WhatsApp Status folder and grant access.
3. View the status in WhatsApp first, then open this app and tap Refresh.
4. Tap **UNDUH** on a file to copy it to the Gallery folder.


## Notes
- Access/visibility of WhatsApp `.Statuses` varies by Android version, WhatsApp edition and device manufacturer. Some file pickers may not expose hidden `.Statuses` folders.
- This app does not bypass Android permissions. It only copies files from a folder the user explicitly selected.
- This is a prototype; it has not yet been tested on physical devices.
