# 프로젝트 구조 / 아키텍처 이슈

> 코드 레벨의 버그와 상용화 이슈는 [`PRODUCTION_ISSUES.md`](./PRODUCTION_ISSUES.md), 반영된 리팩토링 상세 내역은 [`REFACTOR_CHANGELOG.md`](./REFACTOR_CHANGELOG.md) 참조.
> 본 문서는 **패키지 구조, 모듈 구성, 의존성 방향, 빌드 구성** 등 프로젝트 전체 설계 관점의 문제를 다룬다.

마지막 업데이트: 2026-04-17 (Phase 1 ~ Phase 3 리팩토링 반영 상태)

상태 표기: ✅ 해결됨 · 🟡 부분 해결 · ❌ 미해결

---

## 목차

- [A. 아키텍처 원칙](#a-아키텍처-원칙)
- [B. 모듈 / 패키지 구조](#b-모듈--패키지-구조)
- [C. 빌드 및 환경 구성](#c-빌드-및-환경-구성)
- [D. DI 구성](#d-di-구성)
- [E. 네이밍 / 코드 일관성](#e-네이밍--코드-일관성)
- [F. 네비게이션 / UI 계층](#f-네비게이션--ui-계층)
- [현재 남은 과제 로드맵](#현재-남은-과제-로드맵)

---

## A. 아키텍처 원칙

### A-1. Clean Architecture 의존성 방향 역전 — ✅ 해결

Repository 인터페이스를 domain 패키지로 이동하고, 중간 엔티티·Translator 를 제거하여 의존성 방향이 정리되었다.

```
presentation ──→ domain ──→ data
                   ↑          │
                   └──────────┘  (data 가 domain 인터페이스를 구현)
```

**변경 요약**

| 구분 | Before | After |
|---|---|---|
| 인터페이스 위치 | `data.repositories.LibraryRepository` | `domain.repository.LibraryRepository` |
| UseCase import | `app.peter.s526.data.repositories.LibraryRepository` | `app.peter.s526.domain.repository.LibraryRepository` |
| Repository 반환 타입 | `data.entities.*` (DTO 파생) | `domain.model.*` (Book / BookDetail / BookList) |

### A-2. 모델 계층 삼중화 — ✅ 해결

DTO → 중간 엔티티 → 도메인 모델의 3 단계 변환이, **DTO → 도메인 모델 (1 단계)** 로 축소되었다.

```
OpenLibrary API
  └─ OLSearchResponse / OLEditionResponse (data.entities — DTO)
        │
        ▼ data.mapper.BookMapper
     Book / BookDetail / BookList (domain.model)
        │
        ▼ (UI)
```

**삭제된 것들**
- `data.entities.Book`, `data.entities.DetailBook`, `data.entities.ListBook`, `data.entities.Pdf`
- `domain.translator.BookTranslator`
- `data.source.remote.OLResponseMapper`

### A-3. Package by Layer / Feature 혼재 — ❌ 미해결

`presentation` 은 기능(feature) 단위로, `data`/`domain` 은 유형(type) 단위로 분리된 구조가 유지되고 있다.

```
presentation/view/main/
├── book/          ← feature
├── bookmark/      ← feature
├── history/       ← feature
├── search/        ← feature
└── detail/        ← feature

domain/
├── model/
├── repository/
├── usecase/
│   └── impl/
└── module/

data/
├── entities/
├── mapper/
├── repositories/impl/
├── source/{remote,local}/
└── module/
```

**남은 개선 방향** — 멀티 모듈화(B-1) 와 함께 feature-based 로 재정비.

```
app/
├── feature/
│   ├── newbook/
│   ├── search/
│   ├── bookmark/
│   └── detail/
├── core/
│   ├── data/
│   ├── model/
│   └── ui/
```

---

## B. 모듈 / 패키지 구조

### B-1. 단일 모듈 구조 — ❌ 미해결

전체 코드가 `:app` 모듈 하나에 있다. `settings.gradle.kts` 에도 `include(":app")` 만 선언되어 있다.

**남은 개선 방향 (Now In Android 기준)**

```
settings.gradle.kts
├── :app
├── :core:common
├── :core:model
├── :core:data
├── :core:network
├── :core:database          # Room (도입 시)
├── :core:datastore
├── :core:ui
├── :feature:newbook
├── :feature:search
├── :feature:bookmark
└── :feature:detail
```

### B-2. Gradle Version Catalog 미사용 — ✅ 해결

`gradle/libs.versions.toml` 을 도입하여 버전 / 라이브러리 / 번들 / 플러그인을 중앙 집중 관리.

```toml
[versions]
kotlin = "1.9.24"
hilt = "2.51"
retrofit = "2.9.0"
coroutines = "1.7.3"

[libraries]
hilt-android = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }
retrofit-core = { module = "com.squareup.retrofit2:retrofit", version.ref = "retrofit" }

[bundles]
coroutines = ["kotlinx-coroutines-core", "kotlinx-coroutines-android"]
navigation = ["androidx-navigation-fragment-ktx", "androidx-navigation-ui-ktx"]
```

`app/build.gradle.kts` 에서는 `libs.hilt.android` / `libs.bundles.coroutines` 형태로 접근.

### B-3. Gradle Groovy DSL 사용 — ✅ 해결

| Before | After |
|---|---|
| `build.gradle` | `build.gradle.kts` |
| `settings.gradle` | `settings.gradle.kts` |
| `app/build.gradle` | `app/build.gradle.kts` |

`settings.gradle.kts` 는 `pluginManagement`, `dependencyResolutionManagement` 를 도입하고 `RepositoriesMode.FAIL_ON_PROJECT_REPOS` 로 저장소 선언을 일원화.

### B-4. KAPT 사용 (KSP 미적용) — ✅ 해결

KAPT 플러그인을 완전히 제거하고 KSP 로 전환.

```kotlin
// app/build.gradle.kts (현재)
plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}
dependencies {
    ksp(libs.hilt.compiler)
    ksp(libs.glide.ksp)
}
```

Kotlin 1.9.24 / Hilt 2.51 / Glide 4.16.0 으로 업그레이드되면서 KSP 지원이 확보되었다.

---

## C. 빌드 및 환경 구성

### C-1. BuildType / BuildConfig 구성 없음 — ✅ 해결

```kotlin
buildTypes {
    debug {
        isMinifyEnabled = false
        buildConfigField("String", "BASE_URL", "\"https://openlibrary.org/\"")
        buildConfigField("boolean", "ENABLE_LOGGING", "true")
    }
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro",
        )
        buildConfigField("String", "BASE_URL", "\"https://openlibrary.org/\"")
        buildConfigField("boolean", "ENABLE_LOGGING", "false")
    }
}
buildFeatures {
    viewBinding = true
    buildConfig = true
}
```

- `ApiConstants.BASE_URL` 은 `BuildConfig.BASE_URL` 을 직접 참조
- `application.Log` / `NetworkModule` 의 로깅은 `BuildConfig.ENABLE_LOGGING` 으로 게이팅

**남은 여지** : 현재 debug/release 모두 운영 URL(`openlibrary.org`) 을 사용. 개발/스테이징 서버가 생길 경우 debug 용 URL 을 분리할 수 있는 구조는 준비되어 있다.

### C-2. 시크릿/환경 변수 관리 부재 — ❌ 미해결

OpenLibrary 는 인증이 필요 없어 현재 문제는 없으나, Google Play Console 키 / 분석 도구 키 등이 추가될 경우 사용할 `secrets.properties` + `secrets-gradle-plugin` 혹은 `local.properties` → `BuildConfig` 주입 구조는 여전히 준비되어 있지 않다.

### C-3. `minSdk 28` — 시장 커버리지 과소 — ❌ 미해결

`minSdk = 28` (Android 9, 2018). 타겟 사용자 분포에 따라 재검토 필요. API 24 까지 낮출 경우 `java.time` / `java.util.stream` 은 desugaring 필요.

### C-4. JVM target 17 — ❌ 판단 필요

`JavaVersion.VERSION_17` 유지 중. 현 의존성 구성에 적합하나, AGP 8.2+ 로 업그레이드 시 JDK 21 요구사항을 고려해야 한다.

---

## D. DI 구성

### D-1. `@Provides` 남용, `@Binds` 미사용 — ✅ 해결

`RepositoryModule` / `UseCaseModule` 을 `abstract class + @Binds` 로 전환.

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindLibraryRepository(impl: LibraryRepositoryImpl): LibraryRepository
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class UseCaseModule {
    @Binds
    @ViewModelScoped
    abstract fun bindNewBookUseCase(impl: NewBookUseCaseImpl): NewBookUseCase
    // ...
}
```

### D-2. DI 스코프 일관성 — ✅ 해결

| 의존성 | 스코프 | 상태 |
|---|---|---|
| `LibraryRepository` | `@Singleton` | ✅ |
| `LocalBookDataSource` | 클래스 단에 `@Singleton` (`@Inject constructor`) | ✅ |
| `OkHttpClient` | `@Singleton` | ✅ |
| `HttpLoggingInterceptor` | `@Singleton` | ✅ |
| `Retrofit` | `@Singleton` | ✅ |
| `Converter.Factory` | `@Singleton` | ✅ |
| `Api` (Retrofit 프록시) | `@Singleton` | ✅ |
| UseCase 구현체 | `@ViewModelScoped` | ✅ |

### D-3. Module 네이밍 / 위치 불일치 — 🟡 부분 해결

| 모듈 | 위치 |
|---|---|
| `NetworkModule` | `data/module/` ✅ |
| `RepositoryModule` | `data/module/` ✅ |
| `UseCaseModule` | `domain/module/` ✅ |
| `ApiModule` | `data/source/remote/` 🟡 |
| ~~`LocalModule`~~ | *(제거됨)* ✅ |

- `LocalModule` 은 `LocalBookDataSource` 에 `@Singleton + @Inject constructor` 를 적용하여 완전 삭제
- `ApiModule` 은 Retrofit 구성을 캡슐화한 목적상 remote 패키지에 두었으나, 일관성을 위해 `data/module/` 로 이동하거나 `NetworkModule` 과 병합을 검토할 수 있다

---

## E. 네이밍 / 코드 일관성

### E-1. `New` 접두어 남용 — ✅ 해결

```
NewBook       → Book
NewDetailBook → BookDetail
NewListBook   → BookList
NewPdf        → (제거됨)
```

세 개의 data class 는 단일 파일 `domain/model/Book.kt` 로 통합되었다.

### E-2. 패키지명 오타 / 의미 불일치 — ✅ 해결

| Before | After |
|---|---|
| `domain/translator/BookTranslator.kt` | *(삭제 — `data/mapper/BookMapper` 로 대체)* |
| `data/source/local/S526Data.kt` | `data/source/local/LocalBookDataSource.kt` |
| `application/S526.kt` | `application/S526Application.kt` |
| `data/source/remote/RemoteConst.kt` | `data/source/remote/ApiConstants.kt` |
| `data/source/remote/OLResponseMapper.kt` | `data/mapper/BookMapper.kt` |

### E-3. 주석 언어 — ✅ 해결

현재 코드에 남아 있는 KDoc / 설명 주석은 **한글로 통일**되어 있다 (`BookMapper`, `Log`, `MainViewModel`, `ViewPagerFragment`, `FragmentViewBindingDelegate`, `LocalBookDataSource`).
오픈소스 배포 가능성이 커지면 향후 영문 전환을 검토.

### E-4. Application ID 가 개인 식별자 — ❌ 미해결

```kotlin
applicationId = "app.peter.s526"
namespace = "app.peter.s526"
```

상용화 시 조직 도메인 기반으로 변경을 검토해야 한다. 단, 이미 출시된 앱이라면 변경 시 새 앱으로 재등록이 필요하다.

### E-5. 목적 없는 필드 — ✅ 해결

`BookDetail` 에서 `error`, `rating`, `price`, `pdf` 를 모두 제거.
`Book` 에서도 `price` 를 제거. 모든 관련 XML View 와 Adapter 바인딩이 함께 정리되었다.

```kotlin
// domain/model/Book.kt (현재)
data class Book(
    val isbn: String,
    val title: String,
    val subtitle: String,
    val image: String,
    val url: String,
)

data class BookDetail(
    val title: String,
    val subtitle: String,
    val authors: String,
    val publisher: String,
    val language: String,
    val isbn10: String,
    val isbn13: String,
    val pages: String,
    val year: String,
    val desc: String,
    val image: String,
    val url: String,
)
```

---

## F. 네비게이션 / UI 계층

### F-1. 단일 Navigation Graph — ❌ 미해결

`res/navigation/nav_s526.xml` 하나에 모든 목적지(`ViewPagerFragment`, `DetailFragment`, `SearchFragment`) 가 정의되어 있다. feature 모듈화 시 각 feature 가 자기 그래프를 가지도록 분리 필요.

### F-2. ViewBinding 사용 패턴 불일치 — ✅ 해결

`presentation.util.FragmentViewBindingDelegate` 를 도입하고 모든 Fragment 를 동일 패턴으로 통일.

```kotlin
class XxxFragment : Fragment(R.layout.fragment_xxx) {
    private val binding by viewBinding(FragmentXxxBinding::bind)
    // ...
}
```

- `viewLifecycle` 에 묶인 `DefaultLifecycleObserver` 가 `onDestroy` 에서 binding 을 null 로 설정 → `lateinit var binding` 패턴의 메모리 누수 위험 제거
- `onCreateView` 보일러플레이트 완전 제거 (모든 Fragment 가 2차 생성자 `Fragment(@LayoutRes)` 사용)

### F-3. XML Layout + ViewBinding — Compose 미도입 — ❌ 미해결

여전히 XML + ViewBinding + Fragment + Navigation Component 구성. 신규 feature 부터 Compose 로 전환하는 점진적 도입이 합리적.

### F-4. Material Components 버전 — 🟡 부분 해결

| | Before | After |
|---|---|---|
| material 의존성 | 1.6.1 (2022) | 1.12.0 (2024) |

의존성 버전은 M3 지원 수준으로 업그레이드되었으나, **실제 테마 스타일은 여전히 Material 2 기반 (`Theme.MaterialComponents.DayNight.NoActionBar`) 일 가능성이 높다**.
Dynamic Color / M3 토큰 활용을 위해서는 테마 적용 단계의 작업이 추가로 필요하다.

### F-5. 실험 기능이 정식 UI 에 노출됨 — ✅ 해결

- `testAppSetID()` 전체 삭제 + `play-services-appset` 의존성 제거
- `testReviewManager()` → `requestReview()` 로 개명 및 시맨틱 명확화 (`ViewPagerFragment` 에서 리뷰 버튼 클릭 시 호출)
- `MainViewModel` 에서 `client: AppSetIdClient?` 필드와 관련 로직 모두 제거

---

## 현재 남은 과제 로드맵

Phase 1 ~ 3 의 주요 개선은 완료되었으므로, 남은 항목은 **Phase 4 (장기 / 선택)** 성격으로 분류된다.

### Phase 4 — 구조 확장 (1~2개월)

| 항목 | 영역 | 비고 |
|---|---|---|
| A-3 | feature-based 패키지 재편 | B-1 과 함께 진행 권장 |
| B-1 | 멀티 모듈 분리 (`:core:*`, `:feature:*`) | 빌드 속도 / 경계 강제 |
| F-1 | Navigation Graph feature 별 nested 분리 | B-1 의존 |
| F-3 | Compose 부분 도입 | 신규 feature 부터 |
| F-4 | Material 3 테마 적용 | 의존성은 이미 업그레이드됨 |

### Phase 4 — 상용화 / 환경 (상시)

| 항목 | 영역 | 비고 |
|---|---|---|
| C-2 | 시크릿 관리 구조(`secrets-gradle-plugin`) | 외부 키 추가 시 |
| C-3 | `minSdk` 조정 검토 | 타겟 사용자 분포 분석 필요 |
| C-4 | JDK / AGP 상향 검토 | 의존성 업그레이드 시 |
| D-3 | `ApiModule` 위치 일관화 | 낮은 우선순위 |
| E-4 | `applicationId` 조직 도메인화 | 신규 앱 등록 시 |

### 코드 품질 (추가 상시 과제)

`PRODUCTION_ISSUES.md` 의 미해결 항목들과 연계:
- P0-2 영속 저장소 도입 (Room / DataStore)
- P1-6 UiState sealed class 기반 로딩 / 에러 표시
- P1-7 `DetailFragment` FAB 초기 상태 반영
- P2-13 검색 페이지네이션 상태 ViewModel 이관 (`NewBookFragment` 패턴 확장)
- P3-19 `MainViewModel` 을 feature 별 ViewModel 로 분리

---

## 참고 자료

- [Guide to app architecture (Android Developers)](https://developer.android.com/topic/architecture)
- [Now In Android — 공식 레퍼런스 앱](https://github.com/android/nowinandroid)
- [Modularization learning journey](https://developer.android.com/topic/modularization)
