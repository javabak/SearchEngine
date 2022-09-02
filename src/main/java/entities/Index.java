package entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;


@Entity
@Table(name = "`index`")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Index {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    @Column(nullable = false)
    private int id;

    @ManyToOne (fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "page_id", nullable = false)
    @Getter
    @Setter
    private Page page;

    @ManyToOne (fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "lemma_id", nullable = false)
    @Getter
    @Setter
    private Lemma lemma;

    @Column(name = "`rank`", nullable = false)
    @Getter
    @Setter
    private float rank;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Index index = (Index) o;
        return id == index.id && Float.compare(index.rank, rank) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, rank);
    }
}

