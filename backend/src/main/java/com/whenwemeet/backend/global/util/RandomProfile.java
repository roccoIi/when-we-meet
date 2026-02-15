package com.whenwemeet.backend.global.util;

import java.util.Random;

public class RandomProfile {

    private final String ROOTADRESS = "/images/";

    // 형용사 배열
    private static final String[] animals = {
            "cat.png", "dog.png", "capybara.png", "octopus.png", "pig.png", "fox.png"
    };

    // 랜덤 프로필 생성 함수
    public String generateProfileImg() {
        Random random = new Random();
        int number = random.nextInt(6);

        return ROOTADRESS + animals[number];
    }
}
