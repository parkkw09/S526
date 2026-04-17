# 상용화 검토 — 문제점 및 개선 방안

분석 기준일 : 2026-04-17
대상 버전 : versionCode 13 / versionName 1.1.0

Phase 1 ~ Phase 3 리팩토링이 반영된 현재 상태를 기준으로, 각 항목의 해결 여부를 함께 표기한다. 반영 내역의 상세는 [`REFACTOR_CHANGELOG.md`](./REFACTOR_CHANGELOG.md) 참조.

---

## 목차

- [심각도 기준](#심각도-기준)
- [P0 — 즉시 수정 필요 (출시 불가)](#p0--즉시-수정-필요-출시-불가)
- [P1 — 출시 전 반드시 수정](#p1--출시-전-반드시-수정)
- [P2 — 품질 향상 (출시 후 단기 대응)](#p2--품질-향상-출시-후-단기-대응)
- [P3 — 기술 부채 (중장기)](#p3--기술-부채-중장기)
- [요약 체크리스트](#요약-체크리스트)

---

## 심각도 기준

| 등급 | 의미 |
|------|------|
| **P0** | 앱이 정상 동작하지 않거나, 보안/정책 위반 수준의 결함 |
| **P1** | 사용자 경험을 직접 훼손하는 버그 또는 데이터 유실 |
| **P2** | 기능이 동작은 하나 품질이 낮은 상태 |
| **P3** | 장기 유지보수에 영향을 주는 기술 부채 |

상태 표기: ✅ 해결됨 · 🟡 부분 해결 · ❌ 미해결

---

## P0 — 즉시 수정 필요 (출시 불가)

### 1. 릴리즈 빌드에 디버그 로그 전량 노출 — ✅ 해결

**파일** : `NetworkModule.kt`, `application/Log.kt`, `app/build.gradle.kts`

BuildConfig 기반 환경 분리를 도입하여 해결.

```kotlin
// NetworkModule.kt (현재)
interceptor.level = if (BuildConfig.ENABLE_LOGGING) {
    HttpLoggingInterceptor.Level.BODY
} else {
    HttpLoggingInterceptor.Level.NONE
}

// application/Log.kt (현재)
fun d(tag: String, vararg objects: Any) {
    if (!BuildConfig.ENABLE_LOGGING) return
    android.util.Log.d(Utils.TAG_PREFIX + tag, concat(*objects))
}
```

- `build.gradle.kts` 에서 `ENABLE_LOGGING` 을 debug=true / release=false 로 분리
- 오류 로그(`Log.e`) 만 release 에서도 유지되어 장애 분석이 가능

### 2. 데이터 영속성 전무 — 앱 재시작 시 전체 데이터 소멸 — ❌ 미해결

**파일** : `data/source/local/LocalBookDataSource.kt`

즐겨찾기와 검색 기록이 여전히 인메모리(`MutableList`)에만 저장된다. 앱 프로세스가 종료되면 모든 데이터가 사라진다.

```kotlin
@Singleton
class LocalBookDataSource @Inject constructor() {
    private val _bookmark: MutableList<Book> = mutableListOf()
    private val _history: MutableList<String> = mutableListOf()
    // ...
}
```

**Phase 2 에서 반영된 부분**
- `@Singleton` 스코프가 명시되어 세션 내 인스턴스는 안정적
- `LocalModule` 을 삭제하고 `@Inject constructor` 로 대체

**남은 개선 사항**
- 즐겨찾기 : **Room** 데이터베이스로 교체 (영속 저장)
- 검색 기록 : **DataStore\<Preferences\>** 로 교체
- 단기 완화책 : `SharedPreferences` JSON 직렬화

### 3. ProGuard / R8 비활성화 — ✅ 해결

**파일** : `app/build.gradle.kts`

```kotlin
release {
    isMinifyEnabled = true
    isShrinkResources = true
    proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro",
    )
    // ...
}
```

- `proguard-rules.pro` 에 Retrofit / Gson / OkHttp / Hilt / Glide / Coroutines 보존 규칙 추가

---

## P1 — 출시 전 반드시 수정

### 4. `NewBookFragment` 탭 전환 시마다 전체 목록 재로드 — ✅ 해결

**파일** : `NewBookFragment.kt`, `MainViewModel.kt`

페이지네이션 상태를 `MainViewModel` 로 이동하고, 최초 진입 시에만 로드하도록 수정.

```kotlin
// NewBookFragment.onViewCreated (현재)
if (viewModel.bookList.value.isNullOrEmpty()) {
    viewModel.getNewBook()
}

// MainViewModel (현재)
private var currentNewBookPage = 1
private var isNewBookLoading = false
private var hasMoreNewBooks = true

fun getNextNewBook() {
    if (isNewBookLoading || !hasMoreNewBooks) return
    loadNewBooks(currentNewBookPage)
}
```

- 탭 전환 / Detail → Back / 회전 시 목록이 유지됨

### 5. `BookmarkAdapter.addAllData()` 중복 누적 — ✅ 해결

**파일** : `BookmarkAdapter.kt`

`setData()` 메서드로 개명하고 기존 목록을 clear 후 교체하도록 수정.

```kotlin
fun setData(list: List<Book>) {
    bookList.clear()
    bookList.addAll(list)
    notifyDataSetChanged()
}
```

### 6. 에러 상태 및 로딩 상태 UI 없음 — ❌ 미해결

**파일** : `MainViewModel.kt`, 모든 Fragment

`MainViewModel` 은 여전히 네트워크 예외를 `catch (e: Exception) { Log.e(...) }` 로만 처리하고, UI 에 상태를 전달하지 않는다.

```kotlin
// MainViewModel.loadNewBooks (현재)
} catch (e: Exception) {
    Log.e(TAG, "getNewBook exception [${e.localizedMessage}]")
    // UI 는 로딩/에러 상태를 알지 못한 채 빈 화면만 표시
}
```

**권장 개선**

```kotlin
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

private val _newBookState = MutableLiveData<UiState<List<Book>>>()
val newBookState: LiveData<UiState<List<Book>>> = _newBookState
```

### 7. `DetailFragment` 즐겨찾기 현재 상태 미반영 — ❌ 미해결

**파일** : `DetailFragment.kt`

상세 화면 진입 시 FAB 아이콘이 즐겨찾기 여부에 관계없이 동일하게 표시된다.

```kotlin
// DetailFragment.bindDetail (현재)
fab.setOnClickListener { view -> toggleBookmark(detail, view) }
// 초기 아이콘은 XML 의 기본값으로 고정 → 이미 북마크된 책인지 알 수 없음
```

**권장 개선**

```kotlin
private fun bindDetail(detail: BookDetail) = with(binding) {
    // ... 기존 바인딩 ...
    updateFabIcon(viewModel.isBookmarked(detail))
    fab.setOnClickListener { view ->
        toggleBookmark(detail, view)
        updateFabIcon(viewModel.isBookmarked(detail))
    }
}
```

### 8. `searchBook2` — 멀티 키워드 순차 네트워크 호출 — ✅ 해결

**파일** : `MainViewModel.kt`

`searchBookMulti` 로 개명하고 `async / awaitAll` 병렬 호출로 변경.

```kotlin
// MainViewModel.searchBookMulti (현재)
val results = words
    .map { word -> async { searchBookUseCase.searchBook(word, page) } }
    .awaitAll()
val merged = results.flatMap { it.books }.distinctBy { it.isbn }
```

- 병렬 호출로 응답 시간 단축
- ISBN 기반 `distinctBy` 로 중복 제거 추가

### 9. OkHttpClient 타임아웃 미설정 — ✅ 해결

**파일** : `NetworkModule.kt`

```kotlin
OkHttpClient.Builder()
    .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)  // 15s
    .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)        // 15s
    .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)      // 15s
    .addInterceptor(loggingInterceptor)
    .addInterceptor { chain -> /* User-Agent 주입 */ }
    .build()
```

---

## P2 — 품질 향상 (출시 후 단기 대응)

### 10. 상세 화면 — 저자가 OpenLibrary 내부 key 로 노출 — ❌ 미해결

**파일** : `data/mapper/BookMapper.kt`, `presentation/view/main/detail/DetailFragment.kt`

`BookMapper.toBookDetail` 이 여전히 `/authors/{key}` 경로의 `key` 부분만 뽑아 표시한다.

```kotlin
// BookMapper (현재)
val authors = response.authors?.joinToString(", ") { author ->
    author.key.removePrefix("/authors/")  // "OL1234A, OL5678B" → 사람이 읽을 수 없음
}.orEmpty()
```

**권장 개선**
- `/authors/{key}.json` 추가 호출로 실제 이름을 조회
- 또는 `search.json` 응답의 `author_name` 필드를 함께 저장하여 Detail 진입 시 전달

### 11. 목록 카드에 개발용 데이터 노출 — ✅ 해결

**파일** : `item_book.xml`, `NewBookAdapter.kt`, `BookmarkAdapter.kt`, `SearchAdapter.kt`

`item_book.xml` 에서 `book_isbn`, `book_price`, `book_url` View 가 제거되고 `book_subtitle` 로 대체되었다. 모든 Adapter 의 `bind()` 도 동일하게 정리됨.

### 12. `SearchAdapter` — TAG 오탈자 — ✅ 해결

**파일** : `SearchAdapter.kt`

현재는 TAG companion object 자체가 제거되었다. (로깅 호출도 없음)

### 13. 검색 결과 페이지네이션 상태가 Fragment 에 산재 — ❌ 미해결

**파일** : `SearchFragment.kt`

`pageCount`, `currentPage`, `loading`, `complete` 4 개의 페이지네이션 상태가 여전히 `SearchFragment` 의 프로퍼티로 남아 있다.

```kotlin
// SearchFragment (현재)
private var pageCount = 1
private var currentPage = 1
private var loading = false
private var complete = false
```

- 화면 회전 시 페이지네이션 상태가 초기화됨
- `NewBookFragment` 와 달리 `MainViewModel` 로 이동되지 않음

**권장 개선**
- `MainViewModel` 에 `currentSearchPage`, `isSearchLoading`, `hasMoreSearch` 를 신규 추가하여 `NewBookFragment` 패턴과 통일

### 14. 즐겨찾기 스와이프 삭제 — 실행 취소(Undo) 없음 — ❌ 미해결

**파일** : `BookmarkAdapter.kt`, `BookmarkFragment.kt`, `ItemMoveCallback.kt`

스와이프 즉시 삭제만 존재하고 Snackbar UNDO 액션이 없어 실수로 삭제할 위험이 있다.

### 15. 네트워크 없음 상태 처리 부재 — ❌ 미해결

**파일** : 전반

`ACCESS_NETWORK_STATE` 권한은 선언되어 있으나 실제로 네트워크 상태를 확인하는 로직이 없다. 오프라인 환경에서 API 실패 시 빈 화면만 표시된다.

---

## P3 — 기술 부채 (중장기)

### 16. 테스트 커버리지 극히 낮음 — ❌ 미해결

**현재 테스트**
- `NewBookUseCaseTest` — MockWebServer 기반 통합 테스트 1건 (`getNewBook()` 인자 없이 호출 → 기본값 `"1"` 이 적용되어 현재는 정상 동작)
- `BookmarkUseCaseTest` — UseCase 단위 테스트 4건

**미작성 테스트**
- `MainViewModel` 단위 테스트
- `BookMapper` 단위 테스트 (DTO → 도메인 변환 로직)
- `SearchBookUseCase`, `DetailBookUseCase` 단위 테스트
- Fragment UI 테스트
- 페이지네이션 로직 테스트

### 17. 의존성 버전 최신화 — 🟡 부분 해결

Phase 3 에서 대부분의 핵심 의존성이 최신(또는 최신에 근접) 버전으로 업그레이드됨.

| 라이브러리 | 기존 | 현재 | 비고 |
|---|---|---|---|
| Hilt | 2.41 | **2.51** | ✅ |
| Glide | 4.12.0 | **4.16.0** | ✅ |
| Coroutines | 1.6.1 | **1.7.3** | ✅ |
| Navigation | 2.4.2 | **2.7.7** | ✅ |
| OkHttp | 4.9.1 | **4.12.0** | ✅ |
| Material | 1.6.1 | **1.12.0** | 🟡 버전만 올리고 M3 테마는 미적용 |
| core-ktx | 1.8.0 | **1.13.1** | ✅ |
| Kotlin | 1.6.21 | **1.9.24** | ✅ |
| Retrofit | 2.9.0 | 2.9.0 | 🟡 2.11.0 출시 이후 개선 반영 가능 |

### 18. `Book` / `NewBook` 모델 이중화 — ✅ 해결

Phase 3 에서 `data.entities.Book/DetailBook/ListBook` 과 `domain.translator.BookTranslator` 를 제거하고, `BookMapper` 에서 DTO → 도메인 모델로 직접 변환하도록 통합했다. 중복 모델은 더 이상 존재하지 않는다.

### 19. `MainViewModel` 비대화 (God ViewModel) — ❌ 미해결

**파일** : `MainViewModel.kt`

여전히 신간 / 검색 / 즐겨찾기 / 검색 기록 / Review / 앱 이름 등 모든 기능의 상태와 로직이 단일 ViewModel 에 집중되어 있다.

**권장 개선**
- `NewBookViewModel`, `SearchViewModel`, `BookmarkViewModel` 로 분리
- 화면 간 공유가 필요한 상태(앱 이름, `ReviewManager` 등)만 `SharedViewModel` 또는 공유 `StateHolder` 로 관리

---

## 요약 체크리스트

| # | 문제 | 심각도 | 상태 | 수정 난이도 |
|---|------|--------|------|------------|
| 1 | 릴리즈 빌드 로그 전량 노출 | P0 | ✅ 해결 | 낮음 |
| 2 | 데이터 영속성 없음 | P0 | ❌ 미해결 | 높음 |
| 3 | ProGuard 비활성화 | P0 | ✅ 해결 | 낮음 |
| 4 | onResume 마다 신간 목록 재초기화 | P1 | ✅ 해결 | 낮음 |
| 5 | BookmarkAdapter 중복 누적 | P1 | ✅ 해결 | 낮음 |
| 6 | 에러/로딩 상태 UI 없음 | P1 | ❌ 미해결 | 중간 |
| 7 | 상세화면 즐겨찾기 상태 미표시 | P1 | ❌ 미해결 | 낮음 |
| 8 | 멀티 키워드 검색 순차 호출 | P1 | ✅ 해결 | 낮음 |
| 9 | OkHttpClient 타임아웃 미설정 | P1 | ✅ 해결 | 낮음 |
| 10 | 저자 정보 내부 key 노출 | P2 | ❌ 미해결 | 중간 |
| 11 | 목록 카드 개발용 데이터 노출 | P2 | ✅ 해결 | 낮음 |
| 12 | SearchAdapter TAG 오탈자 | P2 | ✅ 해결 | 낮음 |
| 13 | 검색 페이지네이션 상태 Fragment 산재 | P2 | ❌ 미해결 | 중간 |
| 14 | 즐겨찾기 삭제 Undo 없음 | P2 | ❌ 미해결 | 낮음 |
| 15 | 네트워크 없음 상태 미처리 | P2 | ❌ 미해결 | 중간 |
| 16 | 테스트 커버리지 낮음 | P3 | ❌ 미해결 | 높음 |
| 17 | 의존성 버전 | P3 | 🟡 부분 해결 | 중간 |
| 18 | Book / NewBook 모델 중복 | P3 | ✅ 해결 | 중간 |
| 19 | MainViewModel 비대화 | P3 | ❌ 미해결 | 높음 |

**Phase 1 ~ Phase 3 리팩토링으로 전체 19 개 이슈 중 10 개 해결 (P0 2/3, P1 4/6, P2 2/6, P3 2/4).**
**출시를 위한 최우선 과제는 P0-2 (영속성), P1-6 (상태 UI), P1-7 (FAB 상태) 3 건이다.**
