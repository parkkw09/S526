# Phase 1-3 리팩토링 변경 내역

본 문서는 `docs/ARCHITECTURE_ISSUES.md` 에서 도출된 Phase 1-3 개선 항목을 실제 코드에 반영한 결과를 기록한다.

---

## Phase 1 — 즉시 수정 ✅

### E-1. `New` 접두어 제거 (도메인 모델 개명)

| Before | After |
|---|---|
| `domain.model.NewBook` | `domain.model.Book` |
| `domain.model.NewDetailBook` | `domain.model.BookDetail` |
| `domain.model.NewListBook` | `domain.model.BookList` |
| `domain.model.NewPdf` | *(제거됨)* |

- 단일 파일 `domain/model/Book.kt` 로 통합

### E-2. 알 수 없는 약어/식별자 정리

| Before | After |
|---|---|
| `application.S526` | `application.S526Application` |
| `data.source.local.S526Data` | `data.source.local.LocalBookDataSource` |
| `data.source.remote.RemoteConst` | `data.source.remote.ApiConstants` |
| `domain.translator.BookTranslator` | *(삭제 — data/mapper/BookMapper 로 대체)* |
| `data.source.remote.OLResponseMapper` | `data.mapper.BookMapper` |

- `AndroidManifest.xml` 의 `android:name` 도 신규 클래스명으로 갱신

### E-5. 죽은 필드 제거

- `Book` : `price` 제거
- `BookDetail` : `error`, `rating`, `price`, `pdf` 제거
- `item_book.xml` : `book_isbn`, `book_price`, `book_url` View 제거, `book_subtitle` 로 대체
- `fragment_detail.xml` : 가격/PDF 표시 영역 제거, 저자/발행처/언어/ISBN/페이지 표시로 재구성

### F-5. 테스트 성격의 메서드 정리

- `testAppSetID()` 전체 삭제 (`play-services-appset` 의존성 함께 제거)
- `testReviewManager()` → `requestReview()` 로 개명 및 시멘틱 명확화
- `MainViewModel` 에서 `client: AppSetIdClient?` 필드와 관련 로직 모두 제거

---

## Phase 2 — 단기 개선 ✅

### C-1. BuildConfig 기반 환경 분리

`app/build.gradle.kts` 의 `buildTypes` 에 아래와 같이 정의:

```kotlin
debug {
    buildConfigField("String", "BASE_URL", "\"https://openlibrary.org/\"")
    buildConfigField("boolean", "ENABLE_LOGGING", "true")
}
release {
    isMinifyEnabled = true
    isShrinkResources = true
    buildConfigField("String", "BASE_URL", "\"https://openlibrary.org/\"")
    buildConfigField("boolean", "ENABLE_LOGGING", "false")
}
```

- `ApiConstants.BASE_URL` 과 `NetworkModule` 의 로깅 레벨이 `BuildConfig` 를 참조
- `application.Log` 의 모든 `d/i/v/w` 호출이 `ENABLE_LOGGING` 으로 게이팅되어 release 빌드에서 완전히 no-op 이 됨
- OkHttpClient 에 timeout(15s) 및 `User-Agent` 헤더 명시

### D-1. `@Provides` → `@Binds` 전환, D-2. 스코프 정리

- `RepositoryModule` / `UseCaseModule` 을 `abstract class + @Binds` 로 전환
- `LibraryRepository` : `@Singleton`
- `LocalBookDataSource` : 클래스 단에 `@Singleton`, `@Inject constructor` 적용 → `LocalModule` 제거
- `Api`, `OkHttpClient`, `HttpLoggingInterceptor`, `Retrofit`, `Converter.Factory` : 모두 `@Singleton`
- UseCase 구현체 : `@ViewModelScoped` (UseCase 는 ViewModel 단위 생명주기로 충분)

### F-2. ViewBinding 패턴 통일

- `presentation.util.FragmentViewBindingDelegate` 를 추가
- 모든 Fragment 에서 `private val binding by viewBinding(XxxBinding::bind)` 패턴으로 통일
- Fragment 가 `Fragment(R.layout.xxx)` 2차 생성자를 사용 → `onCreateView` 보일러플레이트 제거
- `viewLifecycle` 에 묶인 `DefaultLifecycleObserver` 가 `onDestroy` 에서 binding 을 null 로 설정 → 메모리 누수 제거

---

## Phase 3 — 중기 개선 ✅

### A-1. Clean Architecture 의존성 방향 정리

| Before | After |
|---|---|
| `data.repositories.LibraryRepository` (인터페이스) | `domain.repository.LibraryRepository` |
| `data.repositories.impl.LibraryRepositoryImpl` | 유지 (data 계층 구현체) |

