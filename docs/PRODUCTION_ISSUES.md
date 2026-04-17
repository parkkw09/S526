# 상용화 검토 — 문제점 및 개선 방안

분석 기준일: 2026-04-17  
대상 버전: 1.0.12

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

---

## P0 — 즉시 수정 필요 (출시 불가)

### 1. 릴리즈 빌드에 디버그 로그 전량 노출

**파일**: `NetworkModule.kt`, `Log.kt`

**문제**  
`HttpLoggingInterceptor.Level.BODY`가 빌드 타입 분기 없이 항상 적용되어 있다. 릴리즈 빌드에서도 모든 HTTP 요청·응답 바디(헤더, URL, 파라미터)가 Logcat에 출력된다.

```kotlin
// NetworkModule.kt — 현재
return logger.apply { level = HttpLoggingInterceptor.Level.BODY }
```

**영향**  
- 네트워크 트래픽 전문 노출 → 보안 취약점  
- Google Play 개인정보 처리방침 위반 가능

**개선**
```kotlin
return logger.apply {
    level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
}
```

`Log.kt`도 동일하게 적용해야 한다.
```kotlin
fun d(tag: String, vararg objects: Any) {
    if (BuildConfig.DEBUG) android.util.Log.d(Utils.TAG_PREFIX + tag, toString(*objects))
}
```

---

### 2. 데이터 영속성 전무 — 앱 재시작 시 전체 데이터 소멸

**파일**: `S526Data.kt`, `LocalModule.kt`

**문제**  
즐겨찾기와 검색 기록이 순수 인메모리(`ArrayList`)에만 저장된다. 앱을 종료하거나 시스템이 프로세스를 회수하는 즉시 모든 데이터가 사라진다.

```kotlin
class S526Data {
    val bookmark: ArrayList<Book> = ArrayList()   // 재시작 시 소멸
    val history: ArrayList<String> = ArrayList()  // 재시작 시 소멸
}
```

또한 `LocalModule.provideLocalData()`에 `@Singleton`이 없어, Hilt 컴포넌트 범위 설정이 명시적이지 않다.

```kotlin
// LocalModule.kt — @Singleton 누락
@Provides
fun provideLocalData(): S526Data = S526Data()
```

**개선**  
- 즐겨찾기: **Room** 데이터베이스로 교체 (영속 저장)  
- 검색 기록: **DataStore\<Preferences\>** 로 교체  
- 단기 임시 조치: `@Singleton` 추가 및 `SharedPreferences` JSON 직렬화

---

### 3. ProGuard / R8 비활성화 — 릴리즈 바이너리 난독화 없음

**파일**: `app/build.gradle`

**문제**
```groovy
buildTypes {
    release {
        minifyEnabled false   // 난독화, 코드 축소 모두 미적용
    }
}
```

**영향**  
- APK 역공학 시 패키지 구조, 클래스명, 비즈니스 로직이 그대로 노출됨  
- 불필요한 클래스/리소스가 포함되어 APK 크기 증가

**개선**
```groovy
release {
    minifyEnabled true
    shrinkResources true
    proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
}
```

---

## P1 — 출시 전 반드시 수정

### 4. `NewBookFragment.onResume()` — 탭 전환 시마다 전체 목록 재로드

**파일**: `NewBookFragment.kt`

**문제**  
`onResume()`에서 `getNewBook()`을 호출하면 ViewPager 탭 전환, 화면 회전, Detail에서 Back 등 모든 resume 시점에 페이지를 1로 초기화하고 API를 재호출한다. 사용자가 50번째 페이지까지 스크롤했더라도 탭 전환 한 번이면 처음으로 되돌아간다.

```kotlin
override fun onResume() {
    super.onResume()
    launchUi()          // 매 resume마다 getNewBook() 호출 → 리스트 초기화
}
```

**개선**  
`bookList`가 비어 있을 때만 초기 로드하거나, `onViewCreated()`로 이동하고 ViewModel에 초기화 여부 플래그를 둔다.

