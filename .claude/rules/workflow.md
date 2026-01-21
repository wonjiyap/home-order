# 기능 개발 Workflow

## 1. 이슈 분석
- GitHub 이슈 내용 확인
- 작업 항목을 TodoWrite로 정리
- 필요시 기존 코드 구조 파악

## 2. 개발 순서 (TDD)
1. **테스트 먼저 작성** - Service 테스트 케이스 작성
2. **기능 구현** - 순서대로 진행:
   - Table 정의 (필요시)
   - Entity 정의 (필요시)
   - Repository 구현
   - Service 구현
   - Controller 구현
   - DTO 정의
3. **테스트 실행** - `./gradlew test`
4. **빌드 확인** - `./gradlew build`

## 3. 이슈 업데이트
- 각 작업 완료 시 GitHub 이슈 체크리스트 체크
