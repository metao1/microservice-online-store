//package com.metao.book.shared.rest.client;
//
//import com.fasterxml.jackson.annotation.JsonProperty;
//import com.metao.book.shared.domain.financial.Currency;
//import com.metao.book.shared.domain.financial.CurrencyConverter;
//import com.metao.book.shared.domain.financial.Money;
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.time.LocalDate;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.client.SimpleClientHttpRequestFactory;
//import org.springframework.lang.NonNull;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.util.UriComponentsBuilder;
//
/// **
// * Implementation of {@link CurrencyConverter} that uses https://exchangeratesapi.io/ to fetch the latest rates.
// */
//@Service
//class CurrencyConverterClient implements CurrencyConverter {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyConverterClient.class);
//    private final RestTemplate restTemplate;
//    private final Map<Currency, Double> conversionRates = new ConcurrentHashMap<>();
//
//    @Value("${app.currency-conversion.enabled:false}")
//    private boolean enabled;
//
//    public CurrencyConverterClient() {
//        restTemplate = new RestTemplate();
//        var requestFactory = new SimpleClientHttpRequestFactory();
//        // Never ever do a remote call without a finite timeout!
//        requestFactory.setConnectTimeout(10000);
//        requestFactory.setReadTimeout(10000);
//        restTemplate.setRequestFactory(requestFactory);
//    }
//
//    @Override
//    @NonNull
//    public Money convert(@NonNull Money amount, @NonNull Currency newCurrency) {
//        if (amount.currency() == newCurrency) {
//            return amount;
//        } else {
//            var eur = convertToEuro(amount);
//            return convertEuroTo(eur, newCurrency);
//        }
//    }
//
//    private Money convertToEuro(@NonNull Money amount) {
//        if (amount.currency() == Currency.EUR) {
//            return amount;
//        } else {
//            return new Money(Currency.EUR,
//                amount.doubleAmount().divide(getConversionRateFor(amount.currency()), RoundingMode.HALF_UP));
//        }
//    }
//
//    private Money convertEuroTo(@NonNull Money amount, @NonNull Currency newCurrency) {
//        return new Money(newCurrency, amount.doubleAmount().multiply(getConversionRateFor(newCurrency)));
//    }
//
//    private BigDecimal getConversionRateFor(@NonNull Currency currency) {
//        var rate = conversionRates.get(currency);
//        if (rate == null) {
//            throw new IllegalStateException("Missing conversion rate for " + currency);
//        }
//        return BigDecimal.valueOf(rate);
//    }
//
//    @Scheduled(fixedRate = 6 * 60 * 60 * 1000) // Refresh every 6 hours
//    public void fetchConversionRates() {
//        if (!enabled) {
//            LOGGER.info("Currency conversion is disabled. No rates are fetched.");
//            return;
//        }
//        LOGGER.info("Fetching conversion rates from web service");
//        var uri = UriComponentsBuilder.fromUriString("https://api.exchangeratesapi.io/latest")
//            .queryParam("symbols", Stream.of(Currency.values())
//                .filter(c -> c != Currency.EUR)
//                .map(Enum::name)
//                .collect(Collectors.joining(",")))
//            .build().toUri();
//        LOGGER.debug("Using URI {}", uri);
//        var rates = restTemplate.getForEntity(uri, RatesDTO.class).getBody();
//        if (rates != null) {
//            conversionRates.putAll(rates.rates);
//            LOGGER.info("Received {} rates", rates.rates.size());
//        }
//    }
//
//    static class RatesDTO {
//
//        @JsonProperty("date")
//        LocalDate date;
//
//        @JsonProperty("base")
//        Currency base;
//
//        @JsonProperty("rates")
//        Map<Currency, Double> rates;
//    }
//}
