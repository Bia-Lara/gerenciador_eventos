package br.ifsp.demo.support;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public final class RegistrationApiFixture {

    private RegistrationApiFixture() {}

    public static LocalDateTime futureStart() {
        return LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
    }
}