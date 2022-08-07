package Entites;

import javax.persistence.*;

@Entity
@Table(name = "lemma")
public class Page {

    @OneToMany
    private Lemma lemma;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JoinTable(name = "`index`", joinColumns = {@JoinColumn(name = "id")})
    private int id;

    private String path;
    private int code;
    private String content;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
