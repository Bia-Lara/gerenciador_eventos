package br.ifsp.demo.support;

import br.ifsp.demo.infrastructure.security.auth.RegisterUserRequest;
import com.github.javafaker.Faker;

public class EntityBuilder {

    private static final Faker faker = Faker.instance();

    public static RegisterUserRequest createRandomRegisterRequest(String password) {
        return new RegisterUserRequest(
                faker.name().firstName(),
                faker.name().lastName(),
                faker.internet().emailAddress(),
                password
        );
    }

    public static RegisterUserRequest createRegisterRequest(String name, String lastname, String email, String password) {
        return new RegisterUserRequest(name, lastname, email, password);
    }

    public static String randomFirstName() {
        return faker.name().firstName();
    }

    public static String randomLastName() {
        return faker.name().lastName();
    }

    public static String randomEmail() {
        return faker.internet().emailAddress();
    }
}
