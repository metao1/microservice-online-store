package com.metao.product.retails.domain;

import javax.persistence.ElementCollection;
import java.util.List;

///*@Data
//@Embeddable
//@RequiredArgsConstructor
//@Table(name = "related")*/
public class RelatedEntity {

    @ElementCollection(targetClass = String.class)
    List<String> alsoBought;

    @ElementCollection(targetClass = String.class)
    List<String> alsoViewed;

    @ElementCollection(targetClass = String.class)
    List<String> boughtTogether;

    @ElementCollection(targetClass = String.class)
    List<String> buyAfterViewing;
}
