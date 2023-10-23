package com.pogodynka;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

public class WeatherApp {

   // definiuje elementy interfejsu
    private JFrame frame;
    private JButton btnSearch, btnCurrentConditions, btn1DayForecast, btn5DaysForecast, btn4HoursForecast,
            btnIndices;
    private JComboBox<String> cityList;
    private JLabel searchLabel;
    // definiuje obiekt klasy WeatherApi aby moc korzystac z funkcjonalnosci
    // tworze hashMape to przechowywania location key dla podanego miasta
    private Map<String, String> cityKeyMap = new HashMap<>();

    private WeatherViewModel viewModel; // używam ViewModel zamiast bezpośrednio Modelu

    public WeatherApp(final WeatherViewModel injected_viewModel) { // DI: wstrzykiwanie ViewModel
        this.viewModel = injected_viewModel;

        // konfiguruje interfejs uzytkownika
        frame = new JFrame("AccuWeather App");
        frame.setSize(450, 550);
        frame.setLayout(new GridLayout(6, 2));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        searchLabel = new JLabel("Wyszukaj miasto:");
        cityList = new JComboBox<String>();
        cityList.setEditable(true);
        btnSearch = new JButton("Szukaj");
        btnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String city = (String) cityList.getSelectedItem();
                cityKeyMap = viewModel.getCities(city); // odwołanie do ViewModel
                cityList.removeAllItems();
                for (String cityName : cityKeyMap.keySet()) {
                    cityList.addItem(cityName);
                }
            }
        });
        btnCurrentConditions = createButton("Obecne warunki pogodowe", "currentconditions/v1", 0);
        btn4HoursForecast = createButton("Prognoza za 4 godziny", "forecasts/v1/hourly/12hour", 3);
        btn1DayForecast = createButton("Prognoza na najblizsze 24 godziny", "forecasts/v1/daily/1day", 0);
        btn5DaysForecast = createButton("Prognoza za 5 dni", "forecasts/v1/daily/5day", 4);
        btnIndices = createButton("Czy pogoda sprzyja spacerowi z psem?", "indices/v1/daily/1day", 41);

        JPanel searchPanel = new JPanel();
        searchPanel.add(searchLabel);
        searchPanel.add(cityList);
        searchPanel.add(btnSearch);

        frame.add(searchPanel);
        frame.add(btnCurrentConditions);
        frame.add(btnIndices);
        frame.add(btn4HoursForecast);
        frame.add(btn1DayForecast);
        frame.add(btn5DaysForecast);

        frame.setVisible(true);    
    }

    // metoda tworzaca przycisk oraz przypisuje funkcjonalnosc do odpowiedniego
    // przycisku ze wzgledu na jego unikalna wartosc endpoint
    private JButton createButton(String title, final String endpoint, final int index) {
        JButton button = new JButton(title);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedCity = (String) cityList.getSelectedItem();
                if (selectedCity != null) {
                    String locationKey = cityKeyMap.get(selectedCity);
                    JOptionPane.showMessageDialog(frame, viewModel.getCurrentConditions(selectedCity, locationKey, endpoint, index));
                } else {
                    JOptionPane.showMessageDialog(frame, "Proszę wybrać miasto z listy.");
                }
            }
        });
        return button;
    }

    public static void main(String[] args) {
        WeatherAPI injected_Api = new WeatherAPI("GriiUGF3dEeg4vbdFW0Kyhu1hAygUQBA");
        WeatherViewModel viewModel = new WeatherViewModel(injected_Api); // wstrzykiwanie Modelu do ViewModel
        new WeatherApp(viewModel); // wstrzykiwanie ViewModel do View
    }
    
}
