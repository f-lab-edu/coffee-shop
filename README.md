## 프로젝트 목표

* 쿠폰 1,000장을 선착순으로 발급하는 이벤트를 진행하며, 1,000명 이상의 사용자 요청이 몰려도 1초 이내의 지연 시간으로 안정적인 발급이 가능한 시스템 구축을 목표로 했습니다.
* 최소한의 구성으로 시작하여, 웹 서버와 DB 서버 각 1대로 초기 시스템을 설계한 후, 점진적으로 트래픽을 증가시키며 짧은 시간에 높은 요청이 집중되는 상황에서도 안정적인 서비스 운영이 가능하도록 시스템 구조를
  확장하는 경험을 쌓았습니다.

## 사용 기술 및 환경

- Java, Spring Boot, Gradle
- MariaDB, Redis
- GitHub Actions, Naver Cloud Platform
- K6, Pinpoint, Grafana, Prometheus
- IntelliJ

## 전체 프로젝트 구조

<img width="620" alt="스크린샷 2024-12-30 오후 4 39 26" src="https://github.com/user-attachments/assets/4e279a61-8c33-40e7-8a99-053acb313cbe" />

## 프로젝트를 진행하며 고민한 Technical Issue

- 자세한 사항은 여기서 확인해주세요! 👉 [Technical Issue](https://github.com/f-lab-edu/coffee-shop/wiki/04.-Technical-Issue)
- 선착순 쿠폰 발급 시스템에서 단일 서버 환경에서는 Spring으로 큐 구현, 분산 서버 환경에서는 **Redis로 큐**를 관리하여 운영 환경 특성을 고려하여 설계
- Sentinel을 통한 자동 **페일오버** 메커니즘 설정으로 장애 발생 시 서비스 중단 시간을 최소화
- 상세한 **단위 테스트, 통합 테스트** 작성으로 코드 품질 향상, 테스트 코드 커버리지
  77% | [코드보기](https://github.com/f-lab-edu/coffee-shop/blob/develop/src/test/java/com/coffee_shop/coffeeshop/service/coupon/issue/fail/CouponIssueFailHandlerImplTest.java)
- K6 테스트 스크립트를 작성하여 **대규모 트래픽 시나리오**를 시뮬레이션, **성능 튜닝**
- Prometheus와 Grafana를 연동하여 인프라 및 애플리케이션 **메트릭 실시간 수집 및 시각화**

## ERD

![ERD](https://github.com/user-attachments/assets/5ccf31a8-c4aa-4de9-9b9c-770b8cf5563d)
