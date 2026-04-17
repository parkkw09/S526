# S526 — 도서 검색 및 관리 애플리케이션

S526은 [OpenLibrary API](https://openlibrary.org/developers/api)를 기반으로 신간 도서 조회, 검색, 즐겨찾기 관리를 제공하는 Android 애플리케이션입니다.

---

## 목차

1. [주요 기능](#주요-기능)
2. [기술 스택](#기술-스택)
3. [아키텍처](#아키텍처)
4. [프로젝트 구조](#프로젝트-구조)
5. [데이터 흐름](#데이터-흐름)
6. [주요 컴포넌트 분석](#주요-컴포넌트-분석)
7. [API 명세](#api-명세)
8. [도메인 모델](#도메인-모델)
9. [화면 구성 및 내비게이션](#화면-구성-및-내비게이션)
10. [로컬 데이터 저장](#로컬-데이터-저장)
11. [시작하기](#시작하기)

---

## 주요 기능

| 기능 | 설명 |
|------|------|
| **신간 도서 조회** | OpenLibrary 최신 도서 목록을 페이지 단위로 불러옵니다. |
| **무한 스크롤** | 리스트 하단 도달 시 다음 페이지를 자동으로 로드합니다. |
| **도서 검색** | 키워드 단일 검색을 지원합니다. |
| **멀티 키워드 검색** | `|` 구분자로 여러 키워드를 동시에 검색하고, ISBN 기준 중복을 제거한 결과를 반환합니다. |
| **도서 상세 정보** | ISBN으로 개별 도서의 상세 정보(저자, 출판사, 페이지 수, 설명 등)를 조회합니다. |
| **즐겨찾기** | 도서를 즐겨찾기에 추가/삭제하고 드래그&스와이프로 순서를 변경합니다. |
| **검색 기록** | 검색어를 인메모리에 저장하여 이전 검색을 재사용할 수 있습니다. |

---

## 기술 스택

| 분류 | 사용 기술 |
|------|-----------|
| 언어 | Kotlin 1.7 |
| 빌드 | Gradle 8.0, Android SDK 36 (minSdk 28) |
| 아키텍처 | Clean Architecture + MVVM |
| DI | Hilt 2.41 |
| 비동기 | Kotlin Coroutines 1.6 |
| 상태 관찰 | LiveData 2.4 |
| UI | XML Layout, ViewBinding |
| 내비게이션 | Jetpack Navigation Component 2.4 |
| 네트워크 | Retrofit 2.9, OkHttp 4.9, Gson 2.8 |
| 이미지 로딩 | Glide 4.12 |
| 로컬 저장소 | In-Memory (`S526Data`) |
| 리뷰 | Google Play In-App Review |
| 테스트 | JUnit 4, Mockito, MockWebServer, Coroutines Test |

---

## 아키텍처

```
┌─────────────────────────────────────────────────────┐
│                   Presentation Layer                │
│  MainActivity → ViewPagerFragment → Tab Fragments  │
│  ViewModel (MainViewModel) ← LiveData              │
└────────────────────────┬────────────────────────────┘
                         │ UseCase 호출
┌────────────────────────▼────────────────────────────┐
│                    Domain Layer                     │
│  UseCase (인터페이스 + 구현체)                        │
│  Model (NewBook, NewDetailBook, NewListBook)        │
│  Repository 인터페이스 (LibraryRepository)           │
│  BookTranslator (Data → Domain 모델 변환)            │
└────────────────────────┬────────────────────────────┘
                         │ Repository 구현체
┌────────────────────────▼────────────────────────────┐
│                     Data Layer                      │
│  LibraryRepositoryImpl                             │
│  ├── Remote: Api (Retrofit) + OLResponseMapper     │
│  └── Local:  S526Data (In-Memory)                  │
└─────────────────────────────────────────────────────┘
```

**계층별 역할**

- **Presentation** — Activity/Fragment가 ViewModel을 구독하며, 사용자 이벤트를 ViewModel 메서드로 전달합니다.
- **Domain** — UseCase가 비즈니스 규칙을 캡슐화합니다. 이 계층은 Android 프레임워크에 의존하지 않습니다.
- **Data** — 네트워크(Remote)와 인메모리 저장소(Local)를 추상화하고, API 응답을 내부 모델로 변환합니다.

---

## 프로젝트 구조

```
app/src/main/java/app/peter/s526
│
├── application/
│   ├── S526.kt               # Application 클래스 (Hilt 진입점)
│   ├── Log.kt                # 로그 래퍼 (TAG_PREFIX = "S526_")
│   └── Utils.kt              # 앱 이름에 버전명 추가 등 유틸리티
│
├── domain/
│   ├── model/
│   │   └── NewBook.kt        # NewBook, NewDetailBook, NewListBook, NewPdf
│   ├── usecase/
│   │   ├── NewBookUseCase.kt
│   │   ├── SearchBookUseCase.kt
│   │   ├── DetailBookUseCase.kt
│   │   ├── BookmarkUseCase.kt
│   │   └── impl/             # 각 UseCase 구현체
│   ├── translator/
│   │   └── BookTranslator.kt # data.entities ↔ domain.model 변환
│   └── module/
│       └── UseCaseModule.kt  # Hilt 모듈: UseCase 인터페이스 → 구현체 바인딩
│
├── data/
│   ├── entities/
│   │   ├── Book.kt           # 로컬/Repository 공용 모델
│   │   ├── DetailBook.kt
│   │   ├── ListBook.kt
│   │   ├── OLSearchResponse.kt   # OpenLibrary search.json 응답 DTO
│   │   └── OLEditionResponse.kt  # OpenLibrary isbn/{isbn}.json 응답 DTO
│   ├── repositories/
│   │   ├── LibraryRepository.kt      # Repository 인터페이스
│   │   └── impl/
│   │       └── LibraryRepositoryImpl.kt
│   ├── source/
│   │   ├── remote/
│   │   │   ├── Api.kt               # Retrofit API 인터페이스
│   │   │   ├── ApiModule.kt         # Hilt: Retrofit/OkHttp 제공
│   │   │   ├── OLResponseMapper.kt  # API DTO → data.entities 변환
│   │   │   └── RemoteConst.kt       # BASE_URL, COVER_URL 상수
│   │   └── local/
│   │       ├── S526Data.kt          # 인메모리 저장소
│   │       └── LocalModule.kt       # Hilt: S526Data 싱글턴 제공
│   └── module/
│       └── RepositoryModule.kt      # Hilt: LibraryRepository 바인딩
│
└── presentation/
    ├── util/
    │   ├── GlideModule.kt   # AppGlideModule 커스텀 설정
    │   ├── UiConst.kt       # TabType 인덱스 상수
    │   └── (TabType)        # S526_NEW=0, S526_BOOKMARK=1, S526_HISTORY=2
    └── view/main/
        ├── MainActivity.kt         # 단일 Activity 진입점
        ├── MainViewModel.kt        # 모든 화면이 공유하는 ViewModel
        ├── ViewPagerFragment.kt    # Toolbar + TabLayout + ViewPager2 컨테이너
        ├── BookPagerAdapter.kt     # ViewPager2 어댑터 (3개 탭)
        ├── book/
        │   ├── NewBookFragment.kt  # 신간 도서 탭 (무한 스크롤)
        │   └── NewBookAdapter.kt
        ├── bookmark/
        │   ├── BookmarkFragment.kt
        │   ├── BookmarkAdapter.kt
        │   └── ItemMoveCallback.kt # 드래그&스와이프 콜백
        ├── history/
        │   ├── HistoryFragment.kt
        │   └── HistoryAdapter.kt
        ├── search/
        │   ├── SearchFragment.kt
        │   └── SearchAdapter.kt
        └── detail/
            └── DetailFragment.kt   # isbn 인자를 받아 상세 조회
```

---

## 데이터 흐름

### 신간 도서 조회 (페이지네이션)

```
NewBookFragment.onScrollEnd()
  → viewModel.getNextNewBook()
    → loadNewBooks(currentPage)           // IO Dispatcher
      → newBookUseCase.getNewBook(page)
        → repository.getNewBook(page)
          → Api.getNewBooks(page)          // Retrofit (OpenLibrary search.json)
            → OLResponseMapper.toListBook()  // DTO → data.entities
              → BookTranslator.getListBook() // data.entities → domain.model
      → _bookList.value += newBooks        // Main Dispatcher
        → NewBookFragment (RecyclerView 갱신)
```

### 멀티 키워드 검색

```
SearchFragment.onSearch("소설|과학")
  → viewModel.searchBook2("소설|과학")
    → words = ["소설", "과학"]
    → 각 word 별 searchBookUseCase.searchBook(word, page) 병렬 호출
    → books = 결과 합산
    → _searchBookList.value = books.distinctBy { isbn }  // 중복 제거
```

### 즐겨찾기 추가/삭제

```
DetailFragment.onBookmarkClick()
  → viewModel.addBookmark(newDetailBook)
    → bookmarkUseCase.addBookmark(book)
      → BookTranslator.getBookByDetailBook(book)  // domain → data.entities
        → repository.addBookmark(book)
          → S526Data.bookmark.add(book)            // 인메모리 저장
```

---

## 주요 컴포넌트 분석

### `MainViewModel`

모든 Fragment가 `activityViewModels()`로 공유하는 단일 ViewModel입니다.

| 상태 (LiveData) | 타입 | 설명 |
|----------------|------|------|
| `bookList` | `List<NewBook>` | 신간 도서 누적 목록 |
| `bookmark` | `List<NewBook>` | 즐겨찾기 목록 |
| `searchBookList` | `List<NewBook>` | 검색 결과 |
| `history` | `List<String>` | 검색 기록 키워드 |
| `currentSearchQuery` | `String` | 현재 검색어 (화면 복원용) |

| 메서드 | 설명 |
|--------|------|
| `getNewBook()` | 페이지를 1로 초기화하고 신간 목록을 새로 로드 |
| `getNextNewBook()` | 로딩 중이 아니고 다음 페이지가 있으면 추가 로드 |
| `searchBook(query)` | 단일 키워드 검색 |
| `searchBook2(query)` | `|` 구분 멀티 키워드 검색, ISBN 기준 중복 제거 |
| `getDetailBook(isbn, callback)` | 상세 조회 후 콜백으로 결과 전달 |

### `OLResponseMapper`

OpenLibrary API의 DTO(`OLSearchResponse`, `OLEditionResponse`)를 data 계층 내부 엔티티(`ListBook`, `DetailBook`)로 변환합니다. Repository 상위 계층이 API 변경의 영향을 받지 않도록 격리하는 역할을 합니다.

- **커버 이미지 URL**: `https://covers.openlibrary.org/b/id/{cover_i}-L.jpg`
- **도서 URL**: `https://openlibrary.org/{key}`
- **연도 추출**: `publishDate` 문자열("March 1, 2020" 또는 "2020")에서 정규식으로 4자리 연도 파싱
- **설명 추출**: `description` 필드가 `String` 또는 `{"value": "..."}` Map 두 형태로 올 수 있어 분기 처리

### `ItemMoveCallback` (즐겨찾기 정렬)

`ItemTouchHelper.Callback`을 구현하여 즐겨찾기 목록에서:
- **드래그 (상/하)**: 항목 순서 변경 (`isLongPressDragEnabled = true` — 롱프레스로 활성화)
- **스와이프 (좌/우)**: 항목 삭제

변경된 순서는 `viewModel.updateBookmark(list)`를 통해 `S526Data`에 즉시 반영됩니다.

### `ViewPagerFragment`

앱의 메인 컨테이너 역할을 하며 다음 두 가지 실험적 기능을 포함합니다.

- **App Set ID 확인** (`testAppSetID`): 앱 이름 텍스트 클릭 시 Google Play Services의 AppSet ID와 범위(scope)를 Dialog로 표시합니다.
- **인앱 리뷰** (`testReviewManager`): 리뷰 버튼 클릭 시 Google Play In-App Review 플로우를 실행합니다.

---

## API 명세

**Base URL**: `https://openlibrary.org/`

| 엔드포인트 | 메서드 | 설명 |
|-----------|--------|------|
| `search.json` | GET | 도서 검색 / 신간 목록 조회 |
| `isbn/{isbn}.json` | GET | ISBN으로 특정 도서 상세 조회 |

### 신간 목록 / 검색 공통 파라미터 (`search.json`)

| 파라미터 | 기본값 | 설명 |
|---------|--------|------|
| `q` | `"programming"` (신간) | 검색 키워드 |
| `sort` | `"new"` (신간만) | 정렬 기준 |
| `page` | `1` | 페이지 번호 |
| `limit` | `20` (신간) / `10` (검색) | 페이지당 결과 수 |
| `fields` | 아래 참조 | 응답 필드 제한 |

**fields**: `key,title,subtitle,isbn,cover_i,author_name,first_publish_year,number_of_pages_median,publisher,language`

---

## 도메인 모델

### `NewBook` — 목록용 도서 모델

```kotlin
data class NewBook(
    val isbn: String,       // 중복 제거 키로 사용
    val title: String,
    val subtitle: String,
    val price: String,      // OpenLibrary는 가격 정보 미제공 → 항상 ""
    val image: String,      // 커버 이미지 URL
    val url: String         // 도서 상세 페이지 URL
)
```

### `NewDetailBook` — 상세용 도서 모델

```kotlin
data class NewDetailBook(
    val error: String,      // "0" = 정상
    val title: String,
    val subtitle: String,
    val authors: String,    // 저자 키 목록 (OpenLibrary author key)
    val publisher: String,
    val language: String,   // "/languages/eng" → "eng"로 파싱
    val isbn10: String,
    val isbn13: String,
    val pages: String,
    val year: String,       // publishDate에서 연도 추출
    val rating: String,     // 항상 "0" (미지원)
    val desc: String,       // 책 설명
    val price: String,
    val image: String,
    val url: String,
    val pdf: NewPdf         // freeBook URL (미지원 시 "")
)
```

### `NewListBook` — 페이지네이션 래퍼

```kotlin
data class NewListBook(
    val error: String,
    val total: String,   // numFound (전체 결과 수)
    val page: String,    // 현재 페이지
    val books: List<NewBook>
)
```

---

## 화면 구성 및 내비게이션

```
MainActivity (Single Activity)
  └── NavHostFragment (nav_s526.xml)
        ├── ViewPagerFragment [시작 목적지]
        │     ├── Tab 0: NewBookFragment    (신간 도서)
        │     ├── Tab 1: BookmarkFragment   (즐겨찾기)
        │     └── Tab 2: HistoryFragment    (검색 기록)
        ├── DetailFragment ←─── isbn(String) 인자
        └── SearchFragment ←─── query(String) 인자
```

**화면 전환 애니메이션**: `slide_in_right` / `slide_out_left` (이동), `slide_in_left` / `slide_out_right` (뒤로 가기)

| 전환 | 트리거 | 방법 |
|------|--------|------|
| ViewPager → Detail | 도서 항목 클릭 | `action_view_pager_fragment_to_detail_fragment` |
| ViewPager → Search | 검색 아이콘 클릭 | `action_view_pager_fragment_to_search_fragment` |
| Search → Detail | 검색 결과 항목 클릭 | `action_search_fragment_to_detail_fragment` |
| History → Search | 기록 항목 클릭 | `action_view_pager_fragment_to_search_fragment` (쿼리 포함) |

---

## 로컬 데이터 저장

```kotlin
class S526Data {
    val bookmark: ArrayList<Book> = ArrayList()  // 즐겨찾기 목록
    val history: ArrayList<String> = ArrayList() // 검색어 기록
}
```

- **Hilt 싱글턴**으로 관리되어 앱 세션 동안 데이터가 유지됩니다.
- **앱 재시작 시 초기화**됩니다. 영속 저장이 필요하다면 Room 또는 DataStore로 교체가 필요합니다.
- 즐겨찾기 중복 추가 방지: `List.contains(book)` 으로 체크 후 add합니다.
- 검색 기록 중복 방지: `List.contains(query)` 으로 체크 후 add합니다.

---

## 시작하기

1. 저장소를 클론합니다.
   ```bash
   git clone <repository-url>
   ```
2. Android Studio에서 프로젝트를 엽니다.
3. Gradle Sync를 완료합니다.
4. 에뮬레이터(API 28+) 또는 실제 디바이스에서 실행합니다.

> 별도의 API 키가 필요하지 않습니다. OpenLibrary API는 인증 없이 사용할 수 있습니다.

---

© 2024 S526 Project. All rights reserved.
