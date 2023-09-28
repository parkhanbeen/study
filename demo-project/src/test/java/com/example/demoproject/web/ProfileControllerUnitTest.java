package com.example.demoproject.web;

import org.springframework.mock.env.MockEnvironment;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ProfileControllerUnitTest {

    @Test
    void prod_profile_조회() {
        // given
        String expectedProfile = "prod";
        MockEnvironment env = new MockEnvironment();
        env.addActiveProfile(expectedProfile);
        env.addActiveProfile("oauth");
        env.addActiveProfile("prod-db");

        ProfileController profileController = new ProfileController(env);

        // when
        String profile = profileController.profile();

        // then
        Assertions.assertThat(profile).isEqualTo(expectedProfile);
    }

    @Test
    void prod_profile_없으면_첫_번째_조회() {
        // given
        String expectedProfile = "oauth";
        MockEnvironment env = new MockEnvironment();

        env.addActiveProfile(expectedProfile);
        env.addActiveProfile("prod-db");

        ProfileController profileController = new ProfileController(env);

        // when
        String profile = profileController.profile();

        // then
        Assertions.assertThat(profile).isEqualTo(expectedProfile);
    }

    @Test
    void active_profile_없으면_default_조회() {
        // given
        String expectedProfile = "default";
        MockEnvironment env = new MockEnvironment();

        env.addActiveProfile(expectedProfile);

        ProfileController profileController = new ProfileController(env);

        // when
        String profile = profileController.profile();

        // then
        Assertions.assertThat(profile).isEqualTo(expectedProfile);
    }
}