```kotlin
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    if (viewModel.bookList.value.isNullOrEmpty()) {
        viewModel.getNewBook()
    }
}
```

---

### 5. `BookmarkAdapter.addAllData()` — clear() 없는 addAll로 데이터 중복 누적

**파일**: `BookmarkAdapter.kt`

**문제**  
`addAllData()`가 기존 목록을 지우지 않고 뒤에 추가한다. `BookmarkFragment`가 재생성될 때마다(탭 전환, 화면 회전) 같은 항목이 반복 추가된다.

```kotlin
fun addAllData(list: List<NewBook>) {
    bookList.addAll(list)       // clear() 없음 → 탭 전환마다 중복 추가
    notifyDataSetChanged()
}
```

**개선**
```kotlin
fun addAllData(list: List<NewBook>) {
    bookList.clear()
    bookList.addAll(list)
    notifyDataSetChanged()
}
```

---

### 6. 에러 상태 및 로딩 상태 UI 없음

**파일**: `MainViewModel.kt`, 모든 Fragment

**문제**  
ViewModel은 네트워크 예외를 `catch (e: Exception) { Log.e(...) }` 로만 처리하고, 에러 상태를 UI에 전달하지 않는다. 로딩 중 상태도 없다.

사용자 관점에서 네트워크 오류 발생 시:
- 빈 화면만 표시됨 (원인 알 수 없음)
- 재시도 수단 없음
- 로딩 중인지 완료된 것인지 구분 불가

**개선**  
ViewModel에 상태 sealed class와 로딩/에러 LiveData를 추가한다.

```kotlin
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

private val _uiState = MutableLiveData<UiState<List<NewBook>>>()
val uiState: LiveData<UiState<List<NewBook>>> = _uiState
```

---

### 7. `DetailFragment` — 즐겨찾기 현재 상태 미반영

**파일**: `DetailFragment.kt`

**문제**  
상세 화면 진입 시 FAB 아이콘이 항상 동일한 "추가" 아이콘으로 표시된다. 이미 즐겨찾기에 추가된 책인지 구분이 불가능하다.

```kotlin
// launchUi()에서 checkBookmark 결과를 FAB에 적용하지 않음
this.fab.setOnClickListener { view -> checkBookmark(information, view) }
```

**개선**  
`launchUi()` 콜백 내에서 FAB 아이콘을 즐겨찾기 여부에 따라 초기화하고, 클릭 후에도 아이콘을 토글한다.

```kotlin
val isBookmarked = viewModel.checkBookmark(information)
fab.setImageResource(
    if (isBookmarked) R.drawable.ic_bookmark_filled
    else R.drawable.ic_bookmark_outline
)
```

---

### 8. `searchBook2` — 멀티 키워드 순차 네트워크 호출

**파일**: `MainViewModel.kt`

**문제**  
`|` 구분자로 분리된 키워드를 `forEach`로 순차 호출한다. 키워드 3개면 응답 시간이 3배 증가한다.

```kotlin
words.forEach { word ->
    val result = searchBookUseCase.searchBook(word, page)  // 순차 처리
    books.addAll(result.books.toList())
}
```

**개선**  
`async`/`awaitAll`로 병렬 처리한다.

```kotlin
val deferred = words.map { word ->
    async { searchBookUseCase.searchBook(word, page) }
}
val results = deferred.awaitAll()
results.forEach { books.addAll(it.books) }
```

---

### 9. OkHttpClient 타임아웃 미설정

**파일**: `NetworkModule.kt`

**문제**  
`OkHttpClient`에 타임아웃이 없다. 기본값은 10초이나 명시되지 않아 의도와 다를 수 있고, 네트워크 지연 시 앱이 무한정 대기한다.

**개선**
```kotlin
OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .writeTimeout(15, TimeUnit.SECONDS)
    .addInterceptor(loggingInterceptor)
    .build()
```

---

