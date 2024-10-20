import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
  scenarios: {
    applyCoupons: {
      executor: 'ramping-vus',
      exec: 'applyCoupons',
      stages: [
         { duration: '1s', target: 1 },
         { duration: '1s', target: 0 },
      ],
    },
    checkCoupons: {
      executor: 'ramping-vus',
      exec: 'checkCoupons',
      startTime: '1s',
      stages: [
        { duration: '1s', target: 1 },
        { duration: '1s', target: 0 },
      ],
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

