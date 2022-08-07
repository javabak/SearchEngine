package Services;

import Entites.Page;
import Repositories.PageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class PageService {

    private final PageRepository pageRepository;

    @Autowired
    public PageService(PageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }

    public List<Page> findAll() {
        return pageRepository.findAll();
    }

    public Page saveLemma(Page page) {
        return pageRepository.save(page);
    }

    public void deleteById(int id) {
        pageRepository.deleteById(id);
    }

    public Page getLemmaById(int id) {
        return pageRepository.getById(id);
    }
}





