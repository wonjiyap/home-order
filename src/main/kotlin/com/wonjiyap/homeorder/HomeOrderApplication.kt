package com.wonjiyap.homeorder

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class HomeOrderApplication

fun main(args: Array<String>) {
    runApplication<HomeOrderApplication>(*args)
}
