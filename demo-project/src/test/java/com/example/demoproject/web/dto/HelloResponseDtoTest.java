package com.example.demoproject.web.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HelloResponseDtoTest {

    @Test
    void 롬복_테스트() {
        String name = "test";
        int amount = 10;

        HelloResponseDto dto = new HelloResponseDto(name, amount);

        // then
        assertThat(dto.name()).isEqualTo(name);
        assertThat(dto.amount()).isEqualTo(amount);
    }

}