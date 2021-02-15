package com.sinhro.songturn.backend.utils

import java.time.OffsetDateTime
import java.time.ZoneOffset

val MINIMUM_OFFSET_DATE_TIME: OffsetDateTime = OffsetDateTime.of(
        1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC
)