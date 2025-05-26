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
            Book macbeth = new Book();
            macbeth.setAuthor("William Shakespeare");
            macbeth.setTitle("Macbeth");

            Book othello = new Book();
            othello.setAuthor("William Shakespeare");
            othello.setTitle("Othello");

            //bookService.save(macbeth);
            //bookService.save(othello);
            //System.out.println(bookService.findAll());

        };
    }

}
