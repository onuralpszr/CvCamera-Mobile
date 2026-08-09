# Changelog

All notable changes to this project will be documented in this file.

## [cv-4.14.0] - 2026-08-09

### Bug Fixes

- Update dependency androidx.core:core-ktx to v1.18.0
- Update dependency androidx.core:core-ktx to v1.18.0
- Update dependency com.google.android.material:material to v1.14.0
- Update corektx to v1.19.0
- Update dependency androidx.constraintlayout:constraintlayout to v2.2.2
- Update dependency androidx.core:core-ktx to v1.19.0
- Honor selected camera resolution and add canvas fill mode
- Write EXIF orientation into saved photos

### Features

- Update OpenCV to 4.14.0
- Rotate control icons instead of the whole bottom bar
- Add camera flash torch control

### Miscellaneous Tasks

- Bump compileSdk to 37

### Refactor

- Move each feature into its own pluggable file

### Styling

- Drop redundant parentheses in ControlsRotator



## [cv-4.13.0] - 2026-01-29

### Bug Fixes

- 🎨 auto format pre-commit hooks
- 🔧 update version name to 1.1.0 and clean up unused imports in MainActivity
- 📝 correct url text for information
- Update dependency androidx.test.espresso:espresso-core to v3.7.0
- Update dependency androidx.core:core-ktx to v1.17.0
- Update dependency androidx.core:core-ktx to v1.17.0
- Update dependency com.google.android.material:material to v1.13.0
- Update dependency com.google.android.material:material to v1.13.0
- Update dependency androidx.test.ext:junit-ktx to v1.3.0
- Update dependency androidx.test.ext:junit to v1.3.0
- Update dependency androidx.test.espresso:espresso-core to v3.7.0
- Update dependency androidx.test.ext:junit to v1.3.0
- 🐞 correct type casting for contours in toContours function
- Restore calculateCameraFrameSize with version check
- Crash on startup due to camera permissions
- 🎨 auto format pre-commit hooks
- Correct malformed OpenCV patch file
- 🐞 update build.gradle for OpenCV 4.13 compatibility and Kotlin integration
- 🐞 update JDK version to 21 in CI workflows for compatibility
- 🎨 auto format pre-commit hooks
- 🎨 auto format pre-commit hooks
- 🧪 clean up CI to target app module and resolve build/signing issues

### Documentation

- 📝 update Kotlin and OpenCV versions in README
- 📝 update OpenCV version to 4.12.0 in README
- 📝 update readme android studio code name
- 📝 script typo fix
- 📝 readme small update
- 📝 add to readme about migrate Gradle build scripts from Groovy DSL to Kotlin DSL
- 📝 update README to include JDK 17 requirement
- 📝 badge android studio update
- 📝 update OpenCV version to 4.13.0 in CI workflow and README
- 📝 update project documentation with new features and versions

### Features

- 🎉 add appInsightsSettings.xml for Firebase Crashlytics configuration
- 🎥 add camera resolution selection feature and related UI elements
- 📷 enhance camera resolution handling and make sure select resolution
- 🎥 add camera resolution selection feature and related UI elements #226
- Add new image processing filters
- Add Haar cascade for face detection
- Add new UI icons for menu
- Update UI resources for new features
- Integrate face detection and photo capture in MainActivity
- Update OpenCV to 4.13.0 and enable KleidiCV

### Miscellaneous Tasks

- 🛠️ update Gradle version to 8.14.3 in README and gradle-wrapper.properties
- 🛠️ refine exclude patterns for end-of-file-fixer hook
- ⬆ pre_commit autoupdate
- 👷 add spotless check to debug workflow
- 🐛 fix release workflow keystore path and signing secrets
- 🐛 set absolute keystore path via env var for release build
- 🐛 fix signing config to auto-detect keystore in root or app dir
- ⚡ inject keystore path via gradle property for reliable path resolution
- ⚡ optimize build speed with gradle caching and consolidated verification steps
- ⚡ prioritize app/keystore.jks in signing config and CI
- 🔖 bump app version to 2.0.0
- 🐛 allow release workflow to trigger on cv-* tags

### Refactor

- ♻️  README.md for clarity and conciseness
- Remove calculateCameraFrameSize override
- ✨ update build.gradle and gradle.properties for modern android development and use native kotlin feature
- 🏗️ migrate build scripts to Kotlin DSL
- ♻️ extract face detection logic to FaceDetector class
- ♻️ correct indentation for the build and verify step in the Android CI debug workflow.
- ♻️ move GitVersionValueSource to buildSrc for cleaner build logic

