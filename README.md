# test0731 시스템 관리 공통기능

한국교원대학교 교수업적평가시스템의 시스템 관리 1차 범위입니다.

## 포함 기능

- 사용자 관리 `/system/users`, `/api/admin/users`
- 조직 관리 `/system/organizations`, `/api/admin/organizations`
- 역할 관리 `/system/roles`, `/api/admin/roles`
- 사용자 역할 관리 `/system/user-roles`, `/api/admin/user-roles`
- 메뉴 권한 관리 `/system/menu-permissions`, `/api/admin/menu-permissions`
- 메뉴 구조 관리 `/system/menu-structures`, `/api/admin/menu-structures`
- 메뉴 정보 관리 `/system/menus`, `/api/admin/menus`
- 코드그룹 관리 `/system/code-groups`, `/api/admin/code-groups`
- 상세코드 관리 `/system/detail-codes`, `/api/admin/detail-codes`

## 실행

```bash
docker compose -f infra/docker-compose.yml up --build
```

프론트엔드: http://localhost:3000
백엔드 health: http://localhost:8080/api/health

시드 관리자 계정은 `SYSTEM_ADMIN` 역할 선택으로 9개 메뉴 전체를 확인할 수 있습니다. `SYSTEM_VIEWER` 선택 시 메뉴가 숨겨지고 쓰기 API는 403을 반환합니다.
