import redis from 'k6/experimental/redis';
import http from 'k6/http';
import {check, sleep} from 'k6';

const userIds = new redis.Client({
    socket: {
        host: '49.50.174.218',
        port: 6379,
    },
});

const BASE_URL = 'http://49.50.175.80:8080';
const COUPON_ID = 1;
const listKey = 'stress-test';

export const options = {
    scenarios: {
        applyAndCheckCoupons: {
            executor: 'per-vu-iterations',
            vus: 1,
            iterations: 1,
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'],
        'http_req_duration{name:issueCoupon}': ['p(99)<100'],
        'http_req_duration{name:findPosition}': ['p(99)<100'],
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
            'check coupon is SUCCESS': (r) => response.data.couponIssueStatus !== 'FAILURE',
        });

        if (response.data.couponIssueStatus === 'SUCCESS') {
            break;
        } else {
            console.log(`사용자 ${poppedUserId} 발급중........ ${response.data.couponIssueStatus}`);
            sleep(1);
        }
    }

}
