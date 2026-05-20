---
description: TDD 기반으로 React 페이지 컴포넌트(JSX + CSS Module + Test)를 생성합니다. 사용법: /react-page <PageName> [설명]
---

# react-page 스킬

$ARGUMENTS 에서 페이지 이름(첫 번째 단어, PascalCase)과 설명(나머지)을 파싱한다.

---

## Step 0 — 컨텍스트 수집 (코드 작성 전 필수)

### 0-1. 기존 패턴 파악
다음 파일을 읽어 프로젝트 컨벤션을 파악한다:
- `connect-me-frontend/src/pages/RegisterPage.jsx` — 폼 페이지 패턴
- `connect-me-frontend/src/pages/RegisterPage.module.css` — CSS Module 패턴
- `connect-me-frontend/src/__tests__/RegisterPage.test.jsx` — 테스트 패턴
- `connect-me-frontend/src/store/useAuthStore.js` — 상태관리 패턴
- `connect-me-frontend/src/App.jsx` — 라우팅 현황

### 0-2. 스크린샷 탐색
`screenshots/` 폴더의 파일 목록을 확인하고, $ARGUMENTS 의 페이지 이름과 가장 유사한 이미지를 찾는다.

매핑 예시:
- ChatList / Home → `Chat List (Home).png`
- DirectChat / ChatRoom → `1_1 Chat Room.png`
- GroupChat → `Group Chat Room.png`
- GroupCreation / CreateGroup → `Group Creation.png`
- Settings / Profile → `Settings.png`
- MediaFiles / Attachments → `Media & Files.png`

매칭된 스크린샷이 있으면 **Read 도구로 이미지를 읽어** 아래를 파악한다:
- 레이아웃 구조 (헤더, 바디, 푸터, 사이드바 등)
- 컬러 팔레트 (배경색, 강조색, 텍스트색)
- 컴포넌트 목록 (리스트, 카드, 버튼, 입력 필드, 아이콘 등)
- 인터랙션 요소 (탭, 스크롤, 모달 진입점 등)

매칭 스크린샷이 없으면 → `Login.png` 의 **톤앤매너**(화이트 카드 + 퍼플 포인트 컬러)를 기준으로 설계한다.

---

## Step 1 — 설계 요약 출력

코드 작성 전, 아래 형식으로 사용자에게 설계를 보여준다:

```
페이지: <PageName>
경로: /src/pages/<PageName>.jsx
스타일: /src/pages/<PageName>.module.css
테스트: /src/__tests__/<PageName>.test.jsx

참조 스크린샷: screenshots/<파일명> (없으면 Login.png 톤앤매너)

레이아웃:
  <스크린샷 분석 또는 도메인 기반 설계>

주요 컴포넌트:
  - <컴포넌트 목록>

상태:
  - <useState 목록>
  - <useAuthStore 셀렉터 목록 (필요 시)>

API 연동: <있으면 src/api/ 어떤 함수 호출 여부>

App.jsx 라우트 추가: <경로> (PrivateRoute 여부)

테스트 케이스:
  - renders_<핵심요소>
  - <인터랙션 케이스들>
  - (인증 필요 시) redirects_to_login_when_not_authenticated
```

---

## Step 2 — TDD: 테스트 먼저 작성 (Red)

파일: `connect-me-frontend/src/__tests__/<PageName>.test.jsx`

### 테스트 작성 규칙
- Vitest (`describe`, `it`, `expect`, `vi`, `beforeEach`)
- `@testing-library/react` (`render`, `screen`, `fireEvent`, `waitFor`)
- `useNavigate` 모킹:
  ```js
  const mockNavigate = vi.fn()
  vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual('react-router-dom')
    return { ...actual, useNavigate: () => mockNavigate }
  })
  ```
- API 모킹 (`vi.mock('../api/<apiFile>')`):
  ```js
  vi.mock('../api/someApi')
  // 각 테스트에서: someApi.someFunc.mockResolvedValue(...)
  ```
- Zustand 스토어 초기화 (`beforeEach`):
  ```js
  useAuthStore.setState({ accessToken: null, refreshToken: null, isAuthenticated: false })
  ```
  **주의: 두 번째 인자 `true` 금지** — actions가 제거됨

- 테스트 래핑: 라우터가 필요하면 `<MemoryRouter>` 사용