## P2 — 품질 향상 (출시 후 단기 대응)

### 10. 상세 화면 — 저자가 OpenLibrary 내부 key로 노출

**파일**: `OLResponseMapper.kt`, `DetailFragment.kt`

**문제**  
저자 정보가 `/authors/OL1234A` 형태의 내부 키로 표시된다. OpenLibrary `/authors/{key}.json` API를 추가 호출하거나, 검색 결과의 `author_name` 필드를 활용해야 한다.

현재: `"OL1234A, OL5678B"` → 기대: `"Steve Klabnik, Carol Nichols"`

---

### 11. UI에 개발용 데이터 노출 (가격, URL, ISBN을 목록 카드에 직접 표시)

**파일**: `NewBookAdapter.kt`, `BookmarkAdapter.kt`, `SearchAdapter.kt`, `item_book.xml`

**문제**  
목록 카드에 `price`(항상 빈 값), `url`(긴 URL 문자열), `isbn`이 그대로 노출된다. 이는 개발 확인용 정보로, 상용 앱 UI로는 부적절하다.

**개선**  
목록 카드는 제목, 표지 이미지, 저자, 출판연도 등 사용자 관련 정보만 표시한다.

---

### 12. `SearchAdapter` — TAG 오탈자

**파일**: `SearchAdapter.kt`

**문제**  
```kotlin
companion object {
    private const val TAG = "BookmarkAdapter"  // SearchAdapter인데 BookmarkAdapter로 잘못 기재
}
```

---

### 13. 검색 결과 페이지네이션 상태 변수가 Fragment에 산재

**파일**: `SearchFragment.kt`

**문제**  
`pageCount`, `currentPage`, `loading`, `complete` 4개의 페이지네이션 상태 변수가 Fragment에 직접 선언되어 있다. ViewModel에서 관리해야 화면 회전 등 구성 변경(Configuration Change)에서도 상태가 보존된다.

현재 상태에서는 검색 중 화면을 회전하면 페이지네이션 상태가 초기화된다.

---

### 14. 즐겨찾기 스와이프 삭제 — 실행 취소(Undo) 없음

**파일**: `BookmarkAdapter.kt`, `BookmarkFragment.kt`

**문제**  
스와이프로 삭제 시 확인 없이 즉시 제거되며, 되돌릴 방법이 없다. 즐겨찾기는 사용자가 능동적으로 구성한 데이터이므로 실수로 삭제될 위험이 있다.

**개선**  
삭제 직후 Snackbar "UNDO" 액션을 제공한다.

---

### 15. 네트워크 없음 상태 처리 부재

**파일**: 전반

**문제**  
`ACCESS_NETWORK_STATE` 권한은 선언되어 있으나, 실제로 네트워크 상태를 확인하는 코드가 없다. 오프라인 상태에서 API 호출 실패 시 에러 메시지 없이 빈 화면이 표시된다.

---

## P3 — 기술 부채 (중장기)

### 16. 테스트 커버리지 극히 낮음

현재 테스트:
- `NewBookUseCaseTest` — MockWebServer 기반 통합 테스트 1개
- `BookmarkUseCaseTest` — UseCase 단위 테스트 4개

미작성 테스트:
- `MainViewModel` 단위 테스트 (없음)
- `OLResponseMapper` 단위 테스트 (없음)
- `SearchBookUseCase` 단위 테스트 (없음)
- Fragment UI 테스트 (없음)
- 페이지네이션 로직 테스트 (없음)

또한 `NewBookUseCaseTest`에서 `useCase.getNewBook()`을 인자 없이 호출하나 실제 인터페이스는 `getNewBook(page: String)`으로 선언되어 있어 API 불일치 가능성이 있다.

---

### 17. 의존성 버전 최신화 필요

현재 사용 중인 주요 라이브러리가 출시 당시 기준으로도 최신이 아니며, 이후 보안 패치와 버그 수정이 다수 반영된 버전이 존재한다.

