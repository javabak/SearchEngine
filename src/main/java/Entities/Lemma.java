package Entities;


import javax.persistence.*;

@Entity
@Table(name = "lemma")
public class Lemma {

    @ManyToOne(cascade = CascadeType.ALL)
    private Page page;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JoinTable(name = "`index`", joinColumns = {@JoinColumn(name = "lemma_id")})
    private int id;

    private String lemma;
    private int frequency;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
}
