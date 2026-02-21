package com.anoncommunity.app.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(
    scanBasePackages = ["com.anoncommunity"]
)
@ConfigurationPropertiesScan(basePackages = ["com.anoncommunity.app.api.security"])
@EntityScan(basePackages = ["com.anoncommunity.infra.rdb.entity"])
@EnableJpaRepositories(basePackages = ["com.anoncommunity.infra.rdb.repository"])
class AnonCommunityApiApplication

fun main(args: Array<String>) {
    runApplication<AnonCommunityApiApplication>(*args)
}
