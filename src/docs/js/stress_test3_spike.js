import redis from 'k6/experimental/redis';
import http from 'k6/http';
import {check, sleep} from 'k6';

const userIds = new redis.Client({
    socket: {
        host: '49.50.174.218',
        // host: 'localhost',
        port: 6379,
    },
});

const BASE_URL = 'http://101.101.209.62';
// const BASE_URL = 'http://localhost:8080';
const COUPON_ID = 1;
const listKey = 'stress-test';

export const options = {
    scenarios: {
        applyAndCheckCoupons: {
            executor: 'per-vu-iterations',
            vus: 100,
            iterations: 1,
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'],
        'http_req_duration{name:issueCoupon}': ['p(95)<100'], //쿠폰 발급 api 95%는 100ms 안으로 응답받아야한다.
        'http_req_duration{name:findPosition}': ['p(95)<100'], //쿠폰 조회 api 95%는 100ms 안으로 응답받아야한다.
    },
}

export default async function () {
    const userId = __VU;

    //쿠폰 발급
    var payload = JSON.stringify({
        userId: userId,
        couponId: COUPON_ID
    });

    var params = {
        headers: {
            'Content-Type': 'application/json',
        },
        tags: {name: 'issueCoupon'},
    };

    const applyRes = await http.post(`${BASE_URL}/api/coupons/apply`, payload, params);
    check(applyRes, {
        'apply coupon status is 201': (r) => r.status === 201,
    });
    if (applyRes.status !== 201) {
        console.log(applyRes.status);
        console.log(applyRes.body);
    }
    await userIds.rpush(listKey, userId);

    sleep(1);

    //쿠폰 조회
    const poppedUserId = await userIds.lpop(listKey);

    while (true) {
        const checkRes = await http.get(`${BASE_URL}/api/users/${poppedUserId}/coupons/${COUPON_ID}`, {
            tags: {name: 'findPosition'},
        });
        const response = JSON.parse(checkRes.body);
        check(checkRes, {
            'check coupon status is 200': (r) => checkRes.status === 200,
            'Response is Success or In Progress': (r) => response.data.couponIssueStatus !== 'FAILURE',
        });

        if (response.data.couponIssueStatus === 'SUCCESS' || response.data.couponIssueStatus === 'FAILURE') {
            // console.log(`사용자 ${poppedUserId} 성공!........ ${response.data.couponIssueStatus}`);
            break;
        }

        // console.log(`사용자 ${poppedUserId} 발급중........ ${response.data.couponIssueStatus}`);
        sleep(1);
    }

}
