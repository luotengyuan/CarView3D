# AGENTS.md - CarView3D Development Guide

## Project Overview

This is a native Android application that displays a 3D car model by cycling through 52 sequential images based on swipe gestures. The app uses AndroidX libraries and standard Android development patterns.

## Build Commands

### Gradle Wrapper
All commands use the Gradle wrapper located in the project root:
- Windows: `gradlew.bat`
- Unix/Linux/Mac: `./gradlew`

### Build Variants
- Debug: `./gradlew assembleDebug`
- Release: `./gradlew assembleRelease`

### Running Tests
- Run all unit tests: `./gradlew test`
- Run unit tests for debug variant: `./gradlew testDebugUnitTest`
- Run unit tests for release variant: `./gradlew testReleaseUnitTest`
- Run a single test class: `./gradlew testDebugUnitTest --tests "com.example.carview3d.ExampleUnitTest"`
- Run a single test method: `./gradlew testDebugUnitTest --tests "com.example.carview3d.ExampleUnitTest.addition_isCorrect"`

### Linting
- Run Android lint: `./gradlew lint`
- Run lint for debug variant: `./gradlew lintDebug`
- Run lint for release variant: `./gradlew lintRelease`

### Clean and Rebuild
- Clean build: `./gradlew clean`
- Full rebuild: `./gradlew clean assembleDebug`

### Other Commands
- Generate dependencies report: `./gradlew dependencies`
- Build APK only: `./gradlew assemble`

## Code Style Guidelines

### Language
- Language: Java (source/target compatibility: Java 11)
- Minimum SDK: 24 (Android 7.0)
- Target/Compile SDK: 35

### Naming Conventions
- Classes: PascalCase (e.g., `MainActivity`, `ExampleUnitTest`)
- Methods: camelCase (e.g., `onCreate`, `modifySrcR`)
- Variables: camelCase (e.g., `startX`, `currentX`, `scrNum`)
- Constants: UPPER_SNAKE_CASE (e.g., `maxNum`, `TAG`)
- Package names: lowercase with dots (e.g., `com.example.carview3d`)

### Import Organization
Order imports as follows:
1. Android framework imports (`android.*`)
2. AndroidX imports (`androidx.*`)
3. Third-party library imports
4. Project imports

Example:
```java
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
```

### Formatting
- Use 4 spaces for indentation (not tabs)
- Opening brace on same line for classes/methods
- One space after control statements: `if (condition) {`
- Use line breaks after commas in method arguments
- Maximum line length: 100 characters (soft guideline)

### Comments
- Use Javadoc-style `/** */` for public API documentation
- Use inline comments `//` sparingly to explain non-obvious logic
- Include `@author` tag in class-level Javadoc
- Example:
```java
/**
 * 3D汽车模型展示
 * @author Lty
 */
public class MainActivity extends AppCompatActivity {
```

### Error Handling
- Use appropriate exception handling for file/resource operations
- Check for null before using objects
- Use `@SuppressLint` annotations for lint warnings that are intentionally ignored
- Log errors with appropriate tag: `Log.e(TAG, "message", exception)`

### Type Usage
- Declare variable types explicitly (no `var`)
- Use interfaces where appropriate for flexibility
- Prefer primitive types over wrapper classes when performance matters
- Use `static` for constants and shared data

### Android-Specific Patterns
- Use AndroidX libraries over legacy support libraries
- Use `findViewById` or View Binding for view access
- Handle window insets for edge-to-edge display
- Use `BitmapFactory.decodeResource()` for resource image loading
- Recycle bitmaps when no longer needed to prevent memory leaks

### Testing
- Test classes follow pattern: `<ClassName>Test`
- Test methods follow pattern: `<methodName>_is<ExpectedBehavior>`
- Use JUnit 4 annotations: `@Test`, `@Before`, `@After`
- Place unit tests in: `app/src/test/java/`
- Place instrumented tests in: `app/src/androidTest/java/`

### Resource Files
- Layouts: `res/layout/`
- Values (strings, colors, themes): `res/values/`
- Drawables/mipmaps: `res/mipmap-*/`
- Use descriptive resource names (e.g., `activity_main.xml`, `R.id.imageView`)

### ProGuard/R8
- ProGuard rules go in `app/proguard-rules.pro`
- Release builds have ProGuard disabled (`minifyEnabled false`)

## Project Structure

```
CarView3D/
├── app/
│   ├── build.gradle
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── java/com/example/carview3d/
│       │   │   └── MainActivity.java
│       │   ├── res/
│       │   │   ├── layout/
│       │   │   ├── mipmap-*/
│       │   │   ├── values/
│       │   │   └── xml/
│       │   └── AndroidManifest.xml
│       ├── test/
│       │   └── java/com/example/carview3d/
│       │       └── ExampleUnitTest.java
│       └── androidTest/
│           └── java/com/example/carview3d/
│               └── ExampleInstrumentedTest.java
├── build.gradle
├── settings.gradle
├── gradle.properties
└── gradle/lib.versions.toml
```

## Dependencies

Key dependencies (see `gradle/libs.versions.toml`):
- AndroidX AppCompat: 1.7.1
- Material Components: 1.13.0
- AndroidX Activity: 1.9.3
- AndroidX ConstraintLayout: 2.2.1
- JUnit: 4.13.2
- Espresso: 3.7.0
