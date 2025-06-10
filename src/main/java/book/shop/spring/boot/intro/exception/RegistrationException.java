package book.shop.spring.boot.intro.exception;

public class RegistrationException extends RuntimeException {
    public RegistrationException(String message) {
        super(message);
    }
}
