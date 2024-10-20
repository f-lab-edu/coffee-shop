import http from 'k6/http';
import { SharedArray } from 'k6/data';
import { check, sleep } from 'k6';

// SharedArray를 사용하여 JSON 파일에서 데이터를 읽음
const sharedData = new SharedArray('User data', function () {
  // 'open' 함수로 파일을 읽고, JSON으로 파싱
  const jsonData = JSON.parse(open('./data.json')); // 파일 경로에 맞게 변경
  return jsonData; // 파싱된 JSON 배열을 반환
});

export const options = {
  vus: 5, // 5명의 가상 사용자
  iterations: 10, // 5번 반복
};

// 각 가상 사용자가 SharedArray에서 데이터를 사용
export default function () {
  const currentIndex = __ITER; // 각 반복의 인덱스 (반복 횟수에 따라 데이터 선택)
  const userData = sharedData[currentIndex]; // SharedArray에서 순차적으로 데이터 가져옴

  // 가져온 데이터 출력
  console.log(`Using data: ID=${userData.id}, Name=${userData.name}, VU=${__VU}, ITER=${__ITER}`);

  // 예시로 URL 요청에 데이터 추가
  const url = `https://test.k6.io/contacts.php?id=${userData.id}&name=${userData.name}`;
  const res = http.get(url);

  // 응답 체크
  check(res, {
    'status is 200': (r) => r.status === 200,
  });

  sleep(1); // 대기
}
