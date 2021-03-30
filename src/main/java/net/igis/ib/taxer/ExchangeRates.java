package net.igis.ib.taxer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class ExchangeRates {

    private static ObjectMapper objectMapper = new ObjectMapper();
    private static Map<String, Double> exchangeRateCache = new HashMap<>();
    private static HttpClient client = HttpClient.newHttpClient();

    public static double getExchangeRate(String date) throws IOException, InterruptedException {
        Double rate = exchangeRateCache.get(date);
        if (rate != null) {
            return rate;
        }

        // https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?valcode=USD&date=20210127&json
        String uri = MessageFormat.format(
                "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?valcode=USD&date={0}&json", date);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Couldn't retrieve exchange rate");
        }

        JsonNode node = objectMapper.readTree(response.body());
        rate = node.get(0).get("rate").asDouble();

        exchangeRateCache.put(date, rate);

        return rate;
    }
}
