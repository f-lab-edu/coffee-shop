import http from 'k6/http';
import { sleep, check } from 'k6';

const BASE_URL = 'http://localhost:8081';
const COUPON_ID = 800;

export const options = {
  scenarios: {
    applyCoupons: {
      executor: 'per-vu-iterations',
      exec: 'applyCoupons',
      vus: 70,
      iterations: 1,
    },
    checkCoupons: {
      executor: 'per-vu-iterations',
      exec: 'checkCoupons',
      vus: 70,
      iterations: 1,
      startTime: '2s',
    },
  },
  thresholds: {
    http_req_duration: ['p(99)<1200'],
  },
};

export function applyCoupons() {
    const userId = __VU;
    console.log(`applyCoupons: userId=${userId}`);

    // 쿠폰 발급
    var payload = JSON.stringify({
        userId: userId,
        couponId: COUPON_ID
    });

    var params = {
        headers: {
          'Content-Type': 'application/json',
        },
    };

    let res = http.post(`${BASE_URL}/api/coupons/apply`, payload, params);

//    console.log(`POST response status: ${res.status}`);
//    console.log(`POST response body: ${res.body}`);
//    console.log(`POST userId: ${userId}`);
//    console.log(`####################################`);

    check(res, {
        'status is 201': (r) => r.status === 201,
    });

    // redis 순차 큐 삽입
    var payload = JSON.stringify({
        userId: userId
    });

    var params = {
        headers: {
          'Content-Type': 'application/json',
        },
    };

    let res2 = http.post(`${BASE_URL}/api/queue/enqueue`, payload, params);

//    console.log(`POST Q response status: ${res2.status}`);
//    console.log(`POST Q response body: ${res2.body}`);
//    console.log(`####################################`);

    sleep(1);
}

export function checkCoupons() {
  // redis 순차 큐 꺼내기
  let res = http.get(`${BASE_URL}/api/queue/dequeue`);

  let jsonResponse = JSON.parse(res.body);

//  console.log(`GET Q response status: ${res.status}`);
//  console.log(`GET Q response body: ${res.body}`);
//  console.log(`GET Q response body: ${jsonResponse.userId}`);
//  console.log(`####################################`);


  // 쿠폰 조회
  let res2 = http.get(`${BASE_URL}/api/users/${jsonResponse.userId}/coupons/${COUPON_ID}`);
  console.log(`checkCoupons: userId=${jsonResponse.userId}`);

  let jsonResponse2 = JSON.parse(res2.body);

//  console.log(`GET response status: ${res2.status}`);
//  console.log(`GET response body: ${res2.body}`);
//  console.log(`####################################`);


  check(jsonResponse2, {
    'result is SUCCESS': (r) => r.data.result === 'SUCCESS',
  });

  sleep(1);
}
