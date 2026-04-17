# 프로젝트 구조 / 아키텍처 이슈

> 코드 레벨의 버그는 [`PRODUCTION_ISSUES.md`](./PRODUCTION_ISSUES.md)를 참고. 본 문서는 **패키지 구조, 모듈 구성, 의존성 방향, 빌드 구성** 등 프로젝트 전체 설계 관점의 문제를 다룬다.

---

## 목차

- [A. 아키텍처 원칙 위반](#a-아키텍처-원칙-위반)
- [B. 모듈 / 패키지 구조](#b-모듈--패키지-구조)
- [C. 빌드 및 환경 구성](#c-빌드-및-환경-구성)
- [D. DI 구성](#d-di-구성)
- [E. 네이밍 / 코드 일관성](#e-네이밍--코드-일관성)
- [F. 네비게이션 / UI 계층](#f-네비게이션--ui-계층)
- [우선순위 로드맵](#우선순위-로드맵)

---

## A. 아키텍처 원칙 위반

### A-1. Clean Architecture 의존성 방향 역전

**현상**  
README에는 "Clean Architecture"를 표방하지만 실제 의존성 방향은 다음과 같다.

```
presentation ──→ domain ──→ data   ← 올바른 방향 (domain이 data를 모름)
```

**실제 코드**
```
presentation ──→ domain ──→ data   ← domain이 data를 import 함 (위반)
                   ↑__________│
```

**증거 1 — Repository 인터페이스 위치**
```kotlin
// 파일: data/repositories/LibraryRepository.kt  ❌ data 패키지에 위치
interface LibraryRepository {
    suspend fun getNewBook(page: String = "1"): ListBook
    ...
}
```

Clean Architecture에서 Repository **인터페이스는 domain 계층에 있어야 하고**, 구현체만 data 계층에 있어야 한다. 현재 구조에서는 domain의 UseCase가 data 패키지의 인터페이스를 import한다.

**증거 2 — UseCase가 data entity에 직접 의존**
```kotlin
// 파일: domain/usecase/impl/NewBookUseCaseImpl.kt
import app.peter.s526.data.repositories.LibraryRepository   // ❌ data 참조
import app.peter.s526.domain.translator.BookTranslator

override suspend fun getNewBook(page: String): NewListBook {
    return BookTranslator.getListBook(repository.getNewBook(page))
    //                                └─ ListBook (data.entities) 반환
}
```

**증거 3 — domain.translator가 data.entities를 변환**
```kotlin
// 파일: domain/translator/BookTranslator.kt
import app.peter.s526.data.entities.Book         // ❌
import app.peter.s526.data.entities.DetailBook   // ❌
import app.peter.s526.data.entities.ListBook     // ❌
```

domain 계층의 클래스가 data 계층의 엔티티를 직접 import한다. 이름만 Clean Architecture일 뿐, 실제로는 **domain과 data가 양방향으로 결합**되어 있다.

**개선 방향**
1. `LibraryRepository` 인터페이스를 `domain/repository/`로 이동  
2. Repository 반환 타입을 domain 모델(`NewBook`, `NewDetailBook`, `NewListBook`)로 변경  
3. DTO → domain 모델 변환은 **data 계층 내부에서 완결**하고, `BookTranslator`는 data 계층으로 이동 (또는 Mapper 분산)

```
domain/
├── model/           # NewBook, NewDetailBook
├── repository/      # LibraryRepository (interface만)
└── usecase/

data/
├── entities/        # OL*Response, Book, DetailBook (DTO)
├── repositories/
│   └── LibraryRepositoryImpl (domain의 인터페이스 구현)
├── mapper/          # DTO → domain 변환 (data 내부에서 완결)
└── source/
```

---

### A-2. 모델 계층 삼중화 — 같은 의미의 데이터가 3번 변환됨

**현상**

```
OpenLibrary API  OLSearchResponse  (data.entities)
                      │
                      ▼ OLResponseMapper
                    Book           (data.entities)
                      │
                      ▼ BookTranslator (domain.translator)
                    NewBook        (domain.model)
                      │
                      ▼ (UI 표시)
```

하나의 도서 정보가 DTO → 중간 엔티티 → 도메인 모델 총 3번 변환된다. 중간 엔티티(`Book`, `DetailBook`, `ListBook`)는 도메인 모델과 필드가 **완전히 동일**하다.

```kotlin
// data.entities.Book
data class Book(val isbn: String, val title: String, val subtitle: String,
                val price: String, val image: String, val url: String)

// domain.model.NewBook  ← 필드 구성 동일
data class NewBook(val isbn: String, val title: String, val subtitle: String,
                   val price: String, val image: String, val url: String)
```

**문제**  
- 변환 코드가 패키지 전체에 흩어져 유지보수 비용 증가  
- 필드 하나 추가 시 3곳(+각 매퍼) 수정 필요  
- `BookTranslator`는 단순 복사 외에 아무 일도 하지 않음

**개선**  
중간 계층 `Book`/`DetailBook`/`ListBook`을 제거하고, `OLResponseMapper`가 직접 domain 모델을 반환한다. 변환은 한 번으로 충분하다.

---

### A-3. Package by Layer / Feature 혼재

**현상**  
`presentation`은 기능(feature) 단위로, `data`/`domain`은 유형(type) 단위로 분리되어 있다.

```
presentation/view/main/
├── book/          ← feature 단위 (신간)
├── bookmark/      ← feature
├── history/       ← feature
├── search/        ← feature
└── detail/        ← feature

domain/
├── model/         ← type 단위
├── usecase/
│   └── impl/      ← type 단위
└── translator/

data/
├── entities/      ← type 단위
├── repositories/
│   └── impl/
└── source/
```

**문제**  
- "신간 기능"을 수정하려면 `presentation/view/main/book/`, `domain/usecase/NewBookUseCase.kt`, `domain/usecase/impl/NewBookUseCaseImpl.kt`, `data/repositories/.../getNewBook()` 등 **서로 다른 구조의 패키지 여러 곳을 이동**해야 한다.  
- feature 추가/삭제 시 영향 범위가 커져 코드 리뷰가 어렵다.

**개선**  
전체를 **feature-based**로 통일하거나, 후속 멀티 모듈화(B-1)와 함께 재정비한다.

```
app/
├── feature/
│   ├── newbook/      # UI + ViewModel + UseCase
│   ├── search/
│   ├── bookmark/
│   └── detail/
├── core/
│   ├── data/         # Repository, Remote, Local
│   ├── model/        # 공용 도메인 모델
│   └── ui/           # 공용 Theme, Component
```

---

## B. 모듈 / 패키지 구조

### B-1. 단일 모듈 구조 — 모듈 분리 없음

**현상**  
전체 코드가 `:app` 모듈 하나에 있다.

**문제**  
- 모든 변경이 `app` 전체를 재컴파일 → 빌드 속도 저하  
- 계층 간 경계를 컴파일러가 강제할 수 없음 (예: presentation이 data를 직접 import해도 막을 수 없음)  
- 테스트 모듈 분리, Dynamic Feature Module 확장 불가

**개선 (Now In Android 표준 구조 기준)**
```
settings.gradle.kts
├── :app                              # 진입점, 내비게이션 최상위
├── :core:common                      # Utils, Log
├── :core:model                       # 공용 도메인 모델
├── :core:data                        # Repository 구현
├── :core:network                     # Retrofit, OkHttp
├── :core:database                    # Room (도입 시)
├── :core:datastore                   # Preferences
├── :core:ui                          # Compose / 공용 컴포넌트
├── :feature:newbook
├── :feature:search
├── :feature:bookmark
└── :feature:detail
```

단계적 마이그레이션 시 `:data` / `:domain` / `:presentation` 3-모듈 구성부터 시작하는 것도 실용적이다.

---

### B-2. Gradle Version Catalog 미사용

**현상**  
`app/build.gradle`에 의존성 버전이 문자열로 하드코딩되어 있다.

```groovy
implementation 'androidx.core:core-ktx:1.8.0'
implementation 'androidx.appcompat:appcompat:1.4.2'
implementation 'com.google.android.material:material:1.6.1'
// ... 25개 이상
```

**문제**  
- 여러 모듈로 분리 시 버전 동기화 수동 관리 필요  
- Dependabot/Renovate 자동화 연동 어려움  
- `kotlin_version`을 `ext`로 따로 관리 중 → 일관성 부족

**개선**  
`gradle/libs.versions.toml` 도입.

```toml
[versions]
kotlin = "1.9.22"
hilt = "2.51"
retrofit = "2.11.0"
coroutines = "1.8.0"

[libraries]
androidx-core-ktx = { module = "androidx.core:core-ktx", version = "1.13.1" }
hilt-android = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }
retrofit-core = { module = "com.squareup.retrofit2:retrofit", version.ref = "retrofit" }

[bundles]
coroutines = ["coroutines-core", "coroutines-android"]
```

---

### B-3. Gradle Groovy DSL 사용

**현상**  
`build.gradle` (Groovy 문법)을 사용한다.

**문제**  
- IDE 자동완성/타입 체크 제한  
- 2026년 기준 **Kotlin DSL(`.kts`)** 이 공식 권장 표준

**개선**  
`build.gradle` → `build.gradle.kts`, `settings.gradle` → `settings.gradle.kts`로 마이그레이션. 버전 카탈로그(B-2)와 함께 진행하면 효과적이다.

---

### B-4. KAPT 사용 (KSP 미적용)

**현상**
```groovy
id 'kotlin-kapt'
kapt 'com.github.bumptech.glide:compiler:4.12.0'
kapt 'com.google.dagger:hilt-android-compiler:2.41'
```

**문제**  
KAPT는 Java stub을 생성하는 방식으로 동작하여 빌드 속도가 느리다. Hilt(2.48+)와 Glide(4.14+) 모두 KSP를 지원한다.

**개선**
```kotlin
plugins {
    id("com.google.devtools.ksp")
}
dependencies {
    ksp("com.google.dagger:hilt-compiler:2.51")
    ksp("com.github.bumptech.glide:ksp:4.16.0")
}
```

빌드 속도가 평균 2~3배 빨라진다.

---

## C. 빌드 및 환경 구성

### C-1. BuildType / Flavor 구성 없음

**현상**  
`buildTypes`에 `release`만 정의되어 있고, `debug`/`stage`/`prod` 구분이 없다.

```groovy
buildTypes {
    release { minifyEnabled false ... }
}
```

**문제**  
- `BASE_URL`, 로그 레벨, 분석 SDK 활성화 여부를 환경별로 분리할 수 없음  
- `RemoteConst.URL`이 하드코딩 상수로 존재 → 개발/스테이징/운영 전환 불가

**개선**
```kotlin
android {
    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"https://openlibrary-dev.example.com/\"")
            buildConfigField("boolean", "ENABLE_LOGGING", "true")
        }
        release {
            buildConfigField("String", "BASE_URL", "\"https://openlibrary.org/\"")
            buildConfigField("boolean", "ENABLE_LOGGING", "false")
            isMinifyEnabled = true
        }
    }
    buildFeatures {
        buildConfig = true
    }
}
```

---

### C-2. 시크릿/환경 변수 관리 부재

**현상**  
현재 외부 API 키는 없지만(OpenLibrary는 미인증), 상용화 시 Google Play Console 키, 분석 도구 키 등이 추가될 가능성이 높다. `local.properties` 또는 `secrets-gradle-plugin` 사용 구조가 마련되어 있지 않다.

**개선**  
사전에 `secrets.properties` + `secrets-gradle-plugin` 도입 혹은 `local.properties` 읽어 `BuildConfig`에 주입하는 구조를 마련한다.

---

### C-3. `minSdk 28` — 시장 커버리지 과소

**현상**  
`minSdk = 28` (Android 9, 2018년 출시).

**문제**  
API 24(Android 7)까지 지원하면 국내 시장 점유율 기준 추가 커버 가능. 반대로 상용화를 위해 **M3 디자인 시스템**을 도입하려면 오히려 문제 없음.

**판단 필요**  
타겟 사용자 디바이스 분포에 따라 결정. 낮추면 `java.time`, `java.util.stream` 등 일부 API는 desugaring 필요.

---

### C-4. JVM target / Kotlin target 17 — 최신 라이브러리 호환 경계

**현상**  
`JavaVersion.VERSION_17`, `jvmTarget = '17'`. AGP 8.0 기준 적절하나, AGP 8.2+에서는 21까지 올리는 추세다. 이 자체로 문제는 아니지만 의존성 업데이트 시 JDK 21 요구 사항을 고려해야 한다.

---

## D. DI 구성

### D-1. `@Provides` 남용, `@Binds` 미사용

**현상**  
모든 인터페이스 바인딩을 `@Provides`로 처리한다.

```kotlin
// UseCaseModule.kt
@Singleton @Provides
fun provideBookmarkUseCase(repository: LibraryRepository): BookmarkUseCase
    = BookmarkUseCaseImpl(repository)
```

**문제**  
`@Binds`는 DI 그래프 구축 시 추가 인스턴스 생성이 없어 **컴파일러가 더 효율적인 코드**를 생성한다. 인터페이스-단일구현 매핑에는 `@Binds`가 표준이다.

**개선**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class UseCaseModule {
    @Binds
    abstract fun bindBookmarkUseCase(impl: BookmarkUseCaseImpl): BookmarkUseCase
}

// 구현체에 @Inject constructor 명시 (이미 되어 있음)
class BookmarkUseCaseImpl @Inject constructor(
    private val repository: LibraryRepository
) : BookmarkUseCase { ... }
```

---

### D-2. DI 스코프 일관성 부족

| 의존성 | 현재 스코프 | 문제 |
|--------|------------|------|
| `UseCase`들 | `@Singleton` | stateless인데 굳이 싱글턴일 필요 없음 (스코프 축소 가능) |
| `LibraryRepository` | 스코프 없음 | 여러 인스턴스 생성 가능 → `@Singleton` 필요 |
| `S526Data` | 스코프 없음 | **치명적**: 인메모리 저장소인데 매번 새 인스턴스 생성 가능 |
| `OkHttpClient` | `@Singleton` | 적절 |
| `Api` (Retrofit) | 스코프 없음 | `@Singleton` 필요 |
| `Converter.Factory` | 스코프 없음 | `@Singleton` 권장 |

**개선**  
Repository, DataSource, Retrofit 관련 객체는 전부 `@Singleton`으로 통일. UseCase는 `@ViewModelScoped` 또는 무스코프.

---

### D-3. Module 네이밍 / 위치 불일치

현재 모듈 위치:
```
domain/module/UseCaseModule.kt
data/module/NetworkModule.kt
data/module/RepositoryModule.kt
data/source/remote/ApiModule.kt       ← 다른 모듈들과 다른 위치
data/source/local/LocalModule.kt       ← 다른 모듈들과 다른 위치
```

`NetworkModule`/`RepositoryModule`은 `data/module/` 아래 있으나 `ApiModule`과 `LocalModule`은 각자 `source/remote`, `source/local`에 있다. 한 패키지로 통일하거나 명확한 기준을 적용해야 한다.

---

## E. 네이밍 / 코드 일관성

### E-1. `New` 접두어가 의미 없이 남용됨

`NewBook`, `NewDetailBook`, `NewListBook`, `NewPdf` — 이 클래스들은 "신간(New Book)"이 아니라 **리팩토링 중간 단계에서 기존 모델과 구분하기 위한 임시 접두어**로 보인다. 실제로 `Bookmark`, `Search`, `Detail` 화면 전부에서 `NewBook`을 사용한다.

**결과**: `SearchFragment`에 "NewBook"이 등장하여 의미상 혼란을 준다.

**개선**: 접두어 제거.
```
NewBook       → Book
NewDetailBook → BookDetail
NewListBook   → BookList (또는 PagedBooks)
NewPdf        → Pdf
```

---

### E-2. 패키지명 오타 / 의미 불일치

- `domain/translator/BookTranslator.kt` → 표준 용어는 **Mapper**. "Translator"는 Android 진영에서 거의 쓰이지 않는다.
- `data/source/local/S526Data.kt` → "데이터 저장소"이면 `LocalDataSource` 또는 `InMemoryBookDataSource` 같은 의도 드러나는 이름으로.
- `application/S526.kt` → `S526Application.kt`로 명확화.
- `RemoteConst` → `RemoteConstants` 또는 companion object 상수로 충분.

---

### E-3. 주석 언어 혼용

- `OLResponseMapper.kt`: 한글 주석  
- 나머지 파일: 영어 주석 또는 무주석  
- `build.gradle` 하단: `// Android Studio에서 testClasses 태스크를 찾지 못하는 문제 해결` (한글)

팀 컨벤션을 정해 통일해야 한다. 오픈소스 가능성이 있다면 영어 권장.

---

### E-4. Application ID가 개인 식별자

```groovy
applicationId "app.peter.s526"   // 개인 이름 기반
namespace 'app.peter.s526'
```

상용화 시 조직 도메인 기반(`com.company.s526` 등)으로 변경해야 Google Play 등록에 문제가 없다. 단, 이미 출시된 상태라면 변경 불가 (새 앱으로 등록).

---

### E-5. 목적 없는 `error: String` 필드

```kotlin
data class NewDetailBook(
    val error: String,     // "0"만 하드코딩되어 반환됨
    ...
    val rating: String,    // "0"만 반환됨
    val price: String,     // ""만 반환됨
    val pdf: NewPdf        // NewPdf("")로만 반환됨
)
```

**원인**  
기존 다른 API(아마 Books API 또는 IT Bookstore API)의 응답 포맷을 그대로 가져온 뒤 OpenLibrary로 교체한 것으로 보인다. OpenLibrary는 가격/평점/PDF를 제공하지 않으므로 해당 필드들은 **죽은 필드**다.

**개선**  
OpenLibrary 기준으로 모델을 재설계하거나, 여러 데이터 소스 통합 모델로 의도적으로 유지할지 결정한다. 현재는 UI에서도 이 값들을 의미 없이 표시하여 사용자 혼란을 초래한다.

---

## F. 네비게이션 / UI 계층

### F-1. 단일 Navigation Graph — feature 분리 불가

**현상**  
`nav_s526.xml` 하나에 모든 목적지가 정의되어 있다.

**문제**  
feature 모듈화 시 각 feature가 자기 그래프를 가지지 못하고 app 모듈에 집중된다. feature 추가할수록 그래프 파일이 비대해진다.

**개선**  
feature별 nested nav graph 분리.
```xml
<!-- app/res/navigation/nav_root.xml -->
<include app:graph="@navigation/nav_newbook" />
<include app:graph="@navigation/nav_search" />
<include app:graph="@navigation/nav_detail" />
```

---

### F-2. ViewBinding 사용 패턴 불일치

| Fragment | 패턴 |
|----------|------|
| `NewBookFragment` | `onCreateView`에서 로컬 변수 사용 후 return |
| `BookmarkFragment` | `lateinit var binding` (클래스 필드) |
| `SearchFragment` | `lateinit var binding` |
| `DetailFragment` | `lateinit var binding` |
| `ViewPagerFragment` | `_binding: FragmentViewPagerBinding?` + `onDestroyView`에서 null (권장 패턴) |

**문제**  
ViewBinding 사용 패턴이 Fragment마다 다르다. `lateinit var binding`은 **`onDestroyView` 이후 참조 시 메모리 누수 및 크래시 위험**이 있다.

**개선**  
`ViewPagerFragment` 패턴(nullable + onDestroyView에서 null)으로 통일하거나, `FragmentViewBindingDelegate` 유틸 도입으로 일괄 적용.

```kotlin
class MyFragment : Fragment(R.layout.my_fragment) {
    private val binding by viewBinding(MyFragmentBinding::bind)
}
```

---

### F-3. XML Layout + ViewBinding 고수 — Compose 미도입

**현상**  
2026년 시점에서 신규 Android 프로젝트는 **Jetpack Compose**가 공식 권장이다. 현재 프로젝트는 XML + ViewBinding + Fragment + Navigation Component 구성이다.

**문제**  
- 화면 간 상태 공유를 위해 `activityViewModels()`에 의존 → `MainViewModel` 비대화 원인  
- Fragment 생명주기 관리(C-2의 binding 이슈) 비용 지속 발생  
- 최신 라이브러리들은 Compose를 우선 지원 (M3, CameraX Compose 등)

**개선 방향**  
신규 feature부터 Compose로 작성하거나, 전체를 Compose Screen + Navigation-Compose로 재작성. 단, 리소스 투입 규모가 크므로 중장기 계획 필요.

---

### F-4. Material Components 1.6.1 — M3 미적용

```groovy
implementation 'com.google.android.material:material:1.6.1'   // 2022년 버전
```

상용 앱 기준으로는 Material 3(1.12+) 도입이 표준이다. Dynamic Color, 다크 모드 통합 등 최신 디자인 시스템 기능 사용 불가.

---

### F-5. "실험 기능"이 정식 UI에 노출됨

`ViewPagerFragment`의 다음 기능은 주석상 "test"로 표기되어 있지만 **정식 버튼으로 사용자에게 노출**되어 있다.

```kotlin
private fun testReviewManager() { ... }   // 리뷰 버튼 클릭
private fun testAppSetID() { ... }        // 앱 이름 클릭 시 AppSet ID를 Dialog로 표시
```

- `testAppSetID`: 사용자가 앱 이름을 클릭하면 AppSet ID 값이 Dialog로 노출됨 → 상용 UX로 부적절
- 메서드명에 `test` 접두어가 붙은 채 프로덕션 코드에 존재

**개선**  
- `testAppSetID` 기능 제거 또는 개발자 옵션으로 이동  
- `testReviewManager` → `requestReview()`로 리네이밍, 적절한 트리거(책 10권 북마크 시 등)로 호출 조건 변경

---

## 우선순위 로드맵

구조 개선은 **점진적으로** 수행해야 한다. 순서 제안:

### Phase 1 — 즉시 수정 (1~2일)
- E-1 `New` 접두어 제거
- E-2 네이밍 정리 (`S526Data` → `LocalBookDataSource` 등)
- E-5 죽은 필드 제거
- F-5 `test*` 메서드 정리

### Phase 2 — 단기 (1주)
- C-1 BuildType / BuildConfig로 `BASE_URL` 분리
- D-1 `@Provides` → `@Binds` 전환
- D-2 DI 스코프 정리 (`S526Data`, `Api`, `Repository`에 `@Singleton`)
- F-2 ViewBinding 패턴 통일

### Phase 3 — 중기 (2~3주)
- A-1 Clean Architecture 의존성 방향 정리 (Repository 인터페이스를 domain으로 이동)
- A-2 중간 엔티티 제거 (`Book` / `DetailBook` / `ListBook` 삭제)
- B-2, B-3 Version Catalog + Kotlin DSL 전환
- B-4 KAPT → KSP 전환

### Phase 4 — 장기 (1~2개월)
- B-1 멀티 모듈 분리 (`:core:*`, `:feature:*`)
- A-3 feature-based 패키지로 재정비
- F-3 Compose 부분 도입 (신규 feature부터)
- F-4 Material 3 업그레이드

---

## 참고 자료

- [Guide to app architecture (Android Developers)](https://developer.android.com/topic/architecture)
- [Now In Android — 공식 레퍼런스 앱](https://github.com/android/nowinandroid)
- [Modularization learning journey](https://developer.android.com/topic/modularization)
