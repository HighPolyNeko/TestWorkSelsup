import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.*;

public class CrptApi {

    private final TimeUnit timeUnit;
    private final int requestLimit;
    private final AtomicInteger requestCount;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        this.requestLimit = requestLimit;
        this.requestCount = new AtomicInteger(0);
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();

        startResetTask();
    }

    // запуск потока для сброса requestCount
    private void startResetTask() {
        Thread resetThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(timeUnit.toMillis(1));
                    resetRequestCount();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        resetThread.setDaemon(true);
        resetThread.start();
    }

    // сброса requestCount
    private void resetRequestCount() {
        synchronized (requestCount) {
            requestCount.set(0);
            requestCount.notifyAll();
        }
    }

    public void createDocument(Document document) throws IOException, InterruptedException {
        synchronized (requestCount) {
            while (requestCount.get() >= requestLimit) {
                requestCount.wait();
            }
            requestCount.incrementAndGet();
        }

        String url = "https://ismp.crpt.ru/api/v3/lk/documents/create";
        RequestBody body = RequestBody.create(objectMapper.writeValueAsString(document), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
        }
    }

    public static class Document {
        public Description description;
        public String doc_id;
        public String doc_status;
        public String doc_type = "LP_INTRODUCE_GOODS";
        public boolean importRequest;
        public String owner_inn;
        public String participant_inn;
        public String producer_inn;
        public String production_date;
        public String production_type;
        public Product[] products;
        public String reg_date;
        public String reg_number;

        public static class Description {
            public String participantInn;
        }

        public static class Product {
            public String certificate_document;
            public String certificate_document_date;
            public String certificate_document_number;
            public String owner_inn;
            public String producer_inn;
            public String production_date;
            public String tnved_code;
            public String uit_code;
            public String uitu_code;
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        CrptApi api = new CrptApi(TimeUnit.MINUTES, 10);
        Document document = new Document();
        document.description = new Document.Description();
        document.description.participantInn = "string";
        document.doc_id = "string";
        document.doc_status = "string";
        document.importRequest = true;
        document.owner_inn = "string";
        document.participant_inn = "string";
        document.producer_inn = "string";
        document.production_date = "2020-01-23";
        document.production_type = "string";
        document.products = new Document.Product[1];
        document.products[0] = new Document.Product();
        document.products[0].certificate_document = "string";
        document.products[0].certificate_document_date = "2020-01-23";
        document.products[0].certificate_document_number = "string";
        document.products[0].owner_inn = "string";
        document.products[0].producer_inn = "string";
        document.products[0].production_date = "2020-01-23";
        document.products[0].tnved_code = "string";
        document.products[0].uit_code = "string";
        document.products[0].uitu_code = "string";
        document.reg_date = "2020-01-23";
        document.reg_number = "string";

        api.createDocument(document);
    }
}

