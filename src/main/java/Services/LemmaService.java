package Services;

import Entites.Lemma;
import Repositories.LemmaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class LemmaService {

    private final LemmaRepository lemmaRepository;

    @Autowired
    public LemmaService(LemmaRepository lemmaRepository) {
        this.lemmaRepository = lemmaRepository;
    }

    public List<Lemma> findAll() {
        return lemmaRepository.findAll();
    }

    public Lemma saveLemma(Lemma lemma) {
        return lemmaRepository.save(lemma);
    }

    public void deleteById(int id) {
        lemmaRepository.deleteById(id);
    }

    public Lemma getLemmaById(int id) {
        return lemmaRepository.getById(id);
    }
}