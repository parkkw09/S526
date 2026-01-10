# S526 - 도서 검색 및 관리 애플리케이션

S526은 사용자가 신간 도서를 확인하고, 원하는 책을 검색하며, 관심 있는 도서를 즐겨찾기에 저장하여 관리할 수 있는 모바일 애플리케이션입니다.

## 📱 주요 기능

*   **신간 도서 조회 (`New Books`)**: 최신 도서 목록을 빠르게 확인할 수 있습니다.
*   **도서 검색 (`Search`)**:
    *   키워드를 통한 기본 도서 검색을 지원합니다.
    *   **멀티 키워드 검색**: 파이프(`|`) 기호를 사용하여 여러 키워드(예: `소설|과학`)를 동시에 검색하고, 중복을 제거한 통합 결과를 제공합니다.
*   **즐겨찾기 (`Bookmark`)**: 마음에 드는 책을 즐겨찾기에 추가하거나 삭제하여 나만의 서재를 꾸밀 수 있습니다.
*   **검색 기록 (`History`)**: 최근 검색한 키워드가 저장되어 이전에 찾았던 내용을 쉽게 다시 검색할 수 있습니다.
*   **상세 정보 (`Detail`)**: 책의 상세 정보를 확인하고 바로 즐겨찾기에 추가할 수 있습니다.

## 🛠 기술 스택

이 프로젝트는 **Clean Architecture**와 **MVVM** 패턴을 기반으로 구축되었으며, 최신 Android 기술 스택을 사용합니다.

*   **Language**: Kotlin
*   **Architecture**: Clean Architecture (Presentation, Domain, Data Layers) + MVVM
*   **Dependency Injection**: Hilt
*   **Concurrency**: Kotlin Coroutines
*   **Data Observability**: LiveData
*   **UI Components**: XML Layouts, ViewBinding
*   **Network**: (Inferred: Retrofit/OkHttp)
*   **Local Database**: (Inferred: Room)

## 📂 프로젝트 구조

애플리케이션은 관심사의 분리 원칙에 따라 다음과 같이 구성되어 있습니다.

```
app/src/main/java/app/peter/s526
├── application  # 앱 전반의 설정 및 초기화 (App Class, Utils)
├── domain       # 비즈니스 로직 및 핵심 모델 (UseCase, Model, Repository Interface)
├── data         # 데이터 처리 및 저장소 구현 (Repository Impl, DAO, API Source)
└── presentation # UI 및 화면 로직 (Activity, Fragment, ViewModel)
```

### 핵심 로직 분석 (`MainViewModel`)

`MainViewModel`은 앱의 주요 비즈니스 로직을 중재합니다.
*   **`getNewBook`**: 비동기 처리를 통해 신간 데이터를 불러옵니다.
*   **`searchBook2`**: 사용자 정의 복합 검색 로직을 구현하여 여러 검색어에 대한 결과를 병합하고 ISBN 기준으로 중복을 필터링합니다.
*   **`bookmark`**: `BookmarkUseCase`를 통해 로컬 DB와 연동하여 사용자의 즐겨찾기를 관리합니다.

## 🚀 시작하기

1. Repository를 클론합니다.
2. Android Studio에서 프로젝트를 엽니다.
3. Gradle Sync를 완료한 후 에뮬레이터 또는 디바이스에서 실행합니다.

---
© 2024 S526 Project. All rights reserved.
