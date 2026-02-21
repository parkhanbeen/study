package com.anoncommunity.app.consumer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(
    scanBasePackages = ["com.anoncommunity"]
)
@EntityScan(basePackages = ["com.anoncommunity.infra.rdb.entity"])
@EnableJpaRepositories(basePackages = ["com.anoncommunity.infra.rdb.repository"])
class AnonCommunityConsumerApplication

fun main(args: Array<String>) {
    runApplication<AnonCommunityConsumerApplication>(*args)
}
