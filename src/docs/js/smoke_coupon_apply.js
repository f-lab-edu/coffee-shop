import http from 'k6/http';
import { check, group, sleep, fail } from 'k6';

export let options = {
    vus: 1,
    duration: '10s',

    thresholds: {
        http_req_duration: ['p(99)<1200'],
    },
};

const BASE_URL = 'http://localhost:8081';
const COUPON_ID = 800;

export default function ()  {
    let id = (__VU - 1) * 100 + __ITER + 1;

    var payload = JSON.stringify({
        userId: id,
        couponId: COUPON_ID
    });

    var params = {
        headers: {
          'Content-Type': 'application/json',
        },
    };

    let url = `${BASE_URL}/api/coupons/apply`;
    let res = http.post(`${BASE_URL}/api/coupons/apply`, payload, params);

console.log(`VU: ${id} + ${url}`);
console.log(`POST response status: ${res.status}`);
console.log(`POST response body: ${res.body}`);

    check(res, {
        'status is 201': (r) => r.status === 201,
      });

    sleep(1);
    let res2 = http.get(`${BASE_URL}/api/users/${id}/coupons/${COUPON_ID}`);

    let jsonResponse = JSON.parse(res2.body);
    let resultValue = jsonResponse.data.result;
    console.log(`Extracted result: ${resultValue}`);

    check(jsonResponse, {
        'result is SUCCESS': (r) => r.data.result === 'SUCCESS',
    });
    sleep(1);
};
