package com.pakrhanbeen.modernjavainaction.chapter07;

public class CustomSpliterator {

    /**
     * 반복형으로 단어 수를 세는 메서드.
     */
    public int countWordsIteratively(String s) {
        int counter = 0;
        boolean lastSpace = true;

        // 문자열의 모든 문자를 하나씩 탐색한다.
        for (char c : s.toCharArray()) {
            if (Character.isWhitespace(c)) {
                lastSpace = true;
            } else {
                // 문자를 하나씩 탐색하다 공백 문자를 만나면 지금까지 탐색한 문자를 단어로 간주하여(공백 문자는 제외) 단어 수를 증가시킨다.
                if (lastSpace) {
                    counter++;
                }
                lastSpace = false;
            }
        }
        return counter;
    }
}