- domain 모듈이 data 모듈의 인터페이스를 참조하던 역전된 의존성 해소
- UseCase 는 이제 순수하게 domain 패키지만 import

### A-2. 중간 엔티티 제거

삭제된 클래스:
- `data.entities.Book`, `DetailBook`, `ListBook`, `Pdf`
- `domain.translator.BookTranslator`
- `data.source.remote.OLResponseMapper`

통합:
- `data.mapper.BookMapper` 하나로 `OLSearchResponse` / `OLEditionResponse` → `Book` / `BookDetail` / `BookList` 로 직접 변환
- 모델 변환이 2단계 → 1단계로 축소 (data 엔티티 ↔ 도메인 모델 복사 코드 전면 삭제)

### B-2. Gradle Version Catalog 도입

`gradle/libs.versions.toml` 생성. 버전/라이브러리/번들/플러그인을 중앙 집중 관리:

- `[versions]` : AGP, Kotlin, KSP, Hilt, Glide, Navigation 등 모든 버전을 한 곳에
- `[libraries]` : 라이브러리 좌표 선언
- `[bundles]` : `retrofit`, `okhttp`, `coroutines`, `lifecycle`, `navigation`, `play-review` 그룹화
- `[plugins]` : AGP, Kotlin, KSP, Hilt, Navigation Safe Args 선언

### B-3. Kotlin DSL 전환

| Before | After |
|---|---|
| `build.gradle` | `build.gradle.kts` |
| `settings.gradle` | `settings.gradle.kts` |
| `app/build.gradle` | `app/build.gradle.kts` |

- 타입 안전, IDE 자동완성, 코드 재사용성 확보
- `settings.gradle.kts` 에 `pluginManagement`, `dependencyResolutionManagement` 를 도입하고 `RepositoriesMode.FAIL_ON_PROJECT_REPOS` 로 저장소 선언을 일원화

### B-4. KAPT → KSP 전환

의존성 업그레이드 (KSP 호환 최소 버전):

| 라이브러리 | Before | After |
|---|---|---|
| Kotlin | 1.6.21 | 1.9.24 |
| Gradle | 8.0 | 8.4 |
| AGP | 8.1.2 | 8.1.4 |
| Hilt | 2.41 (KAPT) | 2.51 (KSP) |
| Glide | 4.12.0 (KAPT) | 4.16.0 (KSP) |
| Navigation | 2.4.2 | 2.7.7 |
| coroutines | 1.6.1 | 1.7.3 |
| OkHttp | 4.9.1 | 4.12.0 |

- `kapt` 플러그인 완전 제거 → `ksp` 플러그인으로 전환
- Hilt 컴파일러 / Glide 컴파일러 모두 `ksp(...)` 구성으로 변경
- 빌드 속도 개선 및 IDE 증분 빌드 안정화

---

## 부가 개선 (함께 반영된 보강)

1. **Release 빌드 보안/최적화**
   - `isMinifyEnabled = true`, `isShrinkResources = true` 로 변경
   - `proguard-rules.pro` 에 Retrofit / Gson / OkHttp / Hilt / Glide / Coroutines 보존 규칙 추가

2. **ViewModel 상태 관리 개선**
   - 페이지네이션 상태(`currentNewBookPage`, `hasMoreNewBooks`, `isNewBookLoading`)를 `MainViewModel` 로 이동
   - `onResume` 마다 초기화되던 목록 문제 해결 : `NewBookFragment` 는 최초 진입 시에만 `getNewBook()` 호출

3. **검색 로직 개선**
   - `searchBookMulti` (舊 `searchBook2`) : 여러 키워드를 `async / awaitAll` 로 병렬 호출하고 ISBN 기반 `distinctBy` 로 중복 제거 (기존은 순차 호출 + 중복 누적)

4. **Adapter 품질**
   - `BookmarkAdapter.addAllData` → `setData` : 기존 목록을 clear 후 교체 (중복 누적 버그 수정)
   - `NewBookAdapter`, `HistoryAdapter` : `ListAdapter + DiffUtil` 도입

5. **Retrofit 네트워크 견고성**
   - `OkHttpClient` 에 connect/read/write 15초 timeout 명시
   - 전용 User-Agent 헤더 추가

---

## 이후 남은 Phase 4 (선택 과제)

아래 항목은 이번 리팩토링 범위에 포함되지 않음:

- **F-1. 멀티 모듈 분리** : `:core-common`, `:data-api`, `:feature-books` 등 모듈화
- **F-3. Package-by-Feature 전환** : `feature/book`, `feature/bookmark` 디렉터리 구조로 이동
- **G-1. Navigation 그래프 분리 + Nested Graph** : 기능별 그래프 분리
- **영속 저장소 도입** : `LocalBookDataSource` 를 Room / DataStore 구현으로 교체
