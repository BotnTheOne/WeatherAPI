import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class WeatherAPIData {
    public static void main(String[] args) {
        try{
            Scanner scanner = new Scanner(System.in);
            String city;
            do{
                // Выводим пользовательский инпут
                System.out.println("===================================================");
                System.out.print("Введите город на Английском языке или No для выхода: ");
                city = scanner.nextLine();

                if(city.equalsIgnoreCase("No")) break;

                // Получение данных о локации
                JSONObject cityLocationData = getLocationData(city);
                double latitude = (double) cityLocationData.get("latitude");
                double longitude = (double) cityLocationData.get("longitude");

                displayWeatherData(latitude, longitude);
            }while(!city.equalsIgnoreCase("No"));

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private static JSONObject getLocationData(String city){
        city = city.replaceAll(" ", "+");

        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" +
                           city + "&count=1&language=en&format=json";

        try{
            // 1. Извлекаем API ответа на основе ссылки API
            HttpURLConnection apiConnection = fetchApiResponse(urlString);

            // проверка статуса ответа
            if(apiConnection.getResponseCode() != 200){
                System.out.println("Ошибка: невозможно присоединиться к API");
                return null;
            }

            // 2. Читаем ответ и конвертируем его в представление типа String
            String jsonResponse = readApiResponse(apiConnection);

            // 3. Парсим полученную строку в JSON Object
            JSONParser parser = new JSONParser();
            JSONObject resultsJsonObj = (JSONObject) parser.parse(jsonResponse);

            // 4. Получаем данные о местоположении
            JSONArray locationData = (JSONArray) resultsJsonObj.get("results");
            return (JSONObject) locationData.get(0);

        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static void displayWeatherData(double latitude, double longitude){
        try{
            // 1. Извлечение API ответа на основе ссылки API
            String url = "https://api.open-meteo.com/v1/forecast?latitude=" + latitude +
                         "&longitude=" + longitude + "&current=temperature_2m,relative_humidity_2m,wind_speed_10m";
            HttpURLConnection apiConnection = fetchApiResponse(url);

            // проверка статуса ответа
            if(apiConnection.getResponseCode() != 200){
                System.out.println("Ошибка: невозможно присоединиться к API");
                return;
            }

            // 2. Читаем ответ и конвертируем его в представление типа String
            String jsonResponse = readApiResponse(apiConnection);

            // 3. Парсим полученную строку в JSON Object
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonResponse);
            JSONObject currentWeatherJson = (JSONObject) jsonObject.get("current");

            // 4.Сохраняем данные в соответствующем им типе данных
            String time = (String) currentWeatherJson.get("time");
            System.out.println("Время: " + time);

            double temperature = (double) currentWeatherJson.get("temperature_2m");
            System.out.println("Температура (C): " + temperature);

            long relativeHumidity = (long) currentWeatherJson.get("relative_humidity_2m");
            System.out.println("Относительная влажность водуха: " + relativeHumidity);

            double windSpeed = (double) currentWeatherJson.get("wind_speed_10m");
            System.out.println("Скорость ветра (м/с): " + windSpeed);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private static String readApiResponse(HttpURLConnection apiConnection) {
        try {
            // Создем StringBuilder для хранения результирующих данных в формате JSON
            StringBuilder resultJson = new StringBuilder();

            // Создаем Scanner для чтения из InputStream HttpURLConnection
            Scanner scanner = new Scanner(apiConnection.getInputStream());

            // Проходим по каждой строке в ответе и добавляем ее в StringBuilder
            while (scanner.hasNext()) {
                resultJson.append(scanner.nextLine());
            }

            scanner.close();

            return resultJson.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static HttpURLConnection fetchApiResponse(String urlString){
        try{

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");

            return conn;
        }catch(IOException e){
            e.printStackTrace();
        }

        return null;
    }
}
