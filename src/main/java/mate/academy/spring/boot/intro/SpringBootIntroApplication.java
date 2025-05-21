package mate.academy.spring.boot.intro;

import mate.academy.spring.boot.intro.model.Book;
import mate.academy.spring.boot.intro.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringBootIntroApplication {

    @Autowired
    private BookService bookService;

    public static void main(String[] args) {
        SpringApplication.run(SpringBootIntroApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            Book book1 = new Book();
            book1.setAuthor("Kotlaras");
            book1.setTitle("Morskit");

            Book book2 = new Book();
            book2.setAuthor("Szelw");
            book2.setTitle("AI");

            bookService.save(book1);
            bookService.save(book2);
            System.out.println(bookService.findAll());

        };
    }

}
