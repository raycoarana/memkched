package com.raycoarana.memkched.test

import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

object Containers {
    const val MEMCACHED_PORT = 11211
    val MEMCACHED = GenericContainer(DockerImageName.parse("memcached:1.6.18-alpine"))
        .withExposedPorts(MEMCACHED_PORT)
}