### Styling

- 🎨 apply spotless formatting and naming conventions

### Build

- Enable AGP 9.0 built-in Kotlin support
- Update configuration for JDK 21 and 16KB alignment
- 📦 introduce version catalog for dependency management
- 🤫 suppress excludeLibraryComponentsFromConstraints warning
- ⚡ refactor git versioning to ValueSource for config cache compatibility



## [cv-4.12.0] - 2025-07-05

### Bug Fixes

- Update dependency androidx.test.espresso:espresso-core to v3.6.1
- Update dependency androidx.test.ext:junit to v1.2.1
- Update dependency androidx.test.ext:junit-ktx to v1.2.1
- Update dependency androidx.startup:startup-runtime to v1.2.0
- 🐞 Kotlin internal error fix for MatAt.kt patch added
- Update dependency androidx.constraintlayout:constraintlayout to v2.2.0
- 🎨 auto format pre-commit hooks
- 🎨 auto format pre-commit hooks
- 🎨 auto format pre-commit hooks
- Update dependency androidx.constraintlayout:constraintlayout to v2.2.1
- Update dependency androidx.constraintlayout:constraintlayout to v2.2.1
- Update dependency androidx.core:core-ktx to v1.16.0
- Update dependency androidx.core:core-ktx to v1.16.0
- Update dependency androidx.appcompat:appcompat to v1.7.1
- Update dependency androidx.appcompat:appcompat to v1.7.1
- 🎨 auto format pre-commit hooks
- 🐛 correct path for opencv-lint-baseline.xml in setup script

### Documentation

- 📝 badge versions and opencv version updated
- Formatting changelogs
- Todo ask added for ndk
- 📝 ndk task updated
- 📝 small note on gradle opencv section
- 📝 opencv version docs updated
- 📝 update README to improve feature and keyword sections

### Features

- 🚀 android sdk version upgraded
- ✨ add new image processing filters and update filter logic
- Initialize C++ project structure and add essential files
- Add project configuration files for Kotlin and Gradle settings
- Add initial camera logging and NDK camera classes
- ✨ update OpenCV version to 4.12.0 and adjust related configurations

### Miscellaneous Tasks

- Code formatting
- ⬆ pre_commit autoupdate
- Remove .idea folder
- Formatting fix
- Cmake version updated
- 📦 android gradle packages updates
- Remove un-needed .pre-commit-config.yaml configs
- 🧹 update action versions in CI workflows for consistency and fix opencv script for build

### Refactor

- 🧹 remove custom methods and variables from ExtendJavaCamera2View to use original JavaCamera2View class exception of use Custom FpsMeter
- 🧹 remove unused flashlight functionality and related variables from MainActivity
- 🧹 update comments for flashlight support in MainActivity
- 🧹 update NDK version and compile SDK in build files, add lint baseline for OpenCV



## [cv-4.10.0] - 2024-06-09

### Bug Fixes

- 🎨 auto format pre-commit hooks
- Update dependency androidx.core:core-ktx to v1.13.0
- Update dependency com.google.android.material:material to v1.12.0
- Update dependency androidx.core:core-ktx to v1.13.1
- Update dependency androidx.appcompat:appcompat to v1.7.0
- 🐞 baseline lint updated
- 🧪 unit test cv version fixed
- Disable perm check for opencv project for camera already checked in manifest

### Documentation

- 📝 github badge fix and new ones added for actions
- 📝 badge ver agp 8.2.2 updated
- 📝 changelog for version cv-4.9.0 added

### Features

- Agp version bump to 8.3.1
- ✨ Upgrade opencv version to 4.10.0

### Miscellaneous Tasks

- 👷 disable Post Lint Results
- ⬆ pre_commit autoupdate
- 👷 opencv 4.10.0 folder added



## [cv-4.9.0] - 2024-01-17

### Bug Fixes

- 🎨 auto format pre-commit hooks
- 🐞 sdk version fix for lint error
- 🎨 auto format pre-commit hooks
- 🐞 change lint output to xml for github action debug
- 🐞 debug lint and release jks path fix

### Documentation

- Changelog generated via git-cliff tool and added
- 📝 Features list and readme markdown lints fixed
- 📝 Better short desc for what app does and badge version updated
- 📝 CHANGELOG updated

### Features

- 🚀 opencv 4.9.0 upgrade and BaseLoaderCallback removed
- 🚀 opencv 4.9.0 upgrade and BaseLoaderCallback removed
- ✨ basic cv filter with toggle button to switch between for front and back camera
- 💄 change old icons with new icons and disable flash light button
- ✨ switch between original Cv Frame to Fit to Canvas Mode logic added
- ✨ switch between original Cv Frame to Fit to Canvas Mode logic added  - PR #92 

