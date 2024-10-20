import http from 'k6/http';
import { sleep, check } from 'k6';

let currentUserId = 1;

const maxUserId = 100000;
const usedUserIds = new Set();
const BASE_URL = 'http://localhost:8081';
const COUPON_ID = 800;

export const options = {
  scenarios: {
    applyCoupons: {
      executor: 'ramping-vus',
      exec: 'applyCoupons',
      stages: [
         { duration: '30s', target: 35 },
         { duration: '1m', target: 70 },
         { duration: '30s', target: 0 },
      ],
    },
    checkCoupons: {
      executor: 'ramping-vus',
      exec: 'checkCoupons',
      startTime: '4s', // applyCoupons 시나리오가 시작된 후 2초에 시작
      stages: [
        { duration: '30s', target: 35 },
        { duration: '1m', target: 70 },
        { duration: '30s', target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_duration: ['p(99)<1200'], // 99%의 요청이 1.2초 이하로 완료되어야 함
  },
};

export function applyCoupons() {
    const userId = getUniqueUserId();

    if (userId == null) {
        console.log('No more unique user IDs available.');
        return;
      }

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

    console.log(`POST response status: ${res.status}`);
    console.log(`POST response body: ${res.body}`);
    console.log(`POST userId: ${userId}`)

    check(res, {
        'status is 201': (r) => r.status === 201,
    });
}

export function checkCoupons() {
  const userIdArray = Array.from(usedUserIds);
  const userId = userIdArray[userIdArray.length - 1];
  console.log(userIdArray.length);


  if (userId == undefined) {
    console.log('No more unique user IDs available2.');
    return;
  }

  const url = `${BASE_URL}/api/users/${userId}/coupons/${COUPON_ID}`;
  let res = http.get(url);

  let jsonResponse = JSON.parse(res.body);

  console.log(`GET url: ${url}`);

  console.log(`GET response status: ${res.status}`);
  console.log(`GET response body: ${res.body}`);

  check(jsonResponse, {
      'result is SUCCESS': (r) => r.data.result === 'SUCCESS',
  });

  sleep(1);
}

function getUniqueUserId() {
  while (currentUserId <= maxUserId) {
    if (!usedUserIds.has(currentUserId)) {
      const userId = currentUserId;
      usedUserIds.add(userId);
      currentUserId++;
      return userId;
    }
    currentUserId++;
  }
  return null;
}
