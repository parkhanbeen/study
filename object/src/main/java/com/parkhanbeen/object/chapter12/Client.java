package com.parkhanbeen.object.chapter12;

import java.util.Arrays;

public class Client {

    public void enter() {
        Lecture lecture = new Lecture("객체지향 프로그래밍",
            70,
            Arrays.asList(81, 95, 75, 50, 45));

        String evaluration = lecture.evaluate(); // 결과 => "pass:3 Fail:2"


        Lecture gradeLecture = new GradeLecture("객체지향 프로그래밍",
            70,
            Arrays.asList(81, 95, 75, 50, 45),
            Arrays.asList(
                new Grade("A", 100, 95),
                new Grade("B", 94, 80),
                new Grade("C", 79, 70),
                new Grade("D", 69, 50),
                new Grade("F", 49, 0))
        );

        String evaluration1 = gradeLecture.evaluate(); // 결과 => "pass:3 Fail:2, A:1 B:1 C:1 D:1 F:1"


        Professor professor = new Professor("다익스트라",
            new Lecture("알고리즘", 70, Arrays.asList(81, 95, 75, 50, 45)));

        // 결과 => "[다익스트라] Pass:3 Fail:2 - Avg: 69.2"
        String statistics = professor.compileStatistics();

        Professor professor1 = new Professor("다익스트라",
            new GradeLecture("객체지향 프로그래밍",
                70,
                Arrays.asList(81, 95, 75, 50, 45),
                Arrays.asList(
                    new Grade("A", 100, 95),
                    new Grade("B", 94, 80),
                    new Grade("C", 79, 70),
                    new Grade("D", 69, 50),
                    new Grade("F", 49, 0))
            ));

        // 결과 => "[다익스트라] pass:3 Fail:2, A:1 B:1 C:1 D:1 F:1 - Avg: 69.2"
        String statistics1 = professor.compileStatistics();
    }

}
