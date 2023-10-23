package com.pogodynka;

import java.util.Map;

public class WeatherViewModel {

    private WeatherAPI api;

    public WeatherViewModel(WeatherAPI injected_Api) {
        this.api = injected_Api;
    }

    public Map<String, String> getCities(String city) {
        return api.searchCitiesWithRegion(city);
    }

    public String getCurrentConditions(String selectedCity, String locationKey, String endpoint, int index) {
        String info = api.fetchWeatherData(endpoint, locationKey);
        Map<String, String> map = api.extractWeatherDetails(info, index, endpoint);
        if (endpoint.equals("currentconditions/v1")) {
            return "Dla miasta " + selectedCity + " obecna temperatura wynosi: "
                    + map.get("Temperature") +
                    ", przy czym odczuwalna temperatura wynosi: " + map.get("RealFeelTemperature") + ".\n" +
                    "Warunki pogodowe to: " + map.get("WeatherText") + ".\n" +
                    "Ciśnienie atmosferyczne wynosi: " + map.get("Pressure") + ".\n";
        } else if (endpoint.equals("forecasts/v1/hourly/12hour")) {
            return "Dla miasta " + selectedCity + " temperatura za 4h będzie wynosić: "
                    + map.get("Temperature") +
                    ", \nprzy czym odczuwalna temperatura bedzie wynosić: " + map.get("RealFeelTemperature")
                    + ".\n" +
                    "Warunki pogodowe to: " + map.get("IconPhrase") + ".\n";
        } else if (endpoint.equals("forecasts/v1/daily/1day")) {
            return "Prognoza na najbliższe 24h dla miasta " + selectedCity + ": \n" + map.get("Headline")
                    + "\n\n" +
                    "Przy czym temperatura będzie w zakresie od " + map.get("MinimumTemperature") + " do "
                    + map.get("MaximumTemperature") + ".\n";
        } else if (endpoint.equals("forecasts/v1/daily/5day")) {
            return "Temperatura za 5 dni dla miasta " + selectedCity
                    + "\nbędzie wahała się w przedziale od " +
                    map.get("MinimumTemperature") + " do " + map.get("MaximumTemperature") + ".\n" +
                    "Odczuwalna temperatura w zakresie od " + map.get("minRealFeel") + " do "
                    + map.get("maxRealFeel") + ". \n";
        } else if (endpoint.equals("indices/v1/daily/1day")) {
            return "Czy pogoda sprzyja spacerowi z psem?\n" + map.get("Text");
        } else {
            return "Proszę wybrać jedna z opcji.";
        }
    }
}
