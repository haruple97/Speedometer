# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

GPS 기반 실시간 속도계 Android 앱 (Jetpack Compose). 소스 주석과 UI 문자열은 한국어로 작성된다 — 신규 주석/문자열도 기존 스타일(한국어)에 맞춰라.

- `applicationId`: `com.haruple97.speedometer`
- minSdk 26 / target&compile SDK 36, JVM 11, Kotlin 2.0.21 (Compose compiler plugin)
- 의존성은 `gradle/libs.versions.toml` 버전 카탈로그에서 관리 — 새 라이브러리를 추가할 때 여기에 먼저 등록하고 `app/build.gradle.kts`에서 `libs.*`로 참조한다.

## Build / Run / Test

```bash
./gradlew assembleDebug              # Debug APK 빌드
./gradlew installDebug               # 연결된 기기에 설치
./gradlew lint                       # Android Lint
./gradlew test                       # JVM 유닛 테스트 (app/src/test)
./gradlew connectedAndroidTest       # 계측 테스트 (에뮬레이터/기기 필요)
./gradlew :app:testDebugUnitTest --tests "com.haruple97.speedometer.ExampleUnitTest"  # 단일 테스트
```

`release` 빌드 타입은 프로가드 설정만 있고 서명 구성이 없으므로, 배포용 빌드는 별도 설정이 필요하다.

## Architecture

단방향 데이터 흐름 MVVM — Activity → ViewModel(StateFlow) → Repository(Flow) → Compose UI.

**데이터 레이어** (`data/`)
- `LocationRepository` 인터페이스 + `DefaultLocationRepository` 구현. Google Play Services의 `FusedLocationProviderClient`를 `callbackFlow`로 감싸 `Flow<SpeedData>`를 노출한다.
- `Location.hasSpeed()`가 false일 때는 이전 위치와의 거리/시간으로 속도를 fallback 계산한다 (`DefaultLocationRepository.kt`).
- `SpeedData`는 단일 불변 상태 스냅샷: `speedKmh`, `maxSpeedKmh`, `accuracyMeters`, `isGpsActive`, `timestamp`.

**ViewModel** (`viewmodel/SpeedViewModel.kt`)
- `AndroidViewModel`. `speedFlow`를 수집하면서 최대 속도를 누적(`maxOf`)해 `MutableStateFlow<SpeedData>`에 저장. `onCleared()`에서 `stopTracking()` 호출.
- Repository 수명주기: `callbackFlow`라 Flow 수집이 시작되면 자동으로 위치 업데이트가 걸리고, 취소 시 `awaitClose`에서 해제된다 — `startTracking()`은 의도적으로 비어 있다.

**UI 레이어** (`ui/`)
- 진입점 `MainActivity` → `LocationPermissionGate` (FINE/COARSE 위치 권한 요청) → `SpeedometerRoute` → `SpeedometerScreen`.
- 권한이 거부되면 `PermissionRequestScreen`으로 폴백, 허용 시 본 화면.
- `SpeedometerScreen`은 `isInPipMode` 분기: PiP에서는 게이지만 full-screen, 일반 모드에서는 게이지 + `SpeedInfoPanel`(최고속도/정확도/GPS 상태).
- 게이지는 Compose `Canvas`로 직접 그린다 — `SpeedometerGauge`가 `drawGaugeArc` + `drawTickMarks` + `drawNeedle` + `DigitalSpeedDisplay`를 조합. 속도 변경은 `animateFloatAsState`로 spring 애니메이션.

**게이지 기하 — `ui/util/GaugeGeometry.kt`의 상수가 전체 드로잉의 기준**
- `START_ANGLE = 150°`, `SWEEP_ANGLE = 240°`, `MAX_SPEED = 350 km/h`.
- 속도 구간별 색상: <120 safe, <200 caution, <280 danger, ≥280 critical (`speedToColor`).
- 게이지 각도/좌표 변환은 반드시 `GaugeGeometry.speedToAngle` / `angleToOffset`을 사용 — 개별 컴포넌트에서 재계산하면 호/눈금/바늘이 어긋난다.
- 색상 팔레트는 `ui/theme/Color.kt`에 중앙집중. 새 색상은 여기에 추가한다.

**Picture-in-Picture**
- `MainActivity`는 `onUserLeaveHint()`에서 1:1 비율 PiP로 진입하고, `onPictureInPictureModeChanged`에서 상태를 `mutableStateOf`로 Compose에 전달 → 화면이 PiP 레이아웃으로 스위칭.
- Manifest의 `supportsPictureInPicture=true` / `configChanges` 플래그가 함께 맞물려 있어야 한다.

## 주의할 점

- 위치 권한은 런타임에서 요청되지만 `DefaultLocationRepository`는 `@SuppressLint("MissingPermission")`로 억제되어 있다 — 권한 체크 없이 Repository를 직접 호출하면 런타임 `SecurityException`이 난다. 항상 `LocationPermissionGate` 뒤에서 사용.
- `SpeedometerGauge`는 `widthIn(max = 330.dp).aspectRatio(1f)`로 크기를 제한 — 태블릿/큰 화면에서 게이지가 과도하게 커지지 않게 하는 의도이므로 함부로 풀지 말 것.