### 테스트 케이스 구조
```jsx
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import <PageName> from '../pages/<PageName>'

// 필요 시 mock 선언

describe('<PageName>', () => {
  beforeEach(() => {
    // 모킹 초기화
    vi.resetAllMocks()
    mockNavigate?.mockClear()
  })

  it('핵심 UI 요소를 렌더링한다', () => {
    render(<MemoryRouter><PageName /></MemoryRouter>)
    // getByText, getByRole, getByPlaceholderText 등으로 검증
  })

  // 폼이 있으면:
  it('빈 폼 제출 시 유효성 오류를 표시한다', async () => { ... })
  it('성공 시 <목적지>로 이동한다', async () => { ... })
  it('서버 오류 시 에러 메시지를 표시한다', async () => { ... })

  // 리스트가 있으면:
  it('데이터 로드 후 아이템 목록을 렌더링한다', async () => { ... })
  it('빈 목록일 때 안내 메시지를 표시한다', async () => { ... })

  // 인증 보호 페이지면 (PrivateRoute 바깥에서 직접 테스트 시):
  // PrivateRoute.test.jsx 에서 커버되므로 컴포넌트 자체는 인증된 상태로 테스트
})
```

---

## Step 3 — 구현 (Green)

### 3-1. CSS Module
파일: `connect-me-frontend/src/pages/<PageName>.module.css`

디자인 토큰 (프로젝트 전역 일관성 유지):
```css
/* 컬러 */
--primary: #4F46E5;
--primary-hover: #4338CA;
--primary-disabled: #A5B4FC;
--text-primary: #111827;
--text-secondary: #374151;
--text-muted: #6B7280;
--text-placeholder: #C4CAD4;
--border: #E5E7EB;
--bg-input: #FAFAFA;
--error: #EF4444;
--gradient-hero: linear-gradient(160deg, #9DD4D4 0%, #1B4060 100%);
--gradient-icon: linear-gradient(145deg, #6A6EF4 0%, #4F46E5 100%);
```

스크린샷 기반 레이아웃을 CSS Module로 구현한다.
- 모든 클래스명 camelCase
- 반응형이 필요하면 `@media` 추가
- 애니메이션은 `transition` 으로 간결하게

### 3-2. Page Component
파일: `connect-me-frontend/src/pages/<PageName>.jsx`

```jsx
import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import styles from './<PageName>.module.css'
// 필요 시: import useAuthStore from '../store/useAuthStore'
// 필요 시: import * as <domain>Api from '../api/<domain>Api'

// SVG 아이콘은 인라인 함수로 선언 (외부 라이브러리 금지)
function <IconName>Icon() {
  return <svg ...></svg>
}

export default function <PageName>() {
  // 상태 선언
  // 훅 선언 (useNavigate, useAuthStore 셀렉터)

  // 이벤트 핸들러

  return (
    // 스크린샷 기반 JSX
  )
}
```

규칙:
- SVG 아이콘은 외부 라이브러리 없이 인라인 함수로 작성
- `<a href>` 대신 `<Link to>` 사용 (내부 라우팅)
- 로딩 상태: 버튼 텍스트 변경 + `disabled`
- 에러 상태: `<p className={styles.errorText}>{error}</p>`
- `noValidate` on `<form>` — 브라우저 기본 검증 비활성화, JS로 처리
- 코멘트 없이 선언적 JSX

### 3-3. App.jsx 라우트 추가
`connect-me-frontend/src/App.jsx` 에 라우트를 추가한다:

- 인증 불필요 페이지: `<Route path="/<path>" element={<<PageName> />} />`
- 인증 필요 페이지: `<Route element={<PrivateRoute />}><Route path="/<path>" element={<<PageName> />} /></Route>`

---

## Step 4 — 검증 및 완료 보고

모든 파일 생성 후 출력:

```
생성된 파일:
  ✅ src/__tests__/<PageName>.test.jsx   (TDD 테스트)
  ✅ src/pages/<PageName>.jsx            (페이지 컴포넌트)
  ✅ src/pages/<PageName>.module.css     (CSS Module)
  ✅ src/App.jsx                         (라우트 추가)

참조 스크린샷: <파일명 또는 "없음 (Login.png 톤앤매너 적용")>

테스트 실행:
  npm test  (connect-me-frontend 디렉토리에서)

다음 단계:
  npm run dev 로 개발 서버 실행 후 UI 확인
```

---

## 주의사항

- `setState(..., true)` 금지 — Zustand actions 삭제됨. `setState({ ... })` 만 사용
- CSS 클래스명 camelCase (`styles.btnPrimary`, `styles.inputWrap`)
- 외부 UI 라이브러리(MUI, Chakra 등) 사용 금지 — 인라인 SVG + CSS Module
- `import *` 대신 명시적 named import
- 테스트에서 `screen.getByRole` 우선, 없으면 `getByPlaceholderText`, `getByText` 순으로 사용
- 타임아웃 기본값 1000ms — `waitFor` 내부 비동기 검증은 빠른 mock으로 처리