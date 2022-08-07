package Services;

import Entites.Index;
import Repositories.IndexRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class IndexService {

    private final IndexRepository indexRepository;

    @Autowired
    public IndexService(IndexRepository indexRepository) {
        this.indexRepository = indexRepository;
    }

    public List<Index> findAll() {
        return indexRepository.findAll();
    }

    public Index saveLemma(Index index) {
        return indexRepository.save(index);
    }

    public void deleteById(int id) {
        indexRepository.deleteById(id);
    }

    public Index getLemmaById(int id) {
        return indexRepository.getById(id);
    }
}
