
package main;

import main.model.Lemma;
import main.repositories.LemmaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/api")
public class DefaultController {

    private final LemmaRepository lemmaRepository;


    @Autowired
    public DefaultController(LemmaRepository lemmaRepository) {
        this.lemmaRepository = lemmaRepository;
    }

    @RequestMapping("/admin")
    public String index() {
        return "index";
    }

    //Можно делать так
    @GetMapping("/hello")
    public String hello(@RequestParam(value = "name", required = false) String name,
                        @RequestParam(value = "surname", required = false) String surname,
                        Model model) {

        System.out.println(name + " - " + surname);

        model.addAttribute("message",name + " - " + surname);
        return "hello";
    }


    @GetMapping("/lemmas")
    public List<Lemma> getLemma() {
        return lemmaRepository.findAll();
    }

    @PostMapping("/saveLemma")
    public Lemma save(Lemma lemma) {
        return lemmaRepository.save(lemma);
    }
}


