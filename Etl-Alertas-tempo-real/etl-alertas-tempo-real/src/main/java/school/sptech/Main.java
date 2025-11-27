package school.sptech;

import org.json.JSONArray;

import java.time.format.DateTimeFormatter;

public class Main {
    public static void main(String[] args) {

        JSONArray tickets = JiraService.buscarTodosTicketsAAC();
        System.out.println("Total de tickets recebidos: " + tickets.length());

        GerarCsvTickets.gerarCsv(tickets);

        String csvPath = "tickets_" +
                java.time.LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) +
                ".csv";

        String jsonPath = "resultado_" +
                java.time.LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) +
                ".json";

        JSONArray json = CsvToJsonFileReader.parseCsvToJson(csvPath);
        CsvToJsonFileReader.saveJsonToFile(json, jsonPath);

        System.out.println("CSV gerado em: " + csvPath);
        System.out.println("JSON gerado em: " + jsonPath);
        System.out.println("Processo concluído!");
    }
}
