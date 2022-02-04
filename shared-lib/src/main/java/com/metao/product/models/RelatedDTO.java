package com.metao.product.models;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

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
