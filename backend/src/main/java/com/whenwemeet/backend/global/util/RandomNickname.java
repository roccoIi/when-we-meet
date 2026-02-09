package com.whenwemeet.backend.global.util;

import java.util.Random;

public class RandomNickname {

    // 형용사 배열
    private static final String[] adjectives = {
            "귀여운", "발랄한", "사랑스러운", "똑똑한", "활발한", "졸린", "장난꾸러기", "용감한", "상냥한", "행복한",
            "느긋한", "온화한", "믿음직한", "애교많은", "진지한", "엉뚱한", "당당한", "재빠른", "고요한", "부드러운"
    };

    // 동물 이름 배열
    private static final String[] animals = {
            "강아지", "고양이", "햄스터", "토끼", "앵무새", "거북이", "고슴도치", "호랑이", "코뿔소", "하마",
            "돼지", "코끼리", "여우", "판다", "기린", "원숭이", "새우", "다람쥐", "문어", "카피바라"
    };

    // 랜덤 닉네임 생성 함수
    public String generateNickname() {
        Random random = new Random();

        // 배열의 길이에 맞춰 랜덤으로 배열 값을 조회
        String adjective = adjectives[random.nextInt(adjectives.length)];
        String animal = animals[random.nextInt(animals.length)];
        // 추가적으로 닉네임 중복을 방지하기 위해 1~999까지의 무작위 숫자 조회
        int number = random.nextInt(1000);

        return adjective + animal + number;
    }
}
