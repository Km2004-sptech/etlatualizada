package school.sptech;

import org.json.JSONArray;
import java.time.format.DateTimeFormatter;

public class Main {
    public static void main(String[] args) {

        JSONArray tickets = JiraService.buscarTodosTicketsAAC();
        System.out.println("Total de tickets recebidos: " + tickets.length());

        GerarCsvTickets.gerarCsv(tickets);

        String timestamp = java.time.LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));

        String csvPath = "tickets_" + timestamp + ".csv";
        String jsonPath = "resultado_" + timestamp + ".json";

        JSONArray json = CsvToJsonFileReader.parseCsvToJson(csvPath);
        CsvToJsonFileReader.saveJsonToFile(json, jsonPath);

        System.out.println("CSV gerado em: " + csvPath);
        System.out.println("JSON gerado em: " + jsonPath);


        String region = "us-east-1";

        S3Uploader uploader = new S3Uploader(region);
        uploader.uploadFile("", "jsons/" + timestamp + ".json", jsonPath);

        System.out.println("Processo concluído com upload no S3!");
    }
}