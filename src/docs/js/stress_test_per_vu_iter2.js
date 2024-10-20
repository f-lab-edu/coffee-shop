import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
  scenarios: {
    applyCoupons: {
      executor: 'per-vu-iterations',  // 'ramping-vus'에서 'per-vu-iterations'로 변경
      exec: 'applyCoupons',
      vus: 10, // 2명의 VU가 실행
      iterations: 1, // 각 VU는 5번씩 반복 실행
    },
    checkCoupons: {
      executor: 'per-vu-iterations',  // 'ramping-vus'에서 'per-vu-iterations'로 변경
      exec: 'checkCoupons',
      vus: 10,  // 2명의 VU가 실행
      iterations: 1, // 각 VU는 3번씩 반복 실행
      startTime: '1s', // 이 시나리오는 1초 후에 시작
    },
  },
  thresholds: {
    http_req_duration: ['p(99)<1200'], // 99%의 요청이 1.2초 이하로 완료되어야 함
  },
};

export function applyCoupons() {
  console.log(`applyCoupons: VU=${__VU}, ITER=${__ITER}`);
}

export function checkCoupons() {
  console.log(`checkCoupons: VU=${__VU}, ITER=${__ITER}`);
  sleep(1);
}

