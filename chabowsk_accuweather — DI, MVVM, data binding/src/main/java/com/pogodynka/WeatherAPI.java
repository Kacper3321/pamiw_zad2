package com.pogodynka;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class WeatherAPI {

    private String apiKey;
    private final String baseUrl = "http://dataservice.accuweather.com/";

    public WeatherAPI(String apiKey) {
        this.apiKey = apiKey;
    }

    public String fetchWeatherData(String endpoint, String locationKey) {
        try {
            // Stworzenie odpowiedniego URL dla kazdego z endpointow
            String selectMetric = "";
            if (endpoint.equals("forecasts/v1/hourly/12hour") || endpoint.equals("forecasts/v1/daily/1day")
                    || endpoint.equals("forecasts/v1/daily/5day")) {
                selectMetric = "&metric=true";
            }
            URL url = new URL(
                    baseUrl + endpoint + "/" + locationKey + "?apikey=" + apiKey + "&details=true&language=pl"
                            + selectMetric);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        } catch (Exception e) {
            return "Błąd: " + e.getMessage();
        }
    }

    // metoda majaca na celu wyciaganie potrzebnych informacji o pogodzie z
    // pobieranego pliku json poprzez odpowiednie API,
    // w zaleznosci od wybranego endpointa pobieram rozne wartosci
    public Map<String, String> extractWeatherDetails(String jsonString, int index, String endpoint) {
        Map<String, String> weatherDetails = new HashMap<>();

        if (endpoint.equals("currentconditions/v1")) {
            JSONArray jsonArray = new JSONArray(jsonString);
            JSONObject weatherObject = jsonArray.getJSONObject(index);

            String weatherText = weatherObject.getString("WeatherText");
            weatherDetails.put("WeatherText", weatherText);

            JSONObject temperatureObject = weatherObject.getJSONObject("Temperature");
            JSONObject metricTemperature = temperatureObject.getJSONObject("Metric");
            double temperatureValue = metricTemperature.getDouble("Value");
            String temperatureUnit = metricTemperature.getString("Unit");
            weatherDetails.put("Temperature", temperatureValue + " " + temperatureUnit);

            JSONObject realFeelTemperatureObject = weatherObject.getJSONObject("RealFeelTemperature");
            JSONObject metricRealFeelTemperature = realFeelTemperatureObject.getJSONObject("Metric");
            double realFeelTemperatureValue = metricRealFeelTemperature.getDouble("Value");
            String realFeelTemperatureUnit = metricRealFeelTemperature.getString("Unit");
            weatherDetails.put("RealFeelTemperature", realFeelTemperatureValue + " " + realFeelTemperatureUnit);

            JSONObject pressureObject = weatherObject.getJSONObject("Pressure");
            JSONObject metricPressure = pressureObject.getJSONObject("Metric");
            double pressureValue = metricPressure.getDouble("Value");
            String pressureUnit = metricPressure.getString("Unit");
            weatherDetails.put("Pressure", pressureValue + " " + pressureUnit);
        } else if (endpoint.equals("forecasts/v1/hourly/12hour")) {
            JSONArray jsonArray = new JSONArray(jsonString);
            JSONObject weatherObject = jsonArray.getJSONObject(index);

            String iconPhrase = weatherObject.getString("IconPhrase");
            weatherDetails.put("IconPhrase", iconPhrase);

            JSONObject temperatureObject = weatherObject.getJSONObject("Temperature");
            double temperatureValue = temperatureObject.getDouble("Value");
            String temperatureUnit = temperatureObject.getString("Unit");
            weatherDetails.put("Temperature", temperatureValue + " " + temperatureUnit);

            JSONObject realFeelTemperatureObject = weatherObject.getJSONObject("RealFeelTemperature");
            double realFeelTemperatureValue = realFeelTemperatureObject.getDouble("Value");
            String realFeelTemperatureUnit = realFeelTemperatureObject.getString("Unit");
            weatherDetails.put("RealFeelTemperature", realFeelTemperatureValue + " " + realFeelTemperatureUnit);
        } else if (endpoint.equals("forecasts/v1/daily/1day") || endpoint.equals("forecasts/v1/daily/5day")) {
            JSONObject jsonObject = new JSONObject(jsonString);

            // Wyciągnij informacje z nagłówka
            JSONObject headline = jsonObject.getJSONObject("Headline");
            String headlineText = headline.getString("Text");
            weatherDetails.put("Headline", headlineText);

            // Wyodrebnienie informacji o prognozie
            JSONArray dailyForecastsArray = jsonObject.getJSONArray("DailyForecasts");
            JSONObject forecast = dailyForecastsArray.getJSONObject(index);
            JSONObject temperatureObj = forecast.getJSONObject("Temperature");

            double minValue = temperatureObj.getJSONObject("Minimum").getDouble("Value");
            String minUnit = temperatureObj.getJSONObject("Minimum").getString("Unit");
            weatherDetails.put("MinimumTemperature", minValue + " " + minUnit);

            double maxValue = temperatureObj.getJSONObject("Maximum").getDouble("Value");
            String maxUnit = temperatureObj.getJSONObject("Maximum").getString("Unit");
            weatherDetails.put("MaximumTemperature", maxValue + " " + maxUnit);

            JSONObject realFeelTemperatureObject = forecast.getJSONObject("RealFeelTemperature");

            double minRealFeelValue = realFeelTemperatureObject.getJSONObject("Minimum").getDouble("Value");
            String minRealFeelUnit = realFeelTemperatureObject.getJSONObject("Minimum").getString("Unit");
            weatherDetails.put("minRealFeel", minRealFeelValue + " " + minRealFeelUnit);

            double maxRealFeelValue = realFeelTemperatureObject.getJSONObject("Maximum").getDouble("Value");
            String maxRealFeelUnit = realFeelTemperatureObject.getJSONObject("Maximum").getString("Unit");
            weatherDetails.put("maxRealFeel", maxRealFeelValue + " " + maxRealFeelUnit);
        } else if (endpoint.equals("indices/v1/daily/1day")) {
            JSONArray jsonArray = new JSONArray(jsonString);
            JSONObject weatherObject = jsonArray.getJSONObject(index);

            String text = weatherObject.getString("Text");
            weatherDetails.put("Text", text);
        }
        return weatherDetails;
    }

    public String getLocationKey(String endpoint, String city) {
        try {
            // Zakoduj wartość miasta, aby poprawnie obsługiwać specjalne znaki, takie jak
            // znaki polskie
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8.toString());

            // Buduj URL z zakodowaną nazwą miasta
            URL url = new URL(baseUrl + endpoint + "/" + "?apikey=" + apiKey + "&q=" + encodedCity + "&language=pl");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Przetwarzaj odpowiedź jako JSON
            JSONArray jsonArray = new JSONArray(response.toString());
            if (jsonArray.length() > 0) {
                // Bierzemy location key z wybranego miasta z listy
                return jsonArray.getJSONObject(0).getString("Key");
            } else {
                return null; // Brak wyników dla podanej nazwy miasta
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Błąd podczas pobierania klucza
        }
    }

    public Map<String, String> searchCitiesWithRegion(String city) {
        Map<String, String> cityWithKeyMap = new HashMap<>();
        try {
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8.toString());
            URL url = new URL(baseUrl + "locations/v1/cities/search" + "?apikey=" + apiKey + "&q=" + encodedCity
                    + "&language=pl");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONArray jsonArray = new JSONArray(response.toString());
            for (int i = 0; i < jsonArray.length(); i++) { // petla iterujaca po wszystkich wyszukanych miastach
                JSONObject cityObject = jsonArray.getJSONObject(i);
                String key = cityObject.getString("Key");
                String cityName = cityObject.getString("LocalizedName");
                String region = cityObject.getJSONObject("AdministrativeArea").getString("LocalizedName");
                cityWithKeyMap.put(cityName + ", " + region, key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cityWithKeyMap;
    }
}