### Miscellaneous Tasks

- 👷 exclude .idea folder
- Formatter fix for changelog
- 👷 pre-commit fix for yml file in release
- 👷 .gitignore for jks files
- 👷 android-release yaml added with github secret config
- Rename github actions file names and add app/ into .gitignore
- 👷 github action cosmetic changes and add summary of lint in action
- 👷 remove move jks file action
- 🧹 gh action debug yml file formatting



## [cv-4.8.1] - 2024-01-10

### Bug Fixes

- Rename package name and various code changes and some clean up
- Better orientation but landscape mode
- Show camera full screen via Custom View
- Remove reflection for mListener, modify .gitignore, change setupOpenCV.sh permissions
- ✨ setupOpenCV script extra zip file stage removed (not needed)
- Update dependency com.google.android.material:material to v1.8.0 (#27)
- 🚀 timber log and imports fixed
- Set minSdk 24 cv-patch
- Update dependency com.google.android.material:material to v1.9.0 (#50)
- Update dependency androidx.core:core-ktx to v1.10.1 (#51)
- 🐞 typo on setupOpenCV.sh
- 🐛 pre-commit fixes included and pre-commit added.
- 🐛 ktlint formatter added
- 🐛 opencv patch updated for new version
- 🐛 formatter fix and ktlint removed (pre-commit need java exec)
- 🐛 exclude patch whitespace check for break patch
- Update dependency androidx.core:core-ktx to v1.12.0 (#64)
- Funding file moved so cancel workflow error
- Menu button colors for dark mode (#70)
- Update dependency com.google.android.material:material to v1.10.0 (#73)
- 🐞Android linter,permission,test fixes for release build case
- 🎨 auto format pre-commit hooks
- Update dependency com.google.android.material:material to v1.11.0

### Documentation

- LICENSE added
- README.md updated
- Readme screenshots added
- Android-ci build badge added.
- Update opencv 4.7.0 android-sdk url link
- 🔐 security.md added
- ✨ better badges for readme file
- Ktlint code style added
- Headline and description
- Badge cosmetic fix and new badges added
- Kotlin,gradle version updated.
- Create FUNDING.yml added
- 📝 android kotlin version updated
- 📝 update readme for opencv 4.8.0 sections added
- 📝 pre-commit badge added
- 📝 simple explanation added for camera switch
- 📝 code of conduct added (#69)
- 📝 snyk security badge added
- 📝 codefactor badge added

### Features

- Update opencv version to 4.6.0
- CameraSwitch added
- Github workflow commit lint added
- SetupOpenCV script
- Custom cvFpsMeter for bigger text and different color choice
- Material3 theme configs are added
- Android-ci file added
- ⬆ update opencv-android-sdk 4.7.0
- ✨ new filters and code formatting
- Agp 8.0 and opencv patch added
- 🚀 timber log and androidX startup added
- UI overhaul and strings added
- Git_hash added into BuildConfig
- Timber logging for fpsMeter and ExtendedJavaCamera2View
- ✨ opencv 4.8.0 version update
- AGP updated to 8.1.1
- CompileSdk version 33 to 34
- ✨ android ndk - opencv basic setup (#71)
- ✨opencv upgrade to 4.8.1

### Miscellaneous Tasks

- Update action/setup-java version to 3
- 🛠  .idea folder updates
- 🛠  gradle and readme updates
- 🛠  unused lib remove
- 🛠 JDK version set to 17
- Dependabot removed
- Code-formatting and import and var checks
- Core-ktx version update
- :green_heart: manual workflow run section added
- Agp version update
- ⬆ pre_commit autoupdate (#74)
- 🧹 agp to set to stable version
- 👷 android rel-build,linter actions are added

### Testing

- 🧪 initial instrument test added

### Build

- Bump org.jetbrains.kotlin.android from 1.6.20 to 1.7.20 (#8)
- Bump org.jetbrains.kotlin.android from 1.7.20 to 1.7.21 (#17)
- Bump org.jetbrains.kotlin.android from 1.7.21 to 1.7.22 (#18)
- Bump org.jetbrains.kotlin.android from 1.7.22 to 1.8.0 (#19)
- Bump org.jetbrains.kotlin.android from 1.8.0 to 1.8.10 (#31)

### Dev

- 🚀 gradle version upgrade 7.4.0
- 🛠  basic opencv tests are added



<!-- generated by git-cliff -->
