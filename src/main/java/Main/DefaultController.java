
package Main;

import Entities.Lemma;
import Repositories.LemmaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/api")
public class DefaultController {

    @Autowired
    private LemmaRepository lemmaRepository;

    private Lemma lemma;

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


    //А можно сделать попроще
    @GetMapping("/hi")
    public String hi(@ModelAttribute("lemma") Lemma lemma) {
        System.out.println(lemma.getFrequency());
        System.out.println(lemma.getLemma());
        return "hello";
    }
}


