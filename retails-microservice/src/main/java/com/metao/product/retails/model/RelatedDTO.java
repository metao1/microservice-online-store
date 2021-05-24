package com.metao.product.retails.model;

import lombok.*;

import java.util.List;

@Data
@Value
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class RelatedDTO {

    List<String> alsoBought;

    List<String> alsoViewed;

    List<String> boughtTogether;

    List<String> buyAfterViewing;
}