| 라이브러리 | 현재 버전 | 비고 |
|-----------|-----------|------|
| Hilt | 2.41 | 보안 패치 다수 포함 업데이트 필요 |
| Retrofit | 2.9.0 | Kotlin Coroutines 지원 개선 버전 존재 |
| Glide | 4.12.0 | 메모리 관련 버그 수정 버전 존재 |
| Kotlin Coroutines | 1.6.1 | 구조적 동시성 개선 버전 존재 |
| core-ktx | 1.8.0 | 여러 메이저 버전 업 |

---

### 18. 모델 계층 이중화 — `Book` / `NewBook` 불필요한 중복

**파일**: `data/entities/Book.kt`, `domain/model/NewBook.kt`

**문제**  
두 클래스는 필드 구성이 완전히 동일하다. `BookTranslator`는 이 두 클래스 사이를 단순 복사 변환만 수행한다. Clean Architecture 원칙상 domain 모델과 data entity의 분리는 의미가 있으나, 현재 구조에서는 실질적인 분리 효과 없이 변환 코드만 추가되고 있다.

`data.entities.DetailBook` / `domain.model.NewDetailBook`도 동일한 문제가 있다.

**개선 방향**  
- domain 모델을 유일 모델로 통일하고 data 계층이 직접 domain 모델을 반환하거나  
- 두 계층의 필드가 실질적으로 달라지도록 설계를 재정비한다.

---

### 19. `MainViewModel` 비대화 (God ViewModel)

**파일**: `MainViewModel.kt`

신간, 검색, 즐겨찾기, 검색 기록, Google Play 리뷰, AppSet ID 등 모든 기능의 상태와 로직이 단일 ViewModel에 집중되어 있다. 기능이 추가될수록 유지보수가 어려워진다.

**개선 방향**  
- `NewBookViewModel`, `SearchViewModel`, `BookmarkViewModel`로 분리  
- 화면 간 공유가 필요한 상태만 `SharedViewModel` 또는 공유 `StateHolder`로 관리

---

## 요약 체크리스트

| # | 문제 | 심각도 | 수정 난이도 |
|---|------|--------|------------|
| 1 | 릴리즈 빌드 로그 전량 노출 | P0 | 낮음 |
| 2 | 데이터 영속성 없음 (재시작 시 소멸) | P0 | 높음 |
| 3 | ProGuard 비활성화 | P0 | 낮음 |
| 4 | onResume마다 신간 목록 재초기화 | P1 | 낮음 |
| 5 | BookmarkAdapter addAllData 중복 누적 | P1 | 낮음 |
| 6 | 에러/로딩 상태 UI 없음 | P1 | 중간 |
| 7 | 상세화면 즐겨찾기 상태 미표시 | P1 | 낮음 |
| 8 | 멀티 키워드 검색 순차 호출 | P1 | 낮음 |
| 9 | OkHttpClient 타임아웃 미설정 | P1 | 낮음 |
| 10 | 저자 정보 내부 key 노출 | P2 | 중간 |
| 11 | 목록 카드 개발용 데이터 노출 | P2 | 낮음 |
| 12 | SearchAdapter TAG 오탈자 | P2 | 낮음 |
| 13 | 페이지네이션 상태 Fragment 산재 | P2 | 중간 |
| 14 | 즐겨찾기 삭제 Undo 없음 | P2 | 낮음 |
| 15 | 네트워크 없음 상태 미처리 | P2 | 중간 |
| 16 | 테스트 커버리지 낮음 | P3 | 높음 |
| 17 | 의존성 버전 구식 | P3 | 중간 |
| 18 | Book / NewBook 모델 중복 | P3 | 중간 |
| 19 | MainViewModel 비대화 | P3 | 높음 |

**즉시 수정 가능한 P0·P1 항목 9개 중 6개(1, 3, 4, 5, 7, 8, 9번)는 수정 난이도가 낮아 빠르게 처리 가능하다.**